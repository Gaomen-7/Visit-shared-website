import { http } from './http'
import type { LoginResult } from '@/stores/auth'

export interface RegisterResult {
  id: number
  username: string
  nickname: string
}

export async function login(username: string, password: string): Promise<LoginResult> {
  return (await http.post('/auth/login', { username, password })) as unknown as LoginResult
}

export async function register(username: string, password: string): Promise<RegisterResult> {
  return (await http.post('/auth/register', { username, password })) as unknown as RegisterResult
}
