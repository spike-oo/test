<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>个人中心 · 饮食档案与地址</template>
      <!-- TODO(成员1)：GET /user/profile 基本信息；GET/PUT /user/diet-profile 饮食档案；GET/POST/DELETE /user/address 收货地址 -->
      <el-tabs>
        <el-tab-pane label="基本资料">
          <el-empty description="待实现：昵称、手机号、账户余额" />
        </el-tab-pane>
        <el-tab-pane label="饮食档案">
          <el-empty description="待实现：饮食目标、口味偏好、忌口与过敏原（AI 点餐与推荐的数据基础）" />
        </el-tab-pane>
        <el-tab-pane label="收货地址">
          <el-empty description="待实现：地址增删改查、默认地址" />
        </el-tab-pane>
        <el-tab-pane label="我的工单">
          <el-empty description="待实现：GET /tickets/mine 查看客服工单处理进度" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO(成员1)：加载并保存个人资料与饮食档案
</script>
