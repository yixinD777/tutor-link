<template>
  <view class="page">
    <!-- 搜索栏 -->
    <TlSearchBar v-model="keyword" placeholder="搜索学校、专业、科目..." @search="onSearch" @clear="clearSearch" />

    <!-- 筛选栏 -->
    <scroll-view scroll-x class="filter-bar">
      <picker :range="subjectNames" @change="onSubjectChange">
        <view :class="['filter-pill', { 'filter-pill--active': selectedSubject && selectedSubject !== '全部' }]">
          {{ selectedSubject || '科目' }} ▾
        </view>
      </picker>
      <picker :range="sortOptions" @change="onSortChange">
        <view :class="['filter-pill', { 'filter-pill--active': currentSortIndex !== 0 }]">
          {{ currentSort }} ▾
        </view>
      </picker>
    </scroll-view>

    <!-- 双列家教网格 -->
    <scroll-view scroll-y class="tutor-scroll" @scrolltolower="loadMore">
      <TlEmpty v-if="tutors.length === 0 && !loading" icon="🔍" text="暂无符合条件的家教" />

      <view class="tutor-grid">
        <view class="grid-card" v-for="tutor in tutors" :key="tutor.id" @tap="goDetail(tutor.userId)">
          <view class="grid-avatar-wrap">
            <TlAvatar :src="tutor.avatarUrl" size="medium" :name="tutor.nickname" />
          </view>
          <text class="grid-name">{{ tutor.nickname || '家教老师' }}</text>
          <text class="grid-rating">★ {{ tutor.avgRating ? Number(tutor.avgRating).toFixed(1) : '新' }}</text>
          <text class="grid-school">{{ tutor.university || '' }}{{ tutor.major ? ' · ' + tutor.major : '' }}</text>
          <text class="grid-price">{{ formatRate(tutor.hourlyRateMin, tutor.hourlyRateMax) }}</text>
        </view>
      </view>

      <TlLoading v-if="loading" text="加载中..." />
      <view v-else-if="!hasMore && tutors.length > 0" class="no-more"><text>没有更多了</text></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { searchTutors } from '../../api/tutor'
import { get } from '../../api/request'
import { formatRate } from '../../utils/formatters'

const tutors = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const keyword = ref('')
const selectedSubject = ref('')
const currentSort = ref('评分优先')
const subjectNames = ref([])
const subjectIds = ref([])
const sortOptions = ['评分优先', '价格从低到高', '价格从高到低', '订单最多']
const sortValues = ['rating', 'price_asc', 'price_desc', 'order_count']
const currentSortIndex = ref(0)

let searchTimer = null

onShow(() => {
  if (subjectNames.value.length === 0) loadSubjects()
  if (tutors.value.length === 0) loadTutors(true)
})

// 接收首页搜索框传来的关键词
uni.$on('tutor-search', (data) => {
  if (data?.keyword) {
    keyword.value = data.keyword
    loadTutors(true)
  }
})

async function loadSubjects() {
  try {
    const data = await get('/subjects')
    subjectNames.value = ['全部', ...data.map(s => s.name)]
    subjectIds.value = [null, ...data.map(s => s.id)]
  } catch (e) { console.error(e) }
}

async function loadTutors(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  if (reset) { page.value = 1; tutors.value = []; hasMore.value = true }

  loading.value = true
  try {
    const params = { page: page.value, size: 20, sortBy: sortValues[currentSortIndex.value] }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (selectedSubject.value && selectedSubject.value !== '全部') {
      const idx = subjectNames.value.indexOf(selectedSubject.value)
      if (idx > 0) params.subjectId = subjectIds.value[idx]
    }
    const res = await searchTutors(params)
    const records = res.records || []
    tutors.value = reset ? records : [...tutors.value, ...records]
    hasMore.value = tutors.value.length < (res.total || 0)
    page.value++
  } catch (e) { console.error(e) }
  loading.value = false
}

function loadMore() { loadTutors() }

function onSearch() { loadTutors(true) }

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadTutors(true), 500)
}

function clearSearch() {
  keyword.value = ''
  loadTutors(true)
}

function onSubjectChange(e) {
  selectedSubject.value = subjectNames.value[e.detail.value]
  loadTutors(true)
}

function onSortChange(e) {
  currentSortIndex.value = Number(e.detail.value)
  currentSort.value = sortOptions[currentSortIndex.value]
  loadTutors(true)
}

function goDetail(userId) {
  uni.navigateTo({ url: `/pages/tutor/detail?userId=${userId}` })
}
</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
}

.filter-bar {
  white-space: nowrap;
  padding: $spacing-sm $spacing-page;
  background: $color-bg-card;
  border-bottom: 1rpx solid $color-border;
}

.filter-pill {
  display: inline-block;
  padding: $spacing-sm $spacing-lg;
  background: $color-bg-input;
  border-radius: $radius-pill;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-right: $spacing-sm;
  font-weight: $font-weight-regular;
  transition: all $duration-normal $ease-default;

  &--active {
    background: $color-primary;
    color: #fff;
  }
}

.tutor-scroll {
  flex: 1;
  padding: $spacing-sm $spacing-page 0;
}

.tutor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.grid-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg $spacing-md;
  text-align: center;
  box-shadow: $shadow-card;
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

.grid-school {
  font-size: $font-size-sm;
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

.no-more {
  text-align: center;
  padding: $spacing-md 0 $spacing-2xl;
  color: $color-text-placeholder;
  font-size: $font-size-sm;
}
</style>
