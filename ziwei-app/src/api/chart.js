// Chart calculation API
// Matches backend endpoints: /api/ziwei/chart/*
import http from './request'

/**
 * Calculate a ZiWei DouShu natal chart
 * POST /api/ziwei/chart/calculate
 * @param {Object} data - Birth info
 * @param {number} data.solarYear - Gregorian year
 * @param {number} data.solarMonth - Gregorian month (1-12)
 * @param {number} data.solarDay - Gregorian day
 * @param {number} data.hour - Earthly branch index (0=子, 1=丑, ..., 11=亥)
 * @param {number} [data.minute=0] - Minute (0-59)
 * @param {number} data.gender - 0=female, 1=male
 * @param {string} [data.birthPlace] - Birth location
 * @param {boolean} [data.isDst=false] - Daylight saving time flag
 * @returns {Promise<Object>} Result<ZiweiChartVO>
 */
export function calculate(data) {
  return http.post('/api/ziwei/chart/calculate', data, {
    showLoading: true,
    skipAuth: true, // chart calculation is open to all users
  })
}

/**
 * Get a chart by ID
 * GET /api/ziwei/chart/{id}
 * @param {number|string} id - Chart ID
 * @returns {Promise<Object>} Result<ZiweiChartVO>
 */
export function getChartById(id) {
  return http.get(`/api/ziwei/chart/${id}`)
}

/**
 * Get current user's chart list
 * GET /api/ziwei/chart/my-list
 * @returns {Promise<Object>} Result<List<ZiweiChartVO>>
 */
export function getMyCharts() {
  return http.get('/api/ziwei/chart/my-list')
}

/**
 * Delete a chart
 * DELETE /api/ziwei/chart/{id}
 * @param {number|string} id - Chart ID
 * @returns {Promise<Object>} Result<Void>
 */
export function deleteChart(id) {
  return http.delete(`/api/ziwei/chart/${id}`)
}
