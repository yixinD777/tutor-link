<template>
  <view class="tl-rating">
    <view class="tl-rating__stars">
      <text
        v-for="i in 5"
        :key="i"
        :class="['tl-rating__star', { 'tl-rating__star--filled': i <= current }]"
        @tap="editable && $emit('update:value', i)"
      >
        {{ i <= current ? '★' : '☆' }}
      </text>
    </view>
    <text v-if="showCount && count > 0" class="tl-rating__count">({{ count }})</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: Number, default: 0 },
  editable: { type: Boolean, default: false },
  size: { type: String, default: 'medium' },  // small | medium | large
  showCount: { type: Boolean, default: false },
  count: { type: Number, default: 0 }
})

defineEmits(['update:value'])

const current = computed(() => Math.round(props.value))
</script>

<style lang="scss" scoped>
.tl-rating {
  display: inline-flex;
  align-items: center;

  &__stars {
    display: flex;
  }

  &__star {
    color: $color-text-placeholder;
    font-size: $font-size-md;

    &--filled {
      color: $color-warning;
    }
  }

  &__count {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-left: $spacing-xs;
  }
}
</style>
