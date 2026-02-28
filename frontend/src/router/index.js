import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import AdminLayout from '../layouts/AdminLayout.vue'; // <-- Import Layout

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: AdminLayout, // <-- Layout bọc ở ngoài
        meta: { requiresAuth: true },
        children: [
            {
                path: '', // Default route khi vào '/'
                name: 'Dashboard',
                component: () => import('../views/DashboardView.vue'),
            },
            {
                path: '',
                name: 'Dashboard',
                component: () => import('../views/DashboardView.vue'),
            },
            {
                path: 'products', // <-- Khai báo route mới ở đây
                name: 'Products',
                component: () => import('../views/ProductView.vue'),
            },
            {
                path: 'courts',
                name: 'Courts',
                component: () => import('../views/CourtView.vue'),
            },
            {
                path: 'bookings',
                name: 'Bookings',
                component: () => import('../views/BookingView.vue'),
            },
            {
                path: 'invoices',
                name: 'Invoices',
                component: () => import('../views/InvoiceView.vue'),
            },
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login');
    } else if (to.path === '/login' && authStore.isAuthenticated) {
        next('/');
    } else {
        next();
    }
});

export default router;