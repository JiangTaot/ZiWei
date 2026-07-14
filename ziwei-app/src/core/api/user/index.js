/**
 * User / Auth API
 * Migrated from src/api/user.js
 */
import http from '../../request'

export default {
  /**
   * Login with WeChat code
   * @param {string} code - WeChat login code
   * @param {string} [nickname] - User nickname
   * @param {string} [avatar] - User avatar URL
   * @returns {Promise}
   */
  login(code, nickname, avatar) {
    return http.post('/user/login', { code, nickname, avatar }, {
      custom: { showLoading: true, loadingMsg: '登录中...', auth: false },
    })
  },

  /**
   * Get current user profile
   * @returns {Promise}
   */
  getProfile() {
    return http.get('/user/profile')
  },

  /**
   * Update user profile
   * @param {Object} data - Profile fields to update
   * @returns {Promise}
   */
  updateProfile(data) {
    return http.put('/user/profile', data)
  },
}
