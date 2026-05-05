<template>
  <div class="tutor-analytics">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.rating">
          <BarChart
            title="评分分布"
            :labels="ratingLabels"
            :data="ratingData"
            color="#e6a23c"
          />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.certStatus">
          <PieChart
            title="认证状态分布"
            :data="certStatusData"
          />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.university">
          <BarChart
            title="大学分布 (Top 15)"
            :labels="universityLabels"
            :data="universityData"
            color="#409eff"
          />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.hourlyRate">
          <BarChart
            title="时薪分布"
            :labels="hourlyRateLabels"
            :data="hourlyRateData"
            color="#909399"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PieChart from '../../components/PieChart.vue'
import BarChart from '../../components/BarChart.vue'
import {
  getTutorRatingDistribution,
  getUniversityDistribution,
  getCertificationStatusDistribution,
  getHourlyRateDistribution
} from '../../api/analytics'

const CERT_STATUS_MAP = {
  0: '未认证',
  1: '审核中',
  2: '已通过',
  3: '已拒绝'
}

const loading = ref({
  rating: false,
  university: false,
  certStatus: false,
  hourlyRate: false
})

const ratingLabels = ref([])
const ratingData = ref([])
const universityLabels = ref([])
const universityData = ref([])
const certStatusData = ref([])
const hourlyRateLabels = ref([])
const hourlyRateData = ref([])

async function fetchRating() {
  loading.value.rating = true
  try {
    const res = await getTutorRatingDistribution()
    const items = res.data.items || []
    ratingLabels.value = items.map(item => item.label)
    ratingData.value = items.map(item => item.count)
  } finally {
    loading.value.rating = false
  }
}

async function fetchUniversity() {
  loading.value.university = true
  try {
    const res = await getUniversityDistribution()
    const items = (res.data.items || []).slice(0, 15)
    universityLabels.value = items.map(item => item.label)
    universityData.value = items.map(item => item.count)
  } finally {
    loading.value.university = false
  }
}

async function fetchCertStatus() {
  loading.value.certStatus = true
  try {
    const res = await getCertificationStatusDistribution()
    certStatusData.value = (res.data.items || []).map(item => ({
      name: CERT_STATUS_MAP[item.label] || item.label,
      value: item.count
    }))
  } finally {
    loading.value.certStatus = false
  }
}

async function fetchHourlyRate() {
  loading.value.hourlyRate = true
  try {
    const res = await getHourlyRateDistribution()
    const items = res.data.items || []
    hourlyRateLabels.value = items.map(item => item.label)
    hourlyRateData.value = items.map(item => item.count)
  } finally {
    loading.value.hourlyRate = false
  }
}

function fetchAll() {
  fetchRating()
  fetchUniversity()
  fetchCertStatus()
  fetchHourlyRate()
}

onMounted(() => {
  fetchAll()
})
</script>

<style scoped>
</style>
