<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>我的收入</template>
      <!-- TODO(成员4)：GET /rider/income 渲染今日/本月/累计收入与单量 -->
      <el-row :gutter="16">
        <el-col :span="8"><el-statistic title="今日收入（元）" :value="0" /></el-col>
        <el-col :span="8"><el-statistic title="本月收入（元）" :value="0" /></el-col>
        <el-col :span="8"><el-statistic title="累计收入（元）" :value="0" /></el-col>
      </el-row>
      <el-empty description="待实现：收入统计（grab_log / delivery_record 聚合）、明细列表" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员4)：加载收入统计
</script>
