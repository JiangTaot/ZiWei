package com.ziwei.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziwei.converter.ChartConverter;
import com.ziwei.dto.ZiweiBirthInfoDTO;
import com.ziwei.dto.ZiweiChartVO;
import com.ziwei.engine.calendar.BaziCalculator;
import com.ziwei.engine.calendar.LunarCalendarConverter;
import com.ziwei.engine.cycle.DaxianEngine;
import com.ziwei.engine.model.*;
import com.ziwei.engine.palace.MingGongCalculator;
import com.ziwei.engine.palace.PalaceSetupEngine;
import com.ziwei.engine.palace.WuxingJuCalculator;
import com.ziwei.engine.pattern.PatternMatcher;
import com.ziwei.engine.star.*;
import com.ziwei.entity.*;
import com.ziwei.enums.*;
import com.ziwei.exception.BusinessException;
import com.ziwei.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ziwei chart service implementation
 * <p>
 * Core 12-step chart calculation pipeline, persistence, and query.
 *
 * @author JTWORLD
 */
@Service
@Slf4j
public class ZiweiChartServiceImpl implements ZiweiChartService {

    @Resource
    private ZiweiChartMapper chartMapper;

    @Resource
    private ZiweiPalaceMapper palaceMapper;

    @Resource
    private ZiweiStarPositionMapper starPositionMapper;

    @Resource
    private ZiweiSihuaMapper sihuaMapper;

    @Resource
    private ZiweiDaxianMapper daxianMapper;

