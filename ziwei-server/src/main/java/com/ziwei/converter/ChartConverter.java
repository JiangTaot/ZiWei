package com.ziwei.converter;

import com.ziwei.dto.ZiweiChartVO;
import com.ziwei.dto.ZiweiPalaceVO;
import com.ziwei.dto.ZiweiStarVO;
import com.ziwei.engine.model.*;
import com.ziwei.engine.pattern.PatternResult;
import com.ziwei.entity.*;
import com.ziwei.enums.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter between ChartContext, Entities, and VOs
 *
 * @author JTWORLD
 */
public class ChartConverter {

    /**
     * Convert ChartContext → ZiweiChartVO
     */
    public static ZiweiChartVO toChartVO(ZiweiChartEntity chartEntity, ChartContext ctx) {
        Bazi bazi = ctx.getBazi();
        ZiweiChartVO.ZiweiChartVOBuilder builder = ZiweiChartVO.builder()
                .id(chartEntity.getId())
                .userId(chartEntity.getUserId())
                .solarYear(chartEntity.getSolarYear())
                .solarMonth(chartEntity.getSolarMonth())
                .solarDay(chartEntity.getSolarDay())
                .solarHour(chartEntity.getSolarHour())
                .solarMinute(chartEntity.getSolarMinute())
                .gender(chartEntity.getGender())
                .birthPlace(chartEntity.getBirthPlace())
                .isDst(chartEntity.getIsDst() != null && chartEntity.getIsDst())
                .lunarYear(chartEntity.getLunarYear())
                .lunarMonth(chartEntity.getLunarMonth())
                .lunarDay(chartEntity.getLunarDay())
                .isLeapMonth(chartEntity.getIsLeapMonth() != null && chartEntity.getIsLeapMonth())
                .yearPillar(bazi.getYearPillar() != null ? bazi.getYearPillar().toString() : null)
                .monthPillar(bazi.getMonthPillar() != null ? bazi.getMonthPillar().toString() : null)
                .dayPillar(bazi.getDayPillar() != null ? bazi.getDayPillar().toString() : null)
                .hourPillar(bazi.getHourPillar() != null ? bazi.getHourPillar().toString() : null)
                .mingGong((ctx.getMingGongDiZhi() != null ? ctx.getMingGongDiZhi().getName() : "") + "宫")
                .shenGong((ctx.getShenGongDiZhi() != null ? ctx.getShenGongDiZhi().getName() : "") + "宫")
                .wuxingJu(ctx.getWuxingJu())
                .createTime(chartEntity.getCreateTime());

        // Palaces
        if (ctx.getPalaces() != null) {
            List<ZiweiPalaceVO> palaceVOs = ctx.getPalaces().values().stream()
                    .map(ChartConverter::toPalaceVO)
                    .collect(Collectors.toList());
            builder.palaces(palaceVOs);
        }

        // Patterns
        if (ctx.getPatternNames() != null) {
            builder.patterns(ctx.getPatternNames());
        }
        if (ctx.getPatternResults() != null) {
            builder.patternDetails(ctx.getPatternResults().stream()
                    .map(ChartConverter::toPatternDetailVO)
                    .collect(Collectors.toList()));
        }

        // Natal Sihua
        if (ctx.getNatalSihua() != null && ctx.getNatalSihua().size() >= 4) {
            builder.natalSihua(toNatalSihuaVO(ctx.getNatalSihua()));
        }

        // DaXian
        if (ctx.getDaxianCycles() != null) {
            builder.daxians(ctx.getDaxianCycles().stream()
                    .map(ChartConverter::toDaxianVO)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    /**
     * Convert Palace → ZiweiPalaceVO
     */
    public static ZiweiPalaceVO toPalaceVO(Palace palace) {
        return ZiweiPalaceVO.builder()
                .palaceType(palace.getType().getCode())
                .palaceName(palace.getType().getName())
                .dizhi(palace.getDiZhi().getName())
                .tianGan(palace.getTianGan() != null ? palace.getTianGan().getName() : null)
                .isShenGong(palace.isShenGong())
                .daXianLabel(palace.getDaXianLabel())
                .majorStars(palace.getMajorStars().stream()
                        .map(ChartConverter::toStarVO)
                        .collect(Collectors.toList()))
                .auxiliaryStars(palace.getAuxiliaryStars().stream()
                        .map(ChartConverter::toStarVO)
                        .collect(Collectors.toList()))
                .minorStars(palace.getMinorStars().stream()
                        .map(ChartConverter::toStarVO)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convert StarPosition → ZiweiStarVO
     */
    public static ZiweiStarVO toStarVO(StarPosition sp) {
        return ZiweiStarVO.builder()
                .starCode(sp.getStarCode())
                .starName(sp.getStarName())
                .brightness(sp.getBrightness() != null ? sp.getBrightness().getName() : null)
                .sihuaType(sp.getSihuaType() != null ? sp.getSihuaType().getShortName() : null)
                .build();
    }

    /**
     * Convert natal sihua list → NatalSihuaVO
     */
    public static ZiweiChartVO.NatalSihuaVO toNatalSihuaVO(List<StarPosition> sihuaList) {
        if (sihuaList == null || sihuaList.size() < 4) return null;
        return ZiweiChartVO.NatalSihuaVO.builder()
                .huaLu(toStarVO(sihuaList.get(0)))
                .huaQuan(toStarVO(sihuaList.get(1)))
                .huaKe(toStarVO(sihuaList.get(2)))
                .huaJi(toStarVO(sihuaList.get(3)))
                .build();
    }

    /**
     * Convert DaxianCycle → DaxianVO
     */
    public static ZiweiChartVO.DaxianVO toDaxianVO(DaxianCycle dc) {
        return ZiweiChartVO.DaxianVO.builder()
                .ageStart(dc.getAgeStart())
                .ageEnd(dc.getAgeEnd())
                .palaceName(dc.getPalaceType() != null ? dc.getPalaceType().getName() : "")
                .direction(dc.isForward() ? "顺行" : "逆行")
                .build();
    }

    /**
     * Convert PatternResult → PatternDetailVO
     */
    public static ZiweiChartVO.PatternDetailVO toPatternDetailVO(PatternResult pr) {
        return ZiweiChartVO.PatternDetailVO.builder()
                .name(pr.getName())
                .level(pr.getLevel() != null ? pr.getLevel().getName() : null)
                .description(pr.getDescription())
                .source(pr.getSource())
                .required(pr.getRequired())
                .bonus(pr.getBonus())
                .breaking(pr.getBreaking())
                .build();
    }

    /**
     * Create a VO from calculation-only result (no DB save).
     * Used when DB is unavailable — charts are not persisted.
     */
    public static ZiweiChartVO createUnsavedVO(ChartContext ctx, com.ziwei.dto.ZiweiBirthInfoDTO dto) {
        Bazi bazi = ctx.getBazi();
        if (bazi == null) return ZiweiChartVO.builder().id(0L).build();

        ZiweiChartVO.ZiweiChartVOBuilder builder = ZiweiChartVO.builder()
                .id(0L) // unsaved marker
                .userId(dto.getUserId())
                .solarYear(dto.getSolarYear()).solarMonth(dto.getSolarMonth()).solarDay(dto.getSolarDay())
                .solarHour(dto.getHour()).gender(dto.getGender())
                .yearPillar(bazi.getYearPillar() != null ? bazi.getYearPillar().toString() : null)
                .monthPillar(bazi.getMonthPillar() != null ? bazi.getMonthPillar().toString() : null)
                .dayPillar(bazi.getDayPillar() != null ? bazi.getDayPillar().toString() : null)
                .hourPillar(bazi.getHourPillar() != null ? bazi.getHourPillar().toString() : null)
                .mingGong((ctx.getMingGongDiZhi() != null ? ctx.getMingGongDiZhi().getName() : "") + "宫")
                .shenGong((ctx.getShenGongDiZhi() != null ? ctx.getShenGongDiZhi().getName() : "") + "宫")
                .wuxingJu(ctx.getWuxingJu());

        if (ctx.getPalaces() != null) {
            builder.palaces(ctx.getPalaces().values().stream().map(ChartConverter::toPalaceVO).collect(Collectors.toList()));
        }
        if (ctx.getDaxianCycles() != null) {
            builder.daxians(ctx.getDaxianCycles().stream().map(ChartConverter::toDaxianVO).collect(Collectors.toList()));
        }
        if (ctx.getNatalSihua() != null) {
            builder.natalSihua(toNatalSihuaVO(ctx.getNatalSihua()));
        }
        if (ctx.getPatternResults() != null) {
            builder.patternDetails(ctx.getPatternResults().stream().map(ChartConverter::toPatternDetailVO).collect(Collectors.toList()));
        }

        return builder.build();
    }

}
