<template>
  <div class="booking-view h-100 d-flex flex-column">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Quản Lý Sân & Vãng Lai (V2.0)</h3>
        <p class="text-secondary-txt mt-1 mb-0">Hệ thống đếm giờ thời gian thực</p>
      </div>
    </div>

    <ul class="nav nav-tabs mb-3" id="bookingTabs" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active fw-bold" id="courts-tab" data-bs-toggle="tab" data-bs-target="#courts" type="button" role="tab">
          <i class="bi bi-grid-1x2-fill me-1"></i> LƯỚI SÂN
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link fw-bold text-success" id="walkin-tab" data-bs-toggle="tab" data-bs-target="#walkin" type="button" role="tab" @click="fetchWalkIns">
          <i class="bi bi-person-walking me-1"></i> KHÁCH VÃNG LAI
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link fw-bold text-danger" id="racket-tab" data-bs-toggle="tab" data-bs-target="#rackets" type="button" role="tab" @click="fetchRackets">
          <i class="bi bi-vinyl-fill me-1"></i> THUÊ VỢT CẦU LÔNG
        </button>
      </li>
    </ul>

    <div class="tab-content flex-grow-1" id="bookingTabsContent">

      <div class="tab-pane fade show active" id="courts" role="tabpanel">
        <div class="row g-4">
          <div v-for="court in courts" :key="court.id" class="col-md-4 col-lg-3">
            <div class="card h-100 border-0 shadow-sm rounded-4 court-card transition-all" :class="{ 'border-maintenance': court.status === 'MAINTENANCE' }">
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
                <p class="text-muted small mb-4">Loại sân: {{ court.type }}</p>
                <button class="btn w-100 fw-bold py-2 mt-auto" :class="court.status === 'AVAILABLE' ? 'btn-primary-sport' : 'btn-secondary disabled'">
                  <i class="bi bi-calendar-plus me-1"></i> Đặt Sân (V1)
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tab-pane fade" id="rackets" role="tabpanel">
        <div class="alert alert-warning border-0 bg-opacity-10 d-flex align-items-center mb-4">
          <i class="bi bi-shield-lock-fill fs-4 text-warning me-3"></i>
          <div>
            <strong>Hệ thống An toàn Dữ liệu (Optimistic Locking):</strong><br>
            <small>Đảm bảo không có 2 nhân viên cùng cho thuê 1 cây vợt trong cùng 1 tích tắc.</small>
          </div>
        </div>

        <div class="row g-4">
          <div v-for="racket in rackets" :key="racket.id" class="col-md-3 col-lg-2">
            <div class="card h-100 border-0 shadow-sm rounded-4 text-center transition-all"
                 :class="racket.status === 'IN_USE' ? 'border-danger border border-2 bg-light' : 'border-success border border-2'">
              <div class="card-body p-3 d-flex flex-column align-items-center">
                <i class="bi bi-vinyl-fill display-4 mb-2" :class="racket.status === 'IN_USE' ? 'text-danger' : 'text-success'"></i>
                <h5 class="fw-bold mb-1">{{ racket.racketCode }}</h5>
                <span class="badge bg-secondary mb-3">{{ racket.brand }}</span>

                <div v-if="racket.status === 'IN_USE'" class="mb-3 w-100">
                  <small class="text-danger fw-bold d-block">ĐANG CHO THUÊ</small>
                  <div class="bg-white rounded-2 p-1 border mt-1">
                    <span class="fw-bold timer-font text-dark fs-5">{{ calculateRacketTime(racket.rentedAt) }}</span>
                  </div>
                </div>

                <div v-else class="mb-3 w-100">
                  <small class="text-success fw-bold d-block">SẴN SÀNG</small>
                  <div class="text-muted mt-1">{{ formatCurrency(racket.rentalPrice) }}/ca</div>
                </div>

                <button v-if="racket.status === 'AVAILABLE'" class="btn btn-success btn-sm w-100 fw-bold mt-auto" @click="rentRacket(racket.id)">
                  <i class="bi bi-key-fill me-1"></i> Cho Thuê
                </button>
                <button v-else class="btn btn-outline-danger btn-sm w-100 fw-bold mt-auto" @click="returnRacket(racket.id)">
                  <i class="bi bi-arrow-counterclockwise me-1"></i> Nhận Lại
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tab-pane fade" id="walkin" role="tabpanel">
        <div class="card border-0 shadow-sm rounded-4 mb-4">
          <div class="card-body p-4">
            <h5 class="fw-bold mb-3">Đăng ký khách chơi nhanh</h5>
            <form @submit.prevent="createWalkIn" class="row g-3 align-items-end">
              <div class="col-md-3">
                <label class="form-label small fw-medium">Tên khách hàng <span class="text-danger">*</span></label>
                <input type="text" v-model="walkInForm.customerName" class="form-control bg-light" required placeholder="Nguyễn Văn A">
              </div>
              <div class="col-md-2">
                <label class="form-label small fw-medium">Số điện thoại</label>
                <input type="text" v-model="walkInForm.phoneNumber" class="form-control bg-light" placeholder="09xxxx">
              </div>
              <div class="col-md-2">
                <label class="form-label small fw-medium">Tuổi</label>
                <input type="number" v-model="walkInForm.age" class="form-control bg-light" placeholder="25">
              </div>
              <div class="col-md-2">
                <label class="form-label small fw-medium">Phí thu (VNĐ)</label>
                <input type="number" v-model="walkInForm.price" class="form-control bg-light text-success fw-bold">
              </div>
              <div class="col-md-3">
                <button type="submit" class="btn btn-success w-100 fw-bold" :disabled="isSaving">
                  <i class="bi bi-play-circle-fill me-1"></i> BẮT ĐẦU TÍNH GIỜ
                </button>
              </div>
            </form>
          </div>
        </div>

        <h5 class="fw-bold mb-3"><i class="bi bi-activity text-primary me-2"></i>Đang hoạt động hôm nay</h5>
        <div class="row g-3">
          <div v-if="walkIns.length === 0" class="col-12 text-center text-muted py-5">
            Hôm nay chưa có khách vãng lai nào.
          </div>
          <div v-for="session in walkIns" :key="session.id" class="col-md-6 col-lg-4">
            <div class="card border-0 shadow-sm rounded-4 h-100" :class="session.status === 'PLAYING' ? 'border-start border-primary border-4' : 'opacity-75'">
              <div class="card-body p-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <h6 class="fw-bold m-0 text-truncate">{{ session.customerName }}</h6>
                  <span class="badge" :class="session.status === 'PLAYING' ? 'bg-primary' : 'bg-secondary'">
                    {{ session.status === 'PLAYING' ? 'ĐANG CHƠI' : 'ĐÃ KẾT THÚC' }}
                  </span>
                </div>
                <p class="small text-muted mb-3"><i class="bi bi-telephone me-1"></i> {{ session.phoneNumber || 'Không có SĐT' }}</p>

                <div class="bg-light rounded-3 p-2 text-center mb-3">
                  <small class="text-secondary d-block mb-1">Thời gian đã chơi</small>
                  <h3 class="fw-bold m-0 timer-font" :class="session.status === 'PLAYING' ? 'text-danger' : 'text-dark'">
                    {{ session.status === 'PLAYING' ? calculatePlayTime(session.startTime) : 'Đã chốt' }}
                  </h3>
                  <small class="text-muted">Bắt đầu lúc: {{ session.startTime }}</small>
                </div>

                <button v-if="session.status === 'PLAYING'" class="btn btn-outline-danger w-100 btn-sm fw-bold" @click="finishWalkIn(session.id)">
                  <i class="bi bi-stop-circle me-1"></i> Chốt ca / Kết thúc
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import api from '../api/axios';

