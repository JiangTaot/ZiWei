// AI interpretation API
// Matches backend endpoints: /api/ziwei/ai/*
import http from './request'

/**
 * AI interpretation of a full chart
 * POST /api/ziwei/ai/interpret/{chartId}?question=xxx
 * @param {number|string} chartId - Chart ID
 * @param {string} [question] - Optional user question
 * @returns {Promise<Object>} Result<String>
 */
export function interpretChart(chartId, question) {
  return http.post(`/api/ziwei/ai/interpret/${chartId}`, null, {
    params: { question: question || '请全面解读此命盘' },
    showLoading: false,
  })
}

/**
 * AI interpretation of a specific palace
 * POST /api/ziwei/ai/interpret/{chartId}/palace/{palaceType}?question=xxx
 * @param {number|string} chartId - Chart ID
 * @param {number} palaceType - Palace type code (1-12)
 * @param {string} [question] - Optional user question
 * @returns {Promise<Object>} Result<String>
 */
export function interpretPalace(chartId, palaceType, question) {
  return http.post(`/api/ziwei/ai/interpret/${chartId}/palace/${palaceType}`, null, {
    params: { question: question || '请解读此宫位' },
    showLoading: false,
  })
}
