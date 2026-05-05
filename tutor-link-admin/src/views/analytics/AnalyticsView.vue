<template>
  <div class="analytics-container">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="用户分析" name="user">
        <UserAnalytics v-if="activeTab === 'user'" />
      </el-tab-pane>
      <el-tab-pane label="订单分析" name="order">
        <OrderAnalytics v-if="activeTab === 'order'" />
      </el-tab-pane>
      <el-tab-pane label="财务分析" name="financial">
        <FinancialAnalytics v-if="activeTab === 'financial'" />
      </el-tab-pane>
      <el-tab-pane label="家教分析" name="tutor">
        <TutorAnalytics v-if="activeTab === 'tutor'" />
      </el-tab-pane>
      <el-tab-pane label="评价分析" name="review">
        <ReviewAnalytics v-if="activeTab === 'review'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import UserAnalytics from './UserAnalytics.vue'
import OrderAnalytics from './OrderAnalytics.vue'
import FinancialAnalytics from './FinancialAnalytics.vue'
import TutorAnalytics from './TutorAnalytics.vue'
import ReviewAnalytics from './ReviewAnalytics.vue'

const activeTab = ref('user')
</script>

<style scoped>
.analytics-container {
  padding: 20px;
}
</style>
