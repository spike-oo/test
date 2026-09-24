<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>AI 点餐 · 一句话点餐</template>
      <el-input
        v-model="message"
        type="textarea"
        :rows="3"
        placeholder="例如：20 块以内、不辣、要有肉"
      />
      <div style="margin-top: 12px">
        <el-button type="primary" :loading="loading" @click="onSubmit">开始点餐</el-button>
        <span class="text-muted">AI 只从在售菜品中挑选，忌口与过敏原会被自动过滤</span>
      </div>
      <!-- TODO(成员1)：POST /ai/order，渲染 reply / conditions / dishes，fallback=true 时提示已降级为关键词检索 -->
      <el-empty description="待实现：推荐结果卡片、一键加入购物车" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const message = ref('')
const loading = ref(false)

// TODO(成员1)：调用 aiOrder()，把返回的 dishes 渲染为卡片并支持加入购物车
function onSubmit() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员1): ai order', message.value)
}
</script>
