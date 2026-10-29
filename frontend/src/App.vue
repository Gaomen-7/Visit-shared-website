<script setup lang="ts">
import { ref } from 'vue'

interface PingData {
  service: string
  time: string
}

interface Result<T> {
  code: number
  message: string
  data: T
}

const status = ref<'idle' | 'loading' | 'ok' | 'error'>('idle')
const payload = ref<string>('')

async function ping() {
  status.value = 'loading'
  try {
    // Relative URL on purpose: it must travel through the Vite dev proxy.
    const res = await fetch('/api/ping')
    const body = (await res.json()) as Result<PingData>
    status.value = res.ok && body.code === 200 ? 'ok' : 'error'
    payload.value = JSON.stringify(body, null, 2)
  } catch (e) {
    status.value = 'error'
    payload.value = String(e)
  }
}
</script>

<template>
  <main style="font-family: system-ui, sans-serif; max-width: 720px; margin: 48px auto; padding: 0 16px">
    <h1>旅游攻略分享网站</h1>
    <p>前后端工程骨架 · /api 代理冒烟</p>
    <button :disabled="status === 'loading'" @click="ping">
      {{ status === 'loading' ? '请求中…' : '调用 GET /api/ping' }}
    </button>
    <p v-if="status !== 'idle'">
      状态：<strong>{{ status }}</strong>
    </p>
    <pre v-if="payload" style="background: #f5f5f5; padding: 12px; border-radius: 6px; overflow: auto">{{ payload }}</pre>
  </main>
</template>
