<script setup lang="ts">
import { reactive, watch } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import type {
  CalendarOptions,
  DateSelectInfo,
  EventApi,
  EventClickInfo,
  EventInput,
} from '@fullcalendar/vue3'
import classicThemePlugin from '@fullcalendar/vue3/themes/classic'
import dayGridPlugin from '@fullcalendar/vue3/daygrid'
import timeGridPlugin from '@fullcalendar/vue3/timegrid'
import listPlugin from '@fullcalendar/vue3/list'
import interactionPlugin from '@fullcalendar/vue3/interaction'

import '@fullcalendar/vue3/skeleton.css'
import '@fullcalendar/vue3/themes/classic/theme.css'
import '@fullcalendar/vue3/themes/classic/palette.css'

const props = withDefaults(
  defineProps<{
    weekends?: boolean
  }>(),
  {
    weekends: true,
  },
)

const emit = defineEmits<{
  eventsChange: [events: EventApi[]]
}>()

let eventId = 0
function createEventId() {
  eventId += 1
  return String(eventId)
}

function startOfToday() {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d
}

function addDays(date: Date, days: number) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function atHour(date: Date, hour: number, minute = 0) {
  const next = new Date(date)
  next.setHours(hour, minute, 0, 0)
  return next
}

const today = startOfToday()

const initialEvents: EventInput[] = [
  {
    id: createEventId(),
    title: 'All-day kickoff',
    start: today,
    allDay: true,
  },
  {
    id: createEventId(),
    title: 'Team standup',
    start: atHour(today, 9, 30),
    end: atHour(today, 10),
  },
  {
    id: createEventId(),
    title: 'Design review',
    start: atHour(addDays(today, 1), 13),
    end: atHour(addDays(today, 1), 14, 30),
  },
  {
    id: createEventId(),
    title: 'Client sync',
    start: atHour(addDays(today, 2), 11),
    end: atHour(addDays(today, 2), 12),
  },
  {
    id: createEventId(),
    title: 'Sprint planning',
    start: addDays(today, 3),
    end: addDays(today, 5),
    allDay: true,
  },
]

function handleDateSelect(selectInfo: DateSelectInfo) {
  const title = window.prompt('Event title')
  const calendarApi = selectInfo.view.calendar
  calendarApi.unselect()

  if (!title?.trim()) return

  calendarApi.addEvent({
    id: createEventId(),
    title: title.trim(),
    start: selectInfo.start,
    end: selectInfo.end,
    allDay: selectInfo.allDay,
  })
}

function handleEventClick(clickInfo: EventClickInfo) {
  if (window.confirm(`Delete "${clickInfo.event.title}"?`)) {
    clickInfo.event.remove()
  }
}

function handleEvents(events: EventApi[]) {
  emit('eventsChange', events)
}

const calendarOptions = reactive<CalendarOptions>({
  plugins: [classicThemePlugin, dayGridPlugin, timeGridPlugin, listPlugin, interactionPlugin],
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek',
  },
  initialView: 'dayGridMonth',
  editable: true,
  selectable: true,
  selectMirror: true,
  dayMaxEvents: true,
  weekends: props.weekends,
  events: initialEvents,
  select: handleDateSelect,
  eventClick: handleEventClick,
  eventsSet: handleEvents,
  height: '100%',
})

watch(
  () => props.weekends,
  (value) => {
    calendarOptions.weekends = value
  },
)
</script>

<template>
  <div class="calendar-viewer">
    <FullCalendar :options="calendarOptions" />
  </div>
</template>

<style scoped>
.calendar-viewer {
  height: 100%;
  min-height: 0;
  padding: 1rem 1.25rem 1.25rem;
}

.calendar-viewer :deep(.fc) {
  font-family: 'DM Sans', system-ui, sans-serif;
  --fc-border-color: #d5dde6;
  --fc-button-bg-color: #c43e1c;
  --fc-button-border-color: #c43e1c;
  --fc-button-hover-bg-color: #a83418;
  --fc-button-hover-border-color: #a83418;
  --fc-button-active-bg-color: #8f2c14;
  --fc-button-active-border-color: #8f2c14;
  --fc-event-bg-color: #c43e1c;
  --fc-event-border-color: #c43e1c;
  --fc-today-bg-color: rgba(196, 62, 28, 0.08);
}
</style>
