<template>
  <div class="review-analytics">
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
        <div v-loading="loading.ratingTrend">
          <TrendChart
            title="评分趋势"
            :labels="ratingTrendLabels"
            :series="ratingTrendSeries"
          />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="24">
        <div v-loading="loading.ratingDistribution">
          <BarChart
            title="评分分布"
            :labels="ratingDistLabels"
            :data="ratingDistData"
            color="#e6a23c"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import TrendChart from '../../components/TrendChart.vue'
import BarChart from '../../components/BarChart.vue'
import {
  getReviewRatingTrend,
  getReviewRatingDistribution
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

const RATING_MAP = {
  1: '1星',
  2: '2星',
  3: '3星',
  4: '4星',
  5: '5星'
}

const loading = ref({
  ratingTrend: false,
  ratingDistribution: false
})

const ratingTrendLabels = ref([])
const ratingTrendSeries = ref([])
const ratingDistLabels = ref([])
const ratingDistData = ref([])

function buildParams() {
  return {
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }
}

async function fetchRatingTrend() {
  loading.value.ratingTrend = true
  try {
    const res = await getReviewRatingTrend({ ...buildParams(), granularity: 'day' })
    const amounts = res.data.amounts || []
    ratingTrendLabels.value = res.data.labels || []
    ratingTrendSeries.value = [
      {
        name: '平均评分',
        data: amounts,
        color: '#e6a23c'
      }
    ]
  } finally {
    loading.value.ratingTrend = false
  }
}

async function fetchRatingDistribution() {
  loading.value.ratingDistribution = true
  try {
    const res = await getReviewRatingDistribution(buildParams())
    const items = res.data.items || []
    ratingDistLabels.value = items.map(item => RATING_MAP[item.label] || item.label)
    ratingDistData.value = items.map(item => item.count)
  } finally {
    loading.value.ratingDistribution = false
  }
}

function fetchAll() {
  fetchRatingTrend()
  fetchRatingDistribution()
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
