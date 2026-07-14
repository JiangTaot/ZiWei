<template>
  <!-- Chart input page: birth form + instant navigation to result -->
  <view class="page-chart">
    <!-- Intro -->
    <view class="intro-section">
      <text class="intro-title">出生信息</text>
      <text class="intro-desc">请输入准确的出生信息，生成精准命盘</text>
    </view>

    <!-- Birth Form (easycom: zw-birth-form) -->
    <view class="form-section">
      <zw-birth-form @submit="onFormSubmit" />
    </view>
  </view>
</template>

<script setup>
import zw from '@/core'

function onFormSubmit(formData) {
  // Store form data for the result page to pick up
  uni.setStorageSync('pending_chart_request', JSON.stringify(formData))
  // Navigate immediately — loading happens on the result page
  zw.$router.go('/pages/result/index')
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
</style>
