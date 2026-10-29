import { http } from './http'

export interface Profile {
  id: number
  username: string
  nickname: string
  avatar: string | null
  intro: string | null
  createTime: string
}

export interface ProfilePayload {
  nickname: string
  avatar: string | null
  intro: string | null
}

export async function getProfile(): Promise<Profile> {
  return (await http.get('/user/profile')) as unknown as Profile
}

export async function updateProfile(payload: ProfilePayload): Promise<Profile> {
  return (await http.put('/user/profile', payload)) as unknown as Profile
}

export async function changePassword(oldPassword: string, newPassword: string): Promise<void> {
  await http.put('/user/password', { oldPassword, newPassword })
}
