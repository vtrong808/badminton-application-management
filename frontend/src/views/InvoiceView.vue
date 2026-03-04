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
                        <h6 class="fw-bold text-truncate mb-1">{{ product.name }}</h6>
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
                  <tr v-if="cart.length === 0"><td class="text-center py-5 text-muted">Giỏ hàng trống</td></tr>
                  <tr v-for="(item, index) in cart" :key="item.product.id" class="border-bottom border-light">
                    <td class="ps-3 w-50">
                      <strong class="d-block text-heading text-truncate">{{ item.product.name }}</strong>
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
                  <span class="text-secondary fw-medium">Tổng tiền:</span>
                  <strong class="fs-5 text-heading">{{ formatCurrency(cartTotal) }}</strong>
                </div>

                <div class="mb-3 d-flex gap-2">
                  <input type="radio" class="btn-check" name="payMethod" id="cash" value="CASH" v-model="paymentMethod">
                  <label class="btn btn-outline-primary flex-fill fw-bold" for="cash"><i class="bi bi-cash-coin me-1"></i> TIỀN MẶT</label>

                  <input type="radio" class="btn-check" name="payMethod" id="transfer" value="TRANSFER" v-model="paymentMethod">
                  <label class="btn btn-outline-primary flex-fill fw-bold" for="transfer"><i class="bi bi-qr-code-scan me-1"></i> CHUYỂN KHOẢN</label>
                </div>

                <div v-if="paymentMethod === 'TRANSFER' && cartTotal > 0" class="text-center mb-3 p-3 bg-white rounded-3 border">
                  <h6 class="fw-bold text-primary mb-2">Quét mã để thanh toán</h6>
                  <img :src="`https://img.vietqr.io/image/970436-0123456789-compact2.png?amount=${cartTotal}&addInfo=Thanh toan BSP&accountName=BSP CENTER`" class="img-fluid rounded mb-2" style="max-height: 200px;">

                  <div class="mb-2 text-start">
                    <label class="small text-muted mb-1">Tải ảnh bill đã chuyển khoản (MinIO)</label>
                    <div class="input-group input-group-sm">
                      <input type="file" class="form-control" @change="uploadProof" accept="image/*" :disabled="isUploading">
                    </div>
                  </div>
                  <div v-if="proofUrl" class="text-success small fw-bold"><i class="bi bi-check-circle-fill"></i> Đã tải ảnh thành công</div>
                </div>

                <button class="btn btn-success w-100 py-3 fw-bold fs-5 shadow-sm btn-checkout"
                        :disabled="cart.length === 0 || isProcessing || (paymentMethod === 'TRANSFER' && !proofUrl)" @click="processCheckout">
                  <span v-if="isProcessing" class="spinner-border spinner-border-sm me-2"></span>
                  {{ paymentMethod === 'TRANSFER' ? 'XÁC NHẬN CHỜ DUYỆT' : 'THANH TOÁN LẬP TỨC' }}
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
                <th class="py-3 px-3">Phương thức</th>
                <th class="py-3 px-3">Tổng Tiền</th>
                <th class="py-3 px-3">Trạng Thái</th>
                <th class="py-3 px-3 text-center">Thao Tác Nghiệp Vụ</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="inv in invoices" :key="inv.id">
                <td class="px-3 fw-bold text-primary">{{ inv.invoiceNumber }}</td>
                <td class="px-3">
                  <span class="badge" :class="inv.paymentMethod === 'TRANSFER' ? 'bg-info text-dark' : 'bg-secondary'">{{ inv.paymentMethod }}</span>
                </td>
                <td class="px-3 fw-bold text-success">{{ formatCurrency(inv.totalAmount) }}</td>
                <td class="px-3">
                    <span class="badge"
                          :class="{
                            'bg-warning text-dark': inv.status === 'DRAFT' || inv.status === 'PENDING_CONFIRMATION',
                            'bg-success': inv.status === 'FINALIZED',
                            'bg-primary': inv.status === 'EXPORTED'
                          }">
                      {{ inv.status === 'PENDING_CONFIRMATION' ? 'CHỜ DUYỆT' : inv.status }}
                    </span>
                </td>
                <td class="px-3 text-center">
                  <button class="btn btn-sm btn-outline-info me-2 fw-bold"
                          v-if="inv.proofImageUrl" @click="viewProof(inv.proofImageUrl)" title="Xem Bill">
                    <i class="bi bi-eye"></i> Bill
                  </button>

                  <button class="btn btn-sm btn-outline-success me-2 fw-bold"
                          v-if="inv.status === 'DRAFT' || inv.status === 'PENDING_CONFIRMATION'" @click="finalize(inv.id)" title="Duyệt & Trừ kho">
                    <i class="bi bi-check-circle-fill"></i> Chốt Đơn
                  </button>

                  <button class="btn btn-sm btn-primary fw-bold"
                          v-if="inv.status === 'EXPORTED'" @click="downloadPdf(inv.id)">
                    <i class="bi bi-download"></i> Tải PDF
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>

    <div class="modal fade" id="proofModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Ảnh Minh Chứng</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body text-center">
            <img :src="currentProofImg" class="img-fluid rounded">
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
const cart = ref([]);
const paymentMethod = ref('CASH');
const isProcessing = ref(false);
const isUploading = ref(false);
const proofUrl = ref('');
const currentProofImg = ref('');

