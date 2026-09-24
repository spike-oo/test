<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>我的订单</span>
          <el-radio-group v-model="status" @change="load">
            <el-radio-button :value="undefined">全部</el-radio-button>
            <el-radio-button :value="0">待接单</el-radio-button>
            <el-radio-button :value="1">备餐中</el-radio-button>
            <el-radio-button :value="3">配送中</el-radio-button>
            <el-radio-button :value="4">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <!-- TODO(成员1)：GET /orders 分页渲染订单卡片，展示状态、金额、明细 -->
      <el-empty description="待实现：订单列表、状态标签、取消订单" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const status = ref<number | undefined>(undefined)

// TODO(成员1)：调用 listMyOrders；页面挂载时由 StudentLayout 建立 WebSocket，
// 收到 ORDER_STATUS 消息后刷新列表（订单状态推送 ≤ 2s）
function load() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员1): load orders, status =', status.value)
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