// Data Tab Lưới Sân
const courts = ref([]);

// Data Tab Vãng Lai
const walkIns = ref([]);
const isSaving = ref(false);
const walkInForm = ref({ customerName: '', phoneNumber: '', age: null, price: 50000 });

// Biến lưu thời gian hiện tại (cập nhật mỗi giây)
const now = ref(new Date());
let timerInterval;

// Format tiền tệ
const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);

// Lấy danh sách sân
const fetchCourts = async () => {
  try {
    const response = await api.get('/courts');
    courts.value = response.data.data;
  } catch (error) { console.error(error); }
};

// Lấy danh sách khách vãng lai
const fetchWalkIns = async () => {
  try {
    const response = await api.get('/walk-ins');
    // Đảo ngược để người mới tạo lên đầu
    walkIns.value = response.data.data.reverse();
  } catch (error) { console.error(error); }
};

// Tạo phiên chơi mới
const createWalkIn = async () => {
  isSaving.value = true;
  try {
    await api.post('/walk-ins', walkInForm.value);
    walkInForm.value = { customerName: '', phoneNumber: '', age: null, price: 50000 };
    await fetchWalkIns();
  } catch (error) {
    alert(error.response?.data?.message || 'Có lỗi xảy ra!');
  } finally {
    isSaving.value = false;
  }
};

// Kết thúc ca chơi
const finishWalkIn = async (id) => {
  if(!confirm("Xác nhận kết thúc ca chơi này?")) return;
  try {
    await api.put(`/walk-ins/${id}/finish`);
    await fetchWalkIns();
  } catch (error) {
    alert('Lỗi kết thúc ca chơi');
  }
};

