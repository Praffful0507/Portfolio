<script setup lang="ts">
defineProps<{
  title: string
  description: string
  accent: string
  active?: boolean
}>()

defineEmits<{
  select: []
}>()
</script>

<template>
  <button
    type="button"
    class="tile"
    :class="{ 'tile--active': active }"
    :style="{ '--accent': accent }"
    @click="$emit('select')"
  >
    <span class="tile__accent" aria-hidden="true" />
    <span class="tile__title">{{ title }}</span>
    <span class="tile__desc">{{ description }}</span>
  </button>
</template>

<style scoped>
.tile {
  position: relative;
  min-height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 1.5rem 1.25rem;
  border: 2.5px solid #111827;
  background: #ffffff;
  color: #111827;
  text-align: center;
  cursor: pointer;
  overflow: hidden;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease,
    background-color 0.22s ease;
  animation: rise 0.55s ease both;
  animation-delay: var(--delay, 0ms);
}

.tile:hover {
  transform: translateY(-4px);
  box-shadow: 4px 4px 0 #111827;
}

.tile:focus-visible {
  outline: 3px solid var(--accent);
  outline-offset: 3px;
}

.tile--active {
  background: #111827;
  color: #ffffff;
  box-shadow: 4px 4px 0 var(--accent);
}

.tile--active:hover {
  transform: translateY(-4px);
}

.tile__accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--accent);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.28s ease;
}

.tile:hover .tile__accent,
.tile--active .tile__accent {
  transform: scaleX(1);
}

.tile__title {
  font-family: 'DM Sans', system-ui, sans-serif;
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.tile__desc {
  font-family: 'DM Sans', system-ui, sans-serif;
  font-size: 0.8rem;
  line-height: 1.4;
  color: #6b7280;
  max-width: 16ch;
}

.tile--active .tile__desc {
  color: #d1d5db;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .tile {
    min-height: 120px;
    padding: 1.25rem 1rem;
  }

  .tile__title {
    font-size: 1rem;
  }
}
</style>
