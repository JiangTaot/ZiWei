/**
 * Chart store — chart history, favorites, interpretation count
 * Extracted from the original monolithic user store.
 *
 * Uses pinia-plugin-persist-uni for automatic persistence.
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import $api from '@/core/api'

const useChartStore = defineStore('chart', () => {
  // === State ===
  const chartHistory = ref([])
  const favoriteStars = ref([])
  const interpretationCount = ref(0)
  const isLoading = ref(false)        // 正在加载服务端命盘列表
  const serverCharts = ref([])       // 服务端命盘列表（已标准化含显示字段）
  const isServerLoaded = ref(false)  // 是否已从服务端加载过

  // === Getters ===
  const chartCount = computed(() => chartHistory.value.length)

  const recentCharts = computed(() => {
    return chartHistory.value.slice(0, 5)
  })

  /**
   * 合并后的命盘列表：服务端 + 本地（按 id 去重）
   * 服务端数据优先，本地数据补充。
   * 注意：服务端数据在 loadServerCharts 中已标准化为包含显示字段的格式。
   */
  const allCharts = computed(() => {
    const idMap = new Map()

    // 先添加服务端命盘（已标准化，包含 displayDate 等字段）
    serverCharts.value.forEach((c) => {
      if (c.id) idMap.set(String(c.id), c)
    })

    // 再补充本地独有的命盘
    chartHistory.value.forEach((c) => {
      const key = String(c.id)
      if (!idMap.has(key)) {
        idMap.set(key, c)
      }
    })

    return Array.from(idMap.values())
  })

  // === Actions ===

  /**
   * Add a chart to history
   * @param {Object} chartData - Chart data from API or mock
   * @returns {Object} The saved history record
   */
  function addChart(chartData) {
    const record = {
      id: chartData.id || `chart_${Date.now()}`,
      displayDate: chartData.displayDate || `${chartData.solarYear}-${String(chartData.solarMonth).padStart(2, '0')}-${String(chartData.solarDay).padStart(2, '0')}`,
      displayTime: chartData.displayTime || `${chartData.yearPillar || ''}${chartData.monthPillar || ''}${chartData.dayPillar || ''}${chartData.hourPillar || ''}`,
      genderLabel: chartData.gender === 1 ? '男' : '女',
      location: chartData.birthPlace || '',
      createTime: chartData.createTime || new Date().toISOString(),
    }
    chartHistory.value.unshift(record)

    // Keep max 50 records
    if (chartHistory.value.length > 50) {
      chartHistory.value = chartHistory.value.slice(0, 50)
    }

    return record
  }

  /**
   * Delete a chart from history
   * @param {string|number} chartId
   */
  function removeChart(chartId) {
    chartHistory.value = chartHistory.value.filter((c) => c.id !== chartId)
  }

  /**
   * Clear all chart history
   */
  function clearHistory() {
    chartHistory.value = []
  }

  /**
   * Toggle a star in favorites
   * @param {string} starName
   */
  function toggleFavoriteStar(starName) {
    const idx = favoriteStars.value.indexOf(starName)
    if (idx >= 0) {
      favoriteStars.value.splice(idx, 1)
    } else {
      favoriteStars.value.push(starName)
    }
  }

  /**
   * Increment interpretation count
   */
  function incrementInterpretation() {
    interpretationCount.value++
  }

  /**
   * 从服务端加载当前用户的命盘列表（需已登录）
   * 将服务端数据标准化后存入 serverCharts，同时同步到本地 chartHistory 保持向后兼容。
   * @returns {Promise<boolean>} 加载成功返回 true
   */
  async function loadServerCharts() {
    // 防止并发重复请求
    if (isLoading.value) return false
    isLoading.value = true
    console.log('[ZiWei] 开始加载服务端命盘列表...')
    try {
      const res = await $api.chart.getMyCharts()
      console.log('[ZiWei] /my-list 响应:', JSON.stringify(res))
      const list = res.data || res || []
      if (Array.isArray(list)) {
        // 将服务端原始数据标准化为包含显示字段的格式
        const normalized = list.map((c) => ({
          id: c.id,
          displayDate: c.displayDate || `${c.solarYear || ''}-${String(c.solarMonth || '').padStart(2, '0')}-${String(c.solarDay || '').padStart(2, '0')}`,
          displayTime: c.displayTime || [c.yearPillar, c.monthPillar, c.dayPillar, c.hourPillar].filter(Boolean).join(''),
          genderLabel: c.gender === 1 ? '男' : '女',
          location: c.birthPlace || '',
          createTime: c.createTime || new Date().toISOString(),
          // 保留原始字段，供详情页使用
          solarYear: c.solarYear,
          solarMonth: c.solarMonth,
          solarDay: c.solarDay,
          gender: c.gender,
          birthPlace: c.birthPlace,
        }))
        serverCharts.value = normalized
        isServerLoaded.value = true
        console.log('[ZiWei] 服务端命盘加载完成，共', normalized.length, '条')

        // 同步到本地 history（向后兼容）
        normalized.forEach((nc) => {
          const exists = chartHistory.value.some(
            (h) => String(h.id) === String(nc.id)
          )
          if (!exists) {
            chartHistory.value.push({ ...nc })
          }
        })
        return true
      }
      return false
    } catch (e) {
      console.error('[ZiWei Store] 加载服务端命盘失败:', e)
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Delete a chart from server
   * @param {string|number} chartId
   * @returns {Promise<boolean>}
   */
  async function deleteServerChart(chartId) {
    isLoading.value = true
    try {
      await $api.chart.deleteChart(chartId)
      serverCharts.value = serverCharts.value.filter(
        (c) => String(c.id) !== String(chartId)
      )
      chartHistory.value = chartHistory.value.filter(
        (c) => String(c.id) !== String(chartId)
      )
      return true
    } catch (e) {
      console.error('[ZiWei Store] 删除命盘失败:', e)
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 登出时重置服务端命盘状态
   */
  function clearServerCharts() {
    serverCharts.value = []
    isServerLoaded.value = false
  }

  return {
    // State
    chartHistory,
    favoriteStars,
    interpretationCount,
    isLoading,
    serverCharts,
    isServerLoaded,
    // Getters
    chartCount,
    recentCharts,
    allCharts,
    // Actions
    addChart,
    removeChart,
    clearHistory,
    toggleFavoriteStar,
    incrementInterpretation,
    loadServerCharts,
    deleteServerChart,
    clearServerCharts,
  }
}, {
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'chart-store',
        storage: {
          getItem: (key) => uni.getStorageSync(key),
          setItem: (key, value) => uni.setStorageSync(key, value),
        },
      },
    ],
  },
})

export default useChartStore
