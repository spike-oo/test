<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>数据看板</template>
      <!-- TODO(成员3)：GET /admin/dashboard/overview 核心指标；/trend 趋势；/shop-ranking、/dish-ranking 排行；/category-share、/hour-distribution、/tag-cloud -->
      <el-row :gutter="16">
        <el-col :span="4"><el-statistic title="订单总数" :value="0" /></el-col>
        <el-col :span="4"><el-statistic title="成交金额" :value="0" /></el-col>
        <el-col :span="4"><el-statistic title="学生用户" :value="0" /></el-col>
        <el-col :span="4"><el-statistic title="入驻店铺" :value="0" /></el-col>
        <el-col :span="4"><el-statistic title="在岗骑手" :value="0" /></el-col>
        <el-col :span="4"><el-statistic title="待处理工单" :value="0" /></el-col>
      </el-row>
      <el-empty description="待实现：订单趋势折线图、店铺/菜品排行、品类占比饼图、反馈热词（ECharts）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员3)：图表加载 ≤ 3s，数据由后端聚合接口一次性返回
</script>
