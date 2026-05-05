<template>
  <div class="dashboard-container">
    <!-- KPI Cards -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="8" :md="4">
        <StatCard
          title="用户总数"
          :value="stats.totalUsers"
          icon="User"
          color="#409eff"
        />
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <StatCard
          title="导师总数"
          :value="stats.totalTutors"
          icon="UserFilled"
          color="#67c23a"
        />
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <StatCard
          title="订单总数"
          :value="stats.totalOrders"
          icon="Document"
          color="#e6a23c"
        />
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <StatCard
          title="总营收(元)"
          :value="formatYuan(stats.totalRevenue)"
          icon="Money"
          color="#f56c6c"
        />
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <StatCard
          title="今日新增用户"
          :value="stats.todayNewUsers"
          icon="Plus"
          color="#909399"
        />
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <StatCard
          title="今日新增订单"
          :value="stats.todayNewOrders"
          icon="Tickets"
          color="#b37feb"
        />
      </el-col>
    </el-row>

    <!-- User Growth Trend -->
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">用户增长趋势</span>
              <el-radio-group v-model="userTrendPeriod" size="small" @change="fetchUserTrend">
                <el-radio-button label="daily">日</el-radio-button>
                <el-radio-button label="weekly">周</el-radio-button>
                <el-radio-button label="monthly">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="userTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Order Trend -->
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">订单趋势</span>
              <el-radio-group v-model="orderTrendPeriod" size="small" @change="fetchOrderTrend">
                <el-radio-button label="daily">日</el-radio-button>
                <el-radio-button label="weekly">周</el-radio-button>
                <el-radio-button label="monthly">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="orderTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Pie Charts -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <PieChart
          title="订单状态分布"
          :data="orderStatusData"
          height="350px"
        />
      </el-col>
      <el-col :xs="24" :md="12">
        <PieChart
          title="科目热度排行"
          :data="subjectPopularityData"
          height="350px"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import StatCard from '../../components/StatCard.vue'
import PieChart from '../../components/PieChart.vue'
import { getDetailedStats } from '../../api/dashboard'
import {
  getUserTrend,
  getOrderTrend,
  getOrderStatusDistribution,
  getSubjectPopularity
} from '../../api/analytics'

// --- Format helper ---
const formatYuan = (cents) => {
  if (cents == null) return '0.00'
  return (cents / 100).toFixed(2)
}

// --- Stats ---
const stats = reactive({
  totalUsers: 0,
  totalTutors: 0,
  totalOrders: 0,
  totalRevenue: 0,
  todayNewUsers: 0,
  todayNewOrders: 0
})

async function fetchStats() {
  try {
    const res = await getDetailedStats()
    const data = res.data?.data || res.data || {}
    stats.totalUsers = data.totalUsers ?? 0
    stats.totalTutors = data.totalTutors ?? 0
    stats.totalOrders = data.totalOrders ?? 0
    stats.totalRevenue = data.totalRevenue ?? 0
    stats.todayNewUsers = data.todayNewUsers ?? 0
    stats.todayNewOrders = data.todayNewOrders ?? 0
  } catch (e) {
    console.error('Failed to fetch stats:', e)
  }
}

// --- Date range helper ---
function getDateRange(days = 30) {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days)
  const fmt = (d) => d.toISOString().slice(0, 10)
  return { startDate: fmt(start), endDate: fmt(end) }
}

// --- User Trend Chart ---
const userTrendChartRef = ref(null)
const userTrendPeriod = ref('daily')
let userTrendChart = null

function initUserTrendChart() {
  if (!userTrendChartRef.value) return
  if (userTrendChart) userTrendChart.dispose()
  userTrendChart = echarts.init(userTrendChartRef.value)
}

function renderUserTrendChart(labels, data) {
  if (!userTrendChart) return
  userTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: { color: '#409eff' },
        lineStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff40' },
            { offset: 1, color: '#409eff05' }
          ])
        }
      }
    ]
  }, true)
}

async function fetchUserTrend() {
  try {
    const { startDate, endDate } = getDateRange(30)
    const res = await getUserTrend({
      startDate,
      endDate,
      period: userTrendPeriod.value
    })
    const data = res.data?.data || res.data || { labels: [], values: [] }
    renderUserTrendChart(data.labels || [], data.values || [])
  } catch (e) {
    console.error('Failed to fetch user trend:', e)
  }
}

// --- Order Trend Chart ---
const orderTrendChartRef = ref(null)
const orderTrendPeriod = ref('daily')
let orderTrendChart = null

function initOrderTrendChart() {
  if (!orderTrendChartRef.value) return
  if (orderTrendChart) orderTrendChart.dispose()
  orderTrendChart = echarts.init(orderTrendChartRef.value)
}

function renderOrderTrendChart(labels, data) {
  if (!orderTrendChart) return
  orderTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: { color: '#e6a23c' },
        lineStyle: { color: '#e6a23c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e6a23c40' },
            { offset: 1, color: '#e6a23c05' }
          ])
        }
      }
    ]
  }, true)
}

async function fetchOrderTrend() {
  try {
    const { startDate, endDate } = getDateRange(30)
    const res = await getOrderTrend({
      startDate,
      endDate,
      period: orderTrendPeriod.value
    })
    const data = res.data?.data || res.data || { labels: [], values: [] }
    renderOrderTrendChart(data.labels || [], data.values || [])
  } catch (e) {
    console.error('Failed to fetch order trend:', e)
  }
}

// --- Pie Chart Data ---
const orderStatusData = ref([])
const subjectPopularityData = ref([])

async function fetchOrderStatusDistribution() {
  try {
    const { startDate, endDate } = getDateRange(30)
    const res = await getOrderStatusDistribution({ startDate, endDate })
    const data = res.data?.data || res.data || []
    orderStatusData.value = Array.isArray(data)
      ? data.map((item) => ({ name: item.name || item.status, value: item.value ?? item.count ?? 0 }))
      : []
  } catch (e) {
    console.error('Failed to fetch order status distribution:', e)
  }
}

async function fetchSubjectPopularity() {
  try {
    const { startDate, endDate } = getDateRange(30)
    const res = await getSubjectPopularity({ startDate, endDate })
    const data = res.data?.data || res.data || []
    subjectPopularityData.value = Array.isArray(data)
      ? data.map((item) => ({ name: item.name || item.subject, value: item.value ?? item.count ?? 0 }))
      : []
  } catch (e) {
    console.error('Failed to fetch subject popularity:', e)
  }
}

// --- Resize handler ---
function handleResize() {
  if (userTrendChart) userTrendChart.resize()
  if (orderTrendChart) orderTrendChart.resize()
}

// --- Lifecycle ---
onMounted(async () => {
  await nextTick()
  initUserTrendChart()
  initOrderTrendChart()

  window.addEventListener('resize', handleResize)

  fetchStats()
  fetchUserTrend()
  fetchOrderTrend()
  fetchOrderStatusDistribution()
  fetchSubjectPopularity()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (userTrendChart) {
    userTrendChart.dispose()
    userTrendChart = null
  }
  if (orderTrendChart) {
    orderTrendChart.dispose()
    orderTrendChart = null
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-row {
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>
