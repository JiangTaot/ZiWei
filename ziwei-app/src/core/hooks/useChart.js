/**
 * Chart composable — encapsulates chart calculation and loading logic
 * Extracted from pages/chart/index.vue and pages/result/index.vue
 */
import { ref, reactive } from 'vue'
import $api from '@/core/api'
import $store from '@/core/store'

/**
 * @returns {Object} Chart calculation and loading methods
 */
export function useChart() {
  const loading = ref(false)
  const errorMsg = ref('')

  /**
   * Calculate a chart from birth form data
   * @param {Object} formData - Birth info from BirthForm
   * @returns {Promise<Object|null>} Chart data or null on failure
   */
  async function calculateChart(formData) {
    loading.value = true
    errorMsg.value = ''

    try {
      const res = await $api.chart.calculate(formData)
      const chartData = res.data || res
      if (chartData) {
        // Save to local store history
        const chartStore = $store('chart')
        if (chartStore) {
          chartStore.addChart(chartData)

          // If logged in, refresh server chart list (chart was saved with userId)
          const userStore = $store('user')
          if (userStore && userStore.isLoggedIn) {
            chartStore.loadServerCharts()
          }
        }
        // Save current chart to storage for quick access on result page
        uni.setStorageSync('current_chart', JSON.stringify(chartData))
        return chartData
      } else {
        errorMsg.value = '排盘失败，请重试'
        return null
      }
    } catch (err) {
      console.error('[ZiWei] Chart calculation failed:', err)
      errorMsg.value = err.message || '网络错误，请稍后重试'
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Load a chart by ID (tries storage cache first for the matching ID, then API)
   * @param {string|number} chartId
   * @returns {Promise<Object|null>}
   */
  async function loadChart(chartId) {
    loading.value = true
    errorMsg.value = ''

    // Try storage cache first (only if the stored chart matches the requested ID)
    try {
      const stored = uni.getStorageSync('current_chart')
      if (stored) {
        const data = JSON.parse(stored)
        if (data && data.palaces && String(data.id) === String(chartId)) {
          loading.value = false
          return data
        }
      }
    } catch (_) { /* ignore */ }

    // Fetch from API
    try {
      const res = await $api.chart.getChartById(chartId)
      const data = res.data || res
      if (!data || !data.palaces) {
        throw new Error('返回数据格式异常')
      }
      uni.setStorageSync('current_chart', JSON.stringify(data))
      return data
    } catch (err) {
      console.error('[ZiWei] Failed to load chart:', err)
      errorMsg.value = '后端服务暂未启动，可查看模拟数据'
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Get mock chart data for preview/demo
   * @returns {Object}
   */
  function getMockChartData() {
    // Reuse the same mock data from result page
    return {
      id: 0,
      solarYear: 1990, solarMonth: 5, solarDay: 15, solarHour: 5,
      gender: 1, birthPlace: '北京',
      lunarYear: 1990, lunarMonth: 4, lunarDay: 21, isLeapMonth: false,
      yearPillar: '庚午', monthPillar: '辛巳', dayPillar: '庚辰', hourPillar: '壬午',
      mingGong: '寅宫', shenGong: '子宫',
      wuxingJu: '金四局',
      natalSihua: {
        huaLu: { starCode: 'taiyang', starName: '太阳', brightness: '庙', sihuaType: '禄' },
        huaQuan: { starCode: 'wuqu', starName: '武曲', brightness: '旺', sihuaType: '权' },
        huaKe: { starCode: 'taiyin', starName: '太阴', brightness: '利', sihuaType: '科' },
        huaJi: { starCode: 'tiantong', starName: '天同', brightness: '平', sihuaType: '忌' },
      },
      palaces: [
        { palaceType: 1, palaceName: '命宫', dizhi: '寅', tianGan: '丙', isShenGong: false, daXianLabel: '3-12岁',
          majorStars: [{ starCode: 'ziwei', starName: '紫微', brightness: '庙', sihuaType: '' }, { starCode: 'tianfu', starName: '天府', brightness: '庙', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'zuofu', starName: '左辅', brightness: '得', sihuaType: '' }, { starCode: 'youbi', starName: '右弼', brightness: '得', sihuaType: '' }],
          minorStars: [{ starCode: 'tiankui', starName: '天魁', brightness: '', sihuaType: '' }, { starCode: 'tianyue', starName: '天钺', brightness: '', sihuaType: '' }] },
        { palaceType: 2, palaceName: '兄弟宫', dizhi: '卯', tianGan: '丁', isShenGong: false, daXianLabel: '13-22岁',
          majorStars: [{ starCode: 'tianji', starName: '天机', brightness: '利', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'wenchang', starName: '文昌', brightness: '得', sihuaType: '' }],
          minorStars: [{ starCode: 'tianma', starName: '天马', brightness: '', sihuaType: '' }] },
        { palaceType: 3, palaceName: '夫妻宫', dizhi: '辰', tianGan: '戊', isShenGong: false, daXianLabel: '23-32岁',
          majorStars: [{ starCode: 'taiyang', starName: '太阳', brightness: '旺', sihuaType: '禄' }, { starCode: 'jumen', starName: '巨门', brightness: '平', sihuaType: '' }],
          auxiliaryStars: [], minorStars: [{ starCode: 'tianxi', starName: '天喜', brightness: '', sihuaType: '' }] },
        { palaceType: 4, palaceName: '子女宫', dizhi: '巳', tianGan: '己', isShenGong: false, daXianLabel: '33-42岁',
          majorStars: [{ starCode: 'wuqu', starName: '武曲', brightness: '旺', sihuaType: '权' }, { starCode: 'tianxiang', starName: '天相', brightness: '得', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'wenqu', starName: '文曲', brightness: '平', sihuaType: '' }], minorStars: [] },
        { palaceType: 5, palaceName: '财帛宫', dizhi: '午', tianGan: '庚', isShenGong: false, daXianLabel: '43-52岁',
          majorStars: [{ starCode: 'tiantong', starName: '天同', brightness: '平', sihuaType: '忌' }, { starCode: 'tianliang', starName: '天梁', brightness: '旺', sihuaType: '' }],
          auxiliaryStars: [], minorStars: [{ starCode: 'lucun', starName: '禄存', brightness: '', sihuaType: '' }] },
        { palaceType: 6, palaceName: '疾厄宫', dizhi: '未', tianGan: '辛', isShenGong: false, daXianLabel: '53-62岁',
          majorStars: [{ starCode: 'qisha', starName: '七杀', brightness: '陷', sihuaType: '' }],
          auxiliaryStars: [], minorStars: [{ starCode: 'qingyang', starName: '擎羊', brightness: '陷', sihuaType: '' }] },
        { palaceType: 7, palaceName: '迁移宫', dizhi: '申', tianGan: '壬', isShenGong: false, daXianLabel: '63-72岁',
          majorStars: [{ starCode: 'lianzhen', starName: '廉贞', brightness: '利', sihuaType: '' }, { starCode: 'pojun', starName: '破军', brightness: '得', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'tuoluo', starName: '陀罗', brightness: '陷', sihuaType: '' }],
          minorStars: [{ starCode: 'tianxing', starName: '天刑', brightness: '', sihuaType: '' }] },
        { palaceType: 8, palaceName: '交友宫', dizhi: '酉', tianGan: '癸', isShenGong: false, daXianLabel: '73-82岁',
          majorStars: [], auxiliaryStars: [{ starCode: 'hongluan', starName: '红鸾', brightness: '', sihuaType: '' }],
          minorStars: [{ starCode: 'tianyao', starName: '天姚', brightness: '', sihuaType: '' }] },
        { palaceType: 9, palaceName: '官禄宫', dizhi: '戌', tianGan: '甲', isShenGong: false, daXianLabel: '83-92岁',
          majorStars: [{ starCode: 'tanlang', starName: '贪狼', brightness: '旺', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'huoxing', starName: '火星', brightness: '陷', sihuaType: '' }, { starCode: 'lingxing', starName: '铃星', brightness: '陷', sihuaType: '' }], minorStars: [] },
        { palaceType: 10, palaceName: '田宅宫', dizhi: '亥', tianGan: '乙', isShenGong: false, daXianLabel: '93-102岁',
          majorStars: [{ starCode: 'taiyin', starName: '太阴', brightness: '庙', sihuaType: '科' }],
          auxiliaryStars: [], minorStars: [{ starCode: 'dikong', starName: '地空', brightness: '', sihuaType: '' }, { starCode: 'dijie', starName: '地劫', brightness: '', sihuaType: '' }] },
        { palaceType: 11, palaceName: '福德宫', dizhi: '子', tianGan: '丙', isShenGong: true, daXianLabel: '103-112岁',
          majorStars: [{ starCode: 'tianfu', starName: '天府', brightness: '得', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'santai', starName: '三台', brightness: '', sihuaType: '' }, { starCode: 'bazuo', starName: '八座', brightness: '', sihuaType: '' }], minorStars: [] },
        { palaceType: 12, palaceName: '父母宫', dizhi: '丑', tianGan: '丁', isShenGong: false, daXianLabel: '113-122岁',
          majorStars: [{ starCode: 'tianliang', starName: '天梁', brightness: '庙', sihuaType: '' }],
          auxiliaryStars: [{ starCode: 'tiangui', starName: '天贵', brightness: '', sihuaType: '' }],
          minorStars: [{ starCode: 'jieshen', starName: '解神', brightness: '', sihuaType: '' }] },
      ],
      daxians: [
        { ageStart: 3, ageEnd: 12, palaceName: '命宫', direction: '顺行' },
        { ageStart: 13, ageEnd: 22, palaceName: '兄弟宫', direction: '顺行' },
        { ageStart: 23, ageEnd: 32, palaceName: '夫妻宫', direction: '顺行' },
        { ageStart: 33, ageEnd: 42, palaceName: '子女宫', direction: '顺行' },
        { ageStart: 43, ageEnd: 52, palaceName: '财帛宫', direction: '顺行' },
        { ageStart: 53, ageEnd: 62, palaceName: '疾厄宫', direction: '顺行' },
        { ageStart: 63, ageEnd: 72, palaceName: '迁移宫', direction: '顺行' },
        { ageStart: 73, ageEnd: 82, palaceName: '交友宫', direction: '顺行' },
        { ageStart: 83, ageEnd: 92, palaceName: '官禄宫', direction: '顺行' },
        { ageStart: 93, ageEnd: 102, palaceName: '田宅宫', direction: '顺行' },
        { ageStart: 103, ageEnd: 112, palaceName: '福德宫', direction: '顺行' },
        { ageStart: 113, ageEnd: 122, palaceName: '父母宫', direction: '顺行' },
      ],
      patterns: ['紫府同宫', '日月并明'],
      patternDetails: [
        { name: '紫府同宫', level: '上格', description: '紫微与天府同度命宫，主一生富贵，有领导才能，格局高者可在政商领域有极大作为。' },
        { name: '日月并明', level: '中格', description: '太阳、太阴在庙旺之地对拱，主光明磊落，事业有成，家庭和睦。' },
      ],
      createTime: new Date().toISOString(),
    }
  }

  return {
    loading,
    errorMsg,
    calculateChart,
    loadChart,
    getMockChartData,
  }
}
