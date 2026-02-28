<template>
  <div class="invoice-view h-100 d-flex flex-column">

    <ul class="nav nav-tabs mb-3" id="posTabs" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active fw-bold text-primary-sport" id="pos-tab" data-bs-toggle="tab" data-bs-target="#pos" type="button" role="tab">
          <i class="bi bi-cart-plus me-1"></i> BÁN HÀNG (POS)
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link fw-bold text-secondary-txt" id="history-tab" data-bs-toggle="tab" data-bs-target="#history" type="button" role="tab" @click="fetchInvoices">
          <i class="bi bi-receipt-cutoff me-1"></i> QUẢN LÝ HÓA ĐƠN
        </button>
      </li>
    </ul>

    <div class="tab-content flex-grow-1" id="posTabsContent">

      <div class="tab-pane fade show active h-100" id="pos" role="tabpanel">
        <div class="row h-100 g-3">

          <div class="col-md-7 col-lg-8 h-100 d-flex flex-column">
            <div class="card border-0 shadow-sm rounded-4 flex-grow-1 overflow-hidden">
              <div class="card-header bg-white border-bottom-0 pt-3 pb-0">
                <input type="text" class="form-control form-control-lg bg-light" placeholder="🔍 Tìm kiếm sản phẩm...">
              </div>
              <div class="card-body overflow-auto">
                <div class="row g-3">
                  <div class="col-sm-6 col-md-4 col-xl-3" v-for="product in products" :key="product.id">
                    <div class="product-card card h-100 border-0 bg-light cursor-pointer transition-all" @click="addToCart(product)">
                      <img :src="product.imageUrl || 'https://via.placeholder.com/150'" class="card-img-top p-2 rounded-4" style="object-fit: contain; height: 120px;">
                      <div class="card-body text-center p-2">
                        <h6 class="fw-bold text-truncate mb-1" :title="product.name">{{ product.name }}</h6>
                        <p class="text-success fw-bold mb-0">{{ formatCurrency(product.price) }}</p>
                        <small class="text-muted">Kho: {{ product.stock }}</small>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-5 col-lg-4 h-100">
            <div class="card border-0 shadow-sm rounded-4 h-100 d-flex flex-column bg-white">
              <div class="card-header bg-primary-sport text-white fw-bold py-3 rounded-top-4">
                <i class="bi bi-basket me-2"></i> HÓA ĐƠN BÁN LẺ
              </div>

              <div class="card-body overflow-auto p-0">
                <table class="table table-borderless align-middle mb-0">
                  <tbody>
                  <tr v-if="cart.length === 0">
                    <td class="text-center py-5 text-muted">Giỏ hàng trống</td>
                  </tr>
                  <tr v-for="(item, index) in cart" :key="item.product.id" class="border-bottom border-light">
                    <td class="ps-3 w-50">
                      <strong class="d-block text-heading">{{ item.product.name }}</strong>
                      <span class="text-success small">{{ formatCurrency(item.product.price) }}</span>
                    </td>
                    <td class="text-center w-25">
                      <div class="input-group input-group-sm">
                        <button class="btn btn-outline-secondary" @click="updateQty(index, -1)">-</button>
                        <input type="text" class="form-control text-center px-0" :value="item.qty" readonly>
                        <button class="btn btn-outline-secondary" @click="updateQty(index, 1)">+</button>
                      </div>
                    </td>
                    <td class="text-end pe-3 fw-bold text-primary">{{ formatCurrency(item.product.price * item.qty) }}</td>
                  </tr>
                  </tbody>
                </table>
              </div>

              <div class="card-footer bg-light border-0 p-3 mt-auto">
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-secondary fw-medium">Tổng tiền hàng:</span>
                  <strong class="fs-5 text-heading">{{ formatCurrency(cartTotal) }}</strong>
                </div>
                <div class="mb-3">
                  <select class="form-select bg-white" v-model="paymentMethod">
                    <option value="CASH">Tiền mặt (CASH)</option>
                    <option value="TRANSFER">Chuyển khoản (TRANSFER)</option>
                  </select>
                </div>
                <button class="btn btn-success w-100 py-3 fw-bold fs-5 shadow-sm btn-checkout"
                        :disabled="cart.length === 0 || isProcessing" @click="processCheckout">
                  <span v-if="isProcessing" class="spinner-border spinner-border-sm me-2"></span>
                  THANH TOÁN LẬP TỨC
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tab-pane fade h-100" id="history" role="tabpanel">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body overflow-auto">
            <table class="table table-hover align-middle">
              <thead class="bg-light sticky-top">
              <tr>
                <th class="py-3 px-3">Mã Hóa Đơn</th>
                <th class="py-3 px-3">Ngày Lập</th>
                <th class="py-3 px-3">Tổng Tiền</th>
                <th class="py-3 px-3">Trạng Thái</th>
                <th class="py-3 px-3 text-center">Thao Tác Nghiệp Vụ</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="isLoadingInvoices">
                <td colspan="5" class="text-center py-4"><div class="spinner-border text-primary"></div></td>
              </tr>
              <tr v-else-if="invoices.length === 0">
                <td colspan="5" class="text-center py-4 text-muted">Chưa có hóa đơn nào</td>
              </tr>
              <tr v-else v-for="inv in invoices" :key="inv.id">
                <td class="px-3 fw-bold text-primary">{{ inv.invoiceNumber }}</td>
                <td class="px-3 text-secondary">{{ new Date(inv.createdAt).toLocaleString('vi-VN') }}</td>
                <td class="px-3 fw-bold text-success">{{ formatCurrency(inv.totalAmount) }}</td>
                <td class="px-3">
                    <span class="badge"
                          :class="{
                            'bg-warning text-dark': inv.status === 'DRAFT',
                            'bg-success': inv.status === 'FINALIZED',
                            'bg-primary': inv.status === 'EXPORTED'
                          }">
                      {{ inv.status }}
                    </span>
                </td>
                <td class="px-3 text-center">
                  <button class="btn btn-sm btn-outline-success me-2 fw-bold"
                          v-if="inv.status === 'DRAFT'" @click="finalize(inv.id)" title="Chốt hóa đơn & Trừ kho">
                    <i class="bi bi-check-circle-fill"></i> Chốt Đơn
                  </button>
                  <button class="btn btn-sm btn-outline-primary me-2 fw-bold"
                          v-if="inv.status === 'FINALIZED'" @click="exportPdf(inv.id)" title="Tạo file PDF">
                    <i class="bi bi-file-earmark-pdf-fill"></i> Xuất PDF
                  </button>
                  <button class="btn btn-sm btn-primary fw-bold shadow-sm"
                          v-if="inv.status === 'EXPORTED'" @click="downloadPdf(inv.id)" title="Tải về máy">
                    <i class="bi bi-download"></i> Tải File
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '../api/axios';

