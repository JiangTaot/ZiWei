/**
 * AI interpretation API
 * Migrated from src/api/ai.js
 */
import http from '../../request'

export default {
  /**
   * Request AI interpretation for an entire chart
   * @param {string|number} chartId
   * @param {string} [question] - Optional user question
   * @returns {Promise}
   */
  interpretChart(chartId, question) {
    return http.post(`/ai/interpret/${chartId}`, { question })
  },

  /**
   * Request AI interpretation for a specific palace
   * @param {string|number} chartId
   * @param {string|number} palaceType
   * @param {string} [question] - Optional user question
   * @returns {Promise}
   */
  interpretPalace(chartId, palaceType, question) {
    return http.post(`/ai/interpret/${chartId}/palace/${palaceType}`, { question })
  },
}
