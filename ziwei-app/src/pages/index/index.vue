<template>
  <!-- Home page: ZiWei DouShu entry + history -->
  <view class="page-home">
    <!-- Hero Section -->
    <view class="hero-section">
      <view class="hero-decoration">
        <view class="hero-ring hero-ring-1"></view>
        <view class="hero-ring hero-ring-2"></view>
      </view>
      <view class="hero-content">
        <text class="hero-title">紫薇斗数</text>
        <text class="hero-subtitle">洞悉天命 &#183; 明心见性</text>
      </view>
    </view>

    <!-- Main Action: Start Chart -->
    <view class="action-section">
      <view class="btn-start-chart" @tap="startChart">
        <view class="btn-start-inner">
          <text class="btn-start-icon">&#9775;</text>
          <text class="btn-start-text">开始排盘</text>
        </view>
      </view>
      <text class="action-hint">输入出生信息，生成专属命盘</text>
    </view>

    <!-- WeChat Login (shown when not logged in) -->
    <view class="login-section" v-if="!userStore.isLoggedIn">
      <view class="login-divider">
        <view class="divider-line"></view>
        <text class="divider-text">登录后可保存记录</text>
        <view class="divider-line"></view>
      </view>
      <button class="btn-login" @click="handleWechatLogin">
        <text class="btn-login-text">微信一键登录</text>
      </button>
    </view>

    <!-- History Section -->
    <view class="history-section">
      <view class="section-header" v-if="historyList.length > 0">
        <text class="section-title">历史命盘</text>
        <text class="section-action" @click="clearHistory">清空</text>
      </view>

      <view v-if="historyList.length > 0" class="history-list">
        <view
          v-for="(item, index) in historyList"
          :key="index"
          class="history-card"
          @click="viewChart(item)"
        >
          <view class="history-card-icon">
            <text class="card-icon-text">&#9799;</text>
          </view>
          <view class="history-card-info">
            <text class="history-card-date">{{ item.displayDate }}</text>
            <text class="history-card-meta">{{ item.genderLabel }} &#183; {{ item.location || '未知地点' }}</text>
          </view>
          <view class="history-card-arrow">
            <text class="arrow-icon">&#8250;</text>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-else class="empty-state">
        <view class="empty-icon">
          <text class="empty-icon-text">&#9789;</text>
        </view>
        <text class="empty-title">暂无命盘记录</text>
        <text class="empty-desc">点击上方「开始排盘」，生成第一张命盘</text>
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
import zw from '@/core'
import { useAuth } from '@/core/hooks/useAuth'

const { wechatLogin } = useAuth()
const userStore = zw.$store('user')
const chartStore = zw.$store('chart')

const historyList = computed(() => {
  return chartStore ? chartStore.chartHistory : []
})

onShow(() => {
  // Data is auto-loaded via Pinia persist — no manual load needed
})

async function handleWechatLogin() {
  await wechatLogin()
}

function startChart() {
  zw.$router.switchTab('/pages/chart/index')
}

function viewChart(item) {
  if (item.id) {
    zw.$router.go('/pages/result/index', { chartId: item.id })
  }
}

function clearHistory() {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有历史命盘记录吗？',
    success: (res) => {
      if (res.confirm && chartStore) {
        chartStore.clearHistory()
        uni.showToast({ title: '已清空', icon: 'success', duration: 1500 })
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.page-home {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: $spacing-xl;
}

// ========== Hero Section ==========
.hero-section {
  position: relative;
  padding: $spacing-xl $spacing-lg $spacing-lg;
  text-align: center;
  overflow: hidden;
}

.hero-decoration {
  position: absolute;
  top: -20rpx;
  right: -30rpx;
  width: 280rpx;
  height: 280rpx;
  opacity: 0.07;
  pointer-events: none;
}

.hero-ring {
  position: absolute;
  border: 4rpx solid $color-primary;
  border-radius: 50%;
}

.hero-ring-1 {
  top: 0;
  right: 0;
  width: 200rpx;
  height: 200rpx;
}

.hero-ring-2 {
  top: 36rpx;
  right: 36rpx;
  width: 128rpx;
  height: 128rpx;
  border-color: $color-gold;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  display: block;
  font-size: $font-xxl;
  font-weight: 700;
  color: $color-primary;
  letter-spacing: 12rpx;
  margin-bottom: $spacing-sm;
}

.hero-subtitle {
  display: block;
  font-size: $font-sm;
  color: $text-hint;
  letter-spacing: 6rpx;
}

// ========== Action Section ==========
.action-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-md $spacing-lg $spacing-lg;
}

.btn-start-chart {
  width: 560rpx;
  height: 100rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-light 100%);
  border-radius: $radius-xl;
  box-shadow: 0 8rpx 32rpx rgba(192, 75, 62, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-md;
  transition: transform 0.15s ease;

  &:active {
    transform: scale(0.97);
  }
}

.btn-start-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
}

.btn-start-icon {
  font-size: 44rpx;
  color: $text-inverse;
}

.btn-start-text {
  font-size: $font-xl;
  font-weight: 600;
  color: $text-inverse;
  letter-spacing: 6rpx;
}

.action-hint {
  font-size: $font-xs;
  color: $text-hint;
}

// ========== Login Section ==========
.login-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 $spacing-lg $spacing-xl;
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

// ========== History Section ==========
.history-section {
  padding: 0 $spacing-md;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm 0;
  margin-bottom: $spacing-sm;
}

.section-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
}

.section-action {
  font-size: $font-sm;
  color: $text-hint;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.history-card {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  background-color: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  transition: transform 0.15s ease;

  &:active {
    transform: scale(0.985);
  }
}

.history-card-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $bg-warm;
  border-radius: $radius-md;
  margin-right: $spacing-md;
  flex-shrink: 0;
}

.card-icon-text {
  font-size: 32rpx;
  color: $color-primary;
}

.history-card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-card-date {
  font-size: $font-base;
  font-weight: 500;
  color: $text-primary;
}

.history-card-meta {
  font-size: $font-xs;
  color: $text-secondary;
  margin-top: 4rpx;
}

.history-card-arrow {
  flex-shrink: 0;
  padding-left: $spacing-sm;
}

.arrow-icon {
  font-size: 36rpx;
  color: $border-color;
}

// ========== Empty State ==========
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-xl $spacing-lg;
}

.empty-icon {
  width: 140rpx;
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $divider-color;
  border-radius: $radius-round;
  margin-bottom: $spacing-md;
}

.empty-icon-text {
  font-size: 56rpx;
  color: $text-hint;
}

.empty-title {
  font-size: $font-base;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
}

.empty-desc {
  font-size: $font-sm;
  color: $text-hint;
  text-align: center;
  line-height: 1.6;
}

// ========== Footer ==========
.page-footer {
  display: flex;
  justify-content: center;
  padding: $spacing-xl $spacing-lg $spacing-md;
}

.footer-text {
  font-size: $font-xs;
  color: $border-color;
  letter-spacing: 2rpx;
}
</style>
