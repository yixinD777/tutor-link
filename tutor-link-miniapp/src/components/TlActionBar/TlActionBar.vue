<template>
  <view class="tl-action-bar">
    <slot />
  </view>
</template>

<script setup>
defineProps({
  safeArea: { type: Boolean, default: true }
})
</script>

<style lang="scss" scoped>
.tl-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: $spacing-md $spacing-xl;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: $color-bg-card;
  box-shadow: $shadow-bar;
  gap: $spacing-md;
  z-index: $z-index-fixed;
}
</style>
