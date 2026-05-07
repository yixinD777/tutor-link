<template>
  <view class="page">
    <!-- Premium Hero — white/light background + glow drift -->
    <view class="hero">
      <view class="hero-content">
        <text class="hero-title">找到适合你的好家教</text>
        <text class="hero-sub">打破中介信息费，直连大学生家教</text>
        <view class="search-box" @tap="goTutorListWithSearch">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索科目、学校、年级..."
            confirm-type="search"
            @confirm="goTutorListWithSearch"
          />
        </view>
      </view>
    </view>

    <!-- Quick actions — neutral icons + text -->
    <view class="quick-actions" v-if="isParent">
      <view class="action-item" @tap="goTutorList">
        <view class="action-icon action-icon--primary">
          <text class="action-icon-text">🔍</text>
        </view>
        <text class="action-label">找家教</text>
      </view>
      <view class="action-item" @tap="goCreateOrder">
        <view class="action-icon action-icon--warning">
          <text class="action-icon-text">📋</text>
        </view>
        <text class="action-label">发需求</text>
      </view>
      <view class="action-item" @tap="goAdvisor">
        <view class="action-icon action-icon--purple">
          <text class="action-icon-text">🤖</text>
        </view>
        <text class="action-label">AI顾问</text>
      </view>
    </view>

    <!-- Parent: subject tabs + tutor grid -->
    <template v-if="isParent">
      <view class="section-head">
        <text class="section-title">推荐家教</text>
      </view>

      <scroll-view scroll-x class="pills">
        <view
          v-for="tab in subjectTabs" :key="tab.value"
          :class="['pill', { 'pill--active': activeTab === tab.value }]"
          @tap="switchTab(tab.value)"
        >{{ tab.label }}</view>
      </scroll-view>

      <TlEmpty v-if="tutors.length === 0 && !isLoading" text="暂无推荐家教" />

      <template v-else>
        <!-- 首次加载：居中大 spinner -->
        <view v-if="isLoading && tutors.length === 0" class="feed-loading-center">
          <view class="feed-spinner-lg" />
          <text class="feed-loading-text">正在加载推荐</text>
        </view>

        <view class="tutor-grid">
          <view
            v-for="tutor in tutors" :key="tutor.id"
            class="grid-card"
            @tap="goTutorDetail(tutor.userId)"
          >
            <view class="grid-avatar-wrap">
              <TlAvatar :src="tutor.avatarUrl" size="medium" :name="tutor.realName || tutor.nickname" />
            </view>
            <text class="grid-name">{{ tutor.realName || tutor.nickname || '家教老师' }}</text>
            <text class="grid-rating">★ {{ tutor.avgRating ? Number(tutor.avgRating).toFixed(1) : '新' }}<text v-if="tutor.ratingCount" class="grid-rating-count">({{ tutor.ratingCount }})</text></text>
            <text class="grid-school">{{ tutor.university || '' }} · {{ tutor.major || '综合' }}</text>
            <text class="grid-price">{{ formatRate(tutor.hourlyRateMin, tutor.hourlyRateMax) }}</text>
          </view>
        </view>

        <!-- 翻页加载：底部小 spinner，紧贴最后一行卡片 -->
        <view v-if="isLoading && tutors.length > 0" class="feed-footer">
          <view class="feed-spinner" />
          <text class="feed-footer-text">加载更多</text>
        </view>
        <view v-else-if="!hasMore && tutors.length > 0" class="feed-footer">
          <text class="feed-footer-text">- 到底了 -</text>
        </view>
      </template>
    </template>

    <!-- Student: certification prompt -->
    <view v-if="isTutor && notCertified" class="section">
      <TlCard elevated>
        <view class="cert-tip">
          <text class="cert-tip-text">完成学生认证后即可查看待接单需求</text>
          <TlButton size="medium" @tap="goCertification" style="margin-top: 20rpx;">去认证</TlButton>
        </view>
      </TlCard>
    </view>

    <!-- Student: order feed -->
    <view v-if="isTutor && !notCertified" class="section">
      <TlSectionHeader title="待接单需求" />
      <TlEmpty v-if="orders.length === 0 && !isLoading" text="暂无待接单需求" />
      <view v-else class="order-list">
        <TlCard v-for="order in orders" :key="order.id" elevated @tap="chatWithParent(order)">
          <view class="order-header">
            <text class="order-title">{{ order.title }}</text>
            <text class="order-rate">¥{{ order.hourlyRate / 100 }}/时</text>
          </view>
          <view class="order-tags">
            <TlTag v-if="order.grade" type="subject" :text="order.grade" />
            <TlTag type="mode" :text="modeText(order.teachingMode)" />
            <TlTag type="hours" :text="order.totalHours + '课时'" />
          </view>
          <text class="order-time">{{ formatTime(order.createTime) }}</text>
        </TlCard>
      </view>
      <TlLoading v-if="isLoading" text="加载中..." />
      <view v-else-if="!hasMore && orders.length > 0" class="no-more"><text>没有更多了</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { searchTutors } from '../../api/tutor'
