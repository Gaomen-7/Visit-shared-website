import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export type Role = 'USER' | 'ADMIN'

export interface SessionUser {
  userId: number
  username: string
  nickname: string
  avatar: string | null
  role: Role
}

export interface LoginResult extends SessionUser {
  token: string
}

const TOKEN_KEY = 'travel_share_token'
const USER_KEY = 'travel_share_user'

function readStoredUser(): SessionUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as SessionUser
  } catch {
    return null
  }
}

/**
 * 登录态 = Pinia + localStorage。刷新页面时从 localStorage 还原（GDD 0.3：无服务端会话）。
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<SessionUser | null>(readStoredUser())

  const role = computed<Role | ''>(() => user.value?.role ?? '')
  const isLoggedIn = computed(() => token.value !== '')

  function setSession(session: LoginResult) {
    token.value = session.token
    user.value = {
      userId: session.userId,
      username: session.username,
      nickname: session.nickname,
      avatar: session.avatar,
      role: session.role,
    }
    localStorage.setItem(TOKEN_KEY, session.token)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  function updateUser(partial: Partial<Omit<SessionUser, 'userId' | 'role'>>) {
    if (!user.value) return
    user.value = { ...user.value, ...partial }
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  function clear() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, role, isLoggedIn, setSession, updateUser, clear }
})
