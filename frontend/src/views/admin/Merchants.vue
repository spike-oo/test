<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>商户审核</template>
      <!-- TODO(成员3)：待审核商户列表；POST /admin/audit/merchant/{id}?pass=true|false 审核；POST /admin/audit/shop/{id}/ban 封禁 -->
      <el-empty description="待实现：商户资质列表、通过/驳回、封禁与解封" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员3)：审核列表与操作；驳回需填写原因
</script>
