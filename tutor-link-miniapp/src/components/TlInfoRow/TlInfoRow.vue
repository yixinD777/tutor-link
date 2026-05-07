<template>
  <view :class="['tl-info-row', { 'tl-info-row--border': border }]">
    <text class="tl-info-row__label">{{ label }}</text>
    <slot name="value">
      <text :class="['tl-info-row__value', `tl-info-row__value--${type}`]">{{ value }}</text>
    </slot>
  </view>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], default: '' },
  type: { type: String, default: 'default' },  // default | price | cancel
  border: { type: Boolean, default: true }
})
</script>

<style lang="scss" scoped>
.tl-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;

  &--border {
    border-bottom: 1rpx solid $color-border;

    &:last-child {
      border-bottom: none;
    }
  }

  &__label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    font-weight: $font-weight-regular;
    flex-shrink: 0;
  }

  &__value {
    font-size: $font-size-sm;
    color: $color-text-regular;
    font-weight: $font-weight-medium;
    text-align: right;

    &--price {
      color: $color-warning;
      font-weight: $font-weight-medium;
    }

    &--cancel {
      color: $color-danger;
    }
  }
}
</style>
