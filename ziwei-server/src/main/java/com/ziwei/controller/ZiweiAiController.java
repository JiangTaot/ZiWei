package com.ziwei.controller;

import com.ziwei.dto.Result;
import com.ziwei.service.ZiweiAiInterpretService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * Ziwei AI interpretation controller - REST API for AI-powered chart readings
 *
 * @author JTWORLD
 */
@Tag(name = "紫微斗数 - AI 解读")
@RestController
@RequestMapping("/api/ziwei/ai")
@Validated
@Slf4j
public class ZiweiAiController {

    @Resource
    private ZiweiAiInterpretService aiService;

    /**
     * Synchronous full chart interpretation
     *
     * @param chartId  chart ID
     * @param question optional user question
     * @return AI interpretation result
     */
    @PostMapping("/interpret/{chartId}")
    @Operation(summary = "AI 解读整个命盘")
    public Result<String> interpretChart(
            @PathVariable("chartId") Long chartId,
            @Parameter(description = "请求体，可包含 question 字段")
            @RequestBody(required = false) Map<String, String> body) {
        String question = body != null ? body.get("question") : null;
        log.info("AI interpret chart: chartId={}, question={}", chartId, question);
        String result = aiService.interpretChart(chartId, question);
        return Result.success(result);
    }

    /**
     * Streaming full chart interpretation (Server-Sent Events)
     *
     * @param chartId  chart ID
     * @param question optional user question
     * @return SSE stream of interpretation text
     */
    @PostMapping(value = "/interpret/{chartId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 流式解读整个命盘（SSE）")
    public Flux<String> interpretChartStream(
            @PathVariable("chartId") Long chartId,
            @Parameter(description = "请求体，可包含 question 字段")
            @RequestBody(required = false) Map<String, String> body) {
        String question = body != null ? body.get("question") : null;
        log.info("AI stream interpret chart: chartId={}, question={}", chartId, question);
        return aiService.interpretChartStream(chartId, question);
    }

    /**
     * Synchronous single palace interpretation
     *
     * @param chartId    chart ID
     * @param palaceType palace type code (1-12)
     * @param question   optional user question
     * @return AI palace interpretation result
     */
    @PostMapping("/interpret/{chartId}/palace/{palaceType}")
    @Operation(summary = "AI 解读特定宫位")
    public Result<String> interpretPalace(
            @PathVariable("chartId") Long chartId,
            @PathVariable("palaceType") int palaceType,
            @Parameter(description = "请求体，可包含 question 字段")
            @RequestBody(required = false) Map<String, String> body) {
        String question = body != null ? body.get("question") : null;
        log.info("AI interpret palace: chartId={}, palaceType={}, question={}", chartId, palaceType, question);
        String result = aiService.interpretPalace(chartId, palaceType, question);
        return Result.success(result);
    }

}