    // ========== Engines (stateless, created with new) ==========
    private final LunarCalendarConverter lunarConverter = new LunarCalendarConverter();
    private final BaziCalculator baziCalculator = new BaziCalculator();
    private final MingGongCalculator mingGongCalculator = new MingGongCalculator();
    private final PalaceSetupEngine palaceSetupEngine = new PalaceSetupEngine();
    private final WuxingJuCalculator wuxingJuCalculator = new WuxingJuCalculator();
    private final ZiweiStarPlacer ziweiPlacer = new ZiweiStarPlacer();
    private final TianfuStarPlacer tianfuPlacer = new TianfuStarPlacer();
    private final AuxiliaryStarPlacer auxiliaryPlacer = new AuxiliaryStarPlacer();
    private final MinorStarPlacer minorPlacer = new MinorStarPlacer();
    private final ExtendedMinorStarPlacer extendedMinorPlacer = new ExtendedMinorStarPlacer();
    private final SihuaCalculator sihuaCalculator = new SihuaCalculator();
    private final DaxianEngine daxianEngine = new DaxianEngine();
    private final PatternMatcher patternMatcher = new PatternMatcher();
    private final BrightnessCalculator brightnessCalculator = new BrightnessCalculator();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChartContext calculate(ZiweiBirthInfoDTO dto) {
        int year = dto.getSolarYear();
        int month = dto.getSolarMonth();
        int day = dto.getSolarDay();
        // Frontend sends DiZhi index (0=子..11=亥), engine expects solar hour (0..23)
        int dizhiIndex = dto.getHour();
        int hour = dizhiIndex * 2; // 子=0, 丑=2, ..., 亥=22
        int minute = dto.getMinute();
        GenderEnum gender = GenderEnum.ofCode(dto.getGender());

        // Step 1: BaZi calculation (four pillars, using exact solar term boundary)
        Bazi bazi = baziCalculator.calculate(year, month, day, hour, minute);

        // Step 2: Lunar calendar conversion
        LunarCalendarConverter.LunarResult lunar = lunarConverter.solarToLunar(year, month, day, hour, minute);

        // Ziwei year stem/branch: use lunar new year boundary (aligns with iztro/wenmotianji)
        TianGanEnum yearGan = TianGanEnum.ofName(lunar.yearTianGan());
        DiZhiEnum yearZhi = DiZhiEnum.ofName(lunar.yearDiZhi());

        // Late Zi hour (23:00-00:00) adjustment: treat as next day for star placement
        int lunarDayForStars = lunar.lunarDay();
        if (hour >= 23) {
            lunarDayForStars++;
        }

        // Step 3: Ming Gong + Shen Gong
        DiZhiEnum hourZhi = DiZhiEnum.ofHour(hour);
        DiZhiEnum mingGongDiZhi = mingGongCalculator.calculateMingGong(lunar.lunarMonth(), hourZhi);
        DiZhiEnum shenGongDiZhi = mingGongCalculator.calculateShenGong(lunar.lunarMonth(), hourZhi);

        // Step 4: Twelve palaces setup + palace stem calculation
        EnumMap<PalaceTypeEnum, Palace> palaces = palaceSetupEngine.setupPalaces(mingGongDiZhi, shenGongDiZhi, yearGan);
        Palace mingPalace = palaces.get(PalaceTypeEnum.MING_GONG);
        TianGanEnum mingPalaceGan = mingPalace.getTianGan();

        // Step 5: WuXingJu (five-element bureau)
        int wuxingJuNum = wuxingJuCalculator.calculateJuNumber(mingPalaceGan, mingGongDiZhi);
        String wuxingJuName = wuxingJuCalculator.getJuName(mingPalaceGan, mingGongDiZhi);

        // Step 6: Place Ziwei star
        DiZhiEnum ziweiDiZhi = ziweiPlacer.findZiweiPosition(lunarDayForStars, wuxingJuNum);

        // Step 7: Place all 14 major stars
        Map<String, DiZhiEnum> ziweiGroup = ziweiPlacer.placeZiweiGroup(ziweiDiZhi);
        DiZhiEnum tianfuDiZhi = tianfuPlacer.findTianfuPosition(ziweiDiZhi);
        Map<String, DiZhiEnum> tianfuGroup = tianfuPlacer.placeTianfuGroup(tianfuDiZhi);

        // Step 8: Place auxiliary and minor stars
        Map<String, DiZhiEnum> auxStars = auxiliaryPlacer.placeAll(lunar.lunarMonth(), hourZhi, yearGan, yearZhi);
        Map<String, DiZhiEnum> minorStars = minorPlacer.placeAll(lunar.lunarMonth(), hourZhi, yearZhi, yearGan);

        // Step 8b: Place extended minor stars (depend on auxiliary star positions)
        Map<String, DiZhiEnum> extendedStars = extendedMinorPlacer.placeAll(
                lunar.lunarMonth(), lunarDayForStars, yearZhi, yearGan,
                hourZhi, gender, mingGongDiZhi, shenGongDiZhi,
                auxStars.get("zuofu"), auxStars.get("youbi"),
                auxStars.get("wenchang"), auxStars.get("wenqu"),
                auxStars.get("lucun"));

        // Step 9: Fill stars into palaces
        placeStarsIntoPalaces(palaces, ziweiGroup, tianfuGroup, auxStars, minorStars, extendedStars);

        // Step 10: Sihua (four transformations)
        List<StarPosition> natalSihua = sihuaCalculator.calculateNatalSihua(yearGan);
        applySihuaToPalaces(palaces, natalSihua);

        // Step 11: DaXian (major limit cycles)
        List<DaxianCycle> daxianCycles = daxianEngine.calculate(palaces, mingGongDiZhi, gender, yearGan, wuxingJuNum, year);

        // Step 12: Pattern matching
        ChartContext ctx = ChartContext.builder()
                .solarYear(year).solarMonth(month).solarDay(day).solarHour(hour).solarMinute(minute)
                .gender(gender).birthPlace(dto.getBirthPlace())
                .isDst(dto.getIsDst() != null && dto.getIsDst())
                .lunarYear(lunar.lunarYear()).lunarMonth(lunar.lunarMonth()).lunarDay(lunar.lunarDay())
                .isLeapMonth(lunar.isLeapMonth())
                .bazi(bazi).yearTianGan(yearGan).yearDiZhi(yearZhi)
                .mingGongDiZhi(mingGongDiZhi).shenGongDiZhi(shenGongDiZhi)
                .wuxingJu(wuxingJuName).wuxingJuNumber(wuxingJuNum)
                .palaces(palaces).natalSihua(natalSihua).daxianCycles(daxianCycles)
                .build();

        List<com.ziwei.engine.pattern.PatternResult> patternResults = patternMatcher.matchPatternsDetailed(ctx);
        ctx.setPatternResults(patternResults);
        ctx.setPatternNames(patternMatcher.matchPatterns(ctx));

        log.info("Chart calculated: userId={}", dto.getUserId());
        return ctx;
    }

