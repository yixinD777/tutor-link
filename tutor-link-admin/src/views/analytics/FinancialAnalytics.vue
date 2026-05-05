<template>
  <div class="financial-analytics">
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
        <div v-loading="loading.revenueTrend">
          <TrendChart
            title="收入趋势"
            :labels="revenueTrendLabels"
            :series="revenueTrendSeries"
          />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.paymentStatus">
          <PieChart
            title="支付状态分布"
            :data="paymentStatusData"
          />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div v-loading="loading.refundTrend">
          <TrendChart
            title="退款趋势"
            :labels="refundTrendLabels"
            :series="refundTrendSeries"
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
import {
  getRevenueTrend,
  getPaymentStatusDistribution,
  getRefundTrend
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

const PAYMENT_STATUS_MAP = {
  1: '待支付',
  2: '已支付',
  3: '已释放',
  4: '冻结中'
}

const loading = ref({
  revenueTrend: false,
  paymentStatus: false,
  refundTrend: false
})

const revenueTrendLabels = ref([])
const revenueTrendSeries = ref([])
const paymentStatusData = ref([])
const refundTrendLabels = ref([])
const refundTrendSeries = ref([])

function buildParams() {
  return {
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }
}

async function fetchRevenueTrend() {
  loading.value.revenueTrend = true
  try {
    const res = await getRevenueTrend({ ...buildParams(), granularity: 'day' })
    const amounts = res.data.amounts || []
    revenueTrendLabels.value = res.data.labels || []
    revenueTrendSeries.value = [
      {
        name: '收入 (元)',
        data: amounts.map(a => a / 100),
        color: '#67c23a'
      }
    ]
  } finally {
    loading.value.revenueTrend = false
  }
}

async function fetchPaymentStatus() {
  loading.value.paymentStatus = true
  try {
    const res = await getPaymentStatusDistribution(buildParams())
    paymentStatusData.value = (res.data.items || []).map(item => ({
      name: PAYMENT_STATUS_MAP[item.label] || item.label,
      value: item.count
    }))
  } finally {
    loading.value.paymentStatus = false
  }
}

async function fetchRefundTrend() {
  loading.value.refundTrend = true
  try {
    const res = await getRefundTrend({ ...buildParams(), granularity: 'day' })
    refundTrendLabels.value = res.data.labels || []
    refundTrendSeries.value = [
      {
        name: '退款笔数',
        data: res.data.counts || [],
        color: '#f56c6c'
      }
    ]
  } finally {
    loading.value.refundTrend = false
  }
}

function fetchAll() {
  fetchRevenueTrend()
  fetchPaymentStatus()
  fetchRefundTrend()
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
