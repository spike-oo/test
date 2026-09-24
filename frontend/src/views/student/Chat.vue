<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>智能客服</template>
      <div class="messages">
        <!-- TODO(成员3)：渲染多轮对话气泡；回答无法解决时展示「已转人工」与工单号 -->
        <el-empty description="待实现：对话气泡、快捷问题、转人工提示" />
      </div>
      <el-input v-model="message" placeholder="例如：我的订单怎么还没送到？" @keyup.enter="onSend">
        <template #append>
          <el-button :loading="loading" @click="onSend">发送</el-button>
        </template>
      </el-input>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const message = ref('')
const loading = ref(false)
const sessionId = ref('')

// TODO(成员3)：调用 chat({ sessionId, message })，把返回的 sessionId 保存在本地；
// needTicket=true 时提示「已为你转人工客服」
function onSend() {
  // eslint-disable-next-line no-console
  console.log('TODO(成员3): chat', message.value, sessionId.value)
}
</script>

<style scoped>
.messages {
  min-height: 200px;
  margin-bottom: 12px;
}
</style>
