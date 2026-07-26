<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, toRaw, watch } from 'vue'
import { createGridJsAdapter, type GridJsAdapter, type GridJsEndpoints } from 'gridjs-spreadsheet/shared'
import JSZip from 'jszip'
import 'gridjs-spreadsheet/xspreadsheet.css'

declare global {
  interface Window {
    JSZip?: typeof JSZip
  }
}

if (typeof window !== 'undefined') {
  window.JSZip = JSZip
}

const DEFAULT_API_BASE = '/v2-api/excel'

const props = defineProps<{
  data?: Record<string, unknown> | null
  apiBase?: string
  mode?: 'edit' | 'read'
  locale?: string
  height?: string | number
}>()

const emit = defineEmits<{
  ready: [...args: unknown[]]
  change: [...args: unknown[]]
  error: [...args: unknown[]]
  'cell-selected': [...args: unknown[]]
}>()

const host = ref<HTMLElement | null>(null)
let adapter: GridJsAdapter | null = null

function excelEndpoints(apiBase: string): GridJsEndpoints {
  const base = String(apiBase || DEFAULT_API_BASE).replace(/\/+$/, '')
  return {
    updateCell: `${base}/UpdateCell`,
    imageUrl: `${base}/ImageUrl`,
    addImage: `${base}/AddImage`,
    addImageByUrl: `${base}/AddImageByURL`,
    copyImage: `${base}/CopyImage`,
    download: `${base}/Download`,
    ole: `${base}/Ole`,
    lazyLoading: `${base}/LazyLoadingStreamJson`,
  }
}

function clonePlain(value: Record<string, unknown>) {
  const rawValue = toRaw(value)
  if (typeof structuredClone === 'function') {
    try {
      return structuredClone(rawValue)
    } catch {
      // Vue props can contain reactive proxies.
    }
  }
  return JSON.parse(JSON.stringify(rawValue))
}

async function mountGridJs() {
  if (!host.value || !props.data) return
  adapter?.destroy()
  host.value.innerHTML = ''

  adapter = await createGridJsAdapter({
    container: host.value,
    endpoints: excelEndpoints(props.apiBase ?? DEFAULT_API_BASE),
    data: clonePlain(props.data),
    mode: props.mode ?? 'edit',
    locale: props.locale ?? 'en',
    showToolbar: true,
    showContextmenu: true,
    callbacks: {
      ready: (instance, activeAdapter) => emit('ready', instance, activeAdapter),
      change: (...args: unknown[]) => emit('change', ...args),
      error: (payload) => emit('error', payload),
      cellSelected: (...args: unknown[]) => emit('cell-selected', ...args),
    },
    dependencies: {
      importJSZip: () => import('jszip'),
      importGridJs: () => import('gridjs-spreadsheet'),
    },
  })
}

watch(
  () => [
    props.data?.uniqueid,
    props.data?.filename,
    props.apiBase,
    props.mode,
    props.locale,
  ],
  () => {
    mountGridJs().catch((error) => emit('error', { type: 'mountError', error }))
  },
  { immediate: true },
)

// immediate watch runs during setup before the host ref exists; remount after DOM is ready
onMounted(() => {
  mountGridJs().catch((error) => emit('error', { type: 'mountError', error }))
})

onBeforeUnmount(() => {
  adapter?.destroy()
  adapter = null
  if (host.value) host.value.innerHTML = ''
})
</script>

<template>
  <div
    ref="host"
    class="gridjs-vue-host"
    :style="{ width: '100%', height: height ?? 'calc(100vh - 96px)', background: '#ffffff' }"
  />
</template>
