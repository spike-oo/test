<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>菜品管理</span>
          <el-button type="primary" @click="onCreate">新增菜品</el-button>
        </div>
      </template>
      <!-- TODO(成员2)：GET /merchant/dishes 分页列表；上架/下架 PUT /merchant/dishes/{id}/status；改库存 PUT /merchant/dishes/{id}/stock -->
      <el-empty description="待实现：菜品列表、分类筛选、库存与上架状态、编辑表单（含 tags/ingredients 供 AI 匹配）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员2)：新增/编辑菜品表单，tags 与 ingredients 字段直接影响 AI 点餐与忌口过滤效果
function onCreate() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员2): create dish')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
