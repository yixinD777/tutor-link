<template>
  <view class="page">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <picker :range="subjectNames" @change="onSubjectChange">
        <view class="filter-item">{{ selectedSubject || '科目' }}</view>
      </picker>
      <picker :range="sortOptions" @change="onSortChange">
        <view class="filter-item">{{ currentSort || '排序' }}</view>
      </picker>
    </view>

    <!-- 家教列表 -->
    <scroll-view scroll-y class="tutor-scroll" @scrolltolower="loadMore">
      <view v-if="tutors.length === 0 && !loading" class="empty">
        <text>暂无符合条件的家教</text>
      </view>
      <view class="tutor-card" v-for="tutor in tutors" :key="tutor.id" @tap="goDetail(tutor.userId)">
        <view class="card-header">
          <text class="university">{{ tutor.university || '大学生家教' }}</text>
          <view class="rating-badge">
            <text class="rating-text">{{ tutor.avgRating || '新' }}</text>
          </view>
        </view>
        <text class="info-line">{{ tutor.major }} · {{ tutor.educationLevelDesc || '本科' }}</text>
        <text class="info-line">{{ formatRate(tutor.hourlyRateMin, tutor.hourlyRateMax) }}</text>
        <text class="info-line location">{{ tutor.city || '' }} {{ tutor.district || '' }}</text>
      </view>
    </scroll-view>

    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { searchTutors } from '../../api/tutor'
import { get } from '../../api/request'

const tutors = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const selectedSubject = ref('')
const currentSort = ref('评分优先')
const subjectNames = ref([])
const subjectIds = ref([])
const sortOptions = ['评分优先', '价格从低到高', '价格从高到低', '订单最多']
const sortValues = ['rating', 'price_asc', 'price_desc', 'order_count']
const currentSortIndex = ref(0)

onMounted(() => {
  loadSubjects()
  loadTutors()
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
    const params = {
      page: page.value,
      size: 20,
      sortBy: sortValues[currentSortIndex.value]
    }
    if (selectedSubject.value && selectedSubject.value !== '全部') {
      const idx = subjectNames.value.indexOf(selectedSubject.value)
      if (idx > 0) params.subjectId = subjectIds.value[idx]
    }
    const res = await searchTutors(params)
    tutors.value = reset ? (res.records || []) : [...tutors.value, ...(res.records || [])]
    hasMore.value = tutors.value.length < res.total
    page.value++
  } catch (e) { console.error(e) }
  loading.value = false
}

function loadMore() { loadTutors() }

function onSubjectChange(e) {
  selectedSubject.value = subjectNames.value[e.detail.value]
  loadTutors(true)
}

function onSortChange(e) {
  currentSortIndex.value = e.detail.value
  currentSort.value = sortOptions[e.detail.value]
  loadTutors(true)
}

function formatRate(min, max) {
  if (!min && !max) return '价格面议'
  if (min && max) return `¥${min / 100}-${max / 100}/时`
  return `¥${(min || max) / 100}/时`
}

function goDetail(userId) {
  uni.navigateTo({ url: `/pages/tutor/detail?userId=${userId}` })
}
</script>

<style scoped>
.page { height: 100vh; display: flex; flex-direction: column; }
.filter-bar { display: flex; padding: 20rpx; background: #fff; gap: 20rpx; }
.filter-item { padding: 12rpx 24rpx; background: #f5f5f5; border-radius: 24rpx; font-size: 26rpx; color: #666; }
.tutor-scroll { flex: 1; padding: 20rpx; }
.tutor-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.university { font-size: 32rpx; font-weight: bold; }
.rating-badge { background: #FFF3E0; padding: 4rpx 16rpx; border-radius: 12rpx; }
.rating-text { color: #FF9500; font-size: 24rpx; font-weight: bold; }
.info-line { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.location { color: #999; }
.empty { text-align: center; padding: 100rpx; color: #999; }
.loading { text-align: center; padding: 20rpx; color: #999; font-size: 24rpx; }
</style>
