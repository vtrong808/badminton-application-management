import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// Guard chặn URL
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login'); // Chưa đăng nhập -> về Login
    } else if (to.path === '/login' && isAuthenticated) {
        next('/'); // Đã đăng nhập rồi mà cứ đòi vào trang Login -> ép về trang chủ
    } else {
        next(); // Hợp lệ, cho đi tiếp
    }
});

export default router;