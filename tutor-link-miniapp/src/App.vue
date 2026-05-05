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

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #333;
}

.container {
  padding: 20rpx;
}
</style>
