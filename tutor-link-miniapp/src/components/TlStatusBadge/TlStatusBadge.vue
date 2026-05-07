<template>
  <view :class="['tl-status-badge', `tl-status-badge--${theme}`]">
    {{ displayText }}
  </view>
</template>

<script setup>
import { computed } from 'vue'
import {
  orderStatusText, scheduleStatusText, sessionStatusText,
  trialStatusText, certStatusText
} from '../../utils/formatters'
import {
  ORDER_STATUS_THEME, SCHEDULE_STATUS_THEME, SESSION_STATUS_THEME,
  TRIAL_STATUS_THEME, CERT_STATUS_THEME
} from '../../utils/constants'

const props = defineProps({
  status: { type: [String, Number], required: true },
  type: { type: String, default: 'order' },  // order | schedule | session | trial | cert
  text: { type: String, default: '' }
})

const statusMap = {
  order: { text: orderStatusText, theme: ORDER_STATUS_THEME },
  schedule: { text: scheduleStatusText, theme: SCHEDULE_STATUS_THEME },
  session: { text: sessionStatusText, theme: SESSION_STATUS_THEME },
  trial: { text: trialStatusText, theme: TRIAL_STATUS_THEME },
  cert: { text: certStatusText, theme: CERT_STATUS_THEME },
}

const displayText = computed(() => {
  if (props.text) return props.text
  return statusMap[props.type]?.text(props.status) || '未知'
})

const theme = computed(() => {
  return statusMap[props.type]?.theme[props.status] || 'neutral'
})
</script>

<style lang="scss" scoped>
.tl-status-badge {
  font-size: $font-size-sm;
  padding: $spacing-xs $spacing-sm;
  border-radius: $radius-sm;
  display: inline-block;
  line-height: 1.4;

  &--pending {
    background: $color-status-pending-bg;
    color: $color-status-pending-text;
  }

  &--active {
    background: $color-status-active-bg;
    color: $color-status-active-text;
  }

  &--info {
    background: $color-status-info-bg;
    color: $color-status-info-text;
  }

  &--neutral {
    background: $color-status-neutral-bg;
    color: $color-status-neutral-text;
  }

  &--error {
    background: $color-status-error-bg;
    color: $color-status-error-text;
  }
}
</style>
