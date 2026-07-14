<template>
  <!-- Palace detail bottom-sheet popup -->
  <view v-if="visible" class="palace-detail-overlay" @tap="close">
    <view class="palace-detail-popup" @tap.stop>
      <!-- Drag handle -->
      <view class="popup-handle">
        <view class="handle-bar"></view>
      </view>

      <!-- Header -->
      <view class="popup-header">
        <view class="popup-title-row">
          <text class="popup-title">{{ palace.palaceName || palace.name || '命宫' }}</text>
          <text v-if="isMingGong" class="ming-tag">命</text>
          <text v-if="palace.isShenGong" class="shen-tag">身</text>
        </view>
        <view class="popup-close-btn" @tap="close">
          <text class="close-icon">&#10005;</text>
        </view>
      </view>

      <!-- Stem-Branch & DaXian Info -->
      <view class="popup-meta">
        <view class="meta-item" v-if="palace.tianGan && palace.dizhi">
          <text class="meta-label">干支</text>
          <text class="meta-value ganzhi-value">{{ palace.tianGan }}{{ palace.dizhi }}</text>
        </view>
        <view class="meta-item" v-if="palace.daXianLabel">
          <text class="meta-label">大限</text>
          <text class="meta-value">{{ palace.daXianLabel }}</text>
        </view>
        <view class="meta-item" v-if="palace.isShenGong">
          <text class="meta-label">身宫</text>
          <text class="meta-value shen-value">是</text>
        </view>
      </view>

      <!-- Scrollable Body -->
      <scroll-view class="popup-body" scroll-y>
        <!-- Major Stars -->
        <view class="detail-section" v-if="majorStars.length > 0">
          <text class="section-title">主星</text>
          <view class="star-list">
            <view
              v-for="(star, idx) in majorStars"
              :key="idx"
              class="star-tag"
              :class="getStarClass(star.brightness)"
            >
              <text class="star-name">{{ star.starName }}</text>
              <text v-if="star.brightness" class="star-brightness">{{ star.brightness }}</text>
              <text v-if="star.sihuaType" class="star-sihua" :class="getSihuaClass(star.sihuaType)">
                {{ star.sihuaType }}
              </text>
            </view>
          </view>
          <text v-if="majorStars.length === 0" class="empty-hint">空宫（无主星）</text>
        </view>

        <!-- Auxiliary Stars -->
        <view class="detail-section" v-if="auxiliaryStars.length > 0">
          <text class="section-title">辅星</text>
          <view class="star-list">
            <view v-for="(star, idx) in auxiliaryStars" :key="idx" class="star-tag star-tag-aux">
              <text class="star-name">{{ star.starName }}</text>
              <text v-if="star.brightness" class="star-brightness brightness-aux">{{ star.brightness }}</text>
              <text v-if="star.sihuaType" class="star-sihua" :class="getSihuaClass(star.sihuaType)">
                {{ star.sihuaType }}
              </text>
            </view>
          </view>
        </view>

        <!-- Minor Stars -->
        <view class="detail-section" v-if="minorStars.length > 0">
          <text class="section-title">杂曜</text>
          <view class="star-list">
            <view v-for="(star, idx) in minorStars" :key="idx" class="star-tag star-tag-minor">
              <text class="star-name">{{ star.starName }}</text>
            </view>
          </view>
        </view>

        <!-- Palace Meaning -->
        <view class="detail-section">
          <text class="section-title">宫位含义</text>
          <text class="section-desc">{{ palaceMeaning }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { TWELVE_PALACES } from '@/core/helper/const'

const props = defineProps({
  visible: { type: Boolean, default: false },
  palace: {
    type: Object,
    default: () => ({
      palaceType: 0,
      palaceName: '',
      dizhi: '',
      tianGan: '',
      isShenGong: false,
      daXianLabel: '',
      majorStars: [],
      auxiliaryStars: [],
      minorStars: [],
    }),
  },
  isMingGong: { type: Boolean, default: false },
})

const emit = defineEmits(['close'])

function close() {
  emit('close')
}

// Normalize star data (handle both backend format and alternative formats)
const majorStars = computed(() => {
  const stars = props.palace.majorStars || props.palace.stars || []
  return stars.map(s => typeof s === 'string' ? { starName: s, brightness: '', sihuaType: '' } : s)
})

const auxiliaryStars = computed(() => {
  return (props.palace.auxiliaryStars || []).map(s =>
    typeof s === 'string' ? { starName: s, brightness: '', sihuaType: '' } : s
  )
})

const minorStars = computed(() => {
  return (props.palace.minorStars || []).map(s =>
    typeof s === 'string' ? { starName: s, brightness: '', sihuaType: '' } : s
  )
})

const palaceMeaning = computed(() => {
  const name = (props.palace.palaceName || props.palace.name || '').replace(/\s+/g, '')
  const palace = TWELVE_PALACES.find((p) => p.name === name)
  return palace?.meaning || '暂无详细说明'
})

function getStarClass(brightness) {
  if (!brightness) return ''
  if (brightness === '庙' || brightness === '旺') return 'star-bright-high'
  if (brightness === '得' || brightness === '利') return 'star-bright-mid'
  if (brightness === '平') return 'star-bright-avg'
  return 'star-bright-low'
}

function getSihuaClass(sihua) {
  const map = {
    '禄': 'sihua-lu',
    '权': 'sihua-quan',
    '科': 'sihua-ke',
    '忌': 'sihua-ji',
  }
  return map[sihua] || ''
}
</script>

<style lang="scss" scoped>
.palace-detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(44, 44, 44, 0.45);
  z-index: $z-index-overlay;
  display: flex;
  align-items: flex-end;
  animation: overlayFadeIn 0.25s ease-out;
}

