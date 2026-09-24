<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>订单详情</template>
      <!-- TODO(成员1)：GET /orders/{id} 渲染订单信息、明细、状态时间线（order_status_log） -->
      <el-empty description="待实现：订单明细、状态时间线、取餐码、取消/评价入口" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
// TODO(成员1)：加载订单详情；已完成订单提供「去评价」入口（POST /reviews）
// eslint-disable-next-line no-console
console.log('TODO(成员1): order detail', route.params.orderId)
</script>
