<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({ username: '', password: '', confirmPassword: '' })
const loading = ref(false)

async function submit() {
  if (!form.username.trim() || !form.password) {
    ElMessage.error('请填写用户名和密码')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await register(form.username.trim(), form.password)
    ElMessage.success('注册成功，请登录')
    await router.replace({ path: '/login', query: route.query })
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册</h2>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="3–20 个字符，限中文 / 字母 / 数字 / 下划线" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="6–20 个字符" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入密码"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <p class="switch">
        已有账号？
        <router-link :to="{ path: '/login', query: route.query }">去登录</router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.auth-card {
  width: 420px;
}
.auth-card h2 {
  text-align: center;
  margin: 8px 0 24px;
  color: #303133;
}
.full {
  width: 100%;
  margin-top: 8px;
}
.switch {
  margin-top: 16px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
.switch a {
  color: #409eff;
  text-decoration: none;
}
</style>
