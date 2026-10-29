import { describe, expect, it } from 'vitest'
import { resolvePostLoginPath } from './session'

describe('登录成功落地页', () => {
  it('管理员落到后台', () => {
    expect(resolvePostLoginPath('ADMIN')).toBe('/admin')
  })

  it('注册用户落到首页', () => {
    expect(resolvePostLoginPath('USER')).toBe('/')
  })

  it('带来源路由时 redirect 优先', () => {
    expect(resolvePostLoginPath('ADMIN', '/user/profile')).toBe('/user/profile')
    expect(resolvePostLoginPath('USER', '/user/profile')).toBe('/user/profile')
  })

  it('拒绝站外或协议相对的 redirect', () => {
    expect(resolvePostLoginPath('USER', 'https://evil.example')).toBe('/')
    expect(resolvePostLoginPath('USER', '//evil.example')).toBe('/')
  })
})
