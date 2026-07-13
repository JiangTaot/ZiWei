<template>
  <!-- Chart input page: birth form + API submission -->
  <view class="page-chart">
    <!-- Intro -->
    <view class="intro-section">
      <text class="intro-title">出生信息</text>
      <text class="intro-desc">请输入准确的出生信息，生成精准命盘</text>
    </view>

    <!-- Birth Form -->
    <view class="form-section">
      <BirthForm @submit="onFormSubmit" />
    </view>

    <!-- Loading Overlay -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-content">
        <view class="loading-spinner">
          <text class="loading-icon-text">&#9775;</text>
        </view>
        <text class="loading-text">正在排盘中...</text>
        <text class="loading-hint">紫微斗数 &#183; 洞悉天命</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import BirthForm from '@/components/BirthForm.vue'
import { calculate } from '@/api/chart.js'

const loading = ref(false)

async function onFormSubmit(formData) {
  loading.value = true

  try {
    const res = await calculate(formData)
    const chartData = res.data || res
    if (chartData) {
      uni.setStorageSync('current_chart', JSON.stringify(chartData))
      saveToHistory(chartData)
      uni.navigateTo({
        url: `/pages/result/index?chartId=${chartData.id || 0}`,
      })
    } else {
      uni.showToast({ title: '排盘失败，请重试', icon: 'none' })
    }
  } catch (err) {
    console.error('[ZiWei] Chart calculation failed:', err)
    uni.showToast({ title: err.message || '网络错误，请稍后重试', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

function saveToHistory(chartData) {
  try {
    const historyStr = uni.getStorageSync('chart_history') || '[]'
    const history = JSON.parse(historyStr)
    const entry = {
      id: chartData.id,
      displayDate: `${chartData.solarYear}-${String(chartData.solarMonth).padStart(2, '0')}-${String(chartData.solarDay).padStart(2, '0')}`,
      displayTime: `${chartData.yearPillar || ''}${chartData.monthPillar || ''}${chartData.dayPillar || ''}${chartData.hourPillar || ''}`,
      genderLabel: chartData.gender === 1 ? '男' : '女',
      location: chartData.birthPlace || '',
      createTime: chartData.createTime || new Date().toISOString(),
    }
    history.unshift(entry)
    if (history.length > 50) history.pop()
    uni.setStorageSync('chart_history', JSON.stringify(history))
  } catch (e) {
    console.error('[ZiWei] Failed to save history:', e)
  }
}
</script>

<style lang="scss" scoped>
.page-chart {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: $spacing-xl;
}

// Intro
.intro-section {
  padding: $spacing-lg $spacing-lg $spacing-md;
}

.intro-title {
  display: block;
  font-size: $font-xxl;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.intro-desc {
  display: block;
  font-size: $font-sm;
  color: $text-secondary;
}

// Form Section
.form-section {
  margin: 0 $spacing-md;
}

// Loading Overlay
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(245, 240, 232, 0.94);
  z-index: $z-index-overlay;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-spinner {
  width: 140rpx;
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-md;
}

.loading-icon-text {
  font-size: 72rpx;
  color: $color-primary;
  animation: chartSpin 2s linear infinite;
}

@keyframes chartSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: $font-lg;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.loading-hint {
  font-size: $font-sm;
  color: $text-hint;
  letter-spacing: 2rpx;
}
</style>
