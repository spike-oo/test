<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>系统配置</template>
      <!-- TODO(成员3)：sys_config 与 ai_config 维护（轮播图、平台公告、AI 模型参数） -->
      <el-empty description="待实现：平台参数、AI 模型配置（api-url / model / temperature）、轮播图管理" />
      <el-alert
        type="warning"
        :closable="false"
        title="密钥安全"
        description="大模型 API Key 只允许在后台维护并加密存储，禁止写入代码仓库或前端配置。"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员3)：系统配置读写；敏感配置项脱敏展示
</script>
