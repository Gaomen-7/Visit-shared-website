import { http } from './http'

/** 单图上传，返回可直接引用的相对 URL（如 /uploads/xxx.png）。 */
export async function uploadImage(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const data = (await http.post('/file/upload', form)) as unknown as { url: string }
  return data.url
}
