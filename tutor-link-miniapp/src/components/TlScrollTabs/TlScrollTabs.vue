<template>
  <scroll-view class="tl-scroll-tabs" scroll-x :scroll-into-view="activeId" scroll-with-animation>
    <view
      v-for="tab in tabs"
      :id="'tab-' + tab.value"
      :key="tab.value"
      :class="['tl-scroll-tabs__item', { 'tl-scroll-tabs__item--active': modelValue === tab.value }]"
      @tap="$emit('update:modelValue', tab.value)"
    >
      {{ tab.label }}
    </view>
  </scroll-view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  tabs: { type: Array, required: true },  // [{ label, value }]
  modelValue: { type: [String, Number], default: '' }
})

defineEmits(['update:modelValue'])

const activeId = computed(() => props.modelValue !== '' ? 'tab-' + props.modelValue : '')
</script>

<style lang="scss" scoped>
.tl-scroll-tabs {
  white-space: nowrap;
  padding: $spacing-sm 0;
  margin-bottom: $spacing-md;

  &__item {
    display: inline-block;
    padding: $spacing-sm $spacing-lg;
    margin-right: $spacing-md;
    border-radius: $radius-pill;
    font-size: $font-size-sm;
    color: $color-text-secondary;
    background: $color-bg-input;
    transition: all $duration-normal $ease-default;

    &--active {
      background: $color-primary;
      color: #fff;
      font-weight: $font-weight-bold;
    }
  }
}
</style>
