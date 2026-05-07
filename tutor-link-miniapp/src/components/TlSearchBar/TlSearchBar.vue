<template>
  <view class="tl-search-bar" @tap="readonly && $emit('search')">
    <view class="tl-search-bar__box">
      <text class="tl-search-bar__icon">🔍</text>
      <input
        v-if="!readonly"
        class="tl-search-bar__input"
        :value="modelValue"
        :placeholder="placeholder"
        confirm-type="search"
        @input="onInput"
        @confirm="$emit('search')"
      />
      <text v-else class="tl-search-bar__placeholder">{{ placeholder }}</text>
      <text v-if="modelValue && !readonly" class="tl-search-bar__clear" @tap.stop="onClear">✕</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索' },
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear'])

function onInput(e) {
  emit('update:modelValue', e.detail.value)
}

function onClear() {
  emit('update:modelValue', '')
  emit('clear')
}
</script>

<style lang="scss" scoped>
.tl-search-bar {
  margin-bottom: $spacing-md;

  &__box {
    display: flex;
    align-items: center;
    background: $color-bg-input;
    border-radius: $radius-xl;
    padding: $spacing-md $spacing-lg;
    gap: $spacing-sm;
  }

  &__icon {
    font-size: $font-size-base;
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-primary;
  }

  &__placeholder {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-placeholder;
  }

  &__clear {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    padding: $spacing-xs;
  }
}
</style>
