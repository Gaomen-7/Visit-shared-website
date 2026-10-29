/**
 * 登录成功后的落地页：带来源路由时 redirect 优先，否则按角色落地（GDD 0.3）。
 * 只接受站内绝对路径，避免 redirect 被拿来做开放重定向。
 */
export function resolvePostLoginPath(role: string | null | undefined, redirect?: string | null): string {
  if (redirect && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return redirect
  }
  return role === 'ADMIN' ? '/admin' : '/'
}
