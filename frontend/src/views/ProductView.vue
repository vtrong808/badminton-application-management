<template>
  <div class="product-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold text-heading m-0">Quản Lý Sản Phẩm & Dịch Vụ</h3>
      <button class="btn btn-primary-sport fw-semibold" @click="openAddModal">
        <i class="bi bi-plus-lg me-1"></i> Thêm Sản Phẩm
      </button>
    </div>

    <div class="card border-0 shadow-sm rounded-4 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
          <tr>
            <th class="py-3 px-4 text-secondary-txt">ID</th>
            <th class="py-3 px-4 text-secondary-txt">Hình ảnh</th>
            <th class="py-3 px-4 text-secondary-txt">Tên Sản Phẩm</th>
            <th class="py-3 px-4 text-secondary-txt">Giá Bán</th>
            <th class="py-3 px-4 text-secondary-txt">Tồn Kho</th>
            <th class="py-3 px-4 text-secondary-txt text-center">Hành Động</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="isLoading">
            <td colspan="6" class="text-center py-4">
              <div class="spinner-border text-primary" role="status"></div>
            </td>
          </tr>
          <tr v-else-if="products.length === 0">
            <td colspan="6" class="text-center py-4 text-muted">Chưa có dữ liệu sản phẩm.</td>
          </tr>
          <tr v-else v-for="product in products" :key="product.id">
            <td class="px-4 fw-medium">#{{ product.id }}</td>
            <td class="px-4">
              <img :src="product.imageUrl || 'https://via.placeholder.com/40'" class="rounded-3" width="40" height="40" style="object-fit: cover;">
            </td>
            <td class="px-4 fw-bold text-body-txt">{{ product.name }}</td>
            <td class="px-4 text-success fw-bold">{{ formatCurrency(product.price) }}</td>
            <td class="px-4">
              <span class="badge" :class="product.stock > 10 ? 'bg-success-bsp' : 'bg-danger'">{{ product.stock }}</span>
            </td>
            <td class="px-4 text-center">
              <button class="btn btn-sm btn-light text-primary me-2" @click="openEditModal(product)" title="Sửa">
                <i class="bi bi-pencil-square"></i>
              </button>
              <button class="btn btn-sm btn-light text-danger" @click="deleteProduct(product.id)" title="Xóa">
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="productModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold">{{ isEditMode ? 'Cập Nhật Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">
            <form @submit.prevent="saveProduct">
              <div class="mb-3">
                <label class="form-label fw-medium">Tên sản phẩm <span class="text-danger">*</span></label>
                <input type="text" v-model="form.name" class="form-control" required placeholder="Ví dụ: Nước suối Aquafina">
              </div>
              <div class="row mb-3">
                <div class="col-6">
                  <label class="form-label fw-medium">Giá bán (VNĐ) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="form.price" class="form-control" required min="0">
                </div>
                <div class="col-6">
                  <label class="form-label fw-medium">Tồn kho <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="form.stock" class="form-control" required min="0">
                </div>
              </div>
              <div class="mb-4">
                <label class="form-label fw-medium">Link hình ảnh</label>
                <input type="url" v-model="form.imageUrl" class="form-control" placeholder="https://...">
              </div>

              <div class="d-grid">
                <button type="submit" class="btn btn-primary-sport py-2 fw-bold" :disabled="isSaving">
                  <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
                  {{ isEditMode ? 'Lưu Thay Đổi' : 'Tạo Sản Phẩm' }}
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

const products = ref([]);
const isLoading = ref(false);
const isSaving = ref(false);
const isEditMode = ref(false); // Biến cờ đánh dấu đang Thêm hay Sửa

// Object lưu trữ thông tin form, có thêm trường ID dùng cho lúc Sửa
const form = ref({
  id: null,
  name: '',
  price: 0,
  stock: 0,
  imageUrl: ''
});

// Format tiền tệ VNĐ
const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Gọi API lấy danh sách
const fetchProducts = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/products');
    products.value = response.data.data;
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm:', error);
  } finally {
    isLoading.value = false;
  }
};

// Mở Modal để THÊM MỚI
const openAddModal = () => {
  isEditMode.value = false;
  form.value = { id: null, name: '', price: 0, stock: 0, imageUrl: '' };
  const modalEl = document.getElementById('productModal');
  const modal = Modal.getOrCreateInstance(modalEl);
  modal.show();
};

// Mở Modal để SỬA (Đổ dữ liệu cũ vào Form)
const openEditModal = (product) => {
  isEditMode.value = true;
  form.value = {
    id: product.id,
    name: product.name,
    price: product.price,
    stock: product.stock,
    imageUrl: product.imageUrl || ''
  };
  const modalEl = document.getElementById('productModal');
  const modal = Modal.getOrCreateInstance(modalEl);
  modal.show();
};

// Lưu Form (Tự động rẽ nhánh gọi POST hay PUT)
const saveProduct = async () => {
  isSaving.value = true;
  try {
    if (isEditMode.value) {
      // Đang ở chế độ Sửa -> Gọi PUT API
      await api.put(`/products/${form.value.id}`, form.value);
    } else {
      // Đang ở chế độ Thêm mới -> Gọi POST API
      await api.post('/products', form.value);
    }

    // Tắt modal
    const modalEl = document.getElementById('productModal');
    const modal = Modal.getInstance(modalEl);
    if (modal) modal.hide();

    // Refresh lại bảng
    await fetchProducts();
    alert(isEditMode.value ? "Cập nhật thành công!" : "Thêm sản phẩm thành công!");
  } catch (error) {
    let errorMsg = error.response?.data?.message || 'Có lỗi xảy ra khi lưu!';
    if (error.response?.data?.data && typeof error.response.data.data === 'object') {
      errorMsg = Object.values(error.response.data.data).join('\n');
    }
    alert(errorMsg);
  } finally {
    isSaving.value = false;
  }
};

// Hàm Xóa sản phẩm
const deleteProduct = async (id) => {
  // Confirm trước khi xóa cho an toàn
  if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này không? Hành động này không thể hoàn tác!')) {
    return;
  }

  try {
    await api.delete(`/products/${id}`);
    alert('Đã xóa sản phẩm thành công!');
    await fetchProducts(); // Refresh lại bảng sau khi xóa
  } catch (error) {
    alert(error.response?.data?.message || 'Lỗi khi xóa sản phẩm!');
  }
};

onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.text-heading { color: #0F172A; }
.text-body-txt { color: #334155; }
.text-secondary-txt { color: #64748B; font-weight: 600; font-size: 0.85rem; text-transform: uppercase; }
.btn-primary-sport { background-color: #2563EB; border-color: #2563EB; color: white; }
.btn-primary-sport:hover { background-color: #1D4ED8; border-color: #1D4ED8; }
.bg-success-bsp { background-color: #10B981; }
</style>