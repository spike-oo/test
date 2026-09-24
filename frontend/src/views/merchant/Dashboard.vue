<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>经营概览</template>
      <!-- TODO(成员2)：GET /merchant/orders?status=0 待处理订单；GET /merchant/reviews/overview 评价概览；今日营业额 -->
      <el-row :gutter="16">
        <el-col :span="6"><el-statistic title="今日订单" :value="0" /></el-col>
        <el-col :span="6"><el-statistic title="今日营业额" :value="0" /></el-col>
        <el-col :span="6"><el-statistic title="待接单" :value="0" /></el-col>
        <el-col :span="6"><el-statistic title="店铺评分" :value="0" /></el-col>
      </el-row>
      <el-empty description="待实现：营业状态开关、待处理订单列表、近 7 天趋势图（ECharts）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员2)：聚合订单、评价、销量数据；图表数据由后端聚合接口提供
</script>
