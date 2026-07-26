<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DocxViewer from '@/components/DocxViewer.vue'

interface FileInfo {
  name: string
  size: number
}

const files = ref<FileInfo[]>([])
const selectedFile = ref('')
const htmlContent = ref('')
const hasDocument = ref(false)
const viewerKey = ref(0)
const loading = ref(false)
const error = ref('')

async function refreshFiles() {
  error.value = ''
  try {
    const res = await fetch('/v2-api/docs')
    if (!res.ok) throw new Error(`Failed to list documents (${res.status})`)
    files.value = await res.json()
    if (!selectedFile.value && files.value.length) {
      selectedFile.value = files.value[0]!.name
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  }
}

async function loadSelected() {
  if (!selectedFile.value) return
  loading.value = true
  error.value = ''
  try {
    const url = `/v2-api/docs/html?filename=${encodeURIComponent(selectedFile.value)}`
    const res = await fetch(url)
    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Convert failed (${res.status})`)
    }
    const body = (await res.json()) as { html: string; filename: string }
    htmlContent.value = body.html ?? ''
    hasDocument.value = true
    viewerKey.value += 1
  } catch (e) {
    htmlContent.value = ''
    hasDocument.value = false
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

async function onUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return

  loading.value = true
  error.value = ''
  try {
    const form = new FormData()
    form.append('file', file)
    const res = await fetch('/v2-api/docs/upload', { method: 'POST', body: form })
    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Upload failed (${res.status})`)
    }
    const body = (await res.json()) as { name: string }
    await refreshFiles()
    selectedFile.value = body.name
    await loadSelected()
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await refreshFiles()
  if (selectedFile.value) {
    await loadSelected()
  }
})
</script>

<template>
  <div class="app-shell">
    <header class="toolbar">
      <div class="brand">
        <RouterLink class="back" to="/">← Tools</RouterLink>
        <span class="brand-mark">W</span>
        <div>
          <h1>Docx Viewer</h1>
          <p>Aspose.Words → HTML · Jodit</p>
        </div>
      </div>

      <div class="controls">
        <label class="field">
          <span>File</span>
          <select v-model="selectedFile" :disabled="loading || files.length === 0">
            <option disabled value="">Select a document</option>
            <option v-for="file in files" :key="file.name" :value="file.name">
              {{ file.name }}
            </option>
          </select>
        </label>

        <button class="btn" type="button" :disabled="loading || !selectedFile" @click="loadSelected">
          {{ loading ? 'Loading…' : 'Open' }}
        </button>

        <label class="btn btn-secondary upload">
          Upload
          <input type="file" accept=".docx,.doc,.rtf,.odt,.dotx,.dot" hidden @change="onUpload" />
        </label>

        <button class="btn btn-ghost" type="button" :disabled="loading" @click="refreshFiles">
          Refresh
        </button>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <main class="viewer">
      <DocxViewer
        v-if="hasDocument"
        :key="viewerKey"
        :html="htmlContent"
        height="calc(100vh - 96px)"
      />
      <div v-else class="empty">
        <h2>Open a document</h2>
        <p>Choose a Word file from the list or upload a .docx to convert it with Aspose.Words and edit in Jodit.</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  --bg: #eef2f6;
  --panel: #ffffff;
  --ink: #1a2332;
  --muted: #5b6b7c;
  --line: #d5dde6;
  --accent: #2b579a;
  --accent-hover: #1f3f72;
  --danger: #b42318;
  --shadow: 0 1px 2px rgba(26, 35, 50, 0.06), 0 8px 24px rgba(26, 35, 50, 0.06);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(1200px 500px at 10% -10%, #d6e4ff 0%, transparent 55%),
    radial-gradient(900px 400px at 100% 0%, #e8eef8 0%, transparent 50%),
    var(--bg);
  color: var(--ink);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.85rem 1.25rem;
  background: color-mix(in srgb, var(--panel) 92%, transparent);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow);
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.back {
  font-family: 'DM Sans', system-ui, sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--muted);
  text-decoration: none;
  margin-right: 0.25rem;
}

.back:hover {
  color: var(--ink);
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 0.55rem;
  background: linear-gradient(145deg, #4a7ec7, #2b579a);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand h1 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 650;
  font-family: 'DM Sans', system-ui, sans-serif;
}

.brand p {
  margin: 0.1rem 0 0;
  color: var(--muted);
  font-size: 0.8rem;
  font-family: 'DM Sans', system-ui, sans-serif;
}

.controls {
  display: flex;
  align-items: end;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--muted);
  font-family: 'DM Sans', system-ui, sans-serif;
}

.field select {
  min-width: 14rem;
  padding: 0.45rem 0.6rem;
  border: 1px solid var(--line);
  border-radius: 0.45rem;
  background: #fff;
}

.btn {
  border: none;
  border-radius: 0.45rem;
  padding: 0.5rem 0.9rem;
  background: var(--accent);
  color: #fff;
  font-weight: 600;
  font-family: 'DM Sans', system-ui, sans-serif;
  cursor: pointer;
}

.btn:hover:not(:disabled) {
  background: var(--accent-hover);
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn-secondary {
  background: #1a2332;
}

.btn-secondary:hover:not(:disabled) {
  background: #101722;
}

.btn-ghost {
  background: transparent;
  color: var(--ink);
  border: 1px solid var(--line);
}

.btn-ghost:hover:not(:disabled) {
  background: #f4f7fa;
}

.upload {
  display: inline-flex;
  align-items: center;
}

.error {
  margin: 0;
  padding: 0.65rem 1.25rem;
  background: #fef3f2;
  color: var(--danger);
  border-bottom: 1px solid #fecdca;
  font-size: 0.9rem;
  font-family: 'DM Sans', system-ui, sans-serif;
}

.viewer {
  flex: 1;
  min-height: 0;
}

.empty {
  margin: 4rem auto;
  max-width: 28rem;
  text-align: center;
  color: var(--muted);
  font-family: 'DM Sans', system-ui, sans-serif;
}

.empty h2 {
  margin: 0 0 0.5rem;
  color: var(--ink);
  font-size: 1.35rem;
}

.empty p {
  margin: 0;
  line-height: 1.5;
}
</style>
