<template>
  <div class="customer-layout d-flex flex-column min-vh-100 bg-light">
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary-sport shadow-sm">
      <div class="container">
        <a class="navbar-brand fw-bold" href="#">
          <i class="bi bi-vinyl-fill me-2"></i> BSP SPORT CENTER
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#customerNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="customerNav">
          <ul class="navbar-nav ms-auto mb-2 mb-lg-0 fw-medium">
            <li class="nav-item"><router-link class="nav-link" to="/customer/booking" active-class="active">Lịch Đặt Sân</router-link></li>
            <li class="nav-item"><router-link class="nav-link" to="/customer/service" active-class="active">Thuê Dịch Vụ</router-link></li>
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                <i class="bi bi-person-circle me-1"></i> {{ authStore.user?.username || 'Tài Khoản' }}
              </a>
              <ul class="dropdown-menu dropdown-menu-end shadow border-0">
                <li><a class="dropdown-item" href="#">Hồ sơ cá nhân</a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item text-danger" href="#" @click.prevent="logout"><i class="bi bi-box-arrow-right me-2"></i>Đăng xuất</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <main class="flex-grow-1 container py-4">
      <router-view></router-view>
    </main>

    <footer class="bg-white py-3 text-center text-muted small border-top">
      &copy; 2026 Badminton Sport System. All rights reserved.
    </footer>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth.js';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const logout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.bg-primary-sport { background-color: #2563EB; }
.nav-link.active { color: #FCD34D !important; border-bottom: 2px solid #FCD34D; }
</style>