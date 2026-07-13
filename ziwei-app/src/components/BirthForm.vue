<template>
  <!-- Birth info form for ZiWei chart calculation -->
  <view class="birth-form">
    <!-- Solar Date Picker -->
    <picker mode="date" :value="datePickerValue" :start="'1900-01-01'" :end="todayStr" fields="day" @change="onDateChange">
      <view class="form-row">
        <view class="form-row-left">
          <text class="form-label">公历生日</text>
        </view>
        <view class="form-row-right">
          <text class="form-value" :class="{ 'form-placeholder': !dateDisplay }">
            {{ dateDisplay || '请选择日期' }}
          </text>
          <text class="form-arrow">&#8250;</text>
        </view>
      </view>
    </picker>

    <!-- Birth Time Picker (时辰) -->
    <picker mode="selector" :range="timeOptionLabels" :value="timePickerIndex" @change="onTimeChange">
      <view class="form-row">
        <view class="form-row-left">
          <text class="form-label">出生时辰</text>
        </view>
        <view class="form-row-right">
          <text class="form-value" :class="{ 'form-placeholder': localForm.hour < 0 }">
            {{ timeDisplay }}
          </text>
          <text class="form-arrow">&#8250;</text>
        </view>
      </view>
    </picker>

    <!-- Gender Selection -->
    <view class="form-row">
      <view class="form-row-left">
        <text class="form-label">性别</text>
      </view>
      <view class="form-row-right">
        <view class="gender-toggle">
          <view
            class="gender-option"
            :class="{ 'gender-active': localForm.gender === 1 }"
            @tap="localForm.gender = 1"
          >
            <text class="gender-text">男</text>
          </view>
          <view
            class="gender-option"
            :class="{ 'gender-active': localForm.gender === 0 }"
            @tap="localForm.gender = 0"
          >
            <text class="gender-text">女</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Birth Location (optional) -->
    <view class="form-row">
      <view class="form-row-left">
        <text class="form-label">出生地点</text>
      </view>
      <view class="form-row-right">
        <input
          class="form-input"
          v-model="localForm.birthPlace"
          placeholder="选填，如：北京"
          placeholder-style="color: #C0B8A8; font-size: 26rpx;"
          maxlength="30"
        />
      </view>
    </view>

    <!-- Daylight Saving Time -->
    <view class="form-row">
      <view class="form-row-left">
        <text class="form-label">夏令时</text>
      </view>
      <view class="form-row-right">
        <switch
          :checked="localForm.isDst"
          color="#C04B3E"
          @change="localForm.isDst = $event.detail.value"
        />
      </view>
    </view>

    <!-- Submit Button -->
    <view class="submit-section">
      <button class="btn-submit" @tap="onSubmit">
        <text class="btn-submit-text">开始排盘</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { reactive, computed } from 'vue'

const TIME_BRANCHES = ['子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥']
const TIME_RANGES = [
  '23:00-01:00', '01:00-03:00', '03:00-05:00', '05:00-07:00',
  '07:00-09:00', '09:00-11:00', '11:00-13:00', '13:00-15:00',
  '15:00-17:00', '17:00-19:00', '19:00-21:00', '21:00-23:00',
]

const timeOptionLabels = TIME_BRANCHES.map((b, i) => `${b}时 ${TIME_RANGES[i]}`)

const today = new Date()
const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

const localForm = reactive({
  solarYear: today.getFullYear(),
  solarMonth: today.getMonth() + 1,
  solarDay: today.getDate(),
  hour: -1,
  gender: 1,
  birthPlace: '',
  isDst: false,
})

const emit = defineEmits(['submit'])

const dateDisplay = computed(() => {
  if (!localForm.solarYear) return ''
  return `${localForm.solarYear}-${String(localForm.solarMonth).padStart(2, '0')}-${String(localForm.solarDay).padStart(2, '0')}`
})

const datePickerValue = computed(() => dateDisplay.value || todayStr)

const timePickerIndex = computed(() => localForm.hour >= 0 ? localForm.hour : 0)

const timeDisplay = computed(() => {
  if (localForm.hour < 0) return '请选择时辰'
  return `${TIME_BRANCHES[localForm.hour]}时 ${TIME_RANGES[localForm.hour]}`
})

function onDateChange(e) {
  const val = e.detail.value
  if (val) {
    const parts = val.split('-')
    localForm.solarYear = parseInt(parts[0])
    localForm.solarMonth = parseInt(parts[1])
    localForm.solarDay = parseInt(parts[2])
  }
}

function onTimeChange(e) {
  localForm.hour = e.detail.value
}

function onSubmit() {
  if (!localForm.solarYear || !localForm.solarMonth || !localForm.solarDay) {
    uni.showToast({ title: '请选择出生日期', icon: 'none' })
    return
  }
  if (localForm.hour < 0 || localForm.hour > 11) {
    uni.showToast({ title: '请选择出生时辰', icon: 'none' })
    return
  }
  const birthDate = new Date(localForm.solarYear, localForm.solarMonth - 1, localForm.solarDay)
  const now = new Date()
  const todayDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  if (birthDate > todayDate) {
    uni.showToast({ title: '出生日期不能是未来日期', icon: 'none' })
    return
  }

  const payload = {
    solarYear: localForm.solarYear,
    solarMonth: localForm.solarMonth,
    solarDay: localForm.solarDay,
    hour: localForm.hour,
    minute: 0,
    gender: localForm.gender,
    birthPlace: localForm.birthPlace || '',
    isDst: localForm.isDst || false,
  }

  emit('submit', payload)
}
</script>

<style lang="scss" scoped>
.birth-form {
  background-color: $bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 100rpx;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1rpx solid $divider-color;

  &:last-of-type {
    border-bottom: none;
  }
}

.form-row-left {
  flex-shrink: 0;
}

.form-label {
  font-size: $font-base;
  font-weight: 500;
  color: $text-primary;
}

.form-row-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.form-value {
  font-size: $font-base;
  color: $text-primary;

  &.form-placeholder {
    color: $border-color;
  }
}

.form-arrow {
  font-size: 32rpx;
  color: $border-color;
  margin-left: $spacing-xs;
  line-height: 1;
}

.form-input {
  flex: 1;
  text-align: right;
  font-size: $font-base;
  color: $text-primary;
}

// Gender Toggle
.gender-toggle {
  display: flex;
  gap: $spacing-sm;
}

.gender-option {
  padding: $spacing-xs $spacing-lg;
  border: 2rpx solid $border-color;
  border-radius: $radius-sm;
  transition: all 0.2s ease;

  .gender-text {
    font-size: $font-base;
    color: $text-secondary;
  }

  &.gender-active {
    border-color: $color-primary;
    background-color: rgba($color-primary, 0.05);

    .gender-text {
      color: $color-primary;
      font-weight: 600;
    }
  }
}

// Submit Section
.submit-section {
  padding: $spacing-lg $spacing-md;
  display: flex;
  justify-content: center;
}

.btn-submit {
  width: 560rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-light 100%);
  border: none;
  border-radius: $radius-xl;
  box-shadow: 0 8rpx 32rpx rgba(192, 75, 62, 0.35);

  &::after { border: none; }

  &:active {
    opacity: 0.85;
    transform: scale(0.97);
  }
}

.btn-submit-text {
  font-size: $font-xl;
  font-weight: 600;
  color: $text-inverse;
  letter-spacing: 6rpx;
}
</style>
