<template>
  <view class="page" v-if="tutor">
    <!-- Premium Hero — blue bg + glow drift -->
    <view class="hero">
      <view class="hero-avatar-wrap">
        <TlAvatar :src="tutor.avatarUrl" size="large" :name="tutor.realName || tutor.university" />
      </view>
      <text class="hero-name">{{ tutor.realName || tutor.university || '大学生家教' }}</text>
      <text class="hero-meta">{{ tutor.university }} · {{ tutor.major }}</text>
      <view class="hero-rating">
        <TlRating :value="tutor.avgRating || 0" />
        <text class="hero-count">{{ tutor.ratingCount || 0 }}评价 · {{ tutor.orderCount || 0 }}订单</text>
      </view>
    </view>

    <view class="content-area">
      <TlCard>
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
            <TlStatusBadge :status="tutor.certificationStatus" type="cert" />
          </view>
        </view>
      </TlCard>

      <TlCard>
        <text class="section-label">教学科目</text>
        <view class="subject-tags" v-if="subjects.length">
          <TlTag v-for="ts in subjects" :key="ts.id" type="subject" :text="subjectName(ts.subjectId) + (ts.gradeRange ? ' · ' + ts.gradeRange : ' · 全科')" />
        </view>
        <text v-else class="empty-text">暂未设置科目</text>
      </TlCard>

      <TlCard>
        <text class="section-label">自我介绍</text>
        <text class="content">{{ tutor.intro || '暂未填写' }}</text>
      </TlCard>

      <TlCard>
        <text class="section-label">教学风格</text>
        <text class="content">{{ tutor.teachingStyle || '暂未填写' }}</text>
      </TlCard>
    </view>

    <TlActionBar>
      <TlButton type="outline" @tap="goChat">在线沟通</TlButton>
      <TlButton type="primary" @tap="goTrialLesson">预约试课</TlButton>
    </TlActionBar>
  </view>

  <TlLoading v-else mode="skeleton" :rows="6" />
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTutorDetail, getTutorSubjects } from '../../api/tutor'
import { get } from '../../api/request'
import { formatRate, formatEdu } from '../../utils/formatters'

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
    const [profile, subs] = await Promise.all([getTutorDetail(userId.value), getTutorSubjects(userId.value)])
    tutor.value = profile
    subjects.value = subs || []
  } catch (e) { uni.showToast({ title: '加载失败', icon: 'none' }) }
}

async function loadSubjectList() {
  try { subjectList.value = await get('/subjects') } catch (e) { console.error(e) }
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

function goChat() { uni.navigateTo({ url: `/pages/chat/detail?otherUserId=${userId.value}` }) }
function goTrialLesson() { uni.navigateTo({ url: `/pages/trial/create?tutorUserId=${userId.value}` }) }
</script>

<style lang="scss" scoped>
.page {
  background: $color-bg-page;
  min-height: 100vh;
  padding-bottom: 160rpx;
  animation: fadeIn 0.2s $ease-default;
}

.hero {
  background: $color-primary;
  @include hero-glow;
  padding: $spacing-4xl $spacing-page $spacing-3xl;
  text-align: center;
  border-radius: 0 0 $radius-2xl $radius-2xl;
}

.hero-avatar-wrap {
  width: 140rpx;
  height: 140rpx;
  margin: 0 auto $spacing-md;
  border-radius: $radius-round;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: $z-index-normal;
}

.hero-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: #fff;
  display: block;
  margin-bottom: $spacing-xs;
  position: relative;
  z-index: $z-index-normal;
}

.hero-meta {
  font-size: $font-size-sm;
  font-weight: $font-weight-light;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: $spacing-sm;
  position: relative;
  z-index: $z-index-normal;
}

.hero-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  position: relative;
  z-index: $z-index-normal;
}

.hero-count {
  font-size: $font-size-xs;
  font-weight: $font-weight-light;
  color: rgba(255, 255, 255, 0.6);
}

.content-area {
  padding: $spacing-lg $spacing-page;
  margin-top: -$spacing-md;
  position: relative;
  z-index: $z-index-sticky;
}

.info-grid { display: flex; flex-wrap: wrap; }
.grid-item { width: 50%; margin-bottom: $spacing-md; }
.grid-label {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  font-weight: $font-weight-regular;
  display: block;
  margin-bottom: 4rpx;
}
.grid-value {
  font-size: $font-size-base;
  color: $color-text-regular;
  font-weight: $font-weight-medium;
  &.price { color: $color-primary; font-weight: $font-weight-bold; }
}
.section-label {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  display: block;
  margin-bottom: $spacing-sm;
  color: $color-text-primary;
}
.subject-tags { display: flex; flex-wrap: wrap; gap: $spacing-sm; }
.content { font-size: $font-size-base; color: $color-text-secondary; line-height: $line-height-loose; font-weight: $font-weight-light; }
.empty-text { font-size: $font-size-sm; color: $color-text-placeholder; font-weight: $font-weight-light; }
</style>
