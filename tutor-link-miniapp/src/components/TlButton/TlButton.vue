<template>
  <view
    :class="[
      'tl-button',
      `tl-button--${type}`,
      `tl-button--${size}`,
      { 'tl-button--block': block, 'tl-button--disabled': disabled, 'tl-button--loading': loading }
    ]"
    @tap="handleTap"
  >
    <view v-if="loading" class="tl-button__loading">
      <view class="tl-button__spinner" />
    </view>
    <slot />
  </view>
</template>

<script setup>
const props = defineProps({
  type: { type: String, default: 'primary' },
  size: { type: String, default: 'large' },
  block: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['tap'])

function handleTap() {
  if (props.disabled || props.loading) return
  emit('tap')
}
</script>

<style lang="scss" scoped>
.tl-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-pill;
  font-weight: $font-weight-medium;
  transition: transform $duration-fast $ease-default, opacity $duration-fast $ease-default;
  position: relative;

  &:active {
    transform: scale(0.97);
  }

  &--block {
    width: 100%;
  }

  &--disabled {
    opacity: 0.5;
  }

  &--loading {
    opacity: 0.7;
  }

  // --- Type variants ---
  &--primary {
    background: $color-primary;
    color: #fff;
    @include shimmer-effect;
  }

  &--outline {
    background: $color-bg-card;
    color: $color-primary;
    border: 2rpx solid $color-primary;
  }

  &--success {
    background: $color-success;
    color: #fff;
  }

  &--warning {
    background: $color-warning;
    color: #fff;
  }

  &--danger {
    background: $color-danger;
    color: #fff;
  }

  &--danger-outline {
    background: $color-bg-card;
    color: $color-danger;
    border: 2rpx solid $color-danger;
  }

  &--text {
    background: transparent;
    color: $color-primary;
  }

  &--wechat {
    background: $color-wechat;
    color: #fff;
  }

  // --- Size variants ---
  &--large {
    font-size: $font-size-md;
    padding: $spacing-lg 0;
  }

  &--medium {
    font-size: $font-size-base;
    padding: $spacing-md 0;
  }

  &--small {
    font-size: $font-size-sm;
    padding: $spacing-xs $spacing-lg;
  }
}

.tl-button__loading {
  margin-right: $spacing-xs;
}

.tl-button__spinner {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: $radius-round;
  animation: btn-spin 0.6s linear infinite;
}

.tl-button--outline .tl-button__spinner,
.tl-button--danger-outline .tl-button__spinner,
.tl-button--text .tl-button__spinner {
  border-color: rgba(0, 0, 0, 0.1);
  border-top-color: $color-primary;
}

@keyframes btn-spin {
  to { transform: rotate(360deg); }
}
</style>
