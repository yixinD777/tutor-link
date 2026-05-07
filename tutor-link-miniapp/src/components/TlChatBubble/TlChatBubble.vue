<template>
  <view :class="['tl-chat-bubble', `tl-chat-bubble--${role}`]">
    <view :class="['tl-chat-bubble__content', `tl-chat-bubble__content--${role}`]">
      <slot />
    </view>
    <slot name="footer" />
  </view>
</template>

<script setup>
defineProps({
  role: { type: String, default: 'other' }  // self | other | ai
})
</script>

<style lang="scss" scoped>
.tl-chat-bubble {
  max-width: 75%;
  margin-bottom: $spacing-md;

  &--self {
    align-self: flex-end;
  }

  &--other, &--ai {
    align-self: flex-start;
  }

  &__content {
    padding: $spacing-md $spacing-lg;
    border-radius: $radius-xl;
    font-size: $font-size-base;
    line-height: $line-height-normal;
    word-break: break-all;

    &--self {
      background: $color-primary;
      color: #fff;
      border-bottom-right-radius: $radius-sm;
    }

    &--other {
      background: $color-bg-card;
      color: $color-text-primary;
      border-bottom-left-radius: $radius-sm;
    }

    &--ai {
      background: $color-primary-light;
      color: $color-text-primary;
      border-bottom-left-radius: $radius-sm;
    }
  }
}
</style>
