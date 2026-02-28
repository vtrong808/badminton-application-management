<template>
  <div class="booking-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Hệ Thống Đặt Sân</h3>
        <p class="text-secondary-txt mt-1 mb-0">Chọn sân và thời gian để tạo lịch đặt mới</p>
      </div>
      <button class="btn btn-outline-primary" @click="fetchCourts">
        <i class="bi bi-arrow-clockwise me-1"></i> Làm mới
      </button>
    </div>

    <div v-if="isLoading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2 text-secondary">Đang tải danh sách sân...</p>
    </div>

    <div v-else class="row g-4">
      <div v-for="court in courts" :key="court.id" class="col-md-4 col-lg-3">
        <div class="card h-100 border-0 shadow-sm rounded-4 court-card transition-all"
             :class="{ 'border-maintenance': court.status === 'MAINTENANCE' }">
          <div class="card-body p-4 d-flex flex-column">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="court-icon bg-light text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
                <i class="bi bi-grid-1x2-fill fs-4"></i>
              </div>
              <span class="badge" :class="court.status === 'AVAILABLE' ? 'bg-success-bsp' : 'bg-danger'">
                {{ court.status === 'AVAILABLE' ? 'Sẵn sàng' : 'Bảo trì' }}
              </span>
            </div>

            <h5 class="fw-bold text-heading mb-1">{{ court.name }}</h5>
            <p class="text-muted small mb-3">
              <i class="bi bi-star-fill text-warning me-1" v-if="court.type === 'VIP'"></i>
              Loại sân: {{ court.type }}
            </p>

            <div class="price-info bg-light rounded-3 p-2 mb-4 mt-auto">
              <div class="d-flex justify-content-between small mb-1">
                <span class="text-secondary">Ca ngày (6h-17h):</span>
                <span class="fw-bold text-success">{{ formatCurrency(court.priceDay) }}/h</span>
              </div>
              <div class="d-flex justify-content-between small">
                <span class="text-secondary">Ca đêm (17h-23h):</span>
                <span class="fw-bold text-primary">{{ formatCurrency(court.priceNight) }}/h</span>
              </div>
            </div>

            <button class="btn w-100 fw-bold py-2"
                    :class="court.status === 'AVAILABLE' ? 'btn-primary-sport' : 'btn-secondary disabled'"
                    @click="openBookingModal(court)">
              <i class="bi bi-calendar-plus me-1"></i> Đặt Sân Ngay
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="bookingModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow-lg">
          <div class="modal-header bg-light border-bottom-0 pb-3 rounded-top-4">
            <h5 class="modal-title fw-bold">
              <i class="bi bi-calendar-check text-primary me-2"></i>Tạo Lịch Đặt
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">

            <div class="alert alert-info border-0 bg-opacity-10 d-flex align-items-center mb-4">
              <i class="bi bi-info-circle-fill fs-4 text-info me-3"></i>
              <div>
                Đang đặt: <strong class="fs-5">{{ form.courtName }}</strong><br>
                <small>Hệ thống sẽ tự động tính toán giá tiền dựa theo khung giờ.</small>
              </div>
            </div>

            <form @submit.prevent="submitBooking">
              <div class="mb-3">
                <label class="form-label fw-medium">Thời gian bắt đầu <span class="text-danger">*</span></label>
                <input type="datetime-local" v-model="form.startTime" class="form-control form-control-lg bg-light" required>
              </div>

              <div class="mb-4">
                <label class="form-label fw-medium">Thời gian kết thúc <span class="text-danger">*</span></label>
                <input type="datetime-local" v-model="form.endTime" class="form-control form-control-lg bg-light" required>
              </div>

              <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary-sport btn-lg fw-bold" :disabled="isSaving">
                  <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-check-circle me-1"></i> Xác Nhận Đặt Sân
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../api/axios';
import { Modal } from 'bootstrap';

const courts = ref([]);
const isLoading = ref(false);
const isSaving = ref(false);

const form = ref({
  courtId: null,
  courtName: '',
  startTime: '',
  endTime: ''
});

// Format tiền tệ VNĐ
const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Lấy danh sách Sân để hiển thị Grid
const fetchCourts = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/courts');
    courts.value = response.data.data;
  } catch (error) {
    console.error('Lỗi khi tải danh sách sân:', error);
  } finally {
    isLoading.value = false;
  }
};

// Mở Modal Đặt sân
const openBookingModal = (court) => {
  if (court.status === 'MAINTENANCE') return;

  form.value.courtId = court.id;
  form.value.courtName = court.name;

  // Set default time là thời điểm hiện tại và kết thúc sau 1 tiếng
  const now = new Date();
  const later = new Date(now.getTime() + 60 * 60 * 1000);

  // Format để HTML datetime-local hiểu được (YYYY-MM-DDThh:mm)
  // Cắt bỏ phần timezone và giây
  form.value.startTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  form.value.endTime = new Date(later.getTime() - later.getTimezoneOffset() * 60000).toISOString().slice(0, 16);

  const modalEl = document.getElementById('bookingModal');
  const modal = Modal.getOrCreateInstance(modalEl);
  modal.show();
};

// Gửi Request Đặt Sân
const submitBooking = async () => {
  isSaving.value = true;
  try {
    // payload chuẩn bị gửi cho Spring Boot
    const payload = {
      courtId: form.value.courtId,
      startTime: form.value.startTime,
      endTime: form.value.endTime
    };

    const response = await api.post('/bookings', payload);

    // Tắt modal
    const modalEl = document.getElementById('bookingModal');
    const modal = Modal.getInstance(modalEl);
    if (modal) modal.hide();

    // Hiển thị thông báo thành công (Có thể dùng thư viện Toast, ở đây dùng alert)
    const bookingResult = response.data.data;
    alert(`🎉 ĐẶT SÂN THÀNH CÔNG!\nSân: ${form.value.courtName}\nTổng tiền dự kiến: ${formatCurrency(bookingResult.totalPrice)}`);

  } catch (error) {
    // Hứng lỗi trùng lịch từ GlobalExceptionHandler của Backend
    const errorMsg = error.response?.data?.message || 'Lỗi hệ thống khi đặt sân!';
    alert(`❌ THẤT BẠI:\n${errorMsg}`);
  } finally {
    isSaving.value = false;
  }
};

onMounted(() => {
  fetchCourts();
});
</script>

<style scoped>
.text-heading { color: #0F172A; }
.text-body-txt { color: #334155; }
.text-secondary-txt { color: #64748B; font-size: 0.9rem; }

.btn-primary-sport { background-color: #2563EB; border-color: #2563EB; color: white; }
.btn-primary-sport:hover { background-color: #1D4ED8; border-color: #1D4ED8; }
.bg-success-bsp { background-color: #10B981; }

.court-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
}
.border-maintenance {
  border: 2px dashed #EF4444 !important;
  opacity: 0.8;
}
.alert-info { background-color: #EFF6FF; color: #1E40AF; }
</style>