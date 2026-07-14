<template>
  <!-- Chart result display page -->
  <view class="page-result">
    <!-- Loading State -->
    <view v-if="loading" class="state-section">
      <view class="state-spinner">
        <text class="state-spin-icon">&#9775;</text>
      </view>
      <text class="state-title">正在计算命盘...</text>
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
      <!-- Chart Board Canvas (easycom: zw-chart-board) -->
      <view class="board-section">
        <zw-chart-board
          ref="chartBoardRef"
          :chartData="chartData"
          :hideCanvas="showDetail || showAiPanel"
          @palaceClick="onPalaceClick"
        />
      </view>

      <!-- AI Interpretation Button -->
      <view class="ai-section">
        <button class="btn-ai" @tap="requestAIInterpretation">
          <text class="btn-ai-icon">&#9733;</text>
          <text class="btn-ai-text">AI 解读命盘</text>
        </button>
      </view>
    </view>

    <!-- Palace Detail Popup (easycom: zw-palace-detail) -->
    <zw-palace-detail
      :visible="showDetail"
      :palace="selectedPalace"
      :isMingGong="isSelectedMingGong"
      @close="showDetail = false"
    />

    <!-- AI Interpretation Panel (easycom: zw-ai-panel) -->
    <zw-ai-panel
      :visible="showAiPanel"
      :chartId="chartData.id"
      @close="showAiPanel = false"
    />
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useChart } from '@/core/hooks/useChart'

const { calculateChart, loadChart, getMockChartData, loading, errorMsg } = useChart()

const showDetail = ref(false)
const selectedPalace = ref({})
const chartBoardRef = ref(null)
const showAiPanel = ref(false)
let pendingFormData = null

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

const isSelectedMingGong = computed(() => {
  if (!selectedPalace.value || !chartData.mingGong) return false
  const dizhi = selectedPalace.value.dizhi || ''
  return chartData.mingGong.startsWith(dizhi)
})

onLoad((options) => {
  // Priority 1: pending chart request from chart page (new calculation)
  try {
    const stored = uni.getStorageSync('pending_chart_request')
    if (stored) {
      pendingFormData = JSON.parse(stored)
      uni.removeStorageSync('pending_chart_request')
    }
  } catch (_) { /* ignore */ }

  if (pendingFormData) {
    fetchChartFromForm()
  } else if (options && options.chartId && options.chartId !== '0' && options.chartId !== 'undefined') {
    // Priority 2: existing chart by server ID — fetch from API
    fetchChartData(options.chartId)
  } else {
    // Priority 3: no valid chart ID → show mock data
    useMockData()
  }
})

async function fetchChartFromForm() {
  const data = await calculateChart(pendingFormData)
  if (data) {
    Object.assign(chartData, data)
  }
}

async function fetchChartData(id) {
  const data = await loadChart(id)
  if (data) {
    Object.assign(chartData, data)
  }
}

function onPalaceClick(palace) {
  selectedPalace.value = palace
  showDetail.value = true
}

function requestAIInterpretation() {
  // Require a real server-saved chart ID (not locally generated mock IDs)
  if (chartData.id == null || String(chartData.id).startsWith('chart_')) {
    uni.showToast({ title: 'AI解读需要先登录并保存命盘', icon: 'none' })
    return
  }
  showAiPanel.value = true
}

function useMockData() {
  Object.assign(chartData, getMockChartData())
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
