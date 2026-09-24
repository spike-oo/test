<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>知识库</span>
          <el-button type="primary" @click="onCreate">新增问答</el-button>
        </div>
      </template>
      <!-- TODO(成员3)：GET /admin/knowledge 列表；POST /admin/knowledge 新增/修改；DELETE /admin/knowledge/{id} 删除 -->
      <el-empty description="待实现：问答对列表、分类管理、关键词维护（智能客服召回依据）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员3)：知识库维护；问答质量直接决定客服回答准确率
function onCreate() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员3): create knowledge')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