import { listPendingOrdersFeed } from '../../api/order'
import { get } from '../../api/request'
import { useUserStore } from '../../store/user'
import { formatRate, modeText, formatTime } from '../../utils/formatters'

const userStore = useUserStore()
const isParent = ref(false)
const isTutor = ref(false)
const isLoading = ref(false)
const notCertified = ref(false)

// ── 搜索关键词 ──
const searchKeyword = ref('')

// ── 科目 Tab ──
const TAB_RECOMMEND = '__recommend__'
const subjectTabs = ref([{ label: '推荐', value: TAB_RECOMMEND }])

// ── 家教 feed 流 ──
const tutors = ref([])
const page = ref(1)
const hasMore = ref(true)

// ── 家教订单流 ──
const orders = ref([])
const cursor = ref(null)
const orderHasMore = ref(true)

const activeTab = ref(TAB_RECOMMEND)

onMounted(() => {
  userStore.loadFromStorage()
  isParent.value = userStore.isParent()
  isTutor.value = userStore.isTutor()
  if (isParent.value) {
    loadSubjects()
    loadTutors(true)
  }
  if (isTutor.value) resetAndLoadOrders()
})

onShow(() => {
  userStore.loadFromStorage()
  isParent.value = userStore.isParent()
  isTutor.value = userStore.isTutor()
})

onReachBottom(() => {
  if (isParent.value) loadTutors()
  if (isTutor.value) loadMoreOrders()
})

// ── 科目加载 ──
async function loadSubjects() {
  try {
    const data = await get('/subjects')
    subjectTabs.value = [
      { label: '推荐', value: TAB_RECOMMEND },
      ...data.map(s => ({ label: s.name, value: s.id }))
    ]
  } catch (e) { console.error(e) }
}

// ── 家教加载 (feed 流) ──
async function loadTutors(reset = false) {
  if (isLoading.value) return
  if (!reset && !hasMore.value) return
  if (reset) { page.value = 1; tutors.value = []; hasMore.value = true }

  isLoading.value = true
  try {
    const params = { page: page.value, size: 10, sortBy: 'rating' }

    // 搜索关键词
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    // 科目筛选 (推荐 Tab 不带科目筛选)
    if (activeTab.value && activeTab.value !== TAB_RECOMMEND) {
      params.subjectId = activeTab.value
    }

    const res = await searchTutors(params)
    const records = res.records || []
    tutors.value = reset ? records : [...tutors.value, ...records]
    hasMore.value = records.length >= 10
    page.value++
  } catch (e) { console.error(e) }
  finally { await nextTick(); isLoading.value = false }
}

// ── 科目切换 ──
function switchTab(value) {
  activeTab.value = value
  loadTutors(true)
}

// ── 订单流 ──
function resetAndLoadOrders() { orders.value = []; cursor.value = null; orderHasMore.value = true; loadMoreOrders() }

async function loadMoreOrders() {
  if (isLoading.value || !orderHasMore.value) return
  isLoading.value = true
  try {
    const params = { limit: 10 }
    if (cursor.value) params.cursor = cursor.value
    const res = await listPendingOrdersFeed(params)
    orders.value = [...orders.value, ...(res.list || [])]
    cursor.value = res.nextCursor
    orderHasMore.value = res.hasMore
  } catch (e) {
    if (e?.code === 403 && e?.message?.includes('认证')) notCertified.value = true
    console.error(e)
  } finally { isLoading.value = false }
}

// ── 导航 ──
function goTutorList() { uni.switchTab({ url: '/pages/tutor/list' }) }
function goTutorListWithSearch() {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    // 搜索时留在首页，切到推荐 Tab 并带 keyword 筛选
    activeTab.value = TAB_RECOMMEND
    loadTutors(true)
  } else {
    uni.switchTab({ url: '/pages/tutor/list' })
  }
}
function goCreateOrder() { uni.navigateTo({ url: '/pages/order/create' }) }
function goAdvisor() { uni.navigateTo({ url: '/pages/advisor/chat' }) }
function goTutorDetail(userId) { uni.navigateTo({ url: `/pages/tutor/detail?userId=${userId}` }) }
function chatWithParent(order) { uni.navigateTo({ url: `/pages/chat/detail?otherUserId=${order.parentUserId}&orderId=${order.id}&orderStatus=1` }) }
function goCertification() { uni.navigateTo({ url: '/pages/profile/certification' }) }
</script>

<style lang="scss" scoped>
.page {
  background: $color-bg-page;
  min-height: 100vh;
  padding-bottom: calc(env(safe-area-inset-bottom) + 120rpx);
  animation: fadeIn 0.2s $ease-default;
}

