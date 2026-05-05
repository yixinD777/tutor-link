<template>
  <view class="page" v-if="tutor">
    <!-- 头部信息 -->
    <view class="profile-header">
      <image class="avatar" :src="tutor.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="basic-info">
        <text class="name">{{ tutor.realName || tutor.university || '大学生家教' }}</text>
        <text class="meta">{{ tutor.university }} · {{ tutor.major }}</text>
        <text class="meta-sub">{{ formatEdu(tutor.educationLevel) }} · {{ tutor.enrollmentYear ? tutor.enrollmentYear + '级' : '' }}</text>
        <view class="rating-row">
          <text class="stars">{{ formatRating(tutor.avgRating) }}</text>
          <text class="count">{{ tutor.ratingCount || 0 }}评价 · {{ tutor.orderCount || 0 }}订单</text>
        </view>
      </view>
    </view>

    <!-- 基本信息 -->
    <view class="info-card">
      <view class="info-grid">
        <view class="grid-item">
          <text class="grid-label">时薪</text>
          <text class="grid-value price">{{ formatRate(tutor.hourlyRateMin, tutor.hourlyRateMax) }}</text>
        </view>
        <view class="grid-item">
          <text class="grid-label">授课方式</text>
          <text class="grid-value">线下/线上</text>
        </view>
        <view class="grid-item" v-if="location">
          <text class="grid-label">所在地区</text>
          <text class="grid-value">{{ location }}</text>
        </view>
        <view class="grid-item">
          <text class="grid-label">认证状态</text>
          <text class="grid-value" :class="{ 'certified': tutor.certificationStatus === 2 }">
            {{ certText(tutor.certificationStatus) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 教学科目 -->
    <view class="info-card">
      <text class="label">教学科目</text>
      <view class="subject-tags" v-if="subjects.length">
        <view class="tag-item" v-for="ts in subjects" :key="ts.id">
          <text class="tag-name">{{ subjectName(ts.subjectId) }}</text>
          <text class="tag-grade">{{ ts.gradeRange || '全科' }}</text>
        </view>
      </view>
      <text v-else class="empty-text">暂未设置科目</text>
    </view>

    <!-- 自我介绍 -->
    <view class="info-card">
      <text class="label">自我介绍</text>
      <text class="content">{{ tutor.intro || '暂未填写' }}</text>
    </view>

    <!-- 教学风格 -->
    <view class="info-card">
      <text class="label">教学风格</text>
      <text class="content">{{ tutor.teachingStyle || '暂未填写' }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <button class="chat-btn" @tap="goChat">在线沟通</button>
      <button class="order-btn" @tap="goCreateOrder">预约试课</button>
    </view>
  </view>

  <view v-else class="page loading"><text>加载中...</text></view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTutorDetail, getTutorSubjects } from '../../api/tutor'
import { get } from '../../api/request'

const tutor = ref(null)
const subjects = ref([])
const subjectList = ref([])
const userId = ref('')

onLoad((options) => {
  userId.value = options.userId
  loadDetail()
  loadSubjectList()
})

async function loadDetail() {
  try {
    const [profile, subs] = await Promise.all([
      getTutorDetail(userId.value),
      getTutorSubjects(userId.value)
    ])
    tutor.value = profile
    subjects.value = subs || []
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

async function loadSubjectList() {
  try {
    subjectList.value = await get('/subjects')
  } catch (e) { console.error(e) }
}

const location = computed(() => {
  if (!tutor.value) return ''
  const parts = [tutor.value.province, tutor.value.city, tutor.value.district].filter(Boolean)
  return parts.length ? parts.join(' ') : ''
})

function subjectName(id) {
  const s = subjectList.value.find(item => item.id === id)
  return s ? s.name : '未知科目'
}

function formatRate(min, max) {
  if (!min && !max) return '价格面议'
  if (min && max) return `¥${min / 100}-${max / 100}/时`
  return `¥${(min || max) / 100}/时`
}

function formatEdu(level) {
  return { 1: '本科', 2: '硕士', 3: '博士' }[level] || '本科'
}

function certText(status) {
  return { 0: '未认证', 1: '审核中', 2: '已认证', 3: '未通过' }[status] || '未认证'
}

function formatRating(rating) {
  if (!rating || rating === 0) return '暂无评分'
  const full = Math.floor(rating)
  return '★'.repeat(full) + '☆'.repeat(5 - full) + ` ${rating}`
}

function goChat() { uni.navigateTo({ url: `/pages/chat/detail?otherUserId=${userId.value}` }) }
function goCreateOrder() { uni.navigateTo({ url: `/pages/order/create?tutorUserId=${userId.value}` }) }
</script>

<style scoped>
.page { padding: 20rpx; padding-bottom: 160rpx; }
.loading { text-align: center; padding: 100rpx; color: #999; }

.profile-header { display: flex; background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 20rpx; }
.avatar { width: 140rpx; height: 140rpx; border-radius: 50%; margin-right: 24rpx; border: 4rpx solid #E3F2FD; }
.basic-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.name { font-size: 36rpx; font-weight: bold; display: block; }
.meta { font-size: 26rpx; color: #666; margin-top: 6rpx; display: block; }
.meta-sub { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.rating-row { margin-top: 10rpx; display: flex; align-items: center; }
.stars { color: #FF9500; font-size: 26rpx; }
.count { font-size: 22rpx; color: #999; margin-left: 12rpx; }

.info-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.label { font-size: 28rpx; font-weight: bold; display: block; margin-bottom: 16rpx; }
.price { color: #FF6B35; font-weight: bold; }

.info-grid { display: flex; flex-wrap: wrap; }
.grid-item { width: 50%; margin-bottom: 16rpx; }
.grid-label { font-size: 24rpx; color: #999; display: block; margin-bottom: 4rpx; }
.grid-value { font-size: 28rpx; color: #333; }
.certified { color: #07C160; }

.subject-tags { display: flex; flex-wrap: wrap; gap: 12rpx; }
.tag-item { background: #E3F2FD; border-radius: 12rpx; padding: 12rpx 20rpx; display: flex; align-items: center; gap: 8rpx; }
.tag-name { font-size: 26rpx; color: #4A90D9; font-weight: bold; }
.tag-grade { font-size: 22rpx; color: #666; }

.content { font-size: 28rpx; color: #666; line-height: 1.8; }
.empty-text { font-size: 26rpx; color: #ccc; }

.action-bar { position: fixed; bottom: 0; left: 0; right: 0; display: flex; padding: 20rpx; background: #fff; box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05); gap: 20rpx; }
.chat-btn { flex: 1; background: #fff; color: #4A90D9; border: 2rpx solid #4A90D9; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
.order-btn { flex: 1; background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
</style>