    @Override
    public ZiweiChartVO calculateAndSave(ZiweiBirthInfoDTO dto) {
        // 1. Calculate (pure computation, no DB)
        ChartContext ctx = calculate(dto);

        // 2. Try to persist; gracefully handle DB unavailable
        ZiweiChartEntity chartEntity = null;
        try {
            chartEntity = saveChart(ctx, dto.getUserId());
        } catch (Exception e) {
            log.warn("Failed to save chart (DB unavailable), returning calculated result: {}", e.getMessage());
        }

        // 3. Convert to VO (handles null entity gracefully)
        if (chartEntity != null) {
            return ChartConverter.toChartVO(chartEntity, ctx);
        }
        // DB unavailable: return computed chart without persistence
        return ChartConverter.createUnsavedVO(ctx, dto);
    }

    @Override
    public ZiweiChartVO getChartById(Long id) {
        ZiweiChartEntity chartEntity = chartMapper.selectById(id);
        if (chartEntity == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS, "Chart not found: " + id);
        }

        // Try to deserialize from chart_json first (fast path)
        ChartContext ctx = null;
        if (chartEntity.getChartJson() != null && !chartEntity.getChartJson().isEmpty()) {
            try {
                ctx = objectMapper.readValue(chartEntity.getChartJson(), ChartContext.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize chart_json for chart {}, rebuilding from DB", id, e);
            }
        }

        // Fallback: rebuild from DB tables
        if (ctx == null || ctx.getPalaces() == null || ctx.getPalaces().isEmpty()) {
            ctx = buildChartContextFromDB(id);
        }

        return ChartConverter.toChartVO(chartEntity, ctx);
    }

    @Override
    public List<ZiweiChartVO> getMyCharts(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ZiweiChartEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ZiweiChartEntity::getUserId, userId)
                .orderByDesc(ZiweiChartEntity::getCreateTime);
        List<ZiweiChartEntity> entities = chartMapper.selectList(wrapper);

