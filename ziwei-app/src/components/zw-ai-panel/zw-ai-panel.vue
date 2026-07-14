<template>
  <!-- AI Interpretation Panel - Full screen slide-up overlay -->
  <view v-if="visible" class="ai-panel-overlay" @tap="onBackdropTap">
    <view class="ai-panel" :class="{ 'ai-panel--visible': visible }" @tap.stop>
      <!-- Header -->
      <view class="ai-panel__header">
        <view class="ai-panel__title-row">
          <text class="ai-panel__icon">&#9733;</text>
          <text class="ai-panel__title">AI 命盘解读</text>
        </view>
        <view class="ai-panel__close" @tap="onClose">
          <text class="ai-panel__close-icon">&#10005;</text>
        </view>
      </view>

      <!-- Preset Question Chips -->
      <view class="ai-panel__chips">
        <view
          v-for="chip in presetQuestions"
          :key="chip.key"
          class="ai-panel__chip"
          :class="{ 'ai-panel__chip--active': activeChip === chip.key }"
          @tap="selectPreset(chip.key, chip.question)"
        >
          <text class="ai-panel__chip-text">{{ chip.label }}</text>
        </view>
      </view>

      <!-- Custom Question Input -->
      <view class="ai-panel__input-row">
        <input
          class="ai-panel__input"
          v-model="customQuestion"
          placeholder="输入你想了解的问题..."
          placeholder-style="color: #B8A89A; font-size: 28rpx;"
          :disabled="loading"
          confirm-type="send"
          @confirm="sendQuestion"
        />
        <view class="ai-panel__send-btn" :class="{ 'ai-panel__send-btn--disabled': loading || !customQuestion.trim() }" @tap="sendQuestion">
          <text class="ai-panel__send-text">发送</text>
        </view>
      </view>

      <!-- Content Area -->
      <scroll-view class="ai-panel__content" scroll-y :scroll-into-view="scrollAnchor">
        <!-- Loading State -->
        <view v-if="loading" class="ai-panel__loading">
          <view class="ai-panel__taiji">
            <text class="ai-panel__taiji-icon">&#9775;</text>
          </view>
          <text class="ai-panel__loading-text">命理推演中...</text>
          <text class="ai-panel__loading-hint">紫微斗数 · 洞悉天命</text>
        </view>

        <!-- Empty State -->
        <view v-else-if="!interpretation && !errorMsg" class="ai-panel__empty">
          <text class="ai-panel__empty-icon">&#9775;</text>
          <text class="ai-panel__empty-text">选择预设问题或输入</text>
          <text class="ai-panel__empty-text">你想了解的内容</text>
          <text class="ai-panel__empty-hint">AI 命理师将为你深度解读</text>
        </view>

        <!-- Error State -->
        <view v-else-if="errorMsg" class="ai-panel__error">
          <text class="ai-panel__error-icon">&#10071;</text>
          <text class="ai-panel__error-text">{{ errorMsg }}</text>
          <view class="ai-panel__retry-btn" @tap="retryLast">
            <text class="ai-panel__retry-text">重新解读</text>
          </view>
        </view>

        <!-- Interpretation Result -->
        <view v-else-if="interpretation" class="ai-panel__result">
          <view class="ai-panel__result-meta">
            <text class="ai-panel__result-question">Q: {{ lastQuestion }}</text>
          </view>
          <view class="ai-panel__result-body">
            <text class="ai-panel__result-text">{{ interpretation }}</text>
          </view>
          <view id="result-bottom" class="ai-panel__result-bottom"></view>
        </view>
      </scroll-view>

      <!-- Bottom Action -->
      <view v-if="interpretation && !loading" class="ai-panel__footer">
        <view class="ai-panel__footer-btn" @tap="resetPanel">
          <text class="ai-panel__footer-btn-text">重新提问</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import $api from '@/core/api'

// ========== Props ==========
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  chartId: {
    type: [Number, String],
    default: null,
  },
})

// ========== Emits ==========
const emit = defineEmits(['close'])

