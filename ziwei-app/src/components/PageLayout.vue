<template>
  <view class="page-layout">
    <!-- Status bar placeholder -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <!-- Navigation bar -->
    <view v-if="title" class="nav-bar">
      <view class="nav-back" v-if="showBack" @tap="goBack">
        <text class="nav-back-icon">&#8249;</text>
      </view>
      <text class="nav-title">{{ title }}</text>
      <view class="nav-placeholder"></view>
    </view>
    <!-- Page body -->
    <view class="page-body">
      <slot />
    </view>
    <!-- Safe area bottom -->
    <view class="safe-bottom"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
})

const statusBarHeight = ref(0)

// Get system info for status bar height
try {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
} catch (_) {
  statusBarHeight.value = 20
}

function goBack() {
  uni.navigateBack({ delta: 1 })
}
</script>

<style lang="scss" scoped>
.page-layout {
  min-height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

.status-bar {
  width: 100%;
  background-color: $bg-page;
  flex-shrink: 0;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  padding: 0 $spacing-md;
  background-color: $bg-page;
  position: relative;
  flex-shrink: 0;
}

.nav-back {
  position: absolute;
  left: $spacing-md;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
  line-height: 1;
}

.nav-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
  letter-spacing: 2rpx;
}

.nav-placeholder {
  position: absolute;
  right: $spacing-md;
  width: 64rpx;
}

.page-body {
  flex: 1;
  overflow-y: auto;
}

.safe-bottom {
  height: env(safe-area-inset-bottom, 0);
  flex-shrink: 0;
}
</style>
