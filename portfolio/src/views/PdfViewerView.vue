<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import WebViewer from '@/components/WebViewer.vue'

const DEMO_DOC =
  'https://apryse.s3.amazonaws.com/public/files/samples/WebviewerDemoDoc.pdf'

const documentUrl = ref(DEMO_DOC)
const documentLabel = ref('WebviewerDemoDoc.pdf')
const viewerKey = ref(0)
const error = ref('')

function loadDemo() {
  error.value = ''
  documentUrl.value = DEMO_DOC
  documentLabel.value = 'WebviewerDemoDoc.pdf'
  viewerKey.value += 1
}

function onOpenFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return

  error.value = ''
  if (documentUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(documentUrl.value)
  }
  documentUrl.value = URL.createObjectURL(file)
  documentLabel.value = file.name
  viewerKey.value += 1
}
</script>

<template>
  <div class="app-shell">
    <header class="toolbar">
      <div class="brand">
        <RouterLink class="back" to="/">← Tools</RouterLink>
        <span class="brand-mark">PDF</span>
        <div>
          <h1>PDF Viewer</h1>
          <p>Apryse WebViewer · {{ documentLabel }}</p>
        </div>
      </div>

      <div class="controls">
        <button class="btn" type="button" @click="loadDemo">Load demo</button>

        <label class="btn btn-secondary upload">
          Open PDF
          <input type="file" accept=".pdf,application/pdf" hidden @change="onOpenFile" />
        </label>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <main class="viewer">
      <WebViewer :key="viewerKey" :initial-doc="documentUrl" />
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
  --accent: #e5252a;
  --accent-hover: #c41e22;
  --danger: #b42318;
  --shadow: 0 1px 2px rgba(26, 35, 50, 0.06), 0 8px 24px rgba(26, 35, 50, 0.06);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(1200px 500px at 10% -10%, #ffd6d8 0%, transparent 55%),
    radial-gradient(900px 400px at 100% 0%, #ffe8e9 0%, transparent 50%),
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
  background: linear-gradient(145deg, #f05a5e, #e5252a);
  color: #fff;
  font-weight: 700;
  font-size: 0.7rem;
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

.btn-secondary {
  background: #1a2332;
}

.btn-secondary:hover:not(:disabled) {
  background: #101722;
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
</style>
