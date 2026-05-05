<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="已释放" :value="3" />
            <el-option label="冻结中" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间">
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
        <el-table-column prop="paymentNo" label="支付号" width="180" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            ¥{{ formatYuan(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="平台手续费" width="120">
          <template #default="{ row }">
            ¥{{ formatYuan(row.platformFee) }}
          </template>
        </el-table-column>
        <el-table-column label="家教收入" width="120">
          <template #default="{ row }">
            ¥{{ formatYuan(row.tutorIncome) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.paidAt) }}
          </template>
        </el-table-column>
        <el-table-column label="释放时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.releasedAt) }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPayments } from '../../api/payment'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({ status: '', dateRange: null })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const statusMap = { 1: '待支付', 2: '已支付', 3: '已释放', 4: '冻结中' }
const statusColorMap = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }

const statusLabel = (status) => statusMap[status] ?? '未知'
const statusType = (status) => statusColorMap[status] ?? 'info'

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
    if (searchForm.status !== '') params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await getPayments(params)
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
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchData()
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
