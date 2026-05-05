<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="searchForm.certStatus" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="未认证" :value="0" />
            <el-option label="审核中" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="searchForm.university" placeholder="学校名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column label="姓名" min-width="100">
          <template #default="{ row }">
            {{ row.tutorProfile?.realName || row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="学校" min-width="140">
          <template #default="{ row }">
            {{ row.tutorProfile?.university || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="专业" min-width="120">
          <template #default="{ row }">
            {{ row.tutorProfile?.major || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="certStatusType(row.tutorProfile?.certStatus)">
              {{ certStatusLabel(row.tutorProfile?.certStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平均评分" width="160">
          <template #default="{ row }">
            <el-rate
              v-if="row.tutorProfile?.avgRating"
              :model-value="row.tutorProfile.avgRating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="时薪范围" width="120">
          <template #default="{ row }">
            {{ row.tutorProfile?.hourlyRate != null ? (row.tutorProfile.hourlyRate / 100).toFixed(2) + ' 元' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="接单数" width="90">
          <template #default="{ row }">
            {{ row.tutorProfile?.orderCount ?? '-' }}
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

    <el-dialog v-model="detailVisible" title="家教详情" width="650px" destroy-on-close>
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ detailData.account }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailData.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ maskPhone(detailData.phone) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
              {{ detailData.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detailData.tutorProfile">
          <el-divider>家教资料</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="真实姓名">{{ detailData.tutorProfile.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ detailData.tutorProfile.university || '-' }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ detailData.tutorProfile.major || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ detailData.tutorProfile.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">
              <el-tag :type="certStatusType(detailData.tutorProfile.certStatus)">
                {{ certStatusLabel(detailData.tutorProfile.certStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="时薪">
              {{ detailData.tutorProfile.hourlyRate != null ? (detailData.tutorProfile.hourlyRate / 100).toFixed(2) + ' 元' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="平均评分">
              <el-rate
                v-if="detailData.tutorProfile.avgRating"
                :model-value="detailData.tutorProfile.avgRating"
                disabled
                show-score
                text-color="#ff9900"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="接单数">{{ detailData.tutorProfile.orderCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ detailData.tutorProfile.bio || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教学经历" :span="2">{{ detailData.tutorProfile.experience || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-if="detailData.profile">
          <el-divider>个人资料</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="头像" :span="2">
              <el-image v-if="detailData.profile.avatar" :src="detailData.profile.avatar" style="width: 60px; height: 60px" fit="cover" />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="性别">{{ detailData.profile.gender === 1 ? '男' : detailData.profile.gender === 2 ? '女' : '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, getUserDetail } from '../../api/user'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)

const searchForm = reactive({ keyword: '', certStatus: '', university: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const certStatusLabel = (status) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已通过', 3: '已拒绝' }
  return map[status] ?? '未知'
}

const certStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] ?? 'info'
}

const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      role: 2
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.certStatus !== '') params.certStatus = searchForm.certStatus
    if (searchForm.university) params.university = searchForm.university
    const res = await getUsers(params)
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
  searchForm.certStatus = ''
  searchForm.university = ''
  pagination.page = 1
  fetchData()
}

async function handleView(row) {
  try {
    detailData.value = await getUserDetail(row.id)
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
