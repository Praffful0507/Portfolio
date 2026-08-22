<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import LovelyMochi from '@/components/LovelyMochi.vue'

const DEFAULT_QUESTION = 'Do you love me?'
const DEFAULT_YES = 'I knew it ❤️'
const DRAFT_KEY = 'lovely-advance-draft'

const question = ref(DEFAULT_QUESTION)
const yesMessage = ref(DEFAULT_YES)
const questionImageUrl = ref('')
const yesImageUrl = ref('')
const questionImageBroken = ref(false)
const yesImageBroken = ref(false)
const showResult = ref(false)
const showAdvance = ref(false)
const draftQuestion = ref('')
const draftMessage = ref('I knew it ❤️')
const draftQuestionImage = ref('')
const draftYesImage = ref('')
const copied = ref(false)
const noStyle = ref<Record<string, string>>({})
const fleeing = ref(false)
const isSharedLink = ref(false)

const displayQuestion = computed(() => question.value || DEFAULT_QUESTION)
const safeQuestionImage = computed(() => sanitizeImageUrl(questionImageUrl.value))
const safeYesImage = computed(() => sanitizeImageUrl(yesImageUrl.value))
const draftSafeQuestionImage = computed(() => sanitizeImageUrl(draftQuestionImage.value))
const draftSafeYesImage = computed(() => sanitizeImageUrl(draftYesImage.value))
const showQuestionImage = computed(
  () => Boolean(safeQuestionImage.value) && !questionImageBroken.value,
)
const showYesImage = computed(() => Boolean(safeYesImage.value) && !yesImageBroken.value)

function sanitizeImageUrl(value: string | null | undefined): string {
  const raw = value?.trim() ?? ''
  if (!raw) return ''
  try {
    const url = new URL(raw)
    if (url.protocol !== 'http:' && url.protocol !== 'https:') return ''
    return url.href
  } catch {
    return ''
  }
}

function applyImages(questionSrc: string, yesSrc: string) {
  questionImageUrl.value = sanitizeImageUrl(questionSrc)
  yesImageUrl.value = sanitizeImageUrl(yesSrc)
  questionImageBroken.value = false
  yesImageBroken.value = false
}

function saveDraft() {
  localStorage.setItem(
    DRAFT_KEY,
    JSON.stringify({
      q: draftQuestion.value,
      m: draftMessage.value,
      qimg: draftQuestionImage.value,
      yimg: draftYesImage.value,
    }),
  )
}

function loadDraft() {
  try {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return
    const data = JSON.parse(raw) as {
      q?: string
      m?: string
      img?: string
      qimg?: string
      yimg?: string
    }
    if (data.q) draftQuestion.value = data.q
    if (data.m) draftMessage.value = data.m
    const questionSrc = data.qimg || data.img || ''
    const yesSrc = data.yimg || ''
    if (questionSrc) draftQuestionImage.value = questionSrc
    if (yesSrc) draftYesImage.value = yesSrc
    applyImages(questionSrc, yesSrc)
  } catch {
    /* ignore broken drafts */
  }
}

