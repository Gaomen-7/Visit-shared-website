import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { setupServer } from 'msw/node'
import { http as mockHttp, HttpResponse } from 'msw'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { defineComponent } from 'vue'
import { createHttpClient, installInterceptors } from './http'
import { useAuthStore } from '@/stores/auth'

const Blank = defineComponent({ render: () => null })

// 网络层打桩（MSW），不 mock 组件或业务模块内部。
const server = setupServer(
  mockHttp.get('http://localhost:3000/api/user/profile', () =>
    HttpResponse.json({ code: 401, message: '未登录或登录状态已失效', data: null }, { status: 401 }),
  ),
)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('响应 401', () => {
  it('清空登录态并跳登录页，redirect 为当前路径', async () => {
    setActivePinia(createPinia())
    localStorage.setItem('travel_share_token', 'fake-token')
    localStorage.setItem(
      'travel_share_user',
      JSON.stringify({ userId: 1, username: 'someone', nickname: 'someone', avatar: null, role: 'USER' }),
    )

    const auth = useAuthStore()
    expect(auth.token).toBe('fake-token')

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: Blank },
        { path: '/user/profile', component: Blank },
        { path: '/login', component: Blank },
      ],
    })
    await router.push('/user/profile')

    const client = createHttpClient('http://localhost:3000/api')
    installInterceptors(client, router)

    await expect(client.get('/user/profile')).rejects.toThrow()

    expect(auth.token).toBe('')
    expect(localStorage.getItem('travel_share_token')).toBeNull()
    await vi.waitFor(() => {
      expect(router.currentRoute.value.path).toBe('/login')
    })
    expect(router.currentRoute.value.query.redirect).toBe('/user/profile')
  })
})
