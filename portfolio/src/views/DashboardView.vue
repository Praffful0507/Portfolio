<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import DashboardTile from "@/components/DashboardTile.vue";

interface Tool {
  id: string;
  title: string;
  description: string;
  accent: string;
  route?: string;
}

const router = useRouter();

const tools: Tool[] = [
  {
    id: "excel",
    title: "Excel Render",
    description: "Open and preview spreadsheet files",
    accent: "#217346",
    route: "/excel",
  },
  {
    id: "docx",
    title: "Docx Viewer",
    description: "Read Word documents in the browser",
    accent: "#2B579A",
    route: "/docx",
  },
  {
    id: "calendar",
    title: "Calendar",
    description: "Schedule and manage your events",
    accent: "#C43E1C",
    route: "/calendar",
  },
  {
    id: "pdf",
    title: "PDF Viewer",
    description: "View and navigate PDF documents",
    accent: "#E5252A",
    route: "/pdf",
  },
];

const selectedId = ref<string | null>(null);

function selectTool(id: string) {
  selectedId.value = id;
  const tool = tools.find((t) => t.id === id);
  if (tool?.route) {
    router.push(tool.route);
  }
}
</script>

<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <p class="dashboard__brand">Workspace</p>
      <h1 class="dashboard__title">Tools</h1>
      <p class="dashboard__subtitle">Pick a viewer to get started</p>
    </header>

    <section class="dashboard__grid" aria-label="Available tools">
      <DashboardTile
        v-for="(tool, index) in tools"
        :key="tool.id"
        :title="tool.title"
        :description="tool.description"
        :accent="tool.accent"
        :active="selectedId === tool.id"
        :style="{ '--delay': `${index * 60}ms` }"
        @select="selectTool(tool.id)"
      />

      <button
        v-for="n in 1"
        :key="`empty-${n}`"
        type="button"
        class="dashboard__slot"
        :style="{ '--delay': `${(tools.length + n - 1) * 60}ms` }"
        aria-label="Add tool"
      >
        <span class="dashboard__slot-plus" aria-hidden="true">+</span>
      </button>
    </section>

    <p v-if="selectedId" class="dashboard__status" role="status">
      Selected:
      <strong>{{ tools.find((t) => t.id === selectedId)?.title }}</strong>
    </p>
  </div>
</template>

<style scoped>
.dashboard {
  width: min(960px, 100%);
  margin: 0 auto;
  padding: 3rem 1.5rem 4rem;
}

.dashboard__header {
  margin-bottom: 2.5rem;
  animation: rise 0.5s ease both;
}

.dashboard__brand {
  font-family: "DM Sans", system-ui, sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.dashboard__title {
  font-family: "Fraunces", Georgia, serif;
  font-size: clamp(2rem, 4vw, 2.75rem);
  font-weight: 600;
  letter-spacing: -0.02em;
  color: #111827;
  line-height: 1.15;
}

.dashboard__subtitle {
  margin-top: 0.5rem;
  font-family: "DM Sans", system-ui, sans-serif;
  font-size: 1rem;
  color: #6b7280;
}

.dashboard__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.dashboard__slot {
  min-height: 140px;
  display: grid;
  place-items: center;
  border: 2px dashed #d1d5db;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease;
  animation: rise 0.55s ease both;
  animation-delay: var(--delay, 0ms);
}

.dashboard__slot:hover {
  border-color: #9ca3af;
  color: #4b5563;
  background: #f9fafb;
  transform: translateY(-2px);
}

.dashboard__slot-plus {
  font-size: 1.75rem;
  font-weight: 300;
  line-height: 1;
}

.dashboard__status {
  margin-top: 2rem;
  font-family: "DM Sans", system-ui, sans-serif;
  font-size: 0.95rem;
  color: #4b5563;
  animation: rise 0.35s ease both;
}

.dashboard__status strong {
  font-weight: 600;
  color: #111827;
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
  .dashboard__grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .dashboard__slot {
    min-height: 120px;
  }
}

@media (max-width: 480px) {
  .dashboard {
    padding: 2rem 1rem 3rem;
  }

  .dashboard__grid {
    grid-template-columns: 1fr;
  }
}
</style>
