<template>
  <!-- Personal center page -->
  <view class="page-mine">
    <!-- User Info Card -->
    <view class="user-card">
      <view class="user-avatar">
        <text class="avatar-text">{{ avatarText }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userName || '紫微星人' }}</text>
        <text class="user-desc">探索你的星命之旅</text>
      </view>
      <view class="user-action" @click="editProfile">
        <text class="action-icon">&#9881;</text>
      </view>
    </view>

    <!-- Stats Row -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-value">{{ chartCount }}</text>
        <text class="stat-label">命盘数量</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ favoriteStars.length }}</text>
        <text class="stat-label">收藏星曜</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ interpretationCount }}</text>
        <text class="stat-label">解读次数</text>
      </view>
    </view>

    <!-- My Charts Section -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">我的命盘</text>
        <text class="section-action" @click="viewAllCharts">查看全部 &#8250;</text>
      </view>

      <view v-if="chartList.length > 0" class="chart-list">
        <view
          v-for="(chart, index) in chartList"
          :key="index"
          class="chart-row"
          @click="openChart(chart)"
        >
          <view class="chart-row-icon">
            <text class="row-icon-text">&#9799;</text>
          </view>
          <view class="chart-row-info">
            <text class="chart-row-name">{{ chart.displayDate || '命盘' }}</text>
            <text class="chart-row-meta">{{ chart.genderLabel }} &#183; {{ chart.location || '未知地点' }}</text>
          </view>
          <text class="chart-row-arrow">&#8250;</text>
        </view>
      </view>

      <view v-else class="empty-chart">
        <text class="empty-text">暂无命盘，去首页开始排盘吧</text>
      </view>
    </view>

    <!-- Menu List -->
    <view class="section-card">
      <view class="menu-list">
        <view class="menu-item" @click="navigateSetting('about')">
          <text class="menu-label">关于我们</text>
          <text class="menu-arrow">&#8250;</text>
        </view>
        <view class="menu-item" @click="navigateSetting('privacy')">
          <text class="menu-label">隐私政策</text>
          <text class="menu-arrow">&#8250;</text>
        </view>
        <view class="menu-item" @click="clearCache">
          <text class="menu-label">清除缓存</text>
          <text class="menu-value">{{ cacheSize }}</text>
          <text class="menu-arrow">&#8250;</text>
        </view>
      </view>
    </view>

    <!-- Footer -->
    <view class="page-footer">
      <text class="footer-text">紫薇斗数 &#183; 传承东方星命智慧</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const userName = ref('')
const chartCount = ref(0)
const favoriteStars = ref([])
const interpretationCount = ref(0)
const chartList = ref([])
const cacheSize = ref('0KB')

const avatarText = computed(() => {
  if (userName.value) return userName.value.charAt(0)
  return '紫'
})

onShow(() => {
  loadUserData()
})

function loadUserData() {
  try {
    const history = uni.getStorageSync('chart_history')
    if (history) {
      const parsed = JSON.parse(history)
      chartList.value = parsed.slice(0, 5)
      chartCount.value = parsed.length
    }

    const interpCount = uni.getStorageSync('interpretation_count')
    if (interpCount) {
      interpretationCount.value = parseInt(interpCount) || 0
    }
  } catch (e) {
    console.error('[ZiWei] Failed to load user data:', e)
  }
}

function editProfile() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function viewAllCharts() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function openChart(chart) {
  if (chart.id) {
    uni.navigateTo({ url: `/pages/result/index?chartId=${chart.id}` })
  }
}

function navigateSetting(type) {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function clearCache() {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除本地缓存数据吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('current_chart')
        uni.showToast({ title: '已清除', icon: 'success', duration: 1500 })
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.page-mine {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: $spacing-xl;
}

// ========== User Card ==========
.user-card {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  background: linear-gradient(180deg, $bg-warm 0%, $bg-page 100%);
}

.user-avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: $radius-round;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-light 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(192, 75, 62, 0.25);
  flex-shrink: 0;
}

.avatar-text {
  font-size: $font-xxl;
  font-weight: 700;
  color: $text-inverse;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: $font-xl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 4rpx;
}

.user-desc {
  display: block;
  font-size: $font-sm;
  color: $text-secondary;
}

.user-action {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon {
  font-size: 36rpx;
  color: $text-hint;
}

// ========== Stats Row ==========
.stats-row {
  display: flex;
  align-items: center;
  margin: 0 $spacing-md $spacing-md;
  padding: $spacing-md $spacing-sm;
  background-color: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-xs 0;
}

.stat-value {
  font-size: $font-xl;
  font-weight: 700;
  color: $color-primary;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: $font-xs;
  color: $text-hint;
}

.stat-divider {
  width: 1rpx;
  height: 48rpx;
  background-color: $divider-color;
}

// ========== Section Card ==========
.section-card {
  margin: 0 $spacing-md $spacing-md;
  background-color: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg $spacing-sm;
}

.section-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
}

.section-action {
  font-size: $font-sm;
  color: $color-primary;
}

// Chart List
.chart-list {
  padding: 0 $spacing-lg $spacing-md;
}

.chart-row {
  display: flex;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $divider-color;

  &:last-child {
    border-bottom: none;
  }
}

.chart-row-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $bg-warm;
  border-radius: $radius-sm;
  margin-right: $spacing-md;
  flex-shrink: 0;
}

.row-icon-text {
  font-size: 28rpx;
  color: $color-primary;
}

.chart-row-info {
  flex: 1;
  overflow: hidden;
}

.chart-row-name {
  display: block;
  font-size: $font-base;
  font-weight: 500;
  color: $text-primary;
}

.chart-row-meta {
  display: block;
  font-size: $font-xs;
  color: $text-hint;
  margin-top: 4rpx;
}

.chart-row-arrow {
  font-size: 32rpx;
  color: $border-color;
  flex-shrink: 0;
}

.empty-chart {
  padding: $spacing-xl $spacing-lg;
  text-align: center;
}

.empty-text {
  font-size: $font-sm;
  color: $text-hint;
}

// Menu List
.menu-list {
  padding: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1rpx solid $divider-color;

  &:last-child {
    border-bottom: none;
  }
}

.menu-label {
  flex: 1;
  font-size: $font-base;
  color: $text-primary;
}

.menu-value {
  font-size: $font-sm;
  color: $text-hint;
  margin-right: $spacing-xs;
}

.menu-arrow {
  font-size: 32rpx;
  color: $border-color;
}

// Footer
.page-footer {
  display: flex;
  justify-content: center;
  padding: $spacing-xl $spacing-lg;
}

.footer-text {
  font-size: $font-xs;
  color: $border-color;
  letter-spacing: 2rpx;
}
</style>
