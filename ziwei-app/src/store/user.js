// User state management using Pinia
// Manages user profile, preferences, chart history, and auth state
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getProfile as getProfileApi, updateProfile as updateProfileApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // === State ===

  // Auth state
  const isLoggedIn = ref(false)
  const token = ref('')
  const openId = ref('')

  // User profile
  const userId = ref('')
  const nickName = ref('')
  const avatarUrl = ref('')
  const phoneNumber = ref('')

  // User preferences
  const preferences = ref({
    language: 'zh-CN',
    defaultGender: 'male',
    defaultLocation: '',
    theme: 'classic', // 'classic' | 'dark'
  })

  // Chart related
  const chartHistory = ref([])
  const favoriteStars = ref([])
  const interpretationCount = ref(0)

  // === Getters ===

  const isAuthenticated = computed(() => isLoggedIn.value && !!token.value)

  const displayName = computed(() => nickName.value || '紫微星人')

  const chartCount = computed(() => chartHistory.value.length)

  const recentCharts = computed(() => {
    return chartHistory.value.slice(0, 5)
  })

  // === Actions ===

  /**
   * Initialize store from local storage
   */
  function init() {
    try {
      // Load auth
      const savedToken = uni.getStorageSync('auth_token')
      if (savedToken) {
        token.value = savedToken
        isLoggedIn.value = true
      }

      // Load profile
      const profile = uni.getStorageSync('user_profile')
      if (profile) {
        const p = JSON.parse(profile)
        nickName.value = p.nickName || ''
        avatarUrl.value = p.avatarUrl || ''
        phoneNumber.value = p.phoneNumber || ''
      }

      // Load preferences
      const prefs = uni.getStorageSync('user_preferences')
      if (prefs) {
        preferences.value = { ...preferences.value, ...JSON.parse(prefs) }
      }

      // Load chart history
      const history = uni.getStorageSync('chart_history')
      if (history) {
        chartHistory.value = JSON.parse(history)
      }

      // Load interpretation count
      const count = uni.getStorageSync('interpretation_count')
      if (count) {
        interpretationCount.value = parseInt(count) || 0
      }

      // Load favorite stars
      const stars = uni.getStorageSync('favorite_stars')
      if (stars) {
        favoriteStars.value = JSON.parse(stars)
      }
    } catch (e) {
      console.error('[ZiWei Store] Init failed:', e)
    }
  }

  /**
   * Update user profile (local + sync to backend)
   * @param {Object} profile - Profile data to update
   * @param {boolean} [syncToServer=true] - Whether to sync to backend
   */
  async function updateProfile(profile, syncToServer = true) {
    if (profile.nickName !== undefined) nickName.value = profile.nickName
    if (profile.avatarUrl !== undefined) avatarUrl.value = profile.avatarUrl
    if (profile.phoneNumber !== undefined) phoneNumber.value = profile.phoneNumber

    // Persist to storage
    uni.setStorageSync('user_profile', JSON.stringify({
      nickName: nickName.value,
      avatarUrl: avatarUrl.value,
      phoneNumber: phoneNumber.value,
    }))

    // Sync to backend if logged in
    if (syncToServer && isLoggedIn.value) {
      try {
        await updateProfileApi({
          nickname: nickName.value,
          avatar: avatarUrl.value,
        })
      } catch (e) {
        console.error('[ZiWei Store] Sync profile to server failed:', e)
      }
    }
  }

  /**
   * Update user preferences
   * @param {Object} prefs - Preferences to update (partial)
   */
  function updatePreferences(prefs) {
    preferences.value = { ...preferences.value, ...prefs }
    uni.setStorageSync('user_preferences', JSON.stringify(preferences.value))
  }

  /**
   * Add a chart to history
   * @param {Object} chart - Chart data to save
   */
  function addChart(chart) {
    const record = {
      id: `chart_${Date.now()}`,
      ...chart,
      createdAt: new Date().toISOString(),
    }
    chartHistory.value.unshift(record)

    // Keep max 50 records
    if (chartHistory.value.length > 50) {
      chartHistory.value = chartHistory.value.slice(0, 50)
    }

    uni.setStorageSync('chart_history', JSON.stringify(chartHistory.value))
    return record
  }

  /**
   * Delete a chart from history
   * @param {string} chartId - Chart ID to delete
   */
  function removeChart(chartId) {
    chartHistory.value = chartHistory.value.filter((c) => c.id !== chartId)
    uni.setStorageSync('chart_history', JSON.stringify(chartHistory.value))
  }

  /**
   * Toggle a star in favorites
   * @param {string} starName - Star name to toggle
   */
  function toggleFavoriteStar(starName) {
    const idx = favoriteStars.value.indexOf(starName)
    if (idx >= 0) {
      favoriteStars.value.splice(idx, 1)
    } else {
      favoriteStars.value.push(starName)
    }
    uni.setStorageSync('favorite_stars', JSON.stringify(favoriteStars.value))
  }

  /**
   * Increment interpretation count
   */
  function incrementInterpretation() {
    interpretationCount.value++
    uni.setStorageSync('interpretation_count', String(interpretationCount.value))
  }

  /**
   * Login with WeChat
   * @param {string} code - WeChat login code
   * @param {string} [nickname] - User nickname
   * @param {string} [avatar] - User avatar URL
   */
  async function login(code, nickname, avatar) {
    try {
      const res = await loginApi(code, nickname, avatar)
      // Handle both {data: {...}} and direct {...} response formats
      const data = res.data || res
      if (data && data.token) {
        token.value = data.token
        openId.value = data.openid || ''
        userId.value = data.userId || ''
        nickName.value = data.nickname || ''
        avatarUrl.value = data.avatar || ''
        isLoggedIn.value = true

        // Persist token and profile
        uni.setStorageSync('auth_token', data.token)
        uni.setStorageSync('user_openid', data.openid || '')
        uni.setStorageSync('user_id', data.userId || '')
        uni.setStorageSync('user_profile', JSON.stringify({
          nickName: data.nickname || '',
          avatarUrl: data.avatar || '',
        }))
        return data
      }
      return null
    } catch (e) {
      console.error('[ZiWei Store] Login failed:', e.message || e)
      return null
    }
  }

  /**
   * Logout
   */
  function logout() {
    token.value = ''
    openId.value = ''
    userId.value = ''
    nickName.value = ''
    avatarUrl.value = ''
    isLoggedIn.value = false
    uni.removeStorageSync('auth_token')
    uni.removeStorageSync('user_profile')
  }

  /**
   * Clear all chart history
   */
  function clearHistory() {
    chartHistory.value = []
    uni.removeStorageSync('chart_history')
  }

  return {
    // State
    isLoggedIn,
    token,
    openId,
    userId,
    nickName,
    avatarUrl,
    phoneNumber,
    preferences,
    chartHistory,
    favoriteStars,
    interpretationCount,
    // Getters
    isAuthenticated,
    displayName,
    chartCount,
    recentCharts,
    // Actions
    init,
    updateProfile,
    updatePreferences,
    addChart,
    removeChart,
    toggleFavoriteStar,
    incrementInterpretation,
    login,
    logout,
    clearHistory,
  }
})
