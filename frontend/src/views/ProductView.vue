<template>
  <div class="product-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Quản Lý Sản Phẩm (V2.0)</h3>
        <p class="text-secondary-txt mt-1 mb-0">Hỗ trợ MinIO Upload & Phân loại Danh mục</p>
      </div>
      <button class="btn btn-primary-sport fw-semibold" @click="openAddModal">
        <i class="bi bi-plus-lg me-1"></i> Thêm Sản Phẩm
      </button>
    </div>

    <div class="card border-0 shadow-sm rounded-4 mb-4">
      <div class="card-body p-3">
        <div class="row g-3">
          <div class="col-md-6">
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
              <input type="text" class="form-control border-start-0 ps-0" v-model="searchQuery" placeholder="Tìm kiếm tên sản phẩm...">
            </div>
          </div>
          <div class="col-md-4">
            <select class="form-select" v-model="selectedCategory">
              <option value="">-- Tất cả danh mục --</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm rounded-4 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
          <tr>
            <th class="py-3 px-4 text-secondary-txt">Hình ảnh</th>
            <th class="py-3 px-4 text-secondary-txt">Tên Sản Phẩm</th>
            <th class="py-3 px-4 text-secondary-txt">Danh mục</th>
            <th class="py-3 px-4 text-secondary-txt">Giá Bán</th>
            <th class="py-3 px-4 text-secondary-txt">Tồn Kho</th>
            <th class="py-3 px-4 text-secondary-txt text-center">Hành Động</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="isLoading">
            <td colspan="6" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td>
          </tr>
          <tr v-else-if="filteredProducts.length === 0">
            <td colspan="6" class="text-center py-4 text-muted">Không tìm thấy sản phẩm phù hợp.</td>
          </tr>
          <tr v-else v-for="product in filteredProducts" :key="product.id">
            <td class="px-4">
              <img :src="product.imageUrl || 'https://via.placeholder.com/50'" class="rounded-3 shadow-sm" width="50" height="50" style="object-fit: cover;">
            </td>
            <td class="px-4 fw-bold text-body-txt">{{ product.name }}</td>
            <td class="px-4">
              <span class="badge bg-secondary">{{ getCategoryName(product.categoryId) }}</span>
            </td>
            <td class="px-4 text-success fw-bold">{{ formatCurrency(product.price) }}</td>
            <td class="px-4">
              <span class="badge" :class="product.stock > 10 ? 'bg-success-bsp' : 'bg-danger'">{{ product.stock }}</span>
            </td>
            <td class="px-4 text-center">
              <button class="btn btn-sm btn-light text-primary me-2" @click="openEditModal(product)"><i class="bi bi-pencil-square"></i></button>
              <button class="btn btn-sm btn-light text-danger" @click="deleteProduct(product.id)"><i class="bi bi-trash"></i></button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="productModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold">{{ isEditMode ? 'Cập Nhật Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">
            <form @submit.prevent="saveProduct">
              <div class="row">
                <div class="col-md-4 text-center mb-3">
                  <label class="form-label fw-medium d-block">Hình ảnh sản phẩm</label>
                  <div class="position-relative d-inline-block">
                    <img :src="form.imageUrl || 'https://via.placeholder.com/150'" class="rounded-4 shadow-sm mb-3" width="150" height="150" style="object-fit: cover; border: 2px dashed #CBD5E1;">
                    <div v-if="isUploading" class="position-absolute top-50 start-50 translate-middle">
                      <div class="spinner-border text-primary" role="status"></div>
                    </div>
                  </div>
                  <input type="file" ref="fileInput" class="d-none" accept="image/*" @change="handleFileUpload">
                  <button type="button" class="btn btn-sm btn-outline-primary w-100" @click="$refs.fileInput.click()" :disabled="isUploading">
                    <i class="bi bi-cloud-upload me-1"></i> Chọn ảnh từ máy
                  </button>
                </div>

                <div class="col-md-8">
                  <div class="mb-3">
                    <label class="form-label fw-medium">Tên sản phẩm <span class="text-danger">*</span></label>
                    <input type="text" v-model="form.name" class="form-control" required>
                  </div>
                  <div class="mb-3">
                    <label class="form-label fw-medium">Danh mục</label>
                    <select v-model="form.categoryId" class="form-select" required>
                      <option value="" disabled>-- Chọn danh mục --</option>
                      <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                    </select>
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
                </div>
              </div>

              <div class="d-grid mt-4">
                <button type="submit" class="btn btn-primary-sport py-2 fw-bold" :disabled="isSaving || isUploading">
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
import { ref, computed, onMounted } from 'vue';
import api from '../api/axios';
import { Modal } from 'bootstrap';

const products = ref([]);
const categories = ref([]);
const isLoading = ref(false);
const isSaving = ref(false);
const isUploading = ref(false);
const isEditMode = ref(false);
const fileInput = ref(null); // Ref để trỏ tới thẻ input type="file"

// Trạng thái tìm kiếm và lọc
const searchQuery = ref('');
const selectedCategory = ref('');

const form = ref({ id: null, name: '', price: 0, stock: 0, imageUrl: '', categoryId: '' });

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

// Tải danh mục từ DB
const fetchCategories = async () => {
  try {
    const res = await api.get('/categories');
    categories.value = res.data.data;
  } catch (error) { console.error('Lỗi tải danh mục', error); }
};

// Lấy tên danh mục để hiển thị ra bảng
const getCategoryName = (id) => {
  const cat = categories.value.find(c => c.id === id);
  return cat ? cat.name : 'Chưa phân loại';
};

const fetchProducts = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/products');
    products.value = response.data.data;
  } catch (error) { console.error(error); }
  finally { isLoading.value = false; }
};

// COMPUTED: Lọc mờ (Fuzzy Search) và Lọc theo Danh mục
const filteredProducts = computed(() => {
  return products.value.filter(p => {
    const matchSearch = p.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchCategory = selectedCategory.value === '' || p.categoryId === selectedCategory.value;
    return matchSearch && matchCategory;
  });
});

// XỬ LÝ UPLOAD ẢNH MINIO
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  isUploading.value = true;
  try {
    // Gọi thẳng API Upload em vừa test lúc nãy
    const response = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    // Gán URL MinIO trả về vào Form
    form.value.imageUrl = response.data.data;
  } catch (error) {
    alert('Lỗi khi tải ảnh lên máy chủ MinIO!');
    console.error(error);
  } finally {
    isUploading.value = false;
    if (fileInput.value) fileInput.value.value = ''; // Reset input file
  }
};

const openAddModal = () => {
  isEditMode.value = false;
  form.value = { id: null, name: '', price: 0, stock: 0, imageUrl: '', categoryId: '' };
  Modal.getOrCreateInstance(document.getElementById('productModal')).show();
};

const openEditModal = (product) => {
  isEditMode.value = true;
  form.value = { ...product };
  Modal.getOrCreateInstance(document.getElementById('productModal')).show();
};

const saveProduct = async () => {
  isSaving.value = true;
  try {
    if (isEditMode.value) await api.put(`/products/${form.value.id}`, form.value);
    else await api.post('/products', form.value);

    Modal.getInstance(document.getElementById('productModal'))?.hide();
    await fetchProducts();
  } catch (error) { alert(error.response?.data?.message || 'Có lỗi xảy ra!'); }
  finally { isSaving.value = false; }
};

const deleteProduct = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa?')) return;
  try {
    await api.delete(`/products/${id}`);
    await fetchProducts();
  } catch (error) { alert('Lỗi xóa sản phẩm'); }
};

onMounted(() => {
  fetchCategories();
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