<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { resolvePostLoginPath } from '@/router/session'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.username.trim() || !form.password) {
    ElMessage.error('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const result = await login(form.username.trim(), form.password)
    auth.setSession(result)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    await router.replace(resolvePostLoginPath(result.role, redirect))
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
      <h2>登录</h2>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="6–20 个字符"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="switch">
        还没有账号？
        <router-link :to="{ path: '/register', query: route.query }">去注册</router-link>
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
  width: 380px;
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
