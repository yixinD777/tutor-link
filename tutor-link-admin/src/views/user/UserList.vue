<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="账号/昵称/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="家长" :value="1" />
            <el-option label="家教" :value="2" />
            <el-option label="管理员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="account" label="账号" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="手机号" width="140">
          <template #default="{ row }">
            {{ maskPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role & 1" type="success" size="small" style="margin-right: 4px">家长</el-tag>
            <el-tag v-if="row.role & 2" type="primary" size="small" style="margin-right: 4px">家教</el-tag>
            <el-tag v-if="row.role & 4" type="danger" size="small">管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
              inline-prompt
              active-text="正常"
              inactive-text="禁用"
            />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
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

    <el-dialog v-model="detailVisible" title="用户详情" width="600px" destroy-on-close>
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ detailData.account }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailData.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ maskPhone(detailData.phone) }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <span>{{ roleLabel(detailData.role) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
              {{ detailData.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(detailData.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detailData.profile">
          <el-divider>个人资料</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="真实姓名">{{ detailData.profile.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ detailData.profile.gender === 1 ? '男' : detailData.profile.gender === 2 ? '女' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="头像" :span="2">
              <el-image v-if="detailData.profile.avatar" :src="detailData.profile.avatar" style="width: 60px; height: 60px" fit="cover" />
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-if="detailData.tutorProfile">
          <el-divider>家教资料</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学校">{{ detailData.tutorProfile.university || '-' }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ detailData.tutorProfile.major || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ detailData.tutorProfile.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">
              <el-tag :type="certStatusType(detailData.tutorProfile.certStatus)">
                {{ certStatusLabel(detailData.tutorProfile.certStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="时薪">{{ detailData.tutorProfile.hourlyRate != null ? (detailData.tutorProfile.hourlyRate / 100).toFixed(2) + ' 元' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="平均评分">{{ detailData.tutorProfile.avgRating ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ detailData.tutorProfile.bio || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, getUserDetail, updateUserStatus } from '../../api/user'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)

const searchForm = reactive({ keyword: '', role: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const roleLabel = (r) => {
  let labels = []
  if (r & 1) labels.push('家长')
  if (r & 2) labels.push('家教')
  if (r & 4) labels.push('管理员')
  return labels.join(', ') || '未知'
}

const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const certStatusLabel = (status) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已通过', 3: '已拒绝' }
  return map[status] ?? '未知'
}

const certStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] ?? 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.role !== '') params.role = searchForm.role
    if (searchForm.status !== '') params.status = searchForm.status
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
  searchForm.role = ''
  searchForm.status = ''
  pagination.page = 1
  fetchData()
}

async function handleStatusChange(row, val) {
  const newStatus = val ? 1 : 0
  try {
    await updateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch (e) {
    // error handled by interceptor
  }
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
