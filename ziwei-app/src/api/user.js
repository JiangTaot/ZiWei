// User API - WeChat login and profile management
import http from './request'

/**
 * WeChat login
 * @param {string} code - wx.login() returned code
 * @param {string} [nickname] - User nickname (optional)
 * @param {string} [avatar] - User avatar URL (optional)
 * @returns {Promise<Object>} LoginResponse with token, userId, openid, nickname, avatar
 */
export function login(code, nickname, avatar) {
  return http.post('/api/ziwei/user/login', {
    code,
    nickname,
    avatar,
  }, {
    skipAuth: true, // No token needed for login
  })
}

/**
 * Get current user profile
 * @returns {Promise<Object>} User entity
 */
export function getProfile() {
  return http.get('/api/ziwei/user/profile')
}

/**
 * Update user profile
 * @param {Object} data - Profile data { nickname?, avatar? }
 * @returns {Promise<void>}
 */
export function updateProfile(data) {
  return http.put('/api/ziwei/user/profile', data)
}
