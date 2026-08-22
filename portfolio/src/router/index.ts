import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/excel',
      name: 'excel',
      component: () => import('../views/ExcelRenderView.vue'),
    },
    {
      path: '/docx',
      name: 'docx',
      component: () => import('../views/DocxViewerView.vue'),
    },
    {
      path: '/calendar',
      name: 'calendar',
      component: () => import('../views/CalendarView.vue'),
    },
    {
      path: '/pdf',
      name: 'pdf',
      component: () => import('../views/PdfViewerView.vue'),
    },
    {
      path: '/lovely',
      name: 'lovely',
      component: () => import('../views/LovelyView.vue'),
    },
  ],
})


export default router