@keyframes overlayFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.palace-detail-popup {
  width: 100%;
  max-height: 70vh;
  background-color: $color-bg-card;
  border-radius: $radius-lg $radius-lg 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

// Drag Handle
.popup-handle {
  display: flex;
  justify-content: center;
  padding: $spacing-sm 0;
}

.handle-bar {
  width: 64rpx;
  height: 6rpx;
  background-color: $color-border;
  border-radius: 3rpx;
}

// Header
.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg $spacing-sm;
  flex-shrink: 0;
}

.popup-title-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.popup-title {
  font-size: $font-size-lg;
  font-weight: 700;
  color: $color-text-primary;
}

.ming-tag {
  font-size: $font-size-xs;
  color: $color-accent;
  background-color: rgba($color-accent, 0.1);
  padding: 2rpx 8rpx;
  border-radius: $radius-sm;
}

.shen-tag {
  font-size: $font-size-xs;
  color: #4A6FA5;
  background-color: rgba(74, 111, 165, 0.1);
  padding: 2rpx 8rpx;
  border-radius: $radius-sm;
}

.popup-close-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: $font-size-md;
  color: $color-text-secondary;
}

// Meta row
.popup-meta {
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-xs $spacing-lg $spacing-md;
  flex-shrink: 0;
  border-bottom: 1rpx solid $color-divider;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: $spacing-xs;
}

.meta-label {
  font-size: $font-size-xs;
  color: $color-text-hint;
}

.meta-value {
  font-size: $font-size-sm;
  color: $color-text-primary;
  font-weight: 500;
}

.ganzhi-value {
  color: $color-gold;
  font-weight: 600;
}

.shen-value {
  color: #4A6FA5;
}

// Scroll Body
.popup-body {
  padding: $spacing-md $spacing-lg;
  max-height: 45vh;
  flex: 1;
}

.detail-section {
  margin-bottom: $spacing-md;

  &:last-child {
    margin-bottom: $spacing-xl;
  }
}

.section-title {
  display: block;
  font-size: $font-size-xs;
  color: $color-text-hint;
  margin-bottom: $spacing-sm;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.section-desc {
  display: block;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: 1.8;
}

.empty-hint {
  font-size: $font-size-sm;
  color: $color-text-hint;
  font-style: italic;
}

// Star List
.star-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.star-tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx $spacing-sm;
  background-color: rgba($color-accent, 0.08);
  border-radius: $radius-sm;
  border: 1rpx solid rgba($color-accent, 0.15);
}

.star-tag-aux {
  background-color: rgba($color-gold, 0.08);
  border-color: rgba($color-gold, 0.15);
}

.star-tag-minor {
  background-color: $color-bg-secondary;
  border-color: $color-divider;
}

.star-name {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $color-text-primary;
}

.star-brightness {
  font-size: $font-size-xs;
  padding: 0 4rpx;
  border-radius: 2rpx;
}

.star-bright-high .star-brightness {
  color: $color-accent;
  background-color: rgba($color-accent, 0.1);
}

.star-bright-mid .star-brightness {
  color: #5B8C5A;
  background-color: rgba(91, 140, 90, 0.1);
}

.star-bright-avg .star-brightness {
  color: #999999;
}

.star-bright-low .star-brightness {
  color: #4A6FA5;
  background-color: rgba(74, 111, 165, 0.1);
}

.star-bright-high {
  border-color: rgba($color-accent, 0.3);
}

.star-bright-mid {
  border-color: rgba(91, 140, 90, 0.3);
}

.brightness-aux {
  color: $color-text-hint;
}

// Sihua badges
.star-sihua {
  font-size: 18rpx;
  font-weight: 700;
  padding: 0 4rpx;
  border-radius: 2rpx;
}

.sihua-lu {
  color: #22C55E;
  background-color: rgba(34, 197, 94, 0.1);
}

.sihua-quan {
  color: #3B82F6;
  background-color: rgba(59, 130, 246, 0.1);
}

.sihua-ke {
  color: #CA8A04;
  background-color: rgba(202, 138, 4, 0.1);
}

.sihua-ji {
  color: #EF4444;
  background-color: rgba(239, 68, 68, 0.1);
}
</style>
