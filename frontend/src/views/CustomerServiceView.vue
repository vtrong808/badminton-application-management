<template>
  <div class="customer-service-view h-100 d-flex flex-column">

    <div class="mb-4">
      <h3 class="fw-bold text-heading m-0">Dịch Vụ & Mua Sắm</h3>
      <p class="text-secondary-txt mt-1 mb-0">Nước uống, dụng cụ và thuê vợt. Đặt trước, nhận tại quầy.</p>
    </div>

    <div class="row g-4 h-100 flex-grow-1">
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
            <input type="text" class="form-control bg-light rounded-pill px-4" v-model="searchQuery" placeholder="🔍 Tìm kiếm nước uống, phụ kiện...">
          </div>
          <div class="card-body p-4 overflow-auto" style="max-height: 600px;">
            <div class="row g-3">
              <div class="col-md-4 col-sm-6" v-for="product in filteredProducts" :key="product.id">
                <div class="card h-100 border-0 bg-light rounded-4 product-card">
                  <img :src="product.imageUrl || 'https://via.placeholder.com/150'" class="card-img-top p-3 rounded-4" style="object-fit: contain; height: 140px;">
                  <div class="card-body text-center p-3 pt-0">
                    <h6 class="fw-bold mb-1 text-truncate">{{ product.name }}</h6>
                    <p class="text-success fw-bold mb-3">{{ formatCurrency(product.price) }}</p>
                    <button class="btn btn-outline-primary btn-sm w-100 fw-bold rounded-pill" @click="addToCart(product)">
                      <i class="bi bi-plus-lg"></i> Thêm vào giỏ
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 h-100 d-flex flex-column bg-white border border-primary border-2">
          <div class="card-header bg-primary text-white fw-bold py-3 text-center rounded-top-3">
            <i class="bi bi-cart3 me-2"></i> GIỎ HÀNG CỦA BẠN
          </div>

          <div class="card-body p-0 overflow-auto" style="max-height: 300px;">
            <table class="table table-borderless align-middle mb-0">
              <tbody>
              <tr v-if="cart.length === 0"><td class="text-center py-5 text-muted">Giỏ hàng đang trống</td></tr>
              <tr v-for="(item, index) in cart" :key="item.product.id" class="border-bottom border-light">
                <td class="ps-3 w-50">
                  <strong class="d-block text-heading text-truncate small">{{ item.product.name }}</strong>
                  <span class="text-success small fw-bold">{{ formatCurrency(item.product.price) }}</span>
                </td>
                <td class="text-center w-25">
                  <div class="input-group input-group-sm">
                    <button class="btn btn-light border text-danger fw-bold" @click="updateQty(index, -1)">-</button>
                    <input type="text" class="form-control text-center bg-white border px-0 fw-bold" :value="item.qty" readonly>
                    <button class="btn btn-light border text-success fw-bold" @click="updateQty(index, 1)">+</button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div class="card-footer bg-light border-0 p-4 mt-auto rounded-bottom-4">
            <div class="d-flex justify-content-between mb-3">
              <span class="text-secondary fw-bold">TỔNG CỘNG:</span>
              <strong class="fs-4 text-danger">{{ formatCurrency(cartTotal) }}</strong>
            </div>

            <div class="mb-3 d-grid gap-2">
              <input type="radio" class="btn-check" name="payMethod" id="cash" value="CASH" v-model="paymentMethod">
              <label class="btn btn-outline-primary fw-bold rounded-pill" for="cash"><i class="bi bi-cash-coin me-1"></i> Trả tiền mặt tại quầy</label>

              <input type="radio" class="btn-check" name="payMethod" id="transfer" value="TRANSFER" v-model="paymentMethod">
              <label class="btn btn-outline-success fw-bold rounded-pill" for="transfer"><i class="bi bi-qr-code-scan me-1"></i> Chuyển khoản QR (Duyệt nhanh)</label>
            </div>

            <div v-if="paymentMethod === 'TRANSFER' && cartTotal > 0" class="text-center p-3 bg-white rounded-4 border shadow-sm mb-3">
              <h6 class="fw-bold text-dark mb-2">Quét mã để thanh toán</h6>
              <img :src="`https://img.vietqr.io/image/970436-0123456789-compact2.png?amount=${cartTotal}&addInfo=Thanh toan BSP&accountName=BSP CENTER`" class="img-fluid rounded mb-3" style="max-height: 180px;">

              <div class="text-start">
                <label class="small fw-bold text-primary mb-1"><i class="bi bi-cloud-arrow-up-fill me-1"></i> Tải ảnh hóa đơn CK lên đây:</label>
                <input type="file" class="form-control form-control-sm" @change="uploadProof" accept="image/*" :disabled="isUploading">
                <div v-if="isUploading" class="small text-warning mt-1"><i class="spinner-border spinner-border-sm"></i> Đang tải ảnh...</div>
                <div v-if="proofUrl" class="small text-success fw-bold mt-1"><i class="bi bi-check-circle-fill"></i> Tải ảnh thành công!</div>
              </div>
            </div>

            <button class="btn w-100 py-3 fw-bold fs-6 rounded-pill text-white shadow"
                    :class="paymentMethod === 'TRANSFER' ? 'btn-success' : 'btn-primary'"
                    :disabled="cart.length === 0 || isProcessing || (paymentMethod === 'TRANSFER' && !proofUrl)"
                    @click="processCheckout">
              <span v-if="isProcessing" class="spinner-border spinner-border-sm me-2"></span>
              {{ paymentMethod === 'TRANSFER' ? 'XÁC NHẬN THANH TOÁN CK' : 'ĐẶT TRƯỚC (CHỜ TẠI QUẦY)' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '../api/axios';

const products = ref([]);
const cart = ref([]);
const paymentMethod = ref('TRANSFER'); // Mặc định khuyến khích chuyển khoản
const isProcessing = ref(false);
const isUploading = ref(false);
const proofUrl = ref('');
const searchQuery = ref('');

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const cartTotal = computed(() => cart.value.reduce((sum, item) => sum + (item.product.price * item.qty), 0));

const filteredProducts = computed(() => {
  return products.value.filter(p => p.stock > 0 && p.name.toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const fetchProducts = async () => {
  try {
    const res = await api.get('/products');
    products.value = res.data.data;
  } catch (error) { console.error('Lỗi lấy SP', error); }
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

const uploadProof = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  isUploading.value = true;
  try {
    const res = await api.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }});
    proofUrl.value = res.data.data; // Lưu URL từ MinIO
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

    await api.post('/invoices', payload);

    alert(paymentMethod.value === 'TRANSFER'
        ? 'Gửi yêu cầu thành công! Đơn hàng đang chờ nhân viên kiểm tra hóa đơn.'
        : 'Gửi yêu cầu thành công! Vui lòng thanh toán tiền mặt tại quầy.');

    cart.value = [];
    proofUrl.value = '';
    fetchProducts();
  } catch (error) { alert('Lỗi thanh toán!'); }
  finally { isProcessing.value = false; }
};

onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.product-card:hover { border: 2px solid #2563EB !important; transform: translateY(-3px); transition: all 0.2s; cursor: pointer; }
.text-heading { color: #0F172A; }
</style>