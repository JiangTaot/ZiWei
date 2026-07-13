package com.ziwei.controller;

import com.ziwei.dto.Result;
import com.ziwei.dto.ZiweiBirthInfoDTO;
import com.ziwei.dto.ZiweiChartVO;
import com.ziwei.enums.ErrorCodeConstants;
import com.ziwei.exception.BusinessException;
import com.ziwei.service.ZiweiChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Ziwei chart controller - REST API for chart calculation and query
 *
 * @author JTWORLD
 */
@RestController
@RequestMapping("/api/ziwei/chart")
@Tag(name = "紫微斗数 - 命盘")
@Slf4j
public class ZiweiChartController {

    @Resource
    private ZiweiChartService ziweiChartService;

    /**
     * Calculate and save chart
     *
     * @param dto     birth info
     * @param request HTTP request with userId attribute set by JWT interceptor
     * @return full chart VO
     */
    @PostMapping("/calculate")
    @Operation(summary = "排盘并保存")
    public Result<ZiweiChartVO> calculate(@Valid @RequestBody ZiweiBirthInfoDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        log.info("Calculate chart: year={}, month={}, day={}, hour={}, userId={}",
                dto.getSolarYear(), dto.getSolarMonth(), dto.getSolarDay(), dto.getHour(), userId);
        ZiweiChartVO vo = ziweiChartService.calculateAndSave(dto);
        return Result.success(vo);
    }

    /**
     * Get chart by ID
     *
     * @param id chart ID
     * @return full chart VO
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取命盘详情")
    public Result<ZiweiChartVO> getChart(@PathVariable Long id, HttpServletRequest request) {
        log.info("Get chart: id={}", id);
        Long currentUserId = (Long) request.getAttribute("userId");
        ZiweiChartVO vo = ziweiChartService.getChartById(id);
        if (vo.getUserId() != null && !vo.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN);
        }
        return Result.success(vo);
    }

    /**
     * Get my chart list (userId from JWT)
     *
     * @param request HTTP request with userId attribute
     * @return list of chart VOs (summary only)
     */
    @GetMapping("/my-list")
    @Operation(summary = "我的命盘列表")
    public Result<List<ZiweiChartVO>> myList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("Get my charts: userId={}", userId);
        List<ZiweiChartVO> list = ziweiChartService.getMyCharts(userId);
        return Result.success(list);
    }

    /**
     * Delete chart by ID
     *
     * @param id chart ID
     * @return empty success
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除命盘")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        log.info("Delete chart: id={}", id);
        Long currentUserId = (Long) request.getAttribute("userId");
        // Verify ownership by getting the chart first
        ZiweiChartVO vo = ziweiChartService.getChartById(id);
        if (vo.getUserId() != null && !vo.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN);
        }
        ziweiChartService.deleteChart(id);
        return Result.success(null);
    }

}
