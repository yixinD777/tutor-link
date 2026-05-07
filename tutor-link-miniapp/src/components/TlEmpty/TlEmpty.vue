<template>
  <view class="tl-empty">
    <view class="tl-empty__icon">
      <view class="tl-empty__svg">
        <view class="tl-empty__svg-box" />
        <view class="tl-empty__svg-line tl-empty__svg-line--1" />
        <view class="tl-empty__svg-line tl-empty__svg-line--2" />
        <view class="tl-empty__svg-dot" />
      </view>
    </view>
    <text class="tl-empty__text">{{ text }}</text>
    <TlButton
      v-if="actionText"
      :type="actionType"
      size="medium"
      style="margin-top: 30rpx; width: auto; padding-left: 40rpx; padding-right: 40rpx;"
      @tap="$emit('action')"
    >
      {{ actionText }}
    </TlButton>
  </view>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' },
  actionType: { type: String, default: 'primary' }
})

defineEmits(['action'])
</script>

<style lang="scss" scoped>
.tl-empty {
  text-align: center;
  padding: $spacing-4xl $spacing-2xl;
  display: flex;
  flex-direction: column;
  align-items: center;

  &__icon {
    margin-bottom: $spacing-lg;
  }

  &__svg {
    width: 120rpx;
    height: 120rpx;
    position: relative;
  }

  &__svg-box {
    width: 80rpx;
    height: 64rpx;
    border: 3rpx solid $color-text-placeholder;
    border-radius: $radius-md;
    position: absolute;
    top: 16rpx;
    left: 20rpx;
    opacity: 0.5;

    &::before {
      content: '';
      position: absolute;
      top: 12rpx;
      left: 12rpx;
      width: 56rpx;
      height: 3rpx;
      background: $color-text-placeholder;
      border-radius: 2rpx;
    }

    &::after {
      content: '';
      position: absolute;
      top: 24rpx;
      left: 12rpx;
      width: 40rpx;
      height: 3rpx;
      background: $color-text-placeholder;
      border-radius: 2rpx;
      opacity: 0.5;
    }
  }

  &__svg-line {
    position: absolute;
    height: 3rpx;
    background: $color-text-placeholder;
    border-radius: 2rpx;
    opacity: 0.4;

    &--1 {
      width: 32rpx;
      top: 88rpx;
      left: 20rpx;
    }

    &--2 {
      width: 24rpx;
      top: 96rpx;
      left: 20rpx;
    }
  }

  &__svg-dot {
    width: 12rpx;
    height: 12rpx;
    border: 3rpx solid $color-text-placeholder;
    border-radius: $radius-round;
    position: absolute;
    top: 4rpx;
    right: 16rpx;
    opacity: 0.4;
  }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    font-weight: $font-weight-light;
  }
}
</style>