// ========== State ==========
const loading = ref(false)
const interpretation = ref('')
const errorMsg = ref('')
const customQuestion = ref('')
const activeChip = ref('')
const scrollAnchor = ref('')
const lastQuestion = ref('')

// ========== Preset Questions ==========
const presetQuestions = [
  { key: 'overall', label: '整体运势', question: '请全面解读此命盘的整体运势和人生特点' },
  { key: 'career', label: '事业财运', question: '请解读此命盘的事业发展和财运走势' },
  { key: 'marriage', label: '感情婚姻', question: '请解读此命盘的感情婚姻状况' },
  { key: 'health', label: '健康状况', question: '请解读此命盘的健康状况和注意事项' },
  { key: 'liunian', label: '流年运势', question: '请解读当前流年运势和注意事项' },
]

// ========== Watch ==========
// Reset state when panel opens
watch(() => props.visible, (newVal) => {
  if (newVal && props.chartId) {
    resetState()
  }
})

// ========== Methods ==========
function resetState() {
  loading.value = false
  interpretation.value = ''
  errorMsg.value = ''
  customQuestion.value = ''
  activeChip.value = ''
}

function selectPreset(key, question) {
  if (loading.value) return
  activeChip.value = key
  customQuestion.value = ''
  doInterpret(question)
}

function sendQuestion() {
  if (loading.value) return
  const q = customQuestion.value.trim()
  if (!q) return
  activeChip.value = ''
  doInterpret(q)
}

async function doInterpret(question) {
  if (!props.chartId) {
    errorMsg.value = '命盘数据未加载，请先返回保存命盘'
    return
  }

  loading.value = true
  interpretation.value = ''
  errorMsg.value = ''
  lastQuestion.value = question

  try {
    const res = await $api.ai.interpretChart(props.chartId, question)
    const data = res.data || res
    if (data && typeof data === 'string') {
      interpretation.value = data
    } else if (data && data.data) {
      interpretation.value = data.data
    } else {
      interpretation.value = String(data || '暂无解读结果')
    }
    // Scroll to top of result after render
    nextTick(() => {
      scrollAnchor.value = 'result-bottom'
    })
  } catch (err) {
    console.error('[AiPanel] Interpretation failed:', err)
    errorMsg.value = err.message || 'AI 解读服务暂不可用，请稍后重试'
  } finally {
    loading.value = false
  }
}

function retryLast() {
  if (lastQuestion.value) {
    doInterpret(lastQuestion.value)
  } else {
    doInterpret('请全面解读此命盘')
  }
}

function resetPanel() {
  resetState()
}

function onClose() {
  emit('close')
}

function onBackdropTap() {
  if (!loading.value) {
    emit('close')
  }
}
</script>

<style lang="scss" scoped>
// ========== Overlay ==========
.ai-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: $z-index-modal;
  background-color: rgba(44, 44, 44, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

// ========== Panel ==========
.ai-panel {
  width: 100%;
  height: 92vh;
  background-color: $color-bg-primary;
  border-radius: $radius-lg $radius-lg 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transform: translateY(100%);
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1);

  &--visible {
    transform: translateY(0);
  }
}

// ========== Header ==========
.ai-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  border-bottom: 2rpx solid $color-divider;
  background-color: $color-bg-card;
  flex-shrink: 0;
}

.ai-panel__title-row {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.ai-panel__icon {
  font-size: 36rpx;
  color: $color-gold;
}

.ai-panel__title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $color-text-primary;
  letter-spacing: 2rpx;
}

.ai-panel__close {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active { opacity: 0.5; }
}

.ai-panel__close-icon {
  font-size: 36rpx;
  color: $color-text-hint;
}

// ========== Chips ==========
.ai-panel__chips {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-lg;
  background-color: $color-bg-card;
  border-bottom: 1rpx solid $color-divider;
  flex-shrink: 0;
}

.ai-panel__chip {
  padding: 10rpx 28rpx;
  border-radius: $radius-lg;
  border: 2rpx solid $color-border;
  background-color: $color-bg-primary;
  transition: all 0.2s ease;

  &:active { opacity: 0.7; }

  &--active {
    border-color: $color-accent;
    background-color: rgba($color-accent, 0.06);
  }
}

