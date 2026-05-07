<template>
  <view class="tl-chat-input-bar">
    <slot name="actions" />
    <input
      class="tl-chat-input-bar__input"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      confirm-type="send"
      @input="onInput"
      @confirm="onSend"
    />
    <view :class="['tl-chat-input-bar__send', { 'tl-chat-input-bar__send--active': canSend }]" @tap="onSend">
      发送
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '输入消息...' },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'send'])

const canSend = computed(() => props.modelValue.trim().length > 0 && !props.disabled)

function onInput(e) {
  emit('update:modelValue', e.detail.value)
}

function onSend() {
  if (canSend.value) emit('send')
}
</script>

<style lang="scss" scoped>
.tl-chat-input-bar {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: $color-bg-card;
  border-top: 1rpx solid $color-border;
  gap: $spacing-sm;

  &__input {
    flex: 1;
    background: $color-bg-input;
    border-radius: $radius-pill;
    padding: $spacing-md $spacing-lg;
    font-size: $font-size-base;
    color: $color-text-primary;
  }

  &__send {
    background: $color-bg-input;
    color: $color-text-placeholder;
    border-radius: $radius-pill;
    padding: $spacing-md $spacing-lg;
    font-size: $font-size-sm;
    flex-shrink: 0;
    transition: all $duration-fast $ease-default;

    &--active {
      background: $color-primary;
      color: #fff;
    }
  }
}
</style>
