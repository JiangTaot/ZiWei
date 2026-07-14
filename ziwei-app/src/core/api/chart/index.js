/**
 * Chart calculation API
 * Migrated from src/api/chart.js
 */
import http from '../../request'

export default {
  /**
   * Calculate a ZiWei DouShu chart
   * @param {Object} data - Birth info form data
   * @returns {Promise}
   */
  calculate(data) {
    return http.post('/chart/calculate', data, {
      custom: { showLoading: true, loadingMsg: '正在排盘中...', auth: false },
    })
  },

  /**
   * Get chart by ID
   * @param {string|number} id - Chart ID
   * @returns {Promise}
   */
  getChartById(id) {
    return http.get(`/chart/${id}`, {}, {
      custom: { showLoading: true },
    })
  },

  /**
   * Get current user's chart list
   * @returns {Promise}
   */
  getMyCharts() {
    return http.get('/chart/my-list')
  },

  /**
   * Delete a chart
   * @param {string|number} id - Chart ID
   * @returns {Promise}
   */
  deleteChart(id) {
    return http.delete(`/chart/${id}`)
  },
}