        return entities.stream()
                .map(entity -> {
                    // Build basic VO without full star data for list performance
                    return ZiweiChartVO.builder()
                            .id(entity.getId())
                            .solarYear(entity.getSolarYear())
                            .solarMonth(entity.getSolarMonth())
                            .solarDay(entity.getSolarDay())
                            .solarHour(entity.getSolarHour())
                            .solarMinute(entity.getSolarMinute())
                            .gender(entity.getGender())
                            .birthPlace(entity.getBirthPlace())
                            .lunarYear(entity.getLunarYear())
                            .lunarMonth(entity.getLunarMonth())
                            .lunarDay(entity.getLunarDay())
                            .isLeapMonth(entity.getIsLeapMonth() != null && entity.getIsLeapMonth())
                            .yearPillar(entity.getYearPillar())
                            .monthPillar(entity.getMonthPillar())
                            .dayPillar(entity.getDayPillar())
                            .hourPillar(entity.getHourPillar())
                            .mingGong((entity.getMingGongDizhi() != null ? entity.getMingGongDizhi() : "") + "宫")
                            .shenGong((entity.getShenGongDizhi() != null ? entity.getShenGongDizhi() : "") + "宫")
                            .wuxingJu(entity.getWuxingJu())
                            .createTime(entity.getCreateTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChart(Long id) {
        // Verify existence
        ZiweiChartEntity chartEntity = chartMapper.selectById(id);
        if (chartEntity == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS, "Chart not found: " + id);
        }

        // Cascading delete: use MyBatis-Plus logic delete on all related tables
        chartMapper.deleteById(id);

        // Delete palaces
        LambdaQueryWrapper<ZiweiPalaceEntity> palaceWrapper = new LambdaQueryWrapper<>();
        palaceWrapper.eq(ZiweiPalaceEntity::getChartId, id);
        palaceMapper.delete(palaceWrapper);

        // Delete star positions
        LambdaQueryWrapper<ZiweiStarPositionEntity> starWrapper = new LambdaQueryWrapper<>();
        starWrapper.eq(ZiweiStarPositionEntity::getChartId, id);
        starPositionMapper.delete(starWrapper);

        // Delete sihua
        LambdaQueryWrapper<ZiweiSihuaEntity> sihuaWrapper = new LambdaQueryWrapper<>();
        sihuaWrapper.eq(ZiweiSihuaEntity::getChartId, id);
        sihuaMapper.delete(sihuaWrapper);

        // Delete daxian
        LambdaQueryWrapper<ZiweiDaxianEntity> daxianWrapper = new LambdaQueryWrapper<>();
        daxianWrapper.eq(ZiweiDaxianEntity::getChartId, id);
        daxianMapper.delete(daxianWrapper);

        log.info("Chart deleted: id={}", id);
    }

    // ========== Persistence ==========

    /**
     * Save complete chart context to 5 tables
     */
    private ZiweiChartEntity saveChart(ChartContext ctx, Long userId) {
        // Serialize full chart as JSON backup
        String chartJson = null;
        try {
            chartJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ctx);
        } catch (Exception e) {
            log.warn("Failed to serialize chart_json: {}", e.getMessage());
        }

        // Insert chart master record
        ZiweiChartEntity chartEntity = new ZiweiChartEntity();
        chartEntity.setUserId(userId);
        chartEntity.setSolarYear(ctx.getSolarYear());
        chartEntity.setSolarMonth(ctx.getSolarMonth());
        chartEntity.setSolarDay(ctx.getSolarDay());
        chartEntity.setSolarHour(ctx.getSolarHour());
        chartEntity.setSolarMinute(ctx.getSolarMinute());
        chartEntity.setGender(ctx.getGender() != null ? ctx.getGender().getCode() : 0);
        chartEntity.setBirthPlace(ctx.getBirthPlace());
        chartEntity.setIsDst(ctx.isDst());
        chartEntity.setLunarYear(ctx.getLunarYear());
        chartEntity.setLunarMonth(ctx.getLunarMonth());
        chartEntity.setLunarDay(ctx.getLunarDay());
        chartEntity.setIsLeapMonth(ctx.isLeapMonth());
        chartEntity.setYearPillar(ctx.getBazi().getYearPillar().toString());
        chartEntity.setMonthPillar(ctx.getBazi().getMonthPillar().toString());
        chartEntity.setDayPillar(ctx.getBazi().getDayPillar().toString());
        chartEntity.setHourPillar(ctx.getBazi().getHourPillar().toString());
        chartEntity.setMingGongDizhi(ctx.getMingGongDiZhi() != null ? ctx.getMingGongDiZhi().getName() : null);
        chartEntity.setShenGongDizhi(ctx.getShenGongDiZhi() != null ? ctx.getShenGongDiZhi().getName() : null);
        chartEntity.setWuxingJu(ctx.getWuxingJu());
        chartEntity.setChartJson(chartJson);
        chartMapper.insert(chartEntity);
        Long chartId = chartEntity.getId();

        // Insert palaces + star positions
        for (Palace palace : ctx.getPalaces().values()) {
            ZiweiPalaceEntity palaceEntity = new ZiweiPalaceEntity();
            palaceEntity.setChartId(chartId);
            palaceEntity.setPalaceType(palace.getType().getCode());
            palaceEntity.setDizhi(palace.getDiZhi().getName());
            palaceEntity.setTianGan(palace.getTianGan() != null ? palace.getTianGan().getName() : null);
            palaceEntity.setIsShenGong(palace.isShenGong());
            palaceEntity.setDaXianStartAge(palace.getDaXianStartAge());
            palaceEntity.setDaXianEndAge(palace.getDaXianEndAge());
            palaceMapper.insert(palaceEntity);

            // Insert star positions for this palace
            int sortOrder = 0;
            for (StarPosition sp : palace.getStars()) {
                ZiweiStarPositionEntity spEntity = new ZiweiStarPositionEntity();
                spEntity.setChartId(chartId);
                spEntity.setPalaceId(palaceEntity.getId());
                spEntity.setStarCode(sp.getStarCode());
                spEntity.setStarType(sp.getStarType() != null ? sp.getStarType().getCode() : null);
                spEntity.setBrightness(sp.getBrightness() != null ? sp.getBrightness().getCode() : null);
                spEntity.setSihuaType(sp.getSihuaType() != null ? sp.getSihuaType().getCode() : null);
                spEntity.setSortOrder(sortOrder++);
                starPositionMapper.insert(spEntity);
            }
        }

        // Insert sihua (natal scope=1)
        List<StarPosition> sihuaList = ctx.getNatalSihua();
        if (sihuaList != null && sihuaList.size() >= 4) {
            ZiweiSihuaEntity sihuaEntity = new ZiweiSihuaEntity();
            sihuaEntity.setChartId(chartId);
            sihuaEntity.setSihuaScope(1); // natal
            sihuaEntity.setHuaLuStarCode(sihuaList.get(0).getStarCode());
            sihuaEntity.setHuaQuanStarCode(sihuaList.get(1).getStarCode());
            sihuaEntity.setHuaKeStarCode(sihuaList.get(2).getStarCode());
            sihuaEntity.setHuaJiStarCode(sihuaList.get(3).getStarCode());
            sihuaMapper.insert(sihuaEntity);
        }

        // Insert daxian cycles
        for (DaxianCycle dc : ctx.getDaxianCycles()) {
            ZiweiDaxianEntity daxianEntity = new ZiweiDaxianEntity();
            daxianEntity.setChartId(chartId);
            daxianEntity.setSequenceOrder(dc.getSequenceOrder());
            daxianEntity.setAgeStart(dc.getAgeStart());
            daxianEntity.setAgeEnd(dc.getAgeEnd());
            daxianEntity.setCalYearStart(dc.getCalYearStart());
            daxianEntity.setCalYearEnd(dc.getCalYearEnd());
            daxianEntity.setPalaceType(dc.getPalaceType() != null ? dc.getPalaceType().getCode() : null);
            daxianEntity.setDirection(dc.isForward());
            daxianMapper.insert(daxianEntity);
        }

        log.info("Chart saved: id={}, userId={}", chartId, userId);
        return chartEntity;
    }

    // ========== DB Reconstruction ==========

    /**
     * Rebuild ChartContext from database tables
     * <p>
     * Queries all related tables (palace, star_position, sihua, daxian) and
     * reconstructs the in-memory ChartContext object.
     */
    private ChartContext buildChartContextFromDB(Long chartId) {
        ZiweiChartEntity chartEntity = chartMapper.selectById(chartId);
        if (chartEntity == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS, "Chart not found: " + chartId);
        }

        // Query palaces
        LambdaQueryWrapper<ZiweiPalaceEntity> palaceWrapper = new LambdaQueryWrapper<>();
        palaceWrapper.eq(ZiweiPalaceEntity::getChartId, chartId);
        List<ZiweiPalaceEntity> palaceEntities = palaceMapper.selectList(palaceWrapper);

        // Query star positions
        LambdaQueryWrapper<ZiweiStarPositionEntity> starWrapper = new LambdaQueryWrapper<>();
        starWrapper.eq(ZiweiStarPositionEntity::getChartId, chartId);
        List<ZiweiStarPositionEntity> starPositionEntities = starPositionMapper.selectList(starWrapper);

        // Query sihua
        LambdaQueryWrapper<ZiweiSihuaEntity> sihuaWrapper = new LambdaQueryWrapper<>();
        sihuaWrapper.eq(ZiweiSihuaEntity::getChartId, chartId);
        List<ZiweiSihuaEntity> sihuaEntities = sihuaMapper.selectList(sihuaWrapper);

        // Query daxian
        LambdaQueryWrapper<ZiweiDaxianEntity> daxianWrapper = new LambdaQueryWrapper<>();
        daxianWrapper.eq(ZiweiDaxianEntity::getChartId, chartId);
        List<ZiweiDaxianEntity> daxianEntities = daxianMapper.selectList(daxianWrapper);

        // Group star positions by palaceId
        Map<Long, List<ZiweiStarPositionEntity>> starsByPalaceId = starPositionEntities.stream()
                .collect(Collectors.groupingBy(ZiweiStarPositionEntity::getPalaceId));

        // Build palaces
        EnumMap<PalaceTypeEnum, Palace> palaces = new EnumMap<>(PalaceTypeEnum.class);
        DiZhiEnum mingGongDiZhi = null;
        DiZhiEnum shenGongDiZhi = null;

        for (ZiweiPalaceEntity pe : palaceEntities) {
            PalaceTypeEnum type = PalaceTypeEnum.ofCode(pe.getPalaceType());
            DiZhiEnum diZhi = DiZhiEnum.ofName(pe.getDizhi());
            TianGanEnum tianGan = pe.getTianGan() != null ? TianGanEnum.ofName(pe.getTianGan()) : null;
            boolean isShenGong = pe.getIsShenGong() != null && pe.getIsShenGong();

            Palace palace = Palace.builder()
                    .type(type)
                    .diZhi(diZhi)
                    .tianGan(tianGan)
                    .isShenGong(isShenGong)
                    .daXianStartAge(pe.getDaXianStartAge())
                    .daXianEndAge(pe.getDaXianEndAge())
                    .stars(new ArrayList<>())
                    .build();

            // Fill star positions
            List<ZiweiStarPositionEntity> palaceStars = starsByPalaceId.getOrDefault(pe.getId(), Collections.emptyList());
            for (ZiweiStarPositionEntity spe : palaceStars) {
                StarPosition sp = StarPosition.builder()
                        .starCode(spe.getStarCode())
                        .starName(Star.getNameByCode(spe.getStarCode()))
                        .starType(StarTypeEnum.ofCode(spe.getStarType()))
                        .brightness(spe.getBrightness() != null ? StarBrightnessEnum.ofCode(spe.getBrightness()) : null)
                        .sihuaType(spe.getSihuaType() != null ? SihuaTypeEnum.ofCode(spe.getSihuaType()) : null)
                        .build();
                palace.addStar(sp);
            }

            palaces.put(type, palace);

            if (type == PalaceTypeEnum.MING_GONG) {
                mingGongDiZhi = diZhi;
            }
            if (isShenGong) {
                shenGongDiZhi = diZhi;
            }
        }

        // Build natal sihua
        List<StarPosition> natalSihua = new ArrayList<>();
        for (ZiweiSihuaEntity se : sihuaEntities) {
            if (se.getSihuaScope() != null && se.getSihuaScope() == 1) {
                natalSihua.add(StarPosition.builder()
                        .starCode(se.getHuaLuStarCode())
                        .starName(Star.getNameByCode(se.getHuaLuStarCode()))
                        .sihuaType(SihuaTypeEnum.HUA_LU)
                        .build());
                natalSihua.add(StarPosition.builder()
                        .starCode(se.getHuaQuanStarCode())
                        .starName(Star.getNameByCode(se.getHuaQuanStarCode()))
                        .sihuaType(SihuaTypeEnum.HUA_QUAN)
                        .build());
                natalSihua.add(StarPosition.builder()
                        .starCode(se.getHuaKeStarCode())
                        .starName(Star.getNameByCode(se.getHuaKeStarCode()))
                        .sihuaType(SihuaTypeEnum.HUA_KE)
                        .build());
                natalSihua.add(StarPosition.builder()
                        .starCode(se.getHuaJiStarCode())
                        .starName(Star.getNameByCode(se.getHuaJiStarCode()))
                        .sihuaType(SihuaTypeEnum.HUA_JI)
                        .build());
                break;
            }
        }

        // Build daxian cycles
        List<DaxianCycle> daxianCycles = new ArrayList<>();
        for (ZiweiDaxianEntity de : daxianEntities) {
            PalaceTypeEnum palaceType = PalaceTypeEnum.ofCode(de.getPalaceType());
            DaxianCycle dc = DaxianCycle.builder()
                    .sequenceOrder(de.getSequenceOrder())
                    .ageStart(de.getAgeStart())
                    .ageEnd(de.getAgeEnd())
                    .calYearStart(de.getCalYearStart())
                    .calYearEnd(de.getCalYearEnd())
                    .palaceType(palaceType)
                    .forward(de.getDirection() != null && de.getDirection())
                    .build();
            daxianCycles.add(dc);
        }

        // Parse year stem/branch from year pillar
        TianGanEnum yearGan = null;
        DiZhiEnum yearZhi = null;
        if (chartEntity.getYearPillar() != null && chartEntity.getYearPillar().length() == 2) {
            yearGan = TianGanEnum.ofName(String.valueOf(chartEntity.getYearPillar().charAt(0)));
            yearZhi = DiZhiEnum.ofName(String.valueOf(chartEntity.getYearPillar().charAt(1)));
        } else if (chartEntity.getYearPillar() != null) {
            log.warn("Unexpected year pillar format: length={}, value={}",
                    chartEntity.getYearPillar().length(), chartEntity.getYearPillar());
        }

        // Rebuild Bazi from pillars
        Pillar yearPillar = parsePillar(chartEntity.getYearPillar());
        Pillar monthPillar = parsePillar(chartEntity.getMonthPillar());
        Pillar dayPillar = parsePillar(chartEntity.getDayPillar());
        Pillar hourPillar = parsePillar(chartEntity.getHourPillar());
        Bazi bazi = Bazi.builder()
                .yearPillar(yearPillar)
                .monthPillar(monthPillar)
                .dayPillar(dayPillar)
                .hourPillar(hourPillar)
                .build();

        // Build ChartContext
        ChartContext ctx = ChartContext.builder()
                .solarYear(chartEntity.getSolarYear())
                .solarMonth(chartEntity.getSolarMonth())
                .solarDay(chartEntity.getSolarDay())
                .solarHour(chartEntity.getSolarHour())
                .solarMinute(chartEntity.getSolarMinute())
                .gender(GenderEnum.ofCode(chartEntity.getGender()))
                .birthPlace(chartEntity.getBirthPlace())
                .isDst(chartEntity.getIsDst() != null && chartEntity.getIsDst())
                .lunarYear(chartEntity.getLunarYear())
                .lunarMonth(chartEntity.getLunarMonth())
                .lunarDay(chartEntity.getLunarDay())
                .isLeapMonth(chartEntity.getIsLeapMonth() != null && chartEntity.getIsLeapMonth())
                .bazi(bazi)
                .yearTianGan(yearGan)
                .yearDiZhi(yearZhi)
                .mingGongDiZhi(mingGongDiZhi)
                .shenGongDiZhi(shenGongDiZhi)
                .wuxingJu(chartEntity.getWuxingJu())
                .palaces(palaces)
                .natalSihua(natalSihua)
                .daxianCycles(daxianCycles)
                .build();

        // Pattern matching (re-run for data integrity)
        List<String> patterns = patternMatcher.matchPatterns(ctx);
        ctx.setPatternNames(patterns);

        return ctx;
    }

