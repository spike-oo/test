<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>AI 经营助手</span>
          <el-button type="primary" :loading="loading" @click="onGenerate">生成经营简报</el-button>
        </div>
      </template>
      <!-- TODO(成员2)：GET /merchant/ai/business-report 经营简报；GET /merchant/ai/review-report 评价报告 -->
      <el-empty description="待实现：热销/滞销菜品、经营建议、评价口碑亮点与吐槽点" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const loading = ref(false)

// TODO(成员2)：调用 businessReport()，渲染 summary / suggestions / hotDishes / slowDishes
function onGenerate() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员2): business report')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
