<template>
  <div class="order-analytics">
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
        <div v-loading="loading.orderTrend">
          <TrendChart
            title="订单趋势"
            :labels="orderTrendLabels"
            :series="orderTrendSeries"
          />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.orderStatus">
          <PieChart
            title="订单状态分布"
            :data="orderStatusData"
          />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.teachingMode">
          <PieChart
            title="教学模式分布"
            :data="teachingModeData"
          />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="24">
        <div v-loading="loading.subject">
          <BarChart
            title="科目热度"
            :labels="subjectLabels"
            :data="subjectData"
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
import PieChart from '../../components/PieChart.vue'
import BarChart from '../../components/BarChart.vue'
import {
  getOrderTrend,
  getOrderStatusDistribution,
  getSubjectPopularity,
  getTeachingModeDistribution
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

const ORDER_STATUS_MAP = {
  1: '待确认',
  2: '已确认',
  3: '已支付',
  4: '进行中',
  5: '已完成',
  6: '已取消',
  7: '退款中',
  8: '已退款',
  9: '争议中'
}

const TEACHING_MODE_MAP = {
  1: '线上',
  2: '线下',
  3: '不限'
}

const loading = ref({
  orderTrend: false,
  orderStatus: false,
  subject: false,
  teachingMode: false
})

const orderTrendLabels = ref([])
const orderTrendSeries = ref([])
const orderStatusData = ref([])
const subjectLabels = ref([])
const subjectData = ref([])
const teachingModeData = ref([])

function buildParams() {
  return {
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }
}

async function fetchOrderTrend() {
  loading.value.orderTrend = true
  try {
    const res = await getOrderTrend({ ...buildParams(), granularity: 'day' })
    const counts = res.data.counts || []
    const amounts = res.data.amounts || []
    orderTrendLabels.value = res.data.labels || []
    orderTrendSeries.value = [
      {
        name: '订单量',
        data: counts,
        color: '#409eff'
      },
      {
        name: '收入 (元)',
        data: amounts.map(a => a / 100),
        color: '#67c23a'
      }
    ]
  } finally {
    loading.value.orderTrend = false
  }
}

async function fetchOrderStatus() {
  loading.value.orderStatus = true
  try {
    const res = await getOrderStatusDistribution(buildParams())
    orderStatusData.value = (res.data.items || []).map(item => ({
      name: ORDER_STATUS_MAP[item.label] || item.label,
      value: item.count
    }))
  } finally {
    loading.value.orderStatus = false
  }
}

async function fetchSubject() {
  loading.value.subject = true
  try {
    const res = await getSubjectPopularity(buildParams())
    const items = res.data.items || []
    subjectLabels.value = items.map(item => item.label)
    subjectData.value = items.map(item => item.count)
  } finally {
    loading.value.subject = false
  }
}

async function fetchTeachingMode() {
  loading.value.teachingMode = true
  try {
    const res = await getTeachingModeDistribution(buildParams())
    teachingModeData.value = (res.data.items || []).map(item => ({
      name: TEACHING_MODE_MAP[item.label] || item.label,
      value: item.count
    }))
  } finally {
    loading.value.teachingMode = false
  }
}

function fetchAll() {
  fetchOrderTrend()
  fetchOrderStatus()
  fetchSubject()
  fetchTeachingMode()
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
