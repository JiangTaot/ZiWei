<template>
  <!-- Chart result display page -->
  <view class="page-result">
    <!-- Loading State -->
    <view v-if="loading" class="state-section">
      <view class="state-spinner">
        <text class="state-spin-icon">&#9775;</text>
      </view>
      <text class="state-title">正在加载命盘...</text>
      <text class="state-hint">紫微斗数 &#183; 洞悉天命</text>
    </view>

    <!-- Error State -->
    <view v-else-if="errorMsg" class="state-section">
      <text class="state-error-icon">&#10071;</text>
      <text class="state-title">加载失败</text>
      <text class="state-desc">{{ errorMsg }}</text>
      <button class="btn-retry" @tap="fetchChartData">重新加载</button>
      <view class="mock-section">
        <text class="mock-hint">或使用模拟数据预览</text>
        <button class="btn-mock" @tap="useMockData">预览模拟命盘</button>
      </view>
    </view>

    <!-- Chart Content -->
    <view v-else class="result-content">
      <!-- Basic Info Bar -->
      <view class="info-bar">
        <view class="info-bazi">
          <text class="bazi-pillar" v-if="chartData.yearPillar">{{ chartData.yearPillar }}</text>
          <text class="bazi-pillar" v-if="chartData.monthPillar">{{ chartData.monthPillar }}</text>
          <text class="bazi-pillar" v-if="chartData.dayPillar">{{ chartData.dayPillar }}</text>
          <text class="bazi-pillar" v-if="chartData.hourPillar">{{ chartData.hourPillar }}</text>
        </view>
        <view class="info-meta">
          <text class="meta-text" v-if="chartData.wuxingJu">{{ chartData.wuxingJu }}</text>
          <text class="meta-text" v-if="chartData.mingGong">命宫{{ chartData.mingGong }}</text>
          <text class="meta-text" v-if="chartData.shenGong">身宫{{ chartData.shenGong }}</text>
        </view>
      </view>

      <!-- Chart Board Canvas -->
      <view class="board-section">
        <ChartBoard
          ref="chartBoardRef"
          :chartData="chartData"
          :activeTab="activeTab"
          @palaceClick="onPalaceClick"
        />
      </view>

      <!-- Tab Navigation -->
      <view class="tab-wrap">
        <view class="tab-bar">
          <view
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ 'tab-active': activeTab === tab.key }"
            @tap="activeTab = tab.key"
          >
            <text class="tab-text">{{ tab.label }}</text>
            <view v-if="activeTab === tab.key" class="tab-indicator"></view>
          </view>
        </view>
      </view>

      <!-- Patterns (格局) Section -->
      <view v-if="chartData.patterns && chartData.patterns.length > 0" class="patterns-section">
        <view class="section-header">
          <text class="section-title">命盘格局</text>
        </view>
        <scroll-view class="patterns-scroll" scroll-x :show-scrollbar="false">
          <view class="pattern-list">
            <view
              v-for="(pattern, idx) in patternDetails"
              :key="idx"
              class="pattern-card"
            >
              <view class="pattern-header">
                <text class="pattern-name">{{ pattern.name }}</text>
                <text v-if="pattern.level" class="pattern-level" :class="getPatternClass(pattern.level)">
                  {{ pattern.level }}
                </text>
              </view>
              <text v-if="pattern.description" class="pattern-desc">{{ pattern.description }}</text>
            </view>
            <view
              v-if="patternDetails.length === 0"
              v-for="(name, idx) in chartData.patterns"
              :key="'p' + idx"
              class="pattern-card pattern-card-simple"
            >
              <text class="pattern-name-simple">{{ name }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- AI Interpretation Button -->
      <view class="ai-section">
        <button class="btn-ai" @tap="requestAIInterpretation">
          <text class="btn-ai-icon">&#9733;</text>
          <text class="btn-ai-text">AI 解读命盘</text>
        </button>
      </view>
    </view>

    <!-- Palace Detail Popup -->
    <PalaceDetail
      :visible="showDetail"
      :palace="selectedPalace"
      :isMingGong="isSelectedMingGong"
      @close="showDetail = false"
    />

    <!-- AI Interpretation Panel -->
    <AiPanel
      :visible="showAiPanel"
      :chartId="chartData.id"
      @close="showAiPanel = false"
    />
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ChartBoard from '@/components/ChartBoard.vue'
import PalaceDetail from '@/components/PalaceDetail.vue'
import AiPanel from '@/components/AiPanel.vue'
import { getChartById } from '@/api/chart.js'

const loading = ref(true)
const errorMsg = ref('')
const activeTab = ref('natal')
const showDetail = ref(false)
const selectedPalace = ref({})
const chartBoardRef = ref(null)
const chartId = ref(null)
const showAiPanel = ref(false)

const chartData = reactive({
  id: null,
  solarYear: 0, solarMonth: 0, solarDay: 0, solarHour: 0,
  gender: 1, birthPlace: '',
  lunarYear: 0, lunarMonth: 0, lunarDay: 0, isLeapMonth: false,
  yearPillar: '', monthPillar: '', dayPillar: '', hourPillar: '',
  mingGong: '', shenGong: '', wuxingJu: '',
  palaces: [],
  natalSihua: null,
  daxians: [],
  patterns: [],
  patternDetails: [],
})

const tabs = [
  { key: 'natal', label: '本命盘' },
  { key: 'daxian', label: '大限' },
  { key: 'liunian', label: '流年' },
]

const isSelectedMingGong = computed(() => {
  if (!selectedPalace.value || !chartData.mingGong) return false
  const dizhi = selectedPalace.value.dizhi || ''
  return chartData.mingGong.startsWith(dizhi)
})

const patternDetails = computed(() => {
  const details = chartData.patternDetails || []
  if (details.length > 0) return details
  return (chartData.patterns || []).map(name => ({ name, level: '', description: '' }))
})

onLoad((options) => {
  if (options && options.chartId) {
    chartId.value = options.chartId
    fetchChartData()
  } else {
    useMockData()
  }
})

async function fetchChartData() {
  try {
    const stored = uni.getStorageSync('current_chart')
    if (stored) {
      const data = JSON.parse(stored)
      if (data && data.palaces) {
        Object.assign(chartData, data)
        loading.value = false
        return
      }
    }
  } catch (_) { /* ignore */ }

  if (!chartId.value) {
    useMockData()
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const res = await getChartById(chartId.value)
    const data = res.data || res
    if (!data || !data.palaces) {
      throw new Error('返回数据格式异常')
    }
    Object.assign(chartData, data)
    uni.setStorageSync('current_chart', JSON.stringify(data))
    loading.value = false
  } catch (err) {
    console.error('[ZiWei] Failed to load chart:', err)
    errorMsg.value = '后端服务暂未启动，可查看模拟数据'
    loading.value = false
  }
}

function onPalaceClick(palace) {
  selectedPalace.value = palace
  showDetail.value = true
}

function requestAIInterpretation() {
  if (chartData.id == null) {
    uni.showToast({ title: 'AI解读需要先保存命盘', icon: 'none' })
    return
  }
  showAiPanel.value = true
}

function getPatternClass(level) {
  if (!level) return ''
  if (level.includes('上') || level.includes('高')) return 'level-high'
  if (level.includes('中')) return 'level-mid'
  return 'level-normal'
}

function useMockData() {
  loading.value = false
  errorMsg.value = ''
  Object.assign(chartData, getMockChartData())
}

function getMockChartData() {
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
</script>

<style lang="scss" scoped>
.page-result {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: $spacing-xl;
}

// ========== Loading & Error States ==========
.state-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: $spacing-xl;
}

.state-spinner {
  width: 140rpx;
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-md;
}

.state-spin-icon {
  font-size: 72rpx;
  color: $color-primary;
  animation: resultSpin 2s linear infinite;
}

@keyframes resultSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.state-title {
  font-size: $font-lg;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.state-hint {
  font-size: $font-sm;
  color: $text-hint;
  letter-spacing: 2rpx;
}

.state-error-icon {
  font-size: 64rpx;
  margin-bottom: $spacing-md;
  opacity: 0.5;
}

.state-desc {
  font-size: $font-sm;
  color: $text-secondary;
  text-align: center;
  line-height: 1.6;
  margin-bottom: $spacing-lg;
  padding: 0 $spacing-lg;
}

.btn-retry {
  width: 360rpx;
  height: 88rpx;
  background: linear-gradient(135deg, $color-primary, $color-primary-light);
  border: none;
  border-radius: $radius-lg;
  color: $text-inverse;
  font-size: $font-base;
  font-weight: 500;
  line-height: 88rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(192, 75, 62, 0.25);

  &::after { border: none; }
}

.mock-section {
  margin-top: $spacing-xl;
  text-align: center;
}

.mock-hint {
  display: block;
  font-size: $font-xs;
  color: $text-hint;
  margin-bottom: $spacing-sm;
}

.btn-mock {
  width: 360rpx;
  height: 72rpx;
  background: transparent;
  border: 2rpx solid $color-gold;
  border-radius: $radius-lg;
  color: $color-gold;
  font-size: $font-sm;
  line-height: 72rpx;
  text-align: center;

  &::after { border: none; }
}

// ========== Content ==========
.result-content {
  padding: 0;
}

// Info Bar
.info-bar {
  padding: $spacing-sm $spacing-md;
  background-color: $bg-warm;
  border-bottom: 1rpx solid $divider-color;
}

.info-bazi {
  display: flex;
  justify-content: center;
  gap: $spacing-sm;
  margin-bottom: 6rpx;
}

.bazi-pillar {
  font-size: $font-sm;
  color: $color-primary;
  padding: 4rpx 16rpx;
  background-color: $bg-card;
  border-radius: $radius-sm;
  font-weight: 500;
  border: 1rpx solid rgba($color-primary, 0.12);
}

.info-meta {
  display: flex;
  justify-content: center;
  gap: $spacing-lg;
}

.meta-text {
  font-size: $font-xs;
  color: $text-secondary;
}

// Board Section
.board-section {
  padding: $spacing-md 0;
}

// Tab Navigation
.tab-wrap {
  padding: 0 $spacing-md;
}

.tab-bar {
  display: flex;
  background-color: $bg-card;
  border-radius: $radius-md $radius-md 0 0;
  border-bottom: 1rpx solid $divider-color;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-sm 0;
  position: relative;
}

.tab-text {
  font-size: $font-base;
  color: $text-secondary;
  transition: color 0.2s ease;
}

.tab-active .tab-text {
  color: $color-primary;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 4rpx;
  background-color: $color-primary;
  border-radius: 2rpx;
}

// Patterns Section
.patterns-section {
  margin: 0 $spacing-md;
  background-color: $bg-card;
  border-radius: 0 0 $radius-md $radius-md;
  overflow: hidden;
  padding-bottom: $spacing-md;
}

.section-header {
  padding: $spacing-sm $spacing-md;
}

.section-title {
  font-size: $font-sm;
  font-weight: 600;
  color: $text-primary;
}

.patterns-scroll {
  white-space: nowrap;
}

.pattern-list {
  display: flex;
  gap: $spacing-sm;
  padding: 0 $spacing-md;
}

.pattern-card {
  flex: 0 0 320rpx;
  background-color: $bg-warm;
  border-radius: $radius-md;
  padding: $spacing-md;
  display: inline-block;
  white-space: normal;
}

.pattern-card-simple {
  flex: 0 0 auto;
  padding: $spacing-sm $spacing-lg;
}

.pattern-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xs;
}

.pattern-name {
  font-size: $font-sm;
  font-weight: 600;
  color: $text-primary;
}

.pattern-name-simple {
  font-size: $font-sm;
  font-weight: 600;
  color: $color-primary;
}

.pattern-level {
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: $radius-sm;
  font-weight: 500;
}

.level-high {
  color: $color-primary;
  background-color: rgba($color-primary, 0.08);
}

.level-mid {
  color: $color-gold;
  background-color: rgba($color-gold, 0.08);
}

.level-normal {
  color: $text-hint;
  background: $divider-color;
}

.pattern-desc {
  font-size: $font-xs;
  color: $text-secondary;
  line-height: 1.7;
  white-space: normal;
  display: block;
}

// AI Section
.ai-section {
  display: flex;
  justify-content: center;
  padding: $spacing-xl $spacing-lg;
}

.btn-ai {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  width: 520rpx;
  height: 96rpx;
  background: linear-gradient(135deg, $color-gold 0%, $color-gold-light 100%);
  border: none;
  border-radius: $radius-xl;
  box-shadow: 0 6rpx 24rpx rgba(184, 151, 110, 0.3);

  &::after { border: none; }

  &:active { opacity: 0.85; }
}

.btn-ai-icon {
  font-size: 36rpx;
  color: $text-inverse;
}

.btn-ai-text {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-inverse;
  letter-spacing: 4rpx;
}
</style>
