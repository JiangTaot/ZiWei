/**
 * Chart store — chart history, favorites, interpretation count
 * Extracted from the original monolithic user store.
 *
 * Uses pinia-plugin-persist-uni for automatic persistence.
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const useChartStore = defineStore('chart', () => {
  // === State ===
  const chartHistory = ref([])
  const favoriteStars = ref([])
  const interpretationCount = ref(0)

  // === Getters ===
  const chartCount = computed(() => chartHistory.value.length)

  const recentCharts = computed(() => {
    return chartHistory.value.slice(0, 5)
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

  return {
    // State
    chartHistory,
    favoriteStars,
    interpretationCount,
    // Getters
    chartCount,
    recentCharts,
    // Actions
    addChart,
    removeChart,
    clearHistory,
    toggleFavoriteStar,
    incrementInterpretation,
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
