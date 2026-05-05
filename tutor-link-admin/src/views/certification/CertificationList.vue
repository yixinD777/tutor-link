<template>
  <div class="certification-page">
    <!-- Filter Bar -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent="handleSearch">
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="待审核" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
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
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column label="身份证号" width="180">
          <template #default="{ row }">
            {{ maskIdCard(row.idCard) }}
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 1" type="danger" link @click="handleReject(row)">拒绝</el-button>
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

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="认证详情" width="700px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-content">
        <template v-if="detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ detail.realName }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ maskIdCard(detail.idCard) }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间" :span="2">{{ formatDate(detail.createdAt) }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.status === 3 && detail.rejectReason" label="拒绝原因" :span="2">
              {{ detail.rejectReason }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="photo-section">
            <h4>认证照片</h4>
            <div class="photo-list">
              <div v-if="detail.studentIdFront" class="photo-item">
                <span class="photo-label">学生证正面</span>
                <el-image :src="detail.studentIdFront" :preview-src-list="[detail.studentIdFront]" fit="cover" />
              </div>
              <div v-if="detail.studentIdBack" class="photo-item">
                <span class="photo-label">学生证反面</span>
                <el-image :src="detail.studentIdBack" :preview-src-list="[detail.studentIdBack]" fit="cover" />
              </div>
              <div v-if="detail.handheldId" class="photo-item">
                <span class="photo-label">手持身份证</span>
                <el-image :src="detail.handheldId" :preview-src-list="[detail.handheldId]" fit="cover" />
              </div>
            </div>
          </div>

          <div v-if="detail.ocrResult" class="ocr-section">
            <h4>OCR 识别结果</h4>
            <el-input type="textarea" :model-value="detail.ocrResult" :rows="4" readonly />
          </div>
        </template>
      </div>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectVisible" title="拒绝认证" width="500px" destroy-on-close>
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCertifications,
  getCertificationDetail,
  approveCertification,
  rejectCertification
} from '../../api/certification'

// --- Filter ---
const filter = reactive({ status: 1 })

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
    if (filter.status !== '' && filter.status !== null) {
      params.status = filter.status
    }
    const res = await getCertifications(params)
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
  filter.status = 1
  pagination.page = 1
  fetchData()
}

// --- Status helpers ---
function statusTagType(status) {
  const map = { 1: 'info', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { 1: '待审核', 2: '已通过', 3: '已拒绝' }
  return map[status] || '未知'
}

function maskIdCard(idCard) {
  if (!idCard) return '-'
  if (idCard.length <= 4) return idCard
  return '****' + idCard.slice(-4)
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// --- Detail ---
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

async function handleView(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getCertificationDetail(row.id)
  } catch {
    // error handled by interceptor
  } finally {
    detailLoading.value = false
  }
}

// --- Approve ---
async function handleApprove(row) {
  try {
    await ElMessageBox.confirm('确认通过该认证申请？', '确认操作', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await approveCertification(row.id)
    ElMessage.success('认证已通过')
    fetchData()
  } catch {
    // user cancelled or request failed
  }
}

// --- Reject ---
const rejectVisible = ref(false)
const rejectLoading = ref(false)
const rejectForm = reactive({ id: null, reason: '' })

function handleReject(row) {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  rejectLoading.value = true
  try {
    await rejectCertification(rejectForm.id, rejectForm.reason.trim())
    ElMessage.success('认证已拒绝')
    rejectVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    rejectLoading.value = false
  }
}

// --- Init ---
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.certification-page {
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

.detail-content {
  min-height: 200px;
}

.photo-section {
  margin-top: 20px;

  h4 {
    margin-bottom: 12px;
    color: #333;
  }

  .photo-list {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;

    .photo-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .photo-label {
        font-size: 13px;
        color: #666;
      }

      .el-image {
        width: 180px;
        height: 120px;
        border-radius: 4px;
        border: 1px solid #eee;
      }
    }
  }
}

.ocr-section {
  margin-top: 20px;

  h4 {
    margin-bottom: 12px;
    color: #333;
  }
}
</style>
