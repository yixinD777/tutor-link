<script>
import { useUserStore } from './store/user'

export default {
  onLaunch() {
    console.log('App Launch')
    const userStore = useUserStore()
    userStore.loadFromStorage()
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import './uni.scss';

page {
  background-color: $color-bg-page;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: $color-text-regular;
  font-size: $font-size-base;
  line-height: $line-height-normal;
}

.container {
  padding: $spacing-page;
}
</style>
