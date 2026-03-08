<template>
  <div class="login-wrapper d-flex align-items-center justify-content-center">
    <div class="login-card shadow-lg rounded-4 overflow-hidden d-flex">

      <div class="login-banner d-none d-md-flex flex-column justify-content-center align-items-center text-white p-5">
        <h2 class="fw-bold mb-3">BSP System</h2>
        <p class="text-center text-white-50">Nền tảng Quản lý Sân Cầu Lông Chuyên Nghiệp & Hiện Đại</p>
      </div>

      <div class="login-form-container p-5 bg-white">
        <div class="text-center mb-4">
          <h3 class="fw-bold text-heading">Đăng Nhập</h3>
          <p class="text-secondary-txt">Vui lòng nhập thông tin để tiếp tục</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label fw-semibold text-body-txt">Tên đăng nhập</label>
            <div class="input-group">
              <span class="input-group-text bg-light border-end-0"><i class="bi bi-person"></i></span>
              <input type="text" v-model="username" class="form-control border-start-0 bg-light" placeholder="admin" required>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label fw-semibold text-body-txt">Mật khẩu</label>
            <div class="input-group">
              <span class="input-group-text bg-light border-end-0"><i class="bi bi-lock"></i></span>
              <input type="password" v-model="password" class="form-control border-start-0 bg-light" placeholder="••••••••" required>
            </div>
          </div>

          <div v-if="errorMessage" class="alert alert-danger py-2 fs-6 text-center" role="alert">
            <i class="bi bi-exclamation-circle me-1"></i> {{ errorMessage }}
          </div>

          <button type="submit" class="btn btn-primary-sport w-100 fw-bold py-2 mb-3" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
            {{ isLoading ? 'Đang xử lý...' : 'Đăng Nhập Ngay' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

const authStore = useAuthStore();
const router = useRouter();

const handleLogin = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    await authStore.login(username.value, password.value);

    // ĐIỀU HƯỚNG THÔNG MINH DỰA VÀO ROLE
    const role = authStore.user?.role;
    if (role === 'ROLE_CUSTOMER') {
      router.push('/customer/booking'); // Khách hàng về nhà khách
    } else {
      router.push('/'); // Admin/BS về Dashboard
    }

  } catch (error) {
    if (error.response && error.response.status === 403) {
      errorMessage.value = 'Sai tài khoản hoặc mật khẩu!';
    } else {
      errorMessage.value = 'Lỗi kết nối máy chủ, vui lòng thử lại sau.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* Tuân thủ Palette em yêu cầu */
.login-wrapper {
  min-height: 100vh;
  background-color: #F8FAFC;
  font-family: 'Inter', sans-serif;
}
.login-card {
  max-width: 900px;
  width: 100%;
  margin: 0 20px;
  background: #FFFFFF;
}
.login-banner {
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  width: 45%;
}
.login-form-container {
  width: 55%;
}
.text-heading { color: #0F172A; }
.text-body-txt { color: #334155; }
.text-secondary-txt { color: #64748B; }

.btn-primary-sport {
  background-color: #2563EB;
  border-color: #2563EB;
  color: white;
}
.btn-primary-sport:hover {
  background-color: #1D4ED8;
  border-color: #1D4ED8;
}
.form-control:focus {
  border-color: #2563EB;
  box-shadow: 0 0 0 0.25rem rgba(37, 99, 235, 0.25);
}
@media (max-width: 768px) {
  .login-form-container { width: 100%; }
}
</style>