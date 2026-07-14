<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { ZwInit } from '@/core'

onLaunch(() => {
  console.log('[ZiWei] App launched')
  ZwInit()
})

onShow(() => {
  console.log('[ZiWei] App shown')
})

onHide(() => {
  console.log('[ZiWei] App hidden')
})
</script>

<style lang="scss">
@import '@/uni.scss';

// Global page styles
page {
  background-color: $bg-page;
  color: $text-primary;
  font-size: $font-base;
  font-family: $font-family-base;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}

// Reset
view, text, image, scroll-view, swiper, swiper-item {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

// === Common Utility Classes ===
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

// Text utilities
.text-primary { color: $text-primary; }
.text-secondary { color: $text-secondary; }
.text-hint { color: $text-hint; }
.text-accent { color: $color-primary; }
.text-gold { color: $color-gold; }
.text-center { text-align: center; }

// Background utilities
.bg-page { background-color: $bg-page; }
.bg-card { background-color: $bg-card; }

// Spacing utilities
.mt-sm { margin-top: $spacing-sm; }
.mt-md { margin-top: $spacing-md; }
.mt-lg { margin-top: $spacing-lg; }
.mt-xl { margin-top: $spacing-xl; }
.mb-sm { margin-bottom: $spacing-sm; }
.mb-md { margin-bottom: $spacing-md; }
.mb-lg { margin-bottom: $spacing-lg; }
.px-md { padding-left: $spacing-md; padding-right: $spacing-md; }
</style>
