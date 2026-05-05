<template>
  <div class="user-analytics">
    <div class="date-picker-wrapper">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        :shortcuts="shortcuts"
        @change="fetchAll"
      />
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="24">
        <div v-loading="loading.userTrend">
          <TrendChart
            title="用户注册趋势"
            :labels="userTrendLabels"
            :series="userTrendSeries"
          />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.gender">
          <PieChart
            title="性别分布"
            :data="genderData"
          />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.region">
          <BarChart
            title="地区分布 (Top 10)"
            :labels="regionLabels"
            :data="regionData"
            color="#67c23a"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import TrendChart from '../../components/TrendChart.vue'
import PieChart from '../../components/PieChart.vue'
import BarChart from '../../components/BarChart.vue'
import {
  getUserTrend,
  getGenderDistribution,
  getRegionDistribution
} from '../../api/analytics'

const now = new Date()
const defaultStart = new Date(now - 90 * 24 * 3600 * 1000)
const dateRange = ref([
  defaultStart.toISOString().slice(0, 10),
  now.toISOString().slice(0, 10)
])

const shortcuts = [
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date(end - 7 * 24 * 3600 * 1000)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date(end - 30 * 24 * 3600 * 1000)
      return [start, end]
    }
  },
  {
    text: '最近90天',
    value: () => {
      const end = new Date()
      const start = new Date(end - 90 * 24 * 3600 * 1000)
      return [start, end]
    }
  }
]

const loading = ref({
  userTrend: false,
  gender: false,
  region: false
})

const userTrendLabels = ref([])
const userTrendSeries = ref([])
const genderData = ref([])
const regionLabels = ref([])
const regionData = ref([])

function buildParams() {
  return {
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }
}

async function fetchUserTrend() {
  loading.value.userTrend = true
  try {
    const res = await getUserTrend({ ...buildParams(), granularity: 'day' })
    userTrendLabels.value = res.data.labels || []
    userTrendSeries.value = [
      {
        name: '注册用户数',
        data: res.data.counts || [],
        color: '#409eff'
      }
    ]
  } finally {
    loading.value.userTrend = false
  }
}

async function fetchGender() {
  loading.value.gender = true
  try {
    const res = await getGenderDistribution(buildParams())
    genderData.value = (res.data.items || []).map(item => ({
      name: item.label,
      value: item.count
    }))
  } finally {
    loading.value.gender = false
  }
}

async function fetchRegion() {
  loading.value.region = true
  try {
    const res = await getRegionDistribution(buildParams())
    const items = (res.data.items || []).slice(0, 10)
    regionLabels.value = items.map(item => item.label)
    regionData.value = items.map(item => item.count)
  } finally {
    loading.value.region = false
  }
}

function fetchAll() {
  fetchUserTrend()
  fetchGender()
  fetchRegion()
}

onMounted(() => {
  fetchAll()
})
</script>

<style scoped>
.date-picker-wrapper {
  margin-bottom: 20px;
}
</style>