const invoices = ref([]);

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const cartTotal = computed(() => cart.value.reduce((sum, item) => sum + (item.product.price * item.qty), 0));

const fetchProducts = async () => {
  const res = await api.get('/products');
  products.value = res.data.data.filter(p => p.stock > 0);
};

const addToCart = (product) => {
  const existing = cart.value.find(i => i.product.id === product.id);
  if (existing) {
    if (existing.qty < product.stock) existing.qty++;
  } else {
    cart.value.push({ product, qty: 1 });
  }
};

const updateQty = (index, change) => {
  const item = cart.value[index];
  const newQty = item.qty + change;
  if (newQty <= 0) cart.value.splice(index, 1);
  else if (newQty <= item.product.stock) item.qty = newQty;
};

// Gọi MinIO upload ảnh hóa đơn
const uploadProof = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  isUploading.value = true;
  try {
    const res = await api.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }});
    proofUrl.value = res.data.data;
  } catch (error) { alert('Lỗi tải ảnh minh chứng'); }
  finally { isUploading.value = false; }
};

const processCheckout = async () => {
  isProcessing.value = true;
  try {
    const payload = {
      paymentMethod: paymentMethod.value,
      proofImageUrl: proofUrl.value,
      items: cart.value.map(i => ({ productId: i.product.id, qty: i.qty }))
    };

    // 1. Tạo Hóa đơn
    const res = await api.post('/invoices', payload);
    const invoiceId = res.data.data.id;

    // Nếu tiền mặt, tự động chốt đơn luôn
    if(paymentMethod.value === 'CASH') {
      await api.post(`/invoices/${invoiceId}/finalize`);
      await api.post(`/invoices/${invoiceId}/export`);
      alert('Thanh toán tiền mặt thành công!');
    } else {
      alert('Đã gửi yêu cầu hóa đơn chờ duyệt!');
    }

    cart.value = [];
    proofUrl.value = '';
    paymentMethod.value = 'CASH';
    fetchProducts();
    fetchInvoices();
  } catch (error) { alert('Lỗi thanh toán!'); }
  finally { isProcessing.value = false; }
};

const fetchInvoices = async () => {
  const res = await api.get('/invoices');
  invoices.value = res.data.data.sort((a,b) => b.id - a.id);
};

const viewProof = (url) => {
  currentProofImg.value = url;
  Modal.getOrCreateInstance(document.getElementById('proofModal')).show();
};

const finalize = async (id) => {
  try {
    await api.post(`/invoices/${id}/finalize`);
    await api.post(`/invoices/${id}/export`); // Duyệt xong xuất PDF luôn
    alert('Đã duyệt đơn và xuất PDF!');
    fetchInvoices();
    fetchProducts();
  } catch (error) { alert('Lỗi chốt đơn'); }
};

const downloadPdf = async (id) => {
  try {
    const response = await api.get(`/invoices/${id}/download`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Invoice-${id}.pdf`);
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
.product-card:hover { border-color: #2563EB; box-shadow: 0 4px 12px rgba(37,99,235,0.15); transform: translateY(-2px); }
.nav-tabs .nav-link { border: none; padding: 12px 20px; font-size: 1.1rem; }
.nav-tabs .nav-link.active { border-bottom: 3px solid #2563EB; background: transparent; }
</style>