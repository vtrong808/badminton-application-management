<template>
  <div class="shift-view h-100 d-flex flex-column">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Bảng Phân Ca Làm Việc</h3>
        <p class="text-secondary-txt mt-1 mb-0">Tuần hiện tại (Thứ 2 - Chủ Nhật)</p>
      </div>
      <div>
        <span class="badge bg-warning text-dark me-2">MORNING (06h-12h)</span>
        <span class="badge bg-primary me-2">AFTERNOON (12h-18h)</span>
        <span class="badge bg-dark">EVENING (18h-23h)</span>
      </div>
    </div>

    <div class="card border-0 shadow-sm rounded-4 overflow-hidden flex-grow-1">
      <div class="card-body p-0 overflow-auto">
        <table class="table table-bordered align-middle text-center mb-0" style="min-width: 1000px;">
          <thead class="bg-light">
          <tr>
            <th class="py-3 bg-white sticky-start" style="width: 200px; left: 0; z-index: 2;">Nhân Viên (BS)</th>
            <th v-for="day in weekDays" :key="day.dateStr" class="py-3">
              <span class="d-block fw-bold text-primary">{{ day.dayName }}</span>
              <small class="text-muted">{{ day.shortDate }}</small>
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="staffs.length === 0">
            <td colspan="8" class="py-5 text-muted">Chưa có nhân viên nào.</td>
          </tr>
          <tr v-for="staff in staffs" :key="staff.id">
            <td class="fw-bold text-start ps-4 bg-white sticky-start" style="left: 0; z-index: 1;">
              <i class="bi bi-person-badge text-primary me-2"></i>{{ staff.username }}
            </td>

            <td v-for="day in weekDays" :key="day.dateStr" class="p-2 cursor-pointer cell-hover" @click="openAssignModal(staff, day)">
              <template v-if="getShift(staff.id, day.dateStr)">
                <div class="p-2 rounded-3 text-white fw-bold shadow-sm"
                     :class="getShiftColor(getShift(staff.id, day.dateStr).shiftType)">
                  {{ getShift(staff.id, day.dateStr).shiftType }}
                  <div v-if="getShift(staff.id, day.dateStr).isAttended" class="mt-1 small bg-white text-success rounded px-1">
                    <i class="bi bi-check-circle-fill"></i> Đã Check-in
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="text-black-50 small opacity-50"><i class="bi bi-plus-circle"></i> Trống</div>
              </template>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="shiftModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-0 pb-0">
            <h5 class="fw-bold">Phân Ca: {{ currentForm.staffName }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p class="text-muted mb-3">Ngày: <strong>{{ currentForm.dateStr }}</strong></p>

            <div v-if="isAdmin" class="d-grid gap-2 mb-3">
              <button class="btn btn-outline-warning fw-bold text-dark" @click="assignShift('MORNING')">Ca Sáng (MORNING)</button>
              <button class="btn btn-outline-primary fw-bold" @click="assignShift('AFTERNOON')">Ca Chiều (AFTERNOON)</button>
              <button class="btn btn-outline-dark fw-bold" @click="assignShift('EVENING')">Ca Tối (EVENING)</button>
            </div>

            <div v-else-if="!currentForm.existingShiftId" class="alert alert-warning text-center small mb-0">
              <i class="bi bi-info-circle me-1"></i> Chưa có ca làm việc nào được phân công.
            </div>

            <div v-if="currentForm.existingShiftId && !currentForm.isAttended" class="border-top pt-3 mt-3">
              <button v-if="isAdmin || currentForm.staffName === authStore.user?.username"
                      class="btn btn-success w-100 fw-bold" @click="checkIn(currentForm.existingShiftId)">
                <i class="bi bi-fingerprint me-1"></i> XÁC NHẬN ĐIỂM DANH
              </button>

              <div v-else class="alert alert-danger text-center small mb-0">
                <i class="bi bi-x-circle-fill me-1"></i> Bạn không thể điểm danh hộ đồng nghiệp!
              </div>
            </div>

            <div v-if="currentForm.isAttended" class="alert alert-success text-center small mb-0 mt-3 fw-bold">
              <i class="bi bi-check-circle-fill me-1"></i> Đã hoàn thành điểm danh
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '../api/axios';
import { Modal } from 'bootstrap';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const isAdmin = computed(() => authStore.user?.role === 'ROLE_ADMIN');

const staffs = ref([]);
const shifts = ref([]);
const weekDays = ref([]);

const currentForm = ref({ staffId: null, staffName: '', dateStr: '', existingShiftId: null, isAttended: false });

// Khởi tạo 7 ngày của tuần hiện tại
const generateWeekDays = () => {
  const curr = new Date();
  const first = curr.getDate() - curr.getDay() + 1; // Thứ 2
  const days = [];
  const dayNames = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ Nhật'];

  for (let i = 0; i < 7; i++) {
    const next = new Date(curr.setDate(first + i));
    const dateStr = next.toISOString().split('T')[0]; // YYYY-MM-DD
    const shortDate = `${next.getDate()}/${next.getMonth() + 1}`;
    days.push({ dayName: dayNames[i], dateStr, shortDate });
  }
  weekDays.value = days;
};

const fetchData = async () => {
  try {
    const resStaff = await api.get('/users/staff');
    staffs.value = resStaff.data.data;
    const resShift = await api.get('/shifts');
    shifts.value = resShift.data.data;
  } catch (error) { console.error(error); }
};

// Tìm ca của 1 người trong 1 ngày
const getShift = (userId, dateStr) => {
  return shifts.value.find(s => s.user.id === userId && s.shiftDate === dateStr);
};

const getShiftColor = (type) => {
  if (type === 'MORNING') return 'bg-warning text-dark';
  if (type === 'AFTERNOON') return 'bg-primary';
  return 'bg-dark';
};

const openAssignModal = (staff, day) => {
  const existing = getShift(staff.id, day.dateStr);
  currentForm.value = {
    staffId: staff.id,
    staffName: staff.username,
    dateStr: day.dateStr,
    existingShiftId: existing ? existing.id : null,
    isAttended: existing ? existing.isAttended : false
  };
  Modal.getOrCreateInstance(document.getElementById('shiftModal')).show();
};

const assignShift = async (type) => {
  try {
    await api.post('/shifts', {
      userId: currentForm.value.staffId,
      shiftDate: currentForm.value.dateStr,
      shiftType: type
    });
    Modal.getInstance(document.getElementById('shiftModal')).hide();
    fetchData();
  } catch (error) { alert('Lỗi xếp ca'); }
};

const checkIn = async (id) => {
  try {
    await api.put(`/shifts/${id}/check-in`);
    Modal.getInstance(document.getElementById('shiftModal')).hide();
    fetchData();
    alert('Điểm danh thành công!');
  } catch (error) { alert('Lỗi điểm danh'); }
};

onMounted(() => {
  generateWeekDays();
  fetchData();
});
</script>

<style scoped>
.sticky-start { position: sticky; z-index: 1; }
.cell-hover:hover { background-color: #F8FAFC !important; border: 2px solid #2563EB; }
</style>