// --- DATA POS ---
const products = ref([]);
const cart = ref([]);
const paymentMethod = ref('CASH');
const isProcessing = ref(false);

// --- DATA INVOICES ---
const invoices = ref([]);
const isLoadingInvoices = ref(false);

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const cartTotal = computed(() => {
  return cart.value.reduce((sum, item) => sum + (item.product.price * item.qty), 0);
});

// Lấy danh sách sản phẩm cho POS
const fetchProducts = async () => {
  try {
    const res = await api.get('/products');
    products.value = res.data.data.filter(p => p.stock > 0); // Chỉ lấy hàng còn tồn
  } catch (error) { console.error(error); }
};

// Thêm vào giỏ
const addToCart = (product) => {
  const existing = cart.value.find(i => i.product.id === product.id);
  if (existing) {
    if (existing.qty < product.stock) existing.qty++;
    else alert('Không đủ tồn kho!');
  } else {
    cart.value.push({ product, qty: 1 });
  }
};

// Cập nhật số lượng
const updateQty = (index, change) => {
  const item = cart.value[index];
  const newQty = item.qty + change;
  if (newQty <= 0) {
    cart.value.splice(index, 1); // Xóa khỏi giỏ
  } else if (newQty > item.product.stock) {
    alert('Vượt quá số lượng tồn kho!');
  } else {
    item.qty = newQty;
  }
};