    // ========== Star Placement Helpers ==========

    /**
     * Fill all star groups into palaces
     */
    private void placeStarsIntoPalaces(EnumMap<PalaceTypeEnum, Palace> palaces,
                                        Map<String, DiZhiEnum> ziweiGroup,
                                        Map<String, DiZhiEnum> tianfuGroup,
                                        Map<String, DiZhiEnum> auxStars,
                                        Map<String, DiZhiEnum> minorStars,
                                        Map<String, DiZhiEnum> extendedStars) {
        // Major stars
        placeGroup(palaces, ziweiGroup, StarTypeEnum.MAJOR);
        placeGroup(palaces, tianfuGroup, StarTypeEnum.MAJOR);
        // Auxiliary stars
        placeGroup(palaces, auxStars, StarTypeEnum.AUXILIARY);
        // Minor stars
        placeGroup(palaces, minorStars, StarTypeEnum.MINOR);
        // Extended minor stars
        placeGroup(palaces, extendedStars, StarTypeEnum.MINOR);
    }

    /**
     * Place a single star group into palaces by DiZhi
     */
    private void placeGroup(EnumMap<PalaceTypeEnum, Palace> palaces,
                            Map<String, DiZhiEnum> starMap,
                            StarTypeEnum type) {
        for (Map.Entry<String, DiZhiEnum> entry : starMap.entrySet()) {
            String starCode = entry.getKey();
            DiZhiEnum diZhi = entry.getValue();
            String starName = Star.getNameByCode(starCode);
            StarBrightnessEnum brightness = brightnessCalculator.calculate(starCode, diZhi);

            for (Palace palace : palaces.values()) {
                if (palace.getDiZhi() == diZhi) {
                    StarPosition sp = StarPosition.builder()
                            .starCode(starCode)
                            .starName(starName)
                            .starType(type)
                            .brightness(brightness)
                            .build();
                    palace.addStar(sp);
                    break;
                }
            }
        }
    }

    /**
     * Apply sihua transformations to stars in palaces
     */
    private void applySihuaToPalaces(EnumMap<PalaceTypeEnum, Palace> palaces, List<StarPosition> sihuaList) {
        for (StarPosition sihua : sihuaList) {
            for (Palace palace : palaces.values()) {
                for (StarPosition star : palace.getStars()) {
                    if (star.getStarCode().equals(sihua.getStarCode())) {
                        star.setSihuaType(sihua.getSihuaType());
                    }
                }
            }
        }
    }

    /**
     * Parse a pillar string with length validation.
     */
    private Pillar parsePillar(String pillarStr) {
        if (pillarStr == null || pillarStr.length() != 2) {
            log.warn("Invalid pillar format: length={}, value={}",
                    pillarStr == null ? 0 : pillarStr.length(), pillarStr);
            return Pillar.of(pillarStr);
        }
        return Pillar.of(pillarStr);
    }

}
