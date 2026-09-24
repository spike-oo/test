<template>
  <div class="register-page">
    <el-card class="register-card" shadow="never">
      <h2 class="title">学生账号注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="学号" prop="username">
          <el-input v-model="form.username" placeholder="如 20210001" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">注册</el-button>
        </el-form-item>
      </el-form>
      <el-button text type="primary" @click="router.push('/login')">返回登录</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'

import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '', phone: '', nickname: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await register({ ...form, role: 'STUDENT' })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
.register-card {
  width: 440px;
}
.title {
  margin: 0 0 16px;
  font-size: 18px;
  text-align: center;
  color: var(--campus-primary);
}
</style>
