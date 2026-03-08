<template>
  <div class="customer-booking-view h-100 d-flex flex-column">

    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Hệ Thống Đặt Sân Cầu Lông</h3>
        <p class="text-secondary-txt mt-1 mb-0">Xin chào, <strong class="text-primary">{{ authStore.user?.username }}</strong>! Chọn sân để bắt đầu trận đấu.</p>
      </div>
      <button class="btn btn-success fw-bold px-4 py-2 shadow-sm rounded-pill" @click="openWalkInModal">
        <i class="bi bi-person-walking me-2"></i> Chơi Vãng Lai (50k)
      </button>
    </div>

    <ul class="nav nav-pills mb-4 bg-white p-2 rounded-4 shadow-sm d-inline-flex" id="custTabs">
      <li class="nav-item">
        <button class="nav-link active fw-bold rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#book-court">
          <i class="bi bi-grid-1x2-fill me-1"></i> DANH SÁCH SÂN
        </button>
      </li>
      <li class="nav-item ms-2">
        <button class="nav-link fw-bold rounded-pill px-4 text-secondary" data-bs-toggle="tab" data-bs-target="#my-history" @click="fetchMyHistory">
          <i class="bi bi-clock-history me-1"></i> LỊCH SỬ CỦA TÔI
        </button>
      </li>
    </ul>

    <div class="tab-content flex-grow-1">
      <div class="tab-pane fade show active" id="book-court">
        <div class="row g-4">
          <div v-for="court in courts" :key="court.id" class="col-md-4 col-lg-3">
            <div class="card h-100 border-0 shadow-sm rounded-4 court-card transition-all" :class="{ 'opacity-50': court.status === 'MAINTENANCE' }">
              <div class="card-body p-4 d-flex flex-column">
                <div class="d-flex justify-content-between align-items-start mb-3">
                  <div class="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
                    <i class="bi bi-vinyl-fill fs-4"></i>
                  </div>
                  <span class="badge" :class="court.status === 'AVAILABLE' ? 'bg-success' : 'bg-danger'">
                    {{ court.status === 'AVAILABLE' ? 'Đang trống' : 'Bảo trì' }}
                  </span>
                </div>
                <h5 class="fw-bold mb-1">{{ court.name }}</h5>
                <p class="text-muted small mb-4">Loại: {{ court.type }}</p>
                <button class="btn w-100 fw-bold mt-auto"
                        :class="court.status === 'AVAILABLE' ? 'btn-primary' : 'btn-secondary disabled'"
                        @click="openBookingModal(court)">
                  <i class="bi bi-calendar-check me-1"></i> ĐẶT SÂN NÀY
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tab-pane fade" id="my-history">
        <div class="card border-0 shadow-sm rounded-4">
          <div class="card-body p-0 overflow-hidden">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
              <tr>
                <th class="py-3 px-4">Ngày Chơi</th>
                <th class="py-3 px-4">Khung Giờ</th>
                <th class="py-3 px-4">Loại Hình</th>
                <th class="py-3 px-4">Thông tin Sân</th>
                <th class="py-3 px-4 text-center">Trạng Thái</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="myHistory.length === 0"><td colspan="5" class="text-center py-4 text-muted">Bạn chưa có lịch sử đặt sân nào.</td></tr>
              <tr v-for="(item, index) in myHistory" :key="index">
                <td class="px-4 fw-bold">{{ item.date }}</td>
                <td class="px-4 text-primary fw-medium">{{ item.time }}</td>
                <td class="px-4">
                    <span class="badge" :class="item.isWalkIn ? 'bg-info text-dark' : 'bg-primary'">
                      {{ item.isWalkIn ? 'Khách Vãng Lai' : 'Đặt Sân Trước' }}
                    </span>
                </td>
                <td class="px-4">{{ item.courtName || 'N/A' }}</td>
                <td class="px-4 text-center">
                  <span class="badge bg-success">{{ item.status }}</span>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="customerBookingModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0">
          <div class="modal-header border-0 pb-0">
            <h5 class="fw-bold">Đặt: <span class="text-primary">{{ bookingForm.courtName }}</span></h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body p-4">
            <form @submit.prevent="submitBooking">
              <div class="mb-3">
                <label class="form-label fw-medium">Họ và tên người đặt</label>
                <input type="text" v-model="bookingForm.customerName" class="form-control bg-light" readonly>
              </div>
              <div class="mb-3">
                <label class="form-label fw-medium">Số điện thoại xác nhận <span class="text-danger">*</span></label>
                <input type="text" v-model="bookingForm.phoneNumber" class="form-control" required placeholder="Nhập SĐT của bạn">
              </div>
              <div class="mb-3">
                <label class="form-label fw-medium">Ngày chơi <span class="text-danger">*</span></label>
                <input type="date" v-model="bookingForm.bookingDate" class="form-control" required :min="todayStr">
              </div>
              <div class="row mb-4">
                <div class="col-6">
                  <label class="form-label fw-medium">Giờ bắt đầu</label>
                  <input type="time" v-model="bookingForm.startTime" class="form-control" required>
                </div>
                <div class="col-6">
                  <label class="form-label fw-medium">Giờ kết thúc</label>
                  <input type="time" v-model="bookingForm.endTime" class="form-control" required>
                </div>
              </div>
              <button type="submit" class="btn btn-primary w-100 fw-bold py-2" :disabled="isSaving">
                <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span> XÁC NHẬN ĐẶT SÂN
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="walkInModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content rounded-4 border-0">
          <div class="modal-header border-0 pb-0">
            <h5 class="fw-bold">Chơi Vãng Lai</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body p-4">
            <p class="text-muted small mb-3">Đăng ký nhanh để nhận thông báo thời gian tính giờ từ nhân viên.</p>
            <form @submit.prevent="submitWalkIn">
              <div class="mb-3">
                <label class="form-label fw-medium small">Số điện thoại</label>
                <input type="text" v-model="walkInForm.phoneNumber" class="form-control form-control-sm" required>
              </div>
              <div class="mb-3">
                <label class="form-label fw-medium small">Độ tuổi</label>
                <input type="number" v-model="walkInForm.age" class="form-control form-control-sm" placeholder="VD: 20">
              </div>
              <div class="alert alert-success py-2 text-center fw-bold mb-4">
                Phí mặc định: 50,000 VNĐ
              </div>
              <button type="submit" class="btn btn-success w-100 fw-bold" :disabled="isSaving">ĐĂNG KÝ NGAY</button>
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
import { useAuthStore } from '../stores/auth';
import { Modal } from 'bootstrap';

