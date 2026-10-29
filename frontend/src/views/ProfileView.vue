<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { changePassword, getProfile, updateProfile, type Profile } from '@/api/account'
import { uploadImage } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import avatarPlaceholder from '@/assets/avatar-placeholder.svg'

const router = useRouter()
const auth = useAuthStore()

const profile = ref<Profile | null>(null)
const profileForm = reactive({ nickname: '', avatar: '', intro: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const saving = ref(false)
const uploading = ref(false)
const changing = ref(false)

onMounted(loadProfile)

async function loadProfile() {
  try {
    const data = await getProfile()
    profile.value = data
    profileForm.nickname = data.nickname
    profileForm.avatar = data.avatar ?? ''
    profileForm.intro = data.intro ?? ''
    auth.updateUser({ nickname: data.nickname, avatar: data.avatar })
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

async function saveProfile() {
  if (!profileForm.nickname.trim()) {
    ElMessage.error('昵称不能为空')
    return
  }
  saving.value = true
  try {
    const updated = await updateProfile({
      nickname: profileForm.nickname.trim(),
      avatar: profileForm.avatar || null,
      intro: profileForm.intro.trim() || null,
    })
    profile.value = updated
    auth.updateUser({ nickname: updated.nickname, avatar: updated.avatar })
    ElMessage.success('资料已保存')
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    saving.value = false
  }
}

async function uploadAvatar(options: UploadRequestOptions) {
  uploading.value = true
  try {
    const url = await uploadImage(options.file as File)
    profileForm.avatar = url
    await saveProfile()
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    uploading.value = false
  }
}

async function submitPassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.error('请填写旧密码与新密码')
    return
  }
  changing.value = true
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    ElMessage.success('密码已修改，当前登录状态仍然有效')
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    changing.value = false
  }
}

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
        <router-link to="/">首页</router-link>
        <el-button link type="primary" @click="logout">退出登录</el-button>
      </nav>
    </header>

    <main class="content">
      <el-card class="card">
        <template #header>
          <div class="card-title">个人资料</div>
        </template>

        <div class="avatar-row">
          <el-avatar :size="96" :src="profileForm.avatar || avatarPlaceholder" />
          <div class="avatar-actions">
            <el-upload
              :show-file-list="false"
              accept="image/jpeg,image/png,image/webp"
              :http-request="uploadAvatar"
            >
              <el-button :loading="uploading">上传头像</el-button>
            </el-upload>
            <span class="hint">jpg / png / webp，≤ 5MB</span>
          </div>
        </div>

        <el-form label-position="top">
          <el-form-item label="用户名（终身不可修改）">
            <el-input :model-value="profile?.username ?? ''" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input
              v-model="profileForm.intro"
              type="textarea"
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="简单介绍一下自己"
            />
          </el-form-item>
          <el-form-item label="注册时间">
            <span class="readonly">{{ profile?.createTime ?? '' }}</span>
          </el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
        </el-form>
      </el-card>

      <el-card class="card">
        <template #header>
          <div class="card-title">修改密码</div>
        </template>
        <el-form label-position="top">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="6–20 个字符"
            />
          </el-form-item>
          <el-button type="primary" :loading="changing" @click="submitPassword">修改密码</el-button>
        </el-form>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.brand {
  font-weight: 700;
  font-size: 18px;
  color: #303133;
  text-decoration: none;
}
.nav {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
}
.nav a {
  color: #409eff;
  text-decoration: none;
}
.content {
  max-width: 720px;
  margin: 32px auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.card-title {
  font-weight: 600;
  color: #303133;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}
.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.hint {
  color: #909399;
  font-size: 12px;
}
.readonly {
  color: #606266;
}
</style>