// ⏱️ HÀM TÍNH TOÁN THỜI GIAN THỰC (REAL-TIME TIMER)
const calculatePlayTime = (startTimeStr) => {
  if (!startTimeStr) return '00:00:00';

  // startTimeStr có dạng "14:30:00"
  const [hours, minutes, seconds] = startTimeStr.split(':').map(Number);

  // Tạo object Date cho thời điểm bắt đầu (lấy ngày hôm nay)
  const startTime = new Date();
  startTime.setHours(hours, minutes, seconds || 0, 0);

  // Tính khoảng cách thời gian (miligiây)
  const diffMs = now.value - startTime;
  if (diffMs < 0) return '00:00:00';

  // Chuyển sang Giờ : Phút : Giây
  const diffHrs = Math.floor(diffMs / (1000 * 60 * 60));
  const diffMins = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
  const diffSecs = Math.floor((diffMs % (1000 * 60)) / 1000);

  // Thêm số 0 đằng trước nếu nhỏ hơn 10 (VD: 01:05:09)
  const pad = (num) => String(num).padStart(2, '0');

  return `${pad(diffHrs)}:${pad(diffMins)}:${pad(diffSecs)}`;
};

onMounted(() => {
  fetchCourts();
  fetchWalkIns();
  fetchRackets();
  // Khởi động bộ đếm nhịp tim mỗi 1 giây
  timerInterval = setInterval(() => {
    now.value = new Date();
  }, 1000);
});

onUnmounted(() => {
  // Dọn dẹp bộ nhớ khi chuyển trang
  if (timerInterval) clearInterval(timerInterval);
});

// Data Tab Thuê Vợt
const rackets = ref([]);

// Lấy danh sách Vợt
const fetchRackets = async () => {
  try {
    const response = await api.get('/rackets');
    rackets.value = response.data.data;
  } catch (error) { console.error(error); }
};

// Cho Thuê
const rentRacket = async (id) => {
  try {
    await api.put(`/rackets/${id}/rent`);
    await fetchRackets(); // Tải lại để lấy giờ bắt đầu
  } catch (error) {
    alert(error.response?.data?.message || 'Lỗi khi thuê vợt!');
  }
};

// Nhận Lại Vợt
const returnRacket = async (id) => {
  if(!confirm("Xác nhận khách đã trả vợt này?")) return;
  try {
    await api.put(`/rackets/${id}/return`);
    await fetchRackets();
  } catch (error) {
    alert('Lỗi trả vợt!');
  }
};

// Hàm tính giờ riêng cho Racket (Đã Fix lỗi Múi giờ)
const calculateRacketTime = (rentedAtStr) => {
  if (!rentedAtStr) return '00:00:00';

  // Tách ngày và giờ ra khỏi chuỗi (Ví dụ: "2026-03-04T22:15:00")
  const [datePart, timePart] = rentedAtStr.split('T');
  if (!timePart) return '00:00:00'; // Đề phòng dữ liệu rác

  // Tách Giờ, Phút, Giây
  const [hours, minutes, seconds] = timePart.split(':').map(Number);

  // Tạo lại đối tượng Date dùng giờ địa phương (Bỏ qua ngày tháng, chỉ lấy giờ để so cho chắc)
  const startTime = new Date();
  startTime.setHours(hours, minutes, seconds ? Math.floor(seconds) : 0, 0); // Math.floor đề phòng Spring gửi nano giây (VD: 15.123)

  // Tính khoảng cách
  const diffMs = now.value - startTime;

  // Nếu âm (lệch vài giây do server chậm), cứ ép về 0
  if (diffMs < 0) return '00:00:00';

  const diffHrs = Math.floor(diffMs / (1000 * 60 * 60));
  const diffMins = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
  const diffSecs = Math.floor((diffMs % (1000 * 60)) / 1000);

  const pad = (num) => String(num).padStart(2, '0');
  return `${pad(diffHrs)}:${pad(diffMins)}:${pad(diffSecs)}`;
};
</script>

<style scoped>
.text-heading { color: #0F172A; }
.text-secondary-txt { color: #64748B; font-size: 0.9rem; }
.btn-primary-sport { background-color: #2563EB; border-color: #2563EB; color: white; }
.bg-success-bsp { background-color: #10B981; }

.court-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important; }
.border-maintenance { border: 2px dashed #EF4444 !important; opacity: 0.8; }

.nav-tabs .nav-link { border: none; padding: 12px 20px; font-size: 1.05rem; color: #64748B; }
.nav-tabs .nav-link.active { border-bottom: 3px solid #2563EB; background: transparent; color: #2563EB; }
.nav-tabs .nav-link.text-success.active { border-bottom: 3px solid #10B981; color: #10B981 !important; }

/* Font số đếm giờ xịn xò như đồng hồ LED */
.timer-font {
  font-family: 'Courier New', Courier, monospace;
  letter-spacing: 2px;
}
</style>