<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>订单处理</span>
          <el-radio-group v-model="status" @change="load">
            <el-radio-button :value="0">待接单</el-radio-button>
            <el-radio-button :value="1">备餐中</el-radio-button>
            <el-radio-button :value="2">待取餐</el-radio-button>
            <el-radio-button :value="4">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <!-- TODO(成员2)：GET /merchant/orders；接单 POST /{id}/accept；出餐 POST /{id}/ready；拒单 POST /{id}/reject -->
      <el-empty description="待实现：订单卡片、接单/出餐/拒单按钮、取餐码展示" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const status = ref<number | undefined>(0)

// TODO(成员2)：新订单由 MerchantLayout 的 WebSocket（NEW_ORDER）触发刷新
function load() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员2): load shop orders, status =', status.value)
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
