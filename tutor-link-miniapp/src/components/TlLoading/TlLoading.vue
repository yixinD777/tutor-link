<template>
  <view class="tl-loading">
    <view v-if="mode === 'spinner'" class="tl-loading__spinner" />
    <view v-else-if="mode === 'dots'" class="tl-loading__dots">
      <view class="tl-loading__dot" style="animation-delay: 0s" />
      <view class="tl-loading__dot" style="animation-delay: 0.16s" />
      <view class="tl-loading__dot" style="animation-delay: 0.32s" />
    </view>
    <view v-else-if="mode === 'skeleton'" class="tl-loading__skeleton">
      <view v-for="i in rows" :key="i" :class="['tl-loading__skeleton-row', { 'tl-loading__skeleton-row--short': i === rows && rows > 1 }]" />
    </view>
    <text v-if="text && mode !== 'skeleton'" class="tl-loading__text">{{ text }}</text>
  </view>
</template>

<script setup>
defineProps({
  text: { type: String, default: '' },
  mode: { type: String, default: 'spinner' },  // spinner | dots | skeleton
  rows: { type: Number, default: 3 }
})
</script>

<style lang="scss" scoped>
.tl-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-md;

  &__spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: $radius-round;
    animation: tl-spin 0.8s linear infinite;
  }

  &__dots {
    display: flex;
    gap: $spacing-xs;
  }

  &__dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: $radius-round;
    background: $color-primary;
    animation: bounce 1.4s infinite ease-in-out both;
  }

  &__text {
    margin-top: $spacing-sm;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  &__skeleton {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  &__skeleton-row {
    height: 28rpx;
    border-radius: $radius-sm;
    background: linear-gradient(90deg, $color-bg-input 25%, #E5E7EB 50%, $color-bg-input 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s ease-in-out infinite;

    &--short {
      width: 60%;
    }
  }
}

@keyframes tl-spin {
  to { transform: rotate(360deg); }
}
</style>
