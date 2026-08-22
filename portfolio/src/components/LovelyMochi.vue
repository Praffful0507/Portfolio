<script setup lang="ts">
defineProps<{
  mood?: 'ask' | 'yes'
}>()
</script>

<template>
  <div class="mochi" :class="{ 'mochi--yes': mood === 'yes' }" aria-hidden="true">
    <svg class="mochi__svg" viewBox="0 0 200 200" role="img">
      <title>Cute cat</title>
      <!-- peach cushion -->
      <ellipse cx="100" cy="158" rx="62" ry="22" fill="#ffc4b0" />
      <ellipse cx="100" cy="154" rx="50" ry="14" fill="#ffd8c8" />

      <!-- body -->
      <g class="mochi__body">
        <ellipse cx="100" cy="118" rx="70" ry="52" fill="#fffdfb" stroke="#e8ddd6" stroke-width="1.5" />
        <ellipse cx="100" cy="108" rx="68" ry="58" fill="#fffdfb" />

        <!-- ears -->
        <path d="M48 78 C42 42 72 38 78 72" fill="#fffdfb" stroke="#e8ddd6" stroke-width="1.5" />
        <path d="M152 78 C158 42 128 38 122 72" fill="#fffdfb" stroke="#e8ddd6" stroke-width="1.5" />
        <path d="M52 72 C50 52 68 50 72 70" fill="#ffc5d4" />
        <path d="M148 72 C150 52 132 50 128 70" fill="#ffc5d4" />

        <!-- head stripes -->
        <path d="M88 52 q4 16 0 28" fill="none" stroke="#c9c0ba" stroke-width="3.5" stroke-linecap="round" />
        <path d="M100 48 q4 18 0 32" fill="none" stroke="#c9c0ba" stroke-width="4" stroke-linecap="round" />
        <path d="M112 52 q-4 16 0 28" fill="none" stroke="#c9c0ba" stroke-width="3.5" stroke-linecap="round" />

        <!-- blush -->
        <ellipse cx="58" cy="118" rx="16" ry="10" fill="#ffb3c4" opacity="0.85" />
        <ellipse cx="142" cy="118" rx="16" ry="10" fill="#ffb3c4" opacity="0.85" />

        <!-- eyes -->
        <g class="mochi__eyes">
          <ellipse cx="74" cy="102" rx="13" ry="16" fill="#2a211c" />
          <ellipse cx="126" cy="102" rx="13" ry="16" fill="#2a211c" />
          <circle cx="79" cy="96" r="4.5" fill="#fff" />
          <circle cx="131" cy="96" r="4.5" fill="#fff" />
          <circle cx="69" cy="108" r="2" fill="#fff" opacity="0.8" />
          <circle cx="121" cy="108" r="2" fill="#fff" opacity="0.8" />
        </g>

        <!-- mouth + tongue -->
        <path d="M92 128 Q100 136 108 128" fill="none" stroke="#2a211c" stroke-width="2.4" stroke-linecap="round" />
        <ellipse v-if="mood !== 'yes'" cx="100" cy="136" rx="7" ry="6" fill="#ff7a9a" />
        <path v-else d="M90 126 Q100 142 110 126" fill="#ff7a9a" />

        <!-- tiny paws -->
        <ellipse cx="62" cy="148" rx="16" ry="10" fill="#fffdfb" stroke="#e8ddd6" stroke-width="1.2" />
        <ellipse cx="138" cy="148" rx="16" ry="10" fill="#fffdfb" stroke="#e8ddd6" stroke-width="1.2" />
      </g>
    </svg>
  </div>
</template>

<style scoped>
.mochi {
  width: min(220px, 58vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 10px 28px #00000014;
  overflow: hidden;
}

.mochi__svg {
  width: 88%;
  height: 88%;
}

.mochi__body {
  transform-origin: 100px 160px;
  animation: bob 2.4s ease-in-out infinite;
}

.mochi__eyes {
  transform-origin: 100px 102px;
  animation: blink 4.2s ease-in-out infinite;
}

.mochi--yes .mochi__body {
  animation: hop 0.7s ease;
}

@keyframes bob {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes hop {
  0% {
    transform: scale(0.9);
  }
  40% {
    transform: scale(1.08) translateY(-8px);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes blink {
  0%,
  88%,
  100% {
    transform: scaleY(1);
  }
  92% {
    transform: scaleY(0.08);
  }
}
</style>
