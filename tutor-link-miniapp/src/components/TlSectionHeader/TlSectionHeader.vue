<template>
  <view class="tl-section-header">
    <text class="tl-section-header__title">{{ title }}</text>
    <text v-if="showMore" class="tl-section-header__more" @tap="$emit('more')">
      {{ moreText }} ›
    </text>
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  showMore: { type: Boolean, default: false },
  moreText: { type: String, default: '查看更多' }
})

defineEmits(['more'])
</script>

<style lang="scss" scoped>
.tl-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;

  &__title {
    font-size: $font-size-md;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  &__more {
    font-size: $font-size-sm;
    color: $color-primary;
  }
}
</style>
