<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import CalendarViewer from '@/components/CalendarViewer.vue'
import type { EventApi } from '@fullcalendar/vue3'

const weekendsVisible = ref(true)
const currentEvents = ref<EventApi[]>([])

function formatEventWhen(event: EventApi) {
  if (event.allDay) {
    return event.start?.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    })
  }

  return event.start?.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

function onEventsChange(events: EventApi[]) {
  currentEvents.value = events
}

function toggleWeekends() {
  weekendsVisible.value = !weekendsVisible.value
}
</script>

<template>
  <div class="app-shell">
    <header class="toolbar">
      <div class="brand">
        <RouterLink class="back" to="/">← Tools</RouterLink>
        <span class="brand-mark">Cal</span>
        <div>
          <h1>Calendar</h1>
          <p>FullCalendar · month, week, day, list</p>
        </div>
      </div>

      <div class="controls">
        <button class="btn btn-ghost" type="button" @click="toggleWeekends">
          {{ weekendsVisible ? 'Hide weekends' : 'Show weekends' }}
        </button>
      </div>
    </header>

    <div class="layout">
      <aside class="sidebar">
        <h2>Events</h2>
        <p class="hint">Select a date range to add. Click an event to remove. Drag to reschedule.</p>
        <ul v-if="currentEvents.length" class="event-list">
          <li v-for="event in currentEvents" :key="event.id" class="event-item">
            <span class="event-title">{{ event.title }}</span>
            <span class="event-when">{{ formatEventWhen(event) }}</span>
          </li>
        </ul>
        <p v-else class="empty-events">No events yet</p>
      </aside>

      <main class="viewer">
        <CalendarViewer :weekends="weekendsVisible" @events-change="onEventsChange" />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  --bg: #eef2f6;
  --panel: #ffffff;
  --ink: #1a2332;
  --muted: #5b6b7c;
  --line: #d5dde6;
  --accent: #c43e1c;
  --accent-hover: #a83418;
  --shadow: 0 1px 2px rgba(26, 35, 50, 0.06), 0 8px 24px rgba(26, 35, 50, 0.06);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(1200px 500px at 10% -10%, #ffe4d6 0%, transparent 55%),
    radial-gradient(900px 400px at 100% 0%, #f3e8e2 0%, transparent 50%),
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
  background: linear-gradient(145deg, #e05a35, #c43e1c);
  color: #fff;
  font-weight: 700;
  font-size: 0.75rem;
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

.btn-ghost {
  background: transparent;
  color: var(--ink);
  border: 1px solid var(--line);
}

.btn-ghost:hover:not(:disabled) {
  background: #f4f7fa;
}

.layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 260px 1fr;
}

.sidebar {
  padding: 1.25rem;
  border-right: 1px solid var(--line);
  background: color-mix(in srgb, var(--panel) 88%, transparent);
  overflow: auto;
  font-family: 'DM Sans', system-ui, sans-serif;
}

.sidebar h2 {
  margin: 0 0 0.4rem;
  font-size: 0.95rem;
  font-weight: 650;
}

.hint {
  margin: 0 0 1rem;
  font-size: 0.8rem;
  line-height: 1.45;
  color: var(--muted);
}

.event-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.event-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  padding-left: 0.65rem;
  border-left: 3px solid var(--accent);
}

.event-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--ink);
}

.event-when {
  font-size: 0.75rem;
  color: var(--muted);
}

.empty-events {
  margin: 0;
  font-size: 0.85rem;
  color: var(--muted);
}

.viewer {
  flex: 1;
  min-height: 0;
  min-width: 0;
  background: color-mix(in srgb, var(--panel) 70%, transparent);
}

@media (max-width: 840px) {
  .layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid var(--line);
    max-height: 220px;
  }

  .viewer {
    min-height: calc(100vh - 280px);
  }
}
</style>
