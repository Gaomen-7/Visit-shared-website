<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const displayName = computed(() => auth.user?.nickname ?? '游客')

function logout() {
  auth.clear()
  router.replace('/')
}
</script>

<template>
  <div class="page">
    <header class="header">
      <router-link to="/" class="brand">旅游攻略分享网站</router-link>
      <nav class="nav">
        <template v-if="auth.isLoggedIn">
          <span class="hello">你好，{{ displayName }}</span>
          <router-link to="/user/profile">个人中心</router-link>
          <router-link v-if="auth.role === 'ADMIN'" to="/admin">后台</router-link>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </header>

    <main class="hero">
      <h1>发现下一段旅程</h1>
      <p>注册后即可维护个人资料与头像，并随着模块推进发布自己的旅行攻略。</p>
      <div class="actions">
        <router-link v-if="!auth.isLoggedIn" to="/login">
          <el-button type="primary" size="large">登录 / 注册</el-button>
        </router-link>
        <router-link v-else to="/user/profile">
          <el-button type="primary" size="large">进入个人中心</el-button>
        </router-link>
      </div>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 45%);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}
.brand {
  font-weight: 700;
  font-size: 18px;
  color: #303133;
  text-decoration: none;
}
.nav {
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 14px;
}
.nav a {
  color: #409eff;
  text-decoration: none;
}
.hello {
  color: #909399;
}
.hero {
  max-width: 720px;
  margin: 120px auto 0;
  padding: 0 24px;
  text-align: center;
}
.hero h1 {
  font-size: 40px;
  color: #303133;
  margin-bottom: 16px;
}
.hero p {
  color: #606266;
  line-height: 1.8;
}
.actions {
  margin-top: 32px;
}
.actions a {
  text-decoration: none;
}
</style>
