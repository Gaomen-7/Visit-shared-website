<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.clear()
  router.replace('/')
}
</script>

<template>
  <div class="admin-page">
    <el-result icon="info" title="后台建设中" sub-title="管理员功能将随 admin 模块交付">
      <template #extra>
        <el-button type="primary" @click="router.replace('/')">回到首页</el-button>
        <el-button @click="logout">退出登录</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
</style>