.ai-panel__chip-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  white-space: nowrap;

  .ai-panel__chip--active & {
    color: $color-accent;
    font-weight: 500;
  }
}

// ========== Input Row ==========
.ai-panel__input-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-lg;
  background-color: $color-bg-card;
  border-bottom: 1rpx solid $color-divider;
  flex-shrink: 0;
}

.ai-panel__input {
  flex: 1;
  height: 72rpx;
  padding: 0 $spacing-md;
  background-color: $color-bg-primary;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  color: $color-text-primary;
  border: 2rpx solid $color-border;
}

.ai-panel__send-btn {
  width: 120rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $color-gold 0%, $color-gold-light 100%);
  border-radius: $radius-md;

  &:active { opacity: 0.8; }

  &--disabled {
    opacity: 0.4;
    pointer-events: none;
  }
}

.ai-panel__send-text {
  font-size: $font-size-sm;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 2rpx;
}

// ========== Content Area ==========
.ai-panel__content {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-md $spacing-lg;
}

// ========== Loading ==========
.ai-panel__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120rpx;
}

.ai-panel__taiji {
  width: 160rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-lg;
}

.ai-panel__taiji-icon {
  font-size: 100rpx;
  color: $color-accent;
  animation: taiJiSpin 3s linear infinite;
}

@keyframes taiJiSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-panel__loading-text {
  font-size: $font-size-md;
  color: $color-text-primary;
  font-weight: 500;
  margin-bottom: $spacing-xs;
}

.ai-panel__loading-hint {
  font-size: $font-size-sm;
  color: $color-text-hint;
  letter-spacing: 4rpx;
}

// ========== Empty ==========
.ai-panel__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100rpx;
}

.ai-panel__empty-icon {
  font-size: 80rpx;
  color: $color-gold;
  opacity: 0.3;
  margin-bottom: $spacing-lg;
}

.ai-panel__empty-text {
  font-size: $font-size-md;
  color: $color-text-secondary;
  margin-bottom: $spacing-xs;
}

.ai-panel__empty-hint {
  font-size: $font-size-xs;
  color: $color-text-hint;
  margin-top: $spacing-md;
}

// ========== Error ==========
.ai-panel__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120rpx;
}

.ai-panel__error-icon {
  font-size: 56rpx;
  margin-bottom: $spacing-md;
  opacity: 0.5;
}

.ai-panel__error-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  text-align: center;
  line-height: 1.6;
  margin-bottom: $spacing-lg;
  padding: 0 $spacing-lg;
}

.ai-panel__retry-btn {
  padding: 14rpx 48rpx;
  border-radius: $radius-lg;
  border: 2rpx solid $color-accent;
  background-color: transparent;

  &:active { opacity: 0.7; }
}

.ai-panel__retry-text {
  font-size: $font-size-sm;
  color: $color-accent;
  font-weight: 500;
}

// ========== Result ==========
.ai-panel__result {
  padding-bottom: $spacing-md;
}

.ai-panel__result-meta {
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1rpx solid $color-divider;
}

.ai-panel__result-question {
  font-size: $font-size-sm;
  color: $color-gold;
  font-weight: 500;
  line-height: 1.6;
}

.ai-panel__result-body {
  padding: 0;
}

.ai-panel__result-text {
  font-size: $font-size-base;
  color: $color-text-primary;
  line-height: 2;
  text-align: justify;
  white-space: pre-wrap;
  word-break: break-all;
}

.ai-panel__result-bottom {
  height: 20rpx;
}

// ========== Footer ==========
.ai-panel__footer {
  display: flex;
  justify-content: center;
  padding: $spacing-md $spacing-lg;
  border-top: 1rpx solid $color-divider;
  background-color: $color-bg-card;
  flex-shrink: 0;
}

.ai-panel__footer-btn {
  width: 320rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-lg;
  border: 2rpx solid $color-gold;
  background-color: transparent;

  &:active { opacity: 0.7; }
}

.ai-panel__footer-btn-text {
  font-size: $font-size-sm;
  color: $color-gold;
  font-weight: 500;
}
</style>
