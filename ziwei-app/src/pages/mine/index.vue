<template>
  <!-- Personal center page -->
  <view class="page-mine">
    <!-- User Info Card -->
    <view class="user-card">
      <view class="user-avatar">
        <text class="avatar-text">{{ avatarText }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userStore.displayName }}</text>
        <text class="user-desc">探索你的星命之旅</text>
      </view>
      <view class="user-action" @click="editProfile">
        <text class="action-icon">&#9881;</text>
      </view>
    </view>

    <!-- 登录引导（未登录时显示） -->
    <view class="login-section" v-if="!userStore.isLoggedIn">
      <view class="login-divider">
        <view class="divider-line"></view>
        <text class="divider-text">登录后可查看云端命盘</text>
        <view class="divider-line"></view>
      </view>
      <button class="btn-login" @click="handleWechatLogin">
        <text class="btn-login-text">微信一键登录</text>
      </button>
    </view>

    <!-- Stats Row -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-value">{{ displayChartCount }}</text>
        <text class="stat-label">命盘数量</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ chartStore.favoriteStars.length }}</text>
        <text class="stat-label">收藏星曜</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ chartStore.interpretationCount }}</text>
        <text class="stat-label">解读次数</text>
      </view>
    </view>

    <!-- 我的命盘 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">我的命盘</text>
        <text class="section-action" @click="viewAllCharts">查看全部 &#8250;</text>
      </view>

      <!-- 加载中 -->
      <view v-if="chartStore.isLoading" class="loading-row">
        <text class="loading-icon">&#9775;</text>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 已登录无数据 -->
      <view v-else-if="userStore.isLoggedIn && recentCharts.length === 0" class="empty-chart">
        <text class="empty-icon-text">&#9789;</text>
        <text class="empty-text">暂无命盘，去首页开始排盘吧</text>
      </view>

      <!-- 未登录无本地数据 -->
      <view v-else-if="!userStore.isLoggedIn && recentCharts.length === 0" class="empty-chart">
        <text class="empty-icon-text">&#9789;</text>
        <text class="empty-text">登录后查看云端保存的命盘</text>
      </view>

      <!-- 命盘列表 -->
      <view v-else class="chart-list">
        <view
          v-for="(chart, index) in recentCharts"
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
          <view class="chart-row-delete" @click.stop="confirmDelete(chart)">
            <text class="delete-icon">&#10005;</text>
          </view>
        </view>
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
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import zw from '@/core'
import { useAuth } from '@/core/hooks/useAuth'

const { wechatLogin } = useAuth()
const userStore = zw.$store('user')
const chartStore = zw.$store('chart')
const cacheSize = ref('0KB')

const avatarText = computed(() => {
  if (userStore?.displayName) return userStore.displayName.charAt(0)
  return '紫'
})

// 登录后使用合并列表，否则使用本地列表
const recentCharts = computed(() => {
  if (chartStore && chartStore.isServerLoaded) {
    return chartStore.allCharts.slice(0, 5)
  }
  return chartStore?.recentCharts || []
})

// 统计行：登录后显示服务端合并数量
const displayChartCount = computed(() => {
  if (chartStore && chartStore.isServerLoaded) {
    return chartStore.allCharts.length
  }
  return chartStore?.chartCount || 0
})

onShow(() => {
  console.log('[ZiWei] mine onShow, isLoggedIn:', userStore?.isLoggedIn, 'chartStore:', !!chartStore)
  // 已登录则刷新服务端命盘列表
  if (userStore && userStore.isLoggedIn && chartStore) {
    console.log('[ZiWei] 开始调用 loadServerCharts')
    chartStore.loadServerCharts()
  }
})

// 下拉刷新
onPullDownRefresh(async () => {
  if (userStore && userStore.isLoggedIn && chartStore) {
    await chartStore.loadServerCharts()
  }
  uni.stopPullDownRefresh()
})

async function handleWechatLogin() {
  const result = await wechatLogin()
  if (result && chartStore) {
    await chartStore.loadServerCharts()
  }
}

function editProfile() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function viewAllCharts() {
  if (!chartStore) return

  const charts = chartStore.isServerLoaded
    ? chartStore.allCharts
    : chartStore.chartHistory

  if (charts.length === 0) {
    uni.showToast({ title: '暂无命盘记录', icon: 'none' })
    return
  }

  const itemList = charts.slice(0, 20).map((c) => {
    const date = c.displayDate || `${c.solarYear || ''}-${String(c.solarMonth || '').padStart(2, '0')}-${String(c.solarDay || '').padStart(2, '0')}`
    return `${date}  ${c.genderLabel || ''}  ${c.location || ''}`
  })

  uni.showActionSheet({
    title: '我的命盘（点击查看）',
    itemList,
    success: (res) => {
      const chart = charts[res.tapIndex]
      if (chart && chart.id) {
        zw.$router.go('/pages/result/index', { chartId: chart.id })
      }
    },
  })
}

function openChart(chart) {
  if (chart.id) {
    zw.$router.go('/pages/result/index', { chartId: chart.id })
  }
}

/**
 * 长按命盘 → 确认删除
 */
function confirmDelete(chart) {
  if (!chart || !chart.id) return

  uni.showModal({
    title: '删除命盘',
    content: `确定要删除「${chart.displayDate || '该命盘'}」吗？删除后不可恢复。`,
    confirmText: '删除',
    confirmColor: '#C04B3E',
    success: async (res) => {
      if (res.confirm && chartStore) {
        const success = await chartStore.deleteServerChart(chart.id)
        if (success) {
          uni.showToast({ title: '已删除', icon: 'success', duration: 1500 })
        } else {
          uni.showToast({ title: '删除失败，请稍后重试', icon: 'none' })
        }
      }
    },
  })
}

function navigateSetting() {
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

// ========== Login Section ==========
.login-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-sm $spacing-lg $spacing-md;
}

.login-divider {
  display: flex;
  align-items: center;
  width: 100%;
  margin-bottom: $spacing-md;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background-color: $divider-color;
}

.divider-text {
  font-size: $font-xs;
  color: $text-hint;
  padding: 0 $spacing-md;
}

.btn-login {
  width: 480rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #07C160 0%, #06AD56 100%);
  border: none;
  border-radius: $radius-lg;
  box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.25);

  &::after { border: none; }

  &:active { opacity: 0.85; }
}

.btn-login-text {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-inverse;
  letter-spacing: 2rpx;
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

.chart-row-delete {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: $radius-round;
  background-color: rgba(192, 75, 62, 0.08);
  margin-left: $spacing-xs;
}

.delete-icon {
  font-size: 24rpx;
  color: $color-primary;
  font-weight: 600;
}

.empty-chart {
  padding: $spacing-xl $spacing-lg;
  text-align: center;
}

.empty-icon-text {
  display: block;
  font-size: 56rpx;
  color: $text-hint;
  margin-bottom: $spacing-sm;
  opacity: 0.4;
}

.empty-text {
  font-size: $font-sm;
  color: $text-hint;
}

// Loading
.loading-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-xl $spacing-lg;
}

.loading-icon {
  font-size: 36rpx;
  color: $color-primary;
  animation: mineSpin 2s linear infinite;
}

.loading-text {
  font-size: $font-sm;
  color: $text-hint;
}

@keyframes mineSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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
