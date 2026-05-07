<template>
  <view :class="['tl-avatar', `tl-avatar--${size}`]" @tap="$emit('tap')">
    <image v-if="src" class="tl-avatar__img" :src="src" mode="aspectFill" />
    <view v-else class="tl-avatar__fallback">
      <slot>
        <text class="tl-avatar__text">{{ fallbackText }}</text>
      </slot>
    </view>
    <view v-if="online" class="tl-avatar__online" />
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  size: { type: String, default: 'medium' },  // small | medium | large
  online: { type: Boolean, default: false },
  name: { type: String, default: '' }
})

defineEmits(['tap'])

const sizeMap = { small: 48, medium: 72, large: 108 }

const fallbackText = computed(() => {
  if (props.name) return props.name.charAt(0)
  return ''
})
</script>

<style lang="scss" scoped>
.tl-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-round;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
  background: $color-bg-input;

  &--small { width: 48rpx; height: 48rpx; }
  &--medium { width: 72rpx; height: 72rpx; }
  &--large { width: 108rpx; height: 108rpx; }

  &__img {
    width: 100%;
    height: 100%;
  }

  &__fallback {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $color-bg-input;
  }

  &__text {
    font-size: $font-size-md;
    color: $color-primary;
    font-weight: $font-weight-medium;
  }

  &__online {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 16rpx;
    height: 16rpx;
    border-radius: $radius-round;
    background: $color-success;
    border: 3rpx solid $color-bg-card;
  }
}
</style>
