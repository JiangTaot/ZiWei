<template>
  <!-- Star icon component: displays a single ZiWei star symbol with tooltip -->
  <view
    class="star-icon"
    :class="starClass"
    :style="starStyle"
    @click="handleClick"
  >
    <!-- Star symbol -->
    <text class="star-symbol">{{ displaySymbol }}</text>

    <!-- Star name label -->
    <text class="star-name">{{ name }}</text>

    <!-- Brightness indicator -->
    <view v-if="showBrightness && brightness" class="star-brightness">
      <view
        v-for="i in 5"
        :key="i"
        class="brightness-dot"
        :class="{ 'brightness-dot-empty': i > brightnessLevel }"
      ></view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  // Star name (e.g. 紫微, 天机, 太阳, etc.)
  name: {
    type: String,
    required: true,
  },
  // Star type determines color: 主星 (major), 辅星 (auxiliary), 吉星 (auspicious), 煞星 (inauspicious)
  type: {
    type: String,
    default: 'major',
    validator: (v) => ['major', 'auxiliary', 'auspicious', 'inauspicious'].includes(v),
  },
  // Brightness level: 庙/旺/得/利/平/不/陷
  brightness: {
    type: String,
    default: '',
  },
  // Size: small, medium, large
  size: {
    type: String,
    default: 'medium',
  },
  // Show brightness indicator
  showBrightness: {
    type: Boolean,
    default: false,
  },
  // Custom symbol override
  symbol: {
    type: String,
    default: '',
  },
})

// Emits
const emit = defineEmits(['click'])

// Star type to CSS class mapping
const starClass = computed(() => ({
  'star-major': props.type === 'major',
  'star-auxiliary': props.type === 'auxiliary',
  'star-auspicious': props.type === 'auspicious',
  'star-inauspicious': props.type === 'inauspicious',
  [`star-size-${props.size}`]: true,
}))

// Star size style
const starStyle = computed(() => {
  const sizes = {
    small: { width: '60rpx', height: '60rpx', fontSize: '24rpx' },
    medium: { width: '80rpx', height: '80rpx', fontSize: '32rpx' },
    large: { width: '100rpx', height: '100rpx', fontSize: '40rpx' },
  }
  const s = sizes[props.size] || sizes.medium
  return {
    width: s.width,
    height: s.height,
    fontSize: s.fontSize,
  }
})

// Display symbol (first character of name or custom symbol)
const displaySymbol = computed(() => {
  if (props.symbol) return props.symbol
  return props.name.charAt(0)
})

// Brightness level as a number (1-5)
const brightnessLevel = computed(() => {
  const levels = ['陷', '不', '平', '利', '得', '旺', '庙']
  const idx = levels.indexOf(props.brightness)
  return idx >= 0 ? Math.min(idx + 1, 5) : 0
})

function handleClick() {
  emit('click', { name: props.name, type: props.type, brightness: props.brightness })
}
</script>

<style lang="scss" scoped>
.star-icon {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: $radius-round;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

// Star type colors
.star-major {
  background-color: rgba($color-accent, 0.1);
  border: 2rpx solid $color-accent;
}

.star-auxiliary {
  background-color: rgba($color-gold, 0.1);
  border: 2rpx solid $color-gold;
}

.star-auspicious {
  background-color: rgba(#5B8C5A, 0.1);
  border: 2rpx solid #5B8C5A;
}

.star-inauspicious {
  background-color: rgba(#666666, 0.1);
  border: 2rpx solid #999999;
}

.star-symbol {
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1;
}

.star-name {
  font-size: 16rpx;
  color: $color-text-secondary;
  margin-top: 2rpx;
  line-height: 1;
}

// Brightness indicator
.star-brightness {
  display: flex;
  gap: 2rpx;
  margin-top: 4rpx;
}

.brightness-dot {
  width: 6rpx;
  height: 6rpx;
  border-radius: 50%;
  background-color: $color-accent;
}

.brightness-dot-empty {
  background-color: $color-border;
}
</style>
