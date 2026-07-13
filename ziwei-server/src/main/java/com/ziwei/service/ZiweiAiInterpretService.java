package com.ziwei.service;

import reactor.core.publisher.Flux;

/**
 * Ziwei AI chart interpretation service interface
 * <p>
 * Uses Spring AI with DeepSeek for professional Ziwei DouShu chart readings
 * based on classical SanHe (三合) school methodology.
 *
 * @author JTWORLD
 */
public interface ZiweiAiInterpretService {

    /**
     * Synchronous full chart interpretation
     *
     * @param chartId  chart ID
     * @param question user question (optional, default: comprehensive reading)
     * @return AI interpretation text
     */
    String interpretChart(Long chartId, String question);

    /**
     * Streaming full chart interpretation (Server-Sent Events)
     *
     * @param chartId  chart ID
     * @param question user question (optional)
     * @return streaming interpretation text
     */
    Flux<String> interpretChartStream(Long chartId, String question);

    /**
     * Synchronous single palace interpretation
     *
     * @param chartId    chart ID
     * @param palaceType palace type code (1-12)
     * @param question   user question (optional)
     * @return AI interpretation text for the specific palace
     */
    String interpretPalace(Long chartId, int palaceType, String question);

}
