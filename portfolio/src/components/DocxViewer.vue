<script setup lang="ts">
import { Jodit } from 'jodit'
import 'jodit/es2021/jodit.min.css'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    html?: string
    height?: number | string
    readonly?: boolean
  }>(),
  {
    html: '',
    height: 'calc(100vh - 96px)',
    readonly: false,
  },
)

const emit = defineEmits<{
  change: [html: string]
  ready: [editor: Jodit]
}>()

const editorEl = ref<HTMLTextAreaElement | null>(null)
let editor: Jodit | null = null

function resolveHeight(value: number | string): number | string {
  return value
}

onMounted(() => {
  if (!editorEl.value) return

  editor = Jodit.make(editorEl.value, {
    height: resolveHeight(props.height),
    readonly: props.readonly,
    toolbarSticky: false,
    showCharsCounter: true,
    showWordsCounter: true,
    askBeforePasteHTML: false,
    askBeforePasteFromWord: false,
    defaultActionOnPaste: 'insert_as_html',
    buttons: [
      'source',
      '|',
      'bold',
      'italic',
      'underline',
      'strikethrough',
      '|',
      'ul',
      'ol',
      '|',
      'font',
      'fontsize',
      'brush',
      'paragraph',
      '|',
      'table',
      'link',
      'image',
      '|',
      'align',
      'outdent',
      'indent',
      '|',
      'hr',
      'eraser',
      'copyformat',
      '|',
      'undo',
      'redo',
      '|',
      'fullsize',
      'print',
    ],
    uploader: { insertImageAsBase64URI: true },
  })

  editor.value = props.html
  editor.events.on('change', () => {
    if (editor) emit('change', editor.value)
  })
  emit('ready', editor)
})

watch(
  () => props.html,
  (next) => {
    if (!editor || editor.value === next) return
    editor.value = next ?? ''
  },
)

watch(
  () => props.readonly,
  (next) => {
    if (!editor) return
    editor.setReadOnly(next)
  },
)

onBeforeUnmount(() => {
  if (editor) {
    editor.destruct()
    editor = null
  }
})
</script>

<template>
  <div class="docx-viewer">
    <textarea ref="editorEl" />
  </div>
</template>

<style scoped>
.docx-viewer {
  height: 100%;
  min-height: 0;
  background: #fff;
}

.docx-viewer :deep(.jodit-container) {
  border: none;
  border-radius: 0;
}

.docx-viewer :deep(.jodit-workplace) {
  background: #f7f8fa;
}

.docx-viewer :deep(.jodit-wysiwyg) {
  padding: 2rem 2.5rem !important;
  max-width: 816px;
  margin: 1.25rem auto;
  background: #fff;
  box-shadow: 0 1px 3px rgba(26, 35, 50, 0.08), 0 12px 32px rgba(26, 35, 50, 0.08);
  min-height: 100% !important;
}
</style>