const authStore = useAuthStore();
const courts = ref([]);
const myHistory = ref([]);
const isSaving = ref(false);

const todayStr = new Date().toISOString().split('T')[0];

const bookingForm = ref({ courtId: null, courtName: '', customerName: '', phoneNumber: '', bookingDate: todayStr, startTime: '18:00', endTime: '19:00' });
const walkInForm = ref({ customerName: '', phoneNumber: '', age: null, price: 50000 });

// Lấy danh sách sân
const fetchCourts = async () => {
  try {
    const res = await api.get('/courts');
    courts.value = res.data.data;
  } catch (error) { console.error(error); }
};

// Lấy lịch sử (Gom cả Booking và Walk-in của người này)
const fetchMyHistory = async () => {
  try {
    const myName = authStore.user?.username; // Trong token ta lưu FullName vào username
    myHistory.value = []; // Reset

    // 1. Lấy Bookings
    const resBook = await api.get('/bookings');
    const myBookings = resBook.data.data.filter(b => b.customerName === myName);
    myBookings.forEach(b => {
      myHistory.value.push({
        date: b.bookingDate,
        time: `${b.startTime} - ${b.endTime}`,
        courtName: b.court.name,
        status: b.status,
        isWalkIn: false
      });
    });

    // 2. Lấy Walk-ins
    const resWalk = await api.get('/walk-ins');
    const myWalks = resWalk.data.data.filter(w => w.customerName === myName);
    myWalks.forEach(w => {
      myHistory.value.push({
        date: w.playDate,
        time: `Bắt đầu: ${w.startTime}`,
        courtName: 'Khu vực chung',
        status: w.status,
        isWalkIn: true
      });
    });

    // Sắp xếp theo ngày mới nhất
    myHistory.value.sort((a, b) => new Date(b.date) - new Date(a.date));

  } catch (error) { console.error('Lỗi lấy lịch sử', error); }
};

// Mở Modal Đặt Sân
const openBookingModal = (court) => {
  bookingForm.value.courtId = court.id;
  bookingForm.value.courtName = court.name;
  bookingForm.value.customerName = authStore.user?.username || ''; // Điền sẵn tên
  Modal.getOrCreateInstance(document.getElementById('customerBookingModal')).show();
};

const submitBooking = async () => {
  isSaving.value = true;
  try {
    await api.post('/bookings', bookingForm.value);
    Modal.getInstance(document.getElementById('customerBookingModal')).hide();
    alert('Đặt sân thành công! Nhân viên sẽ liên hệ xác nhận.');
  } catch (error) {
    alert(error.response?.data?.message || 'Lỗi đặt sân (Có thể trùng giờ)');
  } finally { isSaving.value = false; }
};

// Mở Modal Vãng Lai
const openWalkInModal = () => {
  walkInForm.value.customerName = authStore.user?.username || '';
  Modal.getOrCreateInstance(document.getElementById('walkInModal')).show();
};

const submitWalkIn = async () => {
  isSaving.value = true;
  try {
    await api.post('/walk-ins', walkInForm.value);
    Modal.getInstance(document.getElementById('walkInModal')).hide();
    alert('Đăng ký vãng lai thành công! Hãy báo với nhân viên tại quầy.');
  } catch (error) { alert('Lỗi đăng ký'); }
  finally { isSaving.value = false; }
};

onMounted(() => {
  fetchCourts();
});
</script>

<style scoped>
.text-heading { color: #0F172A; }
.nav-pills .nav-link.active { background-color: #2563EB; color: white; }
.court-card:hover { border-color: #2563EB; transform: translateY(-3px); box-shadow: 0 10px 20px rgba(0,0,0,0.1) !important; }
</style>