/* ─── Premium Hero — white bg + glow drift ─── */
.hero {
  background: $color-primary;
  @include hero-glow;
  padding: $spacing-4xl $spacing-page $spacing-3xl;
  border-radius: 0 0 $radius-2xl $radius-2xl;
}

.hero-content {
  position: relative;
  z-index: $z-index-normal;
}

.hero-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #fff;
  display: block;
  margin-bottom: $spacing-xs;
  letter-spacing: 1rpx;
}

.hero-sub {
  font-size: $font-size-base;
  font-weight: $font-weight-light;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: $spacing-2xl;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: $radius-xl;
  padding: $spacing-md $spacing-lg;
  gap: $spacing-sm;
  box-shadow: $shadow-card;
}

.search-icon { font-size: $font-size-base; flex-shrink: 0; }

.search-input {
  flex: 1;
  font-size: $font-size-base;
  color: $color-text-primary;
}

/* ─── Quick actions — neutral round icons ─── */
.quick-actions {
  display: flex;
  gap: $spacing-lg;
  padding: 0 $spacing-page;
  margin-top: $spacing-md;
  position: relative;
  z-index: $z-index-sticky;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-lg $spacing-sm;
  background: $color-bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-card;
}

.action-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-round;
  display: flex;
  align-items: center;
  justify-content: center;

  &--primary { background: $color-primary-light; }
  &--warning { background: $color-warning-light; }
  &--purple  { background: $color-purple-light; }
}

.action-icon-text { font-size: 36rpx; }

.action-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

/* ─── Section header ─── */
.section-head {
  display: flex;
  align-items: center;
  padding: $spacing-2xl $spacing-page $spacing-sm;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

/* ─── Subject pills ─── */
.pills {
  white-space: nowrap;
  padding: 0 $spacing-page $spacing-sm;
}

.pill {
  display: inline-block;
  padding: $spacing-sm $spacing-lg;
  border-radius: $radius-pill;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  background: $color-bg-card;
  border: 2rpx solid $color-border;
  margin-right: $spacing-sm;
  transition: all $duration-normal $ease-default;

  &--active {
    background: $color-primary;
    color: #fff;
    border-color: $color-primary;
  }
}

/* ─── Two-column tutor grid ─── */
.tutor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
  padding: 0 $spacing-page;
}

.grid-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg $spacing-md;
  text-align: center;
  box-shadow: $shadow-card;
  animation: slideUp 0.3s $ease-default both;
}

.grid-avatar-wrap {
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto $spacing-md;
  border-radius: $radius-round;
  background: $color-bg-input;
  display: flex;
  align-items: center;
  justify-content: center;
}

.grid-name {
  font-size: $font-size-md;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  display: block;
  margin-bottom: 4rpx;
}

.grid-rating {
  font-size: $font-size-sm;
  color: $color-warning;
  display: block;
  margin-bottom: $spacing-xs;
}

.grid-rating-count {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

.grid-school {
  font-size: $font-size-sm;
  font-weight: $font-weight-light;
  color: $color-text-secondary;
  display: block;
  margin-bottom: $spacing-sm;
}

.grid-price {
  font-size: $font-size-md;
  font-weight: $font-weight-bold;
  color: $color-primary;
  display: block;
}

/* ─── Student section ─── */
.section { padding: 0 $spacing-page; margin-top: $spacing-2xl; }

.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.order-title { font-size: $font-size-md; font-weight: $font-weight-bold; color: $color-text-primary; flex: 1; }
.order-rate { font-size: $font-size-base; color: $color-primary; font-weight: $font-weight-bold; }
.order-tags { display: flex; flex-wrap: wrap; gap: $spacing-xs; margin-bottom: $spacing-sm; }
.order-time { font-size: $font-size-xs; color: $color-text-placeholder; }

.cert-tip { display: flex; flex-direction: column; align-items: center; }
.cert-tip-text { font-size: $font-size-base; color: $color-text-secondary; font-weight: $font-weight-light; }

.no-more { text-align: center; padding: $spacing-md; color: $color-text-placeholder; font-size: $font-size-sm; }

/* ─── Feed loading states ─── */

/* 首次加载：居中 */
.feed-loading-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 0;
}

.feed-loading-text {
  margin-top: $spacing-md;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.feed-spinner-lg {
  width: 56rpx;
  height: 56rpx;
  border: 5rpx solid $color-border;
  border-top-color: $color-primary;
  border-radius: $radius-round;
  animation: tl-spin 0.8s linear infinite;
}

/* 翻页加载：底部内联 */
.feed-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-xl 0 calc(env(safe-area-inset-bottom) + 140rpx);
}

.feed-footer-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.feed-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid $color-border;
  border-top-color: $color-primary;
  border-radius: $radius-round;
  animation: tl-spin 0.8s linear infinite;
}
</style>
