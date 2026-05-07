<template>
  <view :class="['tl-switch', { 'tl-switch--on': modelValue }]" :style="{ '--active-color': activeColor }" @tap="onToggle">
    <view class="tl-switch__thumb" />
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: [Boolean, Number], default: false },
  activeColor: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

function onToggle() {
  emit('update:modelValue', !props.modelValue)
}
</script>

<style lang="scss" scoped>
.tl-switch {
  width: 88rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: $color-border;
  position: relative;
  transition: background $duration-normal $ease-default;

  &--on {
    background: var(--active-color, $color-primary);
  }

  &__thumb {
    width: 40rpx;
    height: 40rpx;
    border-radius: $radius-round;
    background: #fff;
    position: absolute;
    top: 4rpx;
    left: 4rpx;
    transition: transform $duration-normal $ease-default;
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.15);
  }

  &--on &__thumb {
    transform: translateX(40rpx);
  }
}
</style>
