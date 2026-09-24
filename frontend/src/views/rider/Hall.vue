<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>抢单大厅</span>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>
      <!-- TODO(成员4)：GET /rider/hall 待抢订单；POST /rider/grab/{orderId} 抢单 -->
      <el-empty description="待实现：可抢订单列表、抢单按钮、抢单失败（已被抢走）提示" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员4)：新订单由 RiderLayout 的 WebSocket（GRAB_ORDER）实时推送，推送 ≤ 1s
function load() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员4): load grab hall')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
