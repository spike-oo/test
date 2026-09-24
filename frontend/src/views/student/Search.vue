<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>搜索 · 关键词与语义搜索</template>
      <el-input v-model="keyword" placeholder="试试「想吃点暖和的、不辣的」" clearable>
        <template #append>
          <el-button @click="onSearch">搜索</el-button>
        </template>
      </el-input>
      <!-- TODO(成员1)：GET /dishes/search 关键词搜索；POST /ai/search 语义搜索（结果含 reply 与 fallback 标记） -->
      <el-empty description="待实现：搜索结果列表、语义命中说明、降级提示" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const keyword = ref('')

// TODO(成员1)：调用 searchDishes / semanticSearch，把返回的 fallback 与 conditions 展示给用户
function onSearch() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员1): search', keyword.value)
}
</script>