function parseHash() {
  const raw = window.location.hash.replace(/^#/, '')
  const params = new URLSearchParams(raw)
  const q = params.get('q')?.trim()
  const m = params.get('m')?.trim()
  const questionSrc = params.get('qimg')?.trim() || params.get('img')?.trim() || ''
  const yesSrc = params.get('yimg')?.trim() || ''
  isSharedLink.value = Boolean(q || questionSrc || yesSrc)
  question.value = q || DEFAULT_QUESTION
  yesMessage.value = m || DEFAULT_YES
  showResult.value = false
  fleeing.value = false
  noStyle.value = {}
  if (q) draftQuestion.value = q
  if (m) draftMessage.value = m
  if (questionSrc || yesSrc) {
    if (questionSrc) draftQuestionImage.value = questionSrc
    if (yesSrc) draftYesImage.value = yesSrc
    applyImages(questionSrc, yesSrc)
  } else if (!isSharedLink.value) {
    applyImages(draftQuestionImage.value, draftYesImage.value)
  } else {
    applyImages('', '')
  }
}

function sharePath() {
  const params = new URLSearchParams()
  params.set('q', draftQuestion.value.trim() || DEFAULT_QUESTION)
  const message = draftMessage.value.trim()
  if (message) params.set('m', message)
  const questionSrc = sanitizeImageUrl(draftQuestionImage.value)
  const yesSrc = sanitizeImageUrl(draftYesImage.value)
  if (questionSrc) params.set('img', questionSrc)
  if (yesSrc) params.set('yimg', yesSrc)
  const base = import.meta.env.BASE_URL.endsWith('/')
    ? import.meta.env.BASE_URL
    : `${import.meta.env.BASE_URL}/`
  return `${window.location.origin}${base}lovely#${params.toString()}`
}

async function copyLink() {
  const url = sharePath()
  try {
    await navigator.clipboard.writeText(url)
  } catch {
    window.prompt('Copy this link:', url)
  }
  copied.value = true
  saveDraft()
  window.location.hash = url.split('#')[1] ?? ''
  window.setTimeout(() => {
    copied.value = false
  }, 2200)
}

function moveNo(event?: Event) {
  event?.preventDefault()
  fleeing.value = true
  const btnW = 110
  const btnH = 52
  const pad = 12
  const maxX = Math.max(pad, window.innerWidth - btnW - pad)
  const maxY = Math.max(pad, window.innerHeight - btnH - pad)
  const x = pad + Math.random() * (maxX - pad)
  const y = pad + Math.random() * (maxY - pad)
  noStyle.value = {
    position: 'fixed',
    left: `${x}px`,
    top: `${y}px`,
    transform: 'none',
    zIndex: '30',
  }
}

function sayYes() {
  showResult.value = true
}

watch([draftQuestion, draftMessage, draftQuestionImage, draftYesImage], () => {
  applyImages(draftQuestionImage.value, draftYesImage.value)
  if (!isSharedLink.value) saveDraft()
})

onMounted(() => {
  parseHash()
  if (!isSharedLink.value) loadDraft()
  else showAdvance.value = false
  window.addEventListener('hashchange', parseHash)
})

onUnmounted(() => {
  window.removeEventListener('hashchange', parseHash)
})
</script>

<template>
  <div class="lovely">
    <div class="lovely__glow" aria-hidden="true" />

    <header class="lovely__top">
      <RouterLink class="lovely__back" to="/">← Tools</RouterLink>
      <button
        v-if="!isSharedLink || showAdvance"
        type="button"
        class="lovely__advance-toggle"
        @click="showAdvance = !showAdvance"
      >
        {{ showAdvance ? 'Close' : 'Advance' }}
      </button>
      <button
        v-else
        type="button"
        class="lovely__advance-toggle"
        @click="showAdvance = true"
      >
        Create your own
      </button>
    </header>

    <section v-if="showAdvance" class="builder" aria-label="Advance: custom question">
      <p class="builder__badge">Advance</p>
      <h2 class="builder__title">Write any question</h2>
      <p class="builder__hint">
        First picture shows with the question. Second shows after they tap Yes. JPG, PNG, WebP, and
        GIF (including animated GIFs) all work — paste a direct image link.
      </p>

      <label class="builder__label" for="lovely-q">Question</label>
      <input
        id="lovely-q"
        v-model="draftQuestion"
        class="builder__input"
        type="text"
        maxlength="120"
        placeholder="Do you love me?"
      />

      <label class="builder__label" for="lovely-m">Message after Yes</label>
      <input
        id="lovely-m"
        v-model="draftMessage"
        class="builder__input"
        type="text"
        maxlength="80"
        placeholder="I knew it ❤️"
      />

      <label class="builder__label" for="lovely-qimg">Question image / GIF link</label>
      <input
        id="lovely-qimg"
        v-model="draftQuestionImage"
        class="builder__input"
        type="url"
        inputmode="url"
        placeholder="https://media.example.com/cute.gif"
      />
      <p v-if="draftQuestionImage.trim() && !draftSafeQuestionImage" class="builder__error">
        Use a full http:// or https:// image link.
      </p>

      <label class="builder__label" for="lovely-yimg">Yes image / GIF link</label>
      <input
        id="lovely-yimg"
        v-model="draftYesImage"
        class="builder__input"
        type="url"
        inputmode="url"
        placeholder="https://media.example.com/happy.gif"
      />
      <p v-if="draftYesImage.trim() && !draftSafeYesImage" class="builder__error">
        Use a full http:// or https:// image link.
      </p>

      <div v-if="draftSafeQuestionImage || draftSafeYesImage" class="builder__previews">
        <div v-if="draftSafeQuestionImage" class="builder__preview">
          <span>Question</span>
          <img :src="draftSafeQuestionImage" alt="Question preview" referrerpolicy="no-referrer" />
        </div>
        <div v-if="draftSafeYesImage" class="builder__preview">
          <span>After Yes</span>
          <img :src="draftSafeYesImage" alt="Yes preview" referrerpolicy="no-referrer" />
        </div>
      </div>

      <button type="button" class="builder__copy" @click="copyLink">
        {{ copied ? 'Copied!' : 'Generate & copy link' }}
      </button>
    </section>

    <main v-if="!showResult" class="stage">
      <div class="photo">
        <img
          v-if="showQuestionImage"
          :src="safeQuestionImage"
          alt=""
          referrerpolicy="no-referrer"
          @error="questionImageBroken = true"
        />
        <LovelyMochi v-else mood="ask" />
      </div>
      <h1 class="stage__question">{{ displayQuestion }}</h1>
      <p class="stage__hint">Put Your Answer.</p>
      <div class="stage__buttons">
        <button id="yes" type="button" class="btn btn--yes" @click="sayYes">Yes</button>
        <button
          id="no"
          type="button"
          class="btn btn--no"
          :class="{ 'btn--flee': fleeing }"
          :style="noStyle"
          @mouseenter="moveNo"
          @pointerdown="moveNo"
          @touchstart.prevent="moveNo"
        >
          No
        </button>
      </div>
    </main>

    <main v-else class="result" role="status">
      <div class="photo">
        <img
          v-if="showYesImage"
          :src="safeYesImage"
          alt=""
          referrerpolicy="no-referrer"
          @error="yesImageBroken = true"
        />
        <LovelyMochi v-else mood="yes" />
      </div>
      <h1 class="result__title">{{ yesMessage }}</h1>
      <button type="button" class="result__again" @click="parseHash">Ask again</button>
    </main>
  </div>
</template>

<style scoped>
.lovely {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 1.25rem 1.25rem 3rem;
  color: #4a1c32;
  background: linear-gradient(165deg, #fff5f8 0%, #ffe4ec 42%, #ffd6e5 100%);
}

.lovely__glow {
  pointer-events: none;
  position: absolute;
  inset: -20% 10% auto;
  height: 50%;
  background: radial-gradient(circle, #ffb7c8 0%, transparent 70%);
  opacity: 0.55;
}

.lovely__top {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.lovely__back,
.lovely__advance-toggle {
  font-size: 0.85rem;
  font-weight: 600;
  color: #9f1239;
  background: #ffffffcc;
  border: 1px solid #fecdd3;
  border-radius: 999px;
  padding: 0.4rem 0.9rem;
  text-decoration: none;
  cursor: pointer;
}

.builder {
  position: relative;
  z-index: 2;
  width: min(440px, 100%);
  margin: 1.25rem auto 0;
  padding: 1.25rem 1.2rem 1.35rem;
  background: #ffffffee;
  border: 1px solid #fecdd3;
  border-radius: 1.25rem;
  box-shadow: 0 12px 40px #e11d4814;
}

.builder__badge {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #be123c;
  background: #ffe4e6;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
}

.builder__title {
  margin-top: 0.55rem;
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.35rem;
  font-weight: 600;
}

.builder__hint {
  margin: 0.35rem 0 1rem;
  font-size: 0.85rem;
  color: #9f1239;
}

.builder__label {
  display: block;
  margin-top: 0.7rem;
  margin-bottom: 0.3rem;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #881337;
}

.builder__input {
  width: 100%;
  padding: 0.7rem 0.85rem;
  border: 1.5px solid #fecdd3;
  border-radius: 0.75rem;
  background: #fff;
  color: #4a1c32;
}

.builder__input:focus {
  outline: 2px solid #e11d48;
  outline-offset: 1px;
}

.builder__error {
  margin-top: 0.4rem;
  font-size: 0.8rem;
  color: #be123c;
}

.builder__previews {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-top: 0.85rem;
}

.builder__preview {
  overflow: hidden;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px #00000012;
}

.builder__preview span {
  display: block;
  padding: 0.4rem 0.5rem 0;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #9f1239;
}

.builder__preview img {
  width: 100%;
  height: 110px;
  object-fit: cover;
}

.builder__copy {
  width: 100%;
  margin-top: 1rem;
  padding: 0.8rem 1rem;
  border: 0;
  border-radius: 999px;
  background: #e11d48;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.stage,
.result {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 5.5rem);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 2rem 0.5rem 4rem;
  gap: 0.85rem;
}

.photo {
  width: min(220px, 58vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  overflow: hidden;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 10px 28px #00000014;
}

.photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo :deep(.mochi) {
  width: 100%;
  box-shadow: none;
  border-radius: 0;
}

.stage__question {
  max-width: 22ch;
  font-family: 'DM Sans', system-ui, sans-serif;
  font-size: clamp(1.35rem, 4vw, 1.85rem);
  font-weight: 700;
  line-height: 1.25;
  color: #111827;
}

.stage__hint {
  font-size: 0.95rem;
  color: #374151;
}

.stage__buttons {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 0.75rem;
  min-height: 3.25rem;
  width: min(320px, 100%);
}

.btn {
  min-width: 6.5rem;
  padding: 0.7rem 1.6rem;
  border: 0;
  border-radius: 10px;
  font-size: 1.05rem;
  font-weight: 700;
  cursor: pointer;
  background: #fff;
  color: #111827;
  box-shadow: 0 4px 0 #d1d5db, 0 8px 16px #00000012;
}

.btn--yes:hover {
  background: #fff1f2;
}

.btn--no {
  color: #111827;
}

.btn--flee {
  margin: 0;
}

.result__title {
  margin-top: 0.5rem;
  font-family: 'Fraunces', Georgia, serif;
  font-size: clamp(1.8rem, 5vw, 2.6rem);
}

.result__again {
  margin-top: 1.25rem;
  padding: 0.55rem 1.1rem;
  border: 1px solid #fecdd3;
  border-radius: 999px;
  background: #fff;
  color: #9f1239;
  font-weight: 600;
  cursor: pointer;
}
</style>
