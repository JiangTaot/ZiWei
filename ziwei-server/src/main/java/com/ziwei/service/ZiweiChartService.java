package com.ziwei.service;

import com.ziwei.dto.ZiweiBirthInfoDTO;
import com.ziwei.dto.ZiweiChartVO;
import com.ziwei.engine.model.ChartContext;

import java.util.List;

/**
 * Ziwei chart service interface
 *
 * @author JTWORLD
 */
public interface ZiweiChartService {

    /**
     * Calculate chart (engine only, no persistence)
     *
     * @param dto birth info
     * @return complete ChartContext
     */
    ChartContext calculate(ZiweiBirthInfoDTO dto);

    /**
     * Calculate chart and persist to database
     *
     * @param dto birth info (must include userId)
     * @return chart VO
     */
    ZiweiChartVO calculateAndSave(ZiweiBirthInfoDTO dto);

    /**
     * Get chart by ID from database
     *
     * @param id chart ID
     * @return chart VO, or null if not found
     */
    ZiweiChartVO getChartById(Long id);

    /**
     * Get all charts belonging to a user
     *
     * @param userId user ID
     * @return list of chart VOs
     */
    List<ZiweiChartVO> getMyCharts(Long userId);

    /**
     * Delete a chart and all its related data
     *
     * @param id chart ID
     */
    void deleteChart(Long id);

}
