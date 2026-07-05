import { createRouter, createWebHistory } from 'vue-router'
import StudentWorkspace from '../views/student/StudentWorkspace.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/student' },
    { path: '/student', component: StudentWorkspace },
    { path: '/admin', component: AdminDashboard }
  ]
})

export default router
