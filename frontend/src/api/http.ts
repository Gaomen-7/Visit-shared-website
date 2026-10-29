import axios, { type AxiosInstance } from 'axios'
import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const TOKEN_KEY = 'travel_share_token'

export function createHttpClient(baseURL = '/api'): AxiosInstance {
  return axios.create({ baseURL, timeout: 15000 })
}

export const http = createHttpClient()

/**
 * 请求带 Authorization；响应解包统一信封；401 清态并带来源路由回登录页（GDD 0.2 / 0.3）。
 */
export function installInterceptors(client: AxiosInstance, router: Router) {
  client.interceptors.request.use((config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  client.interceptors.response.use(
    (response) => {
      const body = response.data as { code?: number; data?: unknown } | undefined
      if (body && typeof body.code === 'number') {
        return body.data
      }
      return response.data
    },
    (error) => {
      const status = error.response?.status as number | undefined
      const message =
        (error.response?.data as { message?: string } | undefined)?.message ?? '网络异常，请稍后重试'

      if (status === 401) {
        const auth = useAuthStore()
        auth.clear()
        const current = router.currentRoute.value
        if (current.path !== '/login') {
          router.replace({ path: '/login', query: { redirect: current.fullPath } })
        }
      }

      return Promise.reject(new Error(message))
    },
  )
}
