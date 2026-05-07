<template>
  <view :class="['tl-card', { 'tl-card--elevated': elevated }]" :style="{ padding }">
    <view v-if="$slots.header" class="tl-card__header">
      <slot name="header" />
    </view>
    <view class="tl-card__body">
      <slot />
    </view>
    <view v-if="$slots.footer" class="tl-card__footer">
      <slot name="footer" />
    </view>
  </view>
</template>

<script setup>
defineProps({
  elevated: { type: Boolean, default: false },
  padding: { type: String, default: '' }
})
</script>

<style lang="scss" scoped>
.tl-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $spacing-2xl;
  margin-bottom: $spacing-lg;

  &--elevated {
    box-shadow: $shadow-card;
  }

  &__header {
    margin-bottom: $spacing-md;
  }

  &__footer {
    margin-top: $spacing-md;
  }
}
</style>
