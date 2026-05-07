<template>
  <view class="tl-timeline">
    <view v-for="(item, idx) in items" :key="idx" class="tl-timeline__item">
      <view class="tl-timeline__dot" />
      <view class="tl-timeline__content">
        <slot name="item" :item="item" :index="idx">
          <text class="tl-timeline__action">{{ item.action }}</text>
          <text class="tl-timeline__time">{{ item.time }}</text>
        </slot>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }  // [{ action, time }]
})
</script>

<style lang="scss" scoped>
.tl-timeline {
  &__item {
    display: flex;
    padding-bottom: $spacing-md;
    position: relative;

    &:not(:last-child)::after {
      content: '';
      position: absolute;
      left: 7rpx;
      top: 30rpx;
      bottom: 0;
      width: 2rpx;
      background: $color-border;
    }
  }

  &__dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: $radius-round;
    background: $color-primary;
    margin-top: $spacing-sm;
    margin-right: $spacing-md;
    flex-shrink: 0;
  }

  &__content {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  &__action {
    font-size: $font-size-sm;
    color: $color-text-regular;
    flex: 1;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-left: $spacing-sm;
    flex-shrink: 0;
  }
}
</style>
