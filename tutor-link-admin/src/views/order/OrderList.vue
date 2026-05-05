<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="订单号/标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="待确认" :value="1" />
            <el-option label="已确认" :value="2" />
            <el-option label="已支付" :value="3" />
            <el-option label="进行中" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
            <el-option label="退款中" :value="7" />
            <el-option label="已退款" :value="8" />
            <el-option label="争议中" :value="9" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="subject" label="科目" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ formatYuan(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="700px" destroy-on-close>
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ detailData.title }}</el-descriptions-item>
          <el-descriptions-item label="科目">{{ detailData.subject || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detailData.status)">{{ statusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="家长">{{ detailData.parentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="家教">{{ detailData.tutorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课时数">{{ detailData.hours ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ formatYuan(detailData.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(detailData.updatedAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detailData.payment">
          <el-divider>支付信息</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="支付号">{{ detailData.payment.paymentNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="paymentStatusType(detailData.payment.status)">
                {{ paymentStatusLabel(detailData.payment.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付金额">¥{{ formatYuan(detailData.payment.amount) }}</el-descriptions-item>
            <el-descriptions-item label="平台手续费">¥{{ formatYuan(detailData.payment.platformFee) }}</el-descriptions-item>
            <el-descriptions-item label="家教收入">¥{{ formatYuan(detailData.payment.tutorIncome) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatDate(detailData.payment.paidAt) }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-if="detailData.logs && detailData.logs.length">
          <el-divider>订单日志</el-divider>
          <el-timeline>
            <el-timeline-item
              v-for="log in detailData.logs"
              :key="log.id"
              :timestamp="formatDate(log.createdAt)"
              placement="top"
            >
              <p>{{ log.content || log.action }}</p>
            </el-timeline-item>
          </el-timeline>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders, getOrderDetail } from '../../api/order'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)

const searchForm = reactive({ keyword: '', status: '', dateRange: null })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const statusMap = {
  1: '待确认', 2: '已确认', 3: '已支付', 4: '进行中', 5: '已完成',
  6: '已取消', 7: '退款中', 8: '已退款', 9: '争议中'
}
const statusColorMap = {
  1: 'info', 2: 'warning', 3: 'primary', 4: 'primary', 5: 'success',
  6: 'info', 7: 'warning', 8: 'info', 9: 'danger'
}

const statusLabel = (status) => statusMap[status] ?? '未知'
const statusType = (status) => statusColorMap[status] ?? 'info'

const paymentStatusLabel = (status) => {
  const map = { 1: '待支付', 2: '已支付', 3: '已释放', 4: '冻结中' }
  return map[status] ?? '未知'
}

const paymentStatusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] ?? 'info'
}

const formatYuan = (cents) => cents != null ? (cents / 100).toFixed(2) : '0.00'

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status !== '') params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await getOrders(params)
    tableData.value = res.records || res.list || []
    pagination.total = res.total || 0
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchData()
}

async function handleView(row) {
  try {
    detailData.value = await getOrderDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    // error handled by interceptor
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
