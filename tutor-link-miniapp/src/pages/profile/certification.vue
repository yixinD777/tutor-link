<template>
  <view class="page">
    <view class="form-card">
      <text class="form-title">学生身份认证</text>
      <text class="form-desc">上传学生证照片，审核通过后即可接单</text>

      <!-- 当前状态 -->
      <view v-if="certification" class="status-card">
        <text class="status-text" :class="statusClass">{{ statusText }}</text>
        <text v-if="certification.rejectReason" class="reject-reason">拒绝原因: {{ certification.rejectReason }}</text>
      </view>

      <!-- 表单 -->
      <view v-if="!certification || certification.status === 3">
        <view class="form-group">
          <text class="label">真实姓名</text>
          <input class="input" v-model="form.realName" placeholder="请输入真实姓名" />
        </view>
        <view class="form-group">
          <text class="label">学生证号</text>
          <input class="input" v-model="form.studentIdNo" placeholder="请输入学生证号" />
        </view>
        <view class="form-group">
          <text class="label">学生证照片(正面)</text>
          <view class="upload-area" @tap="uploadPhoto('front')">
            <image v-if="form.photoFront" :src="form.photoFront" mode="aspectFill" class="preview" />
            <text v-else class="upload-text">+ 上传照片</text>
          </view>
        </view>
        <view class="form-group">
          <text class="label">学生证照片(反面)</text>
          <view class="upload-area" @tap="uploadPhoto('back')">
            <image v-if="form.photoBack" :src="form.photoBack" mode="aspectFill" class="preview" />
            <text v-else class="upload-text">+ 上传照片</text>
          </view>
        </view>

        <button class="submit-btn" @tap="submitCertification">提交认证</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, post } from '../../api/request'
import { uploadFile } from '../../api/request'

const certification = ref(null)
const form = ref({
  realName: '',
  studentIdNo: '',
  photoFront: '',
  photoBack: ''
})

const statusText = computed(() => {
  if (!certification.value) return ''
  const map = { 1: '审核中', 2: '已认证', 3: '认证未通过' }
  return map[certification.value.status] || ''
})

const statusClass = computed(() => {
  if (!certification.value) return ''
  const map = { 1: 'pending', 2: 'approved', 3: 'rejected' }
  return map[certification.value.status] || ''
})

onMounted(() => { loadCertification() })

async function loadCertification() {
  try {
    certification.value = await get('/certifications/me')
  } catch (e) { /* 404 = no certification yet */ }
}

async function uploadPhoto(type) {
  try {
    const res = await new Promise((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: resolve,
        fail: reject
      })
    })
    const url = await uploadFile(res.tempFilePaths[0])
    if (type === 'front') form.value.photoFront = url
    else form.value.photoBack = url
  } catch (e) {
    uni.showToast({ title: '上传失败', icon: 'none' })
  }
}

async function submitCertification() {
  if (!form.value.realName || !form.value.photoFront) {
    uni.showToast({ title: '请填写必要信息', icon: 'none' })
    return
  }
  try {
    await post('/certifications/apply', null, {
      params: {
        realName: form.value.realName,
        studentIdNo: form.value.studentIdNo,
        photoFront: form.value.photoFront,
        photoBack: form.value.photoBack
      }
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    loadCertification()
  } catch (e) {
    console.error('Submit certification failed', e)
  }
}
</script>

<style scoped>
.page { padding: 20rpx; }
.form-card { background: #fff; border-radius: 16rpx; padding: 30rpx; }
.form-title { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 12rpx; }
.form-desc { font-size: 26rpx; color: #999; display: block; margin-bottom: 30rpx; }
.status-card { background: #f5f5f5; border-radius: 12rpx; padding: 24rpx; margin-bottom: 30rpx; }
.status-text { font-size: 30rpx; font-weight: bold; display: block; }
.status-text.pending { color: #FF9500; }
.status-text.approved { color: #34C759; }
.status-text.rejected { color: #FF3B30; }
.reject-reason { font-size: 24rpx; color: #999; margin-top: 10rpx; display: block; }
.form-group { margin-bottom: 24rpx; }
.label { font-size: 28rpx; color: #333; display: block; margin-bottom: 12rpx; }
.input { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; }
.upload-area { width: 300rpx; height: 200rpx; background: #f5f5f5; border-radius: 12rpx; display: flex; justify-content: center; align-items: center; }
.upload-text { color: #999; font-size: 28rpx; }
.preview { width: 300rpx; height: 200rpx; border-radius: 12rpx; }
.submit-btn { background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-top: 40rpx; }
</style>
