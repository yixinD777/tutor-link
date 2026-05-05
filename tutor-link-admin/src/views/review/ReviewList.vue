<template>
  <div class="review-page">
    <!-- Filter Bar -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent="handleSearch">
        <el-form-item label="评分">
          <el-select v-model="filter.rating" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="1星" :value="1" />
            <el-option label="2星" :value="2" />
            <el-option label="3星" :value="3" />
            <el-option label="4星" :value="4" />
            <el-option label="5星" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价者角色">
          <el-select v-model="filter.reviewerRole" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="家长" :value="1" />
            <el-option label="家教" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="filter.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Data Table -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="120" />
        <el-table-column prop="reviewerId" label="评价者ID" width="120" />
        <el-table-column prop="targetUserId" label="被评价者ID" width="120" />
        <el-table-column label="评分" width="160">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="200">
          <template #default="{ row }">
            <el-tooltip v-if="row.content && row.content.length > 30" :content="row.content" placement="top" :show-after="300">
              <span>{{ row.content.slice(0, 30) }}...</span>
            </el-tooltip>
            <span v-else>{{ row.content || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <template v-if="parseTags(row.tags).length">
              <el-tag v-for="(tag, idx) in parseTags(row.tags)" :key="idx" size="small" class="tag-item">
                {{ tag }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="是否匿名" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isAnonymous ? 'warning' : 'info'" size="small">
              {{ row.isAnonymous ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReviews } from '../../api/review'

// --- Filter ---
const filter = reactive({
  rating: '',
  reviewerRole: '',
  dateRange: null
})

// --- Table ---
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (filter.rating !== '' && filter.rating !== null) {
      params.rating = filter.rating
    }
    if (filter.reviewerRole !== '' && filter.reviewerRole !== null) {
      params.reviewerRole = filter.reviewerRole
    }
    if (filter.dateRange && filter.dateRange.length === 2) {
      params.startDate = filter.dateRange[0]
      params.endDate = filter.dateRange[1]
    }
    const res = await getReviews(params)
    tableData.value = res.records || res.list || res
    pagination.total = res.total || 0
  } catch {
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
  filter.rating = ''
  filter.reviewerRole = ''
  filter.dateRange = null
  pagination.page = 1
  fetchData()
}

// --- Helpers ---
function parseTags(tags) {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  try {
    const parsed = JSON.parse(tags)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// --- Init ---
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.review-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

.tag-item {
  margin-right: 4px;
  margin-bottom: 2px;
}
</style>
