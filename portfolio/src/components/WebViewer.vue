<template>
  <div ref="viewer" class="webviewer"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import WebViewer from '@pdftron/webviewer'

const props = defineProps<{
  initialDoc?: string
  licenseKey?: string
}>()

const viewer = ref<HTMLElement | null>(null)

type WebViewerInstance = Awaited<ReturnType<typeof WebViewer>>
let instance: WebViewerInstance | null = null

onMounted(async () => {
  if (!viewer.value) return

  instance = await WebViewer(
    {
      path: `${import.meta.env.BASE_URL}lib/webviewer`,
      initialDoc: props.initialDoc,
      licenseKey: props.licenseKey ?? import.meta.env.VITE_APRYSE_LICENSE_KEY ?? '',
    },
    viewer.value,
  )
})

watch(
  () => props.initialDoc,
  (doc) => {
    if (!instance || !doc) return
    instance.UI.loadDocument(doc)
  },
)

onBeforeUnmount(() => {
  instance = null
})
</script>

<style scoped>
.webviewer {
  height: calc(100vh - 96px);
  width: 100%;
}
</style>