// THANH TOÁN (Quy trình: Tạo DRAFT -> Tự động FINALIZED -> Tự động EXPORT)
const processCheckout = async () => {
  if (!confirm(`Thanh toán ${formatCurrency(cartTotal.value)} ?`)) return;
  isProcessing.value = true;

  try {
    const payload = {
      paymentMethod: paymentMethod.value,
      items: cart.value.map(i => ({ productId: i.product.id, qty: i.qty }))
    };

    // 1. Tạo Hóa đơn DRAFT
    const res = await api.post('/invoices', payload);
    const invoiceId = res.data.data.id;

    // 2. Chốt hóa đơn (Kích hoạt @Version / Pessimistic Locking & Trừ kho)
    await api.post(`/invoices/${invoiceId}/finalize`);

    // 3. Xuất file PDF luôn
    await api.post(`/invoices/${invoiceId}/export`);

    alert('🎉 Thanh toán thành công! Hóa đơn đã được lưu và xuất PDF.');

    // Reset giỏ hàng
    cart.value = [];
    await fetchProducts(); // Tải lại tồn kho mới
    await fetchInvoices(); // Cập nhật lại lịch sử
  } catch (error) {
    alert(error.response?.data?.message || 'Lỗi thanh toán!');
  } finally {
    isProcessing.value = false;
  }
};

// ================== NGHIỆP VỤ TAB HÓA ĐƠN ==================
const fetchInvoices = async () => {
  isLoadingInvoices.value = true;
  try {
    const res = await api.get('/invoices');
    invoices.value = res.data.data.sort((a,b) => b.id - a.id); // Mới nhất lên đầu
  } catch (error) { console.error(error); }
  finally { isLoadingInvoices.value = false; }
};

const finalize = async (id) => {
  try {
    await api.post(`/invoices/${id}/finalize`);
    alert('Chốt đơn & Trừ kho thành công!');
    fetchInvoices();
    fetchProducts();
  } catch (error) { alert(error.response?.data?.message || 'Lỗi chốt đơn'); }
};

const exportPdf = async (id) => {
  try {
    await api.post(`/invoices/${id}/export`);
    alert('Xuất PDF thành công!');
    fetchInvoices();
  } catch (error) { alert(error.response?.data?.message || 'Lỗi xuất PDF'); }
};

const downloadPdf = async (id) => {
  try {
    const response = await api.get(`/invoices/${id}/download`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;

    // Đọc tên file từ header (nếu có) hoặc gán mặc định
    let fileName = `BSP-Invoice-${id}.pdf`;
    const disposition = response.headers['content-disposition'];
    if (disposition && disposition.indexOf('filename=') !== -1) {
      fileName = disposition.split('filename=')[1].replace(/"/g, '');
    }

    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) { alert('Lỗi tải file!'); }
};

onMounted(() => {
  fetchProducts();
  fetchInvoices();
});
</script>

<style scoped>
.bg-primary-sport { background-color: #2563EB; }
.text-primary-sport { color: #2563EB; }
.btn-checkout { background-color: #10B981; border: none; }
.btn-checkout:hover { background-color: #059669; }

.product-card { border: 1px solid transparent; }
.product-card:hover {
  border-color: #2563EB;
  box-shadow: 0 4px 12px rgba(37,99,235,0.15);
  transform: translateY(-2px);
}
.nav-tabs .nav-link { border: none; padding: 12px 20px; font-size: 1.1rem; }
.nav-tabs .nav-link.active { border-bottom: 3px solid #2563EB; background: transparent; }
</style>