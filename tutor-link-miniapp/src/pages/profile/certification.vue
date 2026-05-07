<template>
  <view class="page">
    <TlCard>
      <text class="form-title">学生身份认证</text>
      <text class="form-desc">上传学生证照片，审核通过后即可接单</text>

      <!-- 当前状态 -->
      <view v-if="certification" class="status-card">
        <TlStatusBadge :status="certification.status" type="cert" />
        <text v-if="certification.rejectReason" class="reject-reason">拒绝原因: {{ certification.rejectReason }}</text>
      </view>

      <!-- 表单 -->
      <view v-if="!certification || certification.status === 3">
        <TlFormInput label="真实姓名" v-model="form.realName" placeholder="请输入真实姓名" required />
        <TlFormInput label="学生证号" v-model="form.studentIdNo" placeholder="请输入学生证号" />

        <view class="upload-group">
          <text class="upload-label">学生证照片(正面)</text>
          <view class="upload-area" @tap="uploadPhoto('front')">
            <image v-if="form.photoFront" :src="form.photoFront" mode="aspectFill" class="preview" />
            <text v-else class="upload-text">+ 上传照片</text>
          </view>
        </view>

        <view class="upload-group">
          <text class="upload-label">学生证照片(反面)</text>
          <view class="upload-area" @tap="uploadPhoto('back')">
            <image v-if="form.photoBack" :src="form.photoBack" mode="aspectFill" class="preview" />
            <text v-else class="upload-text">+ 上传照片</text>
          </view>
        </view>

        <TlButton @tap="submitCertification" style="margin-top: 40rpx;">提交认证</TlButton>
      </view>
    </TlCard>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, post } from '../../api/request'
import { uploadFile } from '../../api/request'

const certification = ref(null)
const form = ref({
  realName: '',
  studentIdNo: '',
  photoFront: '',
  photoBack: ''
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

<style lang="scss" scoped>
.page {
  padding: $spacing-page;
}

.form-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  display: block;
  margin-bottom: $spacing-sm;
}

.form-desc {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  display: block;
  margin-bottom: $spacing-xl;
}

.status-card {
  background: $color-bg-input;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.reject-reason {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-top: $spacing-sm;
  display: block;
}

.upload-group {
  margin-bottom: $spacing-lg;
}

.upload-label {
  font-size: $font-size-base;
  color: $color-text-regular;
  display: block;
  margin-bottom: $spacing-sm;
}

.upload-area {
  width: 300rpx;
  height: 200rpx;
  background: $color-bg-input;
  border-radius: $radius-md;
  display: flex;
  justify-content: center;
  align-items: center;
}

.upload-text {
  color: $color-text-secondary;
  font-size: $font-size-base;
}

.preview {
  width: 300rpx;
  height: 200rpx;
  border-radius: $radius-md;
}
</style>
