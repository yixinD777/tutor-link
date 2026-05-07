<template>
  <view class="tl-form-textarea">
    <view v-if="label" class="tl-form-textarea__label">{{ label }}</view>
    <textarea
      :class="'tl-form-textarea__field'"
      :value="modelValue"
      :placeholder="placeholder"
      :style="{ height }"
      :maxlength="maxlength"
      @input="onInput"
    />
  </view>
</template>

<script setup>
const props = defineProps({
  label: { type: String, default: '' },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入' },
  height: { type: String, default: '200rpx' },
  maxlength: { type: Number, default: 500 }
})

const emit = defineEmits(['update:modelValue'])

function onInput(e) {
  emit('update:modelValue', e.detail.value)
}
</script>

<style lang="scss" scoped>
.tl-form-textarea {
  margin-bottom: $spacing-lg;

  &__label {
    font-size: $font-size-base;
    color: $color-text-regular;
    margin-bottom: $spacing-sm;
  }

  &__field {
    background: $color-bg-input;
    border-radius: $radius-md;
    padding: $spacing-lg;
    font-size: $font-size-base;
    color: $color-text-primary;
    width: 100%;
    box-sizing: border-box;
  }
}
</style>
