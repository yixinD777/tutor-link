<template>
  <view class="tl-form-input">
    <view v-if="label" class="tl-form-input__label">
      <text v-if="required" class="tl-form-input__required">*</text>
      <text>{{ label }}</text>
    </view>
    <slot>
      <input
        :class="['tl-form-input__field', { 'tl-form-input__field--disabled': disabled }]"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :disabled="disabled"
        @input="onInput"
      />
    </slot>
  </view>
</template>

<script setup>
const props = defineProps({
  label: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '请输入' },
  type: { type: String, default: 'text' },
  maxlength: { type: Number, default: 140 },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

function onInput(e) {
  emit('update:modelValue', e.detail.value)
}
</script>

<style lang="scss" scoped>
.tl-form-input {
  margin-bottom: $spacing-lg;

  &__label {
    font-size: $font-size-base;
    color: $color-text-regular;
    margin-bottom: $spacing-sm;
  }

  &__required {
    color: $color-danger;
    margin-right: $spacing-xs;
  }

  &__field {
    background: $color-bg-input;
    border-radius: $radius-md;
    padding: $spacing-lg;
    font-size: $font-size-base;
    color: $color-text-primary;
    width: 100%;
    box-sizing: border-box;

    &--disabled {
      opacity: 0.6;
    }
  }
}
</style>
