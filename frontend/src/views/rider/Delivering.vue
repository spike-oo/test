<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>配送中</template>
      <!-- TODO(成员4)：GET /rider/orders?status=2 已抢待取餐；status=3 配送中；POST /rider/pickup/{id} 取餐；POST /rider/deliver/{id} 送达 -->
      <el-empty description="待实现：当前配送订单、取餐码校验、取餐/送达按钮" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员4)：取餐 → 状态进入配送中；送达 → 状态进入已完成，并写入 delivery_record
</script>
