<template>
  <view :class="['tl-tag', `tl-tag--${type}`]">
    {{ text }}
  </view>
</template>

<script setup>
defineProps({
  type: { type: String, default: 'default' },  // subject | mode | hours | price | default
  text: { type: String, required: true }
})
</script>

<style lang="scss" scoped>
.tl-tag {
  font-size: $font-size-xs;
  border-radius: $radius-sm;
  padding: 4rpx $spacing-sm;
  line-height: 1.4;
  display: inline-block;
  font-weight: $font-weight-regular;

  &--default {
    background: $color-bg-input;
    color: $color-text-secondary;
  }

  &--subject {
    background: $color-primary-light;
    color: $color-primary-dark;
  }

  &--mode {
    background: $color-purple-light;
    color: $color-purple;
  }

  &--hours {
    background: $color-warning-light;
    color: $color-warning;
  }

  &--price {
    background: $color-primary-light;
    color: $color-primary-dark;
    font-weight: $font-weight-medium;
  }
}
</style>
