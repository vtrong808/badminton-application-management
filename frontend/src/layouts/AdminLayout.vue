<template>
  <div class="admin-layout d-flex">
    <aside class="sidebar d-flex flex-column transition-all" :class="{ 'collapsed': isCollapsed }">
      <div class="sidebar-brand d-flex align-items-center justify-content-center py-4">
        <i class="bi bi-heptagon-half fs-3 text-primary-bsp me-2"></i>
        <span v-if="!isCollapsed" class="fw-bold fs-4 text-white">BSP System</span>
      </div>

      <ul class="nav nav-pills flex-column mb-auto px-2">
        <li class="nav-item mb-1">
          <router-link to="/" class="nav-link sidebar-link d-flex align-items-center" active-class="active">
            <i class="bi bi-speedometer2 fs-5"></i>
            <span v-if="!isCollapsed" class="ms-3">Dashboard</span>
          </router-link>
        </li>
        <li class="nav-item mb-1">
          <router-link to="/courts" class="nav-link sidebar-link d-flex align-items-center" active-class="active">
            <i class="bi bi-grid-1x2 fs-5"></i>
            <span v-if="!isCollapsed" class="ms-3">Quản Lý Sân</span>
          </router-link>
        </li>
        <li class="nav-item mb-1">
          <router-link to="/products" class="nav-link sidebar-link d-flex align-items-center" active-class="active">
            <i class="bi bi-box-seam fs-5"></i> <span v-if="!isCollapsed" class="ms-3">Quản Lý Sản Phẩm</span>
          </router-link>
        </li>
        <li class="nav-item mb-1">
          <router-link to="/bookings" class="nav-link sidebar-link d-flex align-items-center" active-class="active">
            <i class="bi bi-calendar-check fs-5"></i>
            <span v-if="!isCollapsed" class="ms-3">Lịch Đặt Sân</span>
          </router-link>
        </li>
        <li class="nav-item mb-1">
          <router-link to="/shifts" class="nav-link sidebar-link d-flex align-items-center" active-class="active">
            <i class="bi-calendar-range fs-5"></i>
            <span v-if="!isCollapsed" class="ms-3">Ca làm việc</span>
          </router-link>
        </li>
        <li class="nav-item mb-1">
          <router-link to="/invoices" class="nav-link sidebar-link d-flex align-items-center" active-class="active">
            <i class="bi bi-receipt fs-5"></i>
            <span v-if="!isCollapsed" class="ms-3">Hóa Đơn / POS</span>
          </router-link>
        </li>
      </ul>

      <div class="sidebar-user p-3 mt-auto border-top border-secondary border-opacity-25">
        <div class="d-flex align-items-center dropup cursor-pointer" data-bs-toggle="dropdown" aria-expanded="false">
          <img src="https://ui-avatars.com/api/?name=Admin&background=2563EB&color=fff" alt="User" width="40" height="40" class="rounded-circle me-2">
          <div v-if="!isCollapsed" class="text-white user-info overflow-hidden">
            <strong class="d-block text-truncate">{{ authStore.getUsername }}</strong>
            <small class="text-secondary-txt role-text">{{ authStore.getUserRole }}</small>
          </div>
        </div>
        <ul class="dropdown-menu dropdown-menu-dark text-small shadow">
          <li><a class="dropdown-item" href="#"><i class="bi bi-person me-2"></i>Hồ sơ</a></li>
          <li><hr class="dropdown-divider"></li>
          <li><a class="dropdown-item text-danger" href="#" @click.prevent="handleLogout"><i class="bi bi-box-arrow-right me-2"></i>Đăng xuất</a></li>
        </ul>
      </div>
    </aside>

    <main class="main-content flex-grow-1 bg-dashboard d-flex flex-column">
      <header class="topbar d-flex align-items-center justify-content-between px-4 shadow-sm bg-white">
        <button class="btn btn-light border-0" @click="isCollapsed = !isCollapsed">
          <i class="bi bi-list fs-4"></i>
        </button>
        <div class="text-end">
          <span class="badge bg-success-bsp px-3 py-2 rounded-pill"><i class="bi bi-circle-fill small me-1"></i> Hệ thống ổn định</span>
        </div>
      </header>

      <div class="p-4 flex-grow-1 overflow-auto">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

const isCollapsed = ref(false);
const authStore = useAuthStore();
const router = useRouter();

const handleLogout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
}

/* Chuẩn Palette Admin Dashboard */
.bg-dashboard { background-color: #F1F5F9; }
.text-primary-bsp { color: #2563EB; }
.bg-success-bsp { background-color: #10B981; }

/* Sidebar styling */
.sidebar {
  width: 260px;
  background-color: #0F172A; /* Deep Indigo */
  color: #CBD5E1;
  transition: width 0.3s ease;
  z-index: 1000;
}
.sidebar.collapsed {
  width: 70px;
}
.sidebar-link {
  color: #CBD5E1;
  border-radius: 8px;
  padding: 10px 15px;
  transition: all 0.2s;
  white-space: nowrap;
}
.sidebar-link:hover {
  background-color: rgba(255, 255, 255, 0.06);
  color: #FFFFFF;
}
.sidebar-link.active {
  background-color: #2563EB !important;
  color: #FFFFFF !important;
  font-weight: 500;
}
.role-text { color: #94A3B8; font-size: 0.75rem;}
.topbar {
  height: 60px;
  z-index: 10;
}
</style>