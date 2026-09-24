<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>饮食分析</template>
      <el-radio-group v-model="days" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <!-- TODO(成员1)：GET /ai/diet-report?days=7，渲染 summary / suggestions / structure / allergyRisk -->
      <el-empty description="待实现：饮食结构图、健康建议、过敏原提醒" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const days = ref(7)

// TODO(成员1)：调用 dietReport(days)，结构数据可用 ECharts 饼图展示
function load() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员1): diet report, days =', days.value)
}
</script>
