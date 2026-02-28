<template>
  <div class="court-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold text-heading m-0">Quản Lý Sân Cầu Lông</h3>
      <button class="btn btn-primary-sport fw-semibold" @click="openAddModal">
        <i class="bi bi-plus-lg me-1"></i> Thêm Sân Mới
      </button>
    </div>

    <div class="card border-0 shadow-sm rounded-4 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
          <tr>
            <th class="py-3 px-4 text-secondary-txt">Tên Sân</th>
            <th class="py-3 px-4 text-secondary-txt text-center">Loại Sân</th>
            <th class="py-3 px-4 text-secondary-txt text-center">Trạng Thái</th>
            <th class="py-3 px-4 text-secondary-txt">Giá Ngày (6h-17h)</th>
            <th class="py-3 px-4 text-secondary-txt">Giá Đêm (17h-23h)</th>
            <th class="py-3 px-4 text-secondary-txt text-center">Hành Động</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="isLoading">
            <td colspan="6" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td>
          </tr>
          <tr v-else-if="courts.length === 0">
            <td colspan="6" class="text-center py-4 text-muted">Chưa có dữ liệu sân.</td>
          </tr>
          <tr v-else v-for="court in courts" :key="court.id">
            <td class="px-4 fw-bold text-body-txt">
              <i class="bi bi-grid-1x2-fill text-primary me-2"></i> {{ court.name }}
            </td>
            <td class="px-4 text-center">
              <span class="badge" :class="court.type === 'VIP' ? 'bg-warning text-dark' : 'bg-secondary'">{{ court.type }}</span>
            </td>
            <td class="px-4 text-center">
                <span class="badge" :class="court.status === 'AVAILABLE' ? 'bg-success-bsp' : 'bg-danger'">
                  {{ court.status === 'AVAILABLE' ? 'Sẵn Sàng' : 'Bảo Trì' }}
                </span>
            </td>
            <td class="px-4 text-success fw-medium">{{ formatCurrency(court.priceDay) }}</td>
            <td class="px-4 text-primary fw-medium">{{ formatCurrency(court.priceNight) }}</td>
            <td class="px-4 text-center">
              <button class="btn btn-sm btn-light text-primary me-2" @click="openEditModal(court)" title="Sửa"><i class="bi bi-pencil-square"></i></button>
              <button class="btn btn-sm btn-light text-danger" @click="deleteCourt(court.id)" title="Xóa"><i class="bi bi-trash"></i></button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="courtModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold">{{ isEditMode ? 'Cập Nhật Sân' : 'Thêm Sân Mới' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">
            <form @submit.prevent="saveCourt">
              <div class="mb-3">
                <label class="form-label fw-medium">Tên sân <span class="text-danger">*</span></label>
                <input type="text" v-model="form.name" class="form-control" required placeholder="Sân 1, Sân 2...">
              </div>

              <div class="row mb-3">
                <div class="col-6">
                  <label class="form-label fw-medium">Loại Sân</label>
                  <select v-model="form.type" class="form-select">
                    <option value="STANDARD">Tiêu chuẩn (Standard)</option>
                    <option value="VIP">Thảm xịn (VIP)</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label fw-medium">Trạng Thái</label>
                  <select v-model="form.status" class="form-select">
                    <option value="AVAILABLE">Sẵn sàng hoạt động</option>
                    <option value="MAINTENANCE">Đang bảo trì</option>
                  </select>
                </div>
              </div>

              <div class="row mb-4">
                <div class="col-6">
                  <label class="form-label fw-medium">Giá Ca Ngày (VNĐ)</label>
                  <input type="number" v-model.number="form.priceDay" class="form-control" required min="0">
                </div>
                <div class="col-6">
                  <label class="form-label fw-medium">Giá Ca Đêm (VNĐ)</label>
                  <input type="number" v-model.number="form.priceNight" class="form-control" required min="0">
                </div>
              </div>

              <div class="d-grid">
                <button type="submit" class="btn btn-primary-sport py-2 fw-bold" :disabled="isSaving">
                  <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
                  {{ isEditMode ? 'Lưu Thay Đổi' : 'Tạo Sân Mới' }}
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
const isEditMode = ref(false);

const form = ref({ id: null, name: '', type: 'STANDARD', status: 'AVAILABLE', priceDay: 70000, priceNight: 90000 });

const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);

const fetchCourts = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/courts');
    courts.value = response.data.data;
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const openAddModal = () => {
  isEditMode.value = false;
  form.value = { id: null, name: '', type: 'STANDARD', status: 'AVAILABLE', priceDay: 70000, priceNight: 90000 };
  Modal.getOrCreateInstance(document.getElementById('courtModal')).show();
};

const openEditModal = (court) => {
  isEditMode.value = true;
  form.value = { ...court }; // Clone data đổ vào form
  Modal.getOrCreateInstance(document.getElementById('courtModal')).show();
};

const saveCourt = async () => {
  isSaving.value = true;
  try {
    if (isEditMode.value) await api.put(`/courts/${form.value.id}`, form.value);
    else await api.post('/courts', form.value);

    Modal.getInstance(document.getElementById('courtModal'))?.hide();
    await fetchCourts();
  } catch (error) {
    alert(error.response?.data?.message || 'Có lỗi xảy ra!');
  } finally {
    isSaving.value = false;
  }
};

const deleteCourt = async (id) => {
  if (!confirm('Xóa sân này? Lưu ý: Nếu sân đã có người đặt, hệ thống sẽ báo lỗi khóa ngoại!')) return;
  try {
    await api.delete(`/courts/${id}`);
    await fetchCourts();
  } catch (error) {
    alert(error.response?.data?.message || 'Lỗi khi xóa!');
  }
};

onMounted(() => fetchCourts());
</script>

<style scoped>
.text-heading { color: #0F172A; }
.text-body-txt { color: #334155; }
.text-secondary-txt { color: #64748B; font-weight: 600; font-size: 0.85rem; text-transform: uppercase; }
.btn-primary-sport { background-color: #2563EB; border-color: #2563EB; color: white; }
.btn-primary-sport:hover { background-color: #1D4ED8; border-color: #1D4ED8; }
.bg-success-bsp { background-color: #10B981; }
</style>