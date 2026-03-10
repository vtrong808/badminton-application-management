import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import AdminLayout from '../layouts/AdminLayout.vue';
import CustomerLayout from '../layouts/CustomerLayout.vue';

const routes = [
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },

    // LUỒNG DÀNH CHO ADMIN / BS
    {
        path: '/',
        component: AdminLayout,
        meta: { requiresAuth: true, allowedRoles: ['ROLE_ADMIN', 'ROLE_BS'] },
        children: [
            { path: '', name: 'Dashboard', component: () => import('../views/DashboardView.vue') },
            { path: 'courts', name: 'Courts', component: () => import('../views/CourtView.vue') },
            { path: 'bookings', name: 'Bookings', component: () => import('../views/BookingView.vue') },
            { path: 'products', name: 'Products', component: () => import('../views/ProductView.vue') },
            { path: 'invoices', name: 'Invoices', component: () => import('../views/InvoiceView.vue') },
            { path: 'shifts', name: 'Shifts', component: () => import('../views/ShiftView.vue') },
            { path: 'accounts', name: 'Accounts', component: () => import('../views/AccountView.vue') },
        ]
    },

    // LUỒNG DÀNH CHO KHÁCH HÀNG (CUSTOMER)
    {
        path: '/customer',
        component: CustomerLayout,
        meta: { requiresAuth: true, allowedRoles: ['ROLE_CUSTOMER'] },
        children: [
            { path: 'booking', name: 'CustomerBooking', component: () => import('../views/CustomerBookingView.vue') },
            { path: 'service', name: 'CustomerService', component: () => import('../views/CustomerServiceView.vue') }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;
    const userRole = authStore.user?.role;

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
    }
    // Chặn user vào nhầm layout (Khách hàng không được vào "/", Admin không được vào "/customer")
    else if (to.meta.allowedRoles && !to.meta.allowedRoles.includes(userRole)) {
        if (userRole === 'ROLE_CUSTOMER') next('/customer/booking');
        else next('/');
    }
    // Nếu đã login mà cố tình vào trang login -> đẩy về trang chủ của họ
    else if (to.path === '/login' && isAuthenticated) {
        if (userRole === 'ROLE_CUSTOMER') next('/customer/booking');
        else next('/');
    }
    else {
        next();
    }
});

export default router;