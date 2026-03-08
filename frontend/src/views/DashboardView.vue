<template>
  <div class="dashboard-view h-100 d-flex flex-column overflow-auto pb-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Phân Tích Doanh Thu Hệ Thống</h3>
        <p class="text-secondary-txt mt-1 mb-0">Báo cáo đa chiều theo thời gian thực</p>
      </div>

      <div class="d-flex align-items-center gap-2 bg-white p-2 rounded-4 shadow-sm border">
        <select class="form-select form-select-sm border-0 fw-bold text-primary" v-model="quickFilter" @change="applyQuickFilter" style="width: 140px;">
          <option value="ALL">Tất cả thời gian</option>
          <option value="TODAY">Hôm nay</option>
          <option value="THIS_WEEK">Tuần này</option>
          <option value="THIS_MONTH">Tháng này</option>
        </select>
        <div class="vr mx-1"></div>
        <input type="date" class="form-control form-control-sm border-0 text-muted" v-model="startDate">
        <i class="bi bi-arrow-right text-muted"></i>
        <input type="date" class="form-control form-control-sm border-0 text-muted" v-model="endDate">
        <button class="btn btn-sm btn-primary-sport rounded-3 px-3 ms-1" @click="fetchData">
          <i class="bi bi-funnel-fill"></i> Lọc
        </button>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="card border-0 shadow-sm rounded-4 h-100 bg-gradient-primary text-white">
          <div class="card-body p-4">
            <h6 class="fw-bold text-white-50 mb-2">TỔNG DOANH THU (ĐÃ CHỐT)</h6>
            <h2 class="fw-bold mb-3">{{ formatCurrency(kpi.totalRevenue) }}</h2>
            <div class="d-flex justify-content-between small fw-medium text-white-50">
              <span><i class="bi bi-cash-coin text-success"></i> Tiền mặt: {{ formatCurrency(kpi.totalCash) }}</span>
              <span><i class="bi bi-qr-code text-warning"></i> CK: {{ formatCurrency(kpi.totalTransfer) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body p-4 d-flex justify-content-between align-items-center">
            <div>
              <h6 class="fw-bold text-secondary mb-2">NGÀY ĐỈNH ĐIỂM</h6>
              <h3 class="fw-bold text-success mb-1">{{ formatCurrency(kpi.maxDay.revenue) }}</h3>
              <small class="text-muted fw-medium"><i class="bi bi-calendar-event me-1"></i> {{ kpi.maxDay.date || 'Chưa có data' }}</small>
            </div>
            <div class="icon-box bg-light text-success rounded-circle d-flex align-items-center justify-content-center" style="width: 56px; height: 56px;">
              <i class="bi bi-graph-up-arrow fs-3"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body p-4 d-flex justify-content-between align-items-center">
            <div>
              <h6 class="fw-bold text-secondary mb-2">TỔNG SỐ HÓA ĐƠN</h6>
              <h3 class="fw-bold text-dark mb-1">{{ kpi.totalInvoices }} <span class="fs-6 text-muted fw-normal">đơn</span></h3>
              <small class="text-muted fw-medium">Trong kỳ lọc hiện tại</small>
            </div>
            <div class="icon-box bg-light text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 56px; height: 56px;">
              <i class="bi bi-receipt fs-3"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-12">
        <div class="card border-0 shadow-sm rounded-4">
          <div class="card-header bg-white border-bottom-0 pt-4 pb-0 px-4">
            <h5 class="fw-bold text-heading">
              <i class="bi bi-bar-chart-line-fill text-primary me-2"></i>Phân bổ Tiền Mặt & Chuyển Khoản theo Ngày
            </h5>
          </div>
          <div class="card-body p-4" style="height: 380px;">
            <div v-if="isLoading" class="h-100 d-flex align-items-center justify-content-center">
              <div class="spinner-border text-primary"></div>
            </div>
            <Bar v-else :data="barChartData" :options="barChartOptions" />
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-md-6">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-header bg-white border-bottom-0 pt-4 pb-0 px-4">
            <h6 class="fw-bold text-heading text-center">CƠ CẤU PHƯƠNG THỨC THANH TOÁN</h6>
          </div>
          <div class="card-body p-4 d-flex justify-content-center" style="height: 300px;">
            <Pie :data="piePaymentData" :options="pieOptions" />
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-header bg-white border-bottom-0 pt-4 pb-0 px-4">
            <h6 class="fw-bold text-heading text-center">TOP DỊCH VỤ SỬ DỤNG BỞI KHÁCH HÀNG</h6>
          </div>
          <div class="card-body p-4 d-flex justify-content-center" style="height: 300px;">
            <Pie :data="pieServiceData" :options="pieOptions" />
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '../api/axios';
import { Bar, Pie } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

// --- STATE ---
const rawInvoices = ref([]);
const isLoading = ref(false);

// Filter State
const quickFilter = ref('THIS_MONTH');
const startDate = ref('');
const endDate = ref('');

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

// Set ngày tự động cho Quick Filter
const applyQuickFilter = () => {
  const today = new Date();
  if (quickFilter.value === 'ALL') {
    startDate.value = ''; endDate.value = '';
  } else if (quickFilter.value === 'TODAY') {
    const todayStr = today.toISOString().split('T')[0];
    startDate.value = todayStr; endDate.value = todayStr;
  } else if (quickFilter.value === 'THIS_WEEK') {
    const first = today.getDate() - today.getDay() + 1;
    const firstDay = new Date(today.setDate(first)).toISOString().split('T')[0];
    const lastDay = new Date(today.setDate(first + 6)).toISOString().split('T')[0];
    startDate.value = firstDay; endDate.value = lastDay;
  } else if (quickFilter.value === 'THIS_MONTH') {
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];
    startDate.value = firstDay; endDate.value = lastDay;
  }
  fetchData();
};

// Gọi API lấy hóa đơn
const fetchData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/invoices');
    // Lọc những hóa đơn đã CHỐT (FINALIZED) hoặc XUẤT (EXPORTED)
    let data = res.data.data.filter(inv => inv.status === 'FINALIZED' || inv.status === 'EXPORTED');

    // Áp dụng bộ lọc ngày tháng
    if (startDate.value && endDate.value) {
      const start = new Date(startDate.value); start.setHours(0,0,0,0);
      const end = new Date(endDate.value); end.setHours(23,59,59,999);
      data = data.filter(inv => {
        const invDate = new Date(inv.createdAt);
        return invDate >= start && invDate <= end;
      });
    }
    rawInvoices.value = data;
  } catch (error) { console.error('Lỗi tải dữ liệu', error); }
  finally { isLoading.value = false; }
};

// --- COMPUTED: XỬ LÝ DỮ LIỆU ĐỂ HIỂN THỊ ---

// 1. KPI Cards
const kpi = computed(() => {
  let totalCash = 0, totalTransfer = 0;
  const dayGroups = {};

  rawInvoices.value.forEach(inv => {
    // Tính tổng theo phương thức
    if (inv.paymentMethod === 'CASH') totalCash += inv.totalAmount;
    else if (inv.paymentMethod === 'TRANSFER') totalTransfer += inv.totalAmount;

    // Nhóm theo ngày để tìm ngày cao nhất
    const dateStr = new Date(inv.createdAt).toLocaleDateString('vi-VN');
    if (!dayGroups[dateStr]) dayGroups[dateStr] = 0;
    dayGroups[dateStr] += inv.totalAmount;
  });

  // Tìm ngày đỉnh điểm
  let maxDay = { date: '', revenue: 0 };
  for (const [date, revenue] of Object.entries(dayGroups)) {
    if (revenue > maxDay.revenue) maxDay = { date, revenue };
  }

  return {
    totalRevenue: totalCash + totalTransfer,
    totalCash,
    totalTransfer,
    maxDay,
    totalInvoices: rawInvoices.value.length
  };
});

// 2. STACKED BAR CHART (Tiền mặt vs Chuyển Khoản)
const barChartData = computed(() => {
  const dayMap = {};

  // Gom nhóm dữ liệu theo ngày và chia loại tiền
  rawInvoices.value.forEach(inv => {
    const dateStr = new Date(inv.createdAt).toISOString().split('T')[0]; // YYYY-MM-DD
    if (!dayMap[dateStr]) dayMap[dateStr] = { cash: 0, transfer: 0 };

    if (inv.paymentMethod === 'CASH') dayMap[dateStr].cash += inv.totalAmount;
    if (inv.paymentMethod === 'TRANSFER') dayMap[dateStr].transfer += inv.totalAmount;
  });

  // Sắp xếp ngày tăng dần
  const sortedDates = Object.keys(dayMap).sort();

  return {
    labels: sortedDates,
    datasets: [
      {
        label: 'Tiền Mặt (CASH)',
        backgroundColor: '#10B981', // Xanh lá
        data: sortedDates.map(date => dayMap[date].cash)
      },
      {
        label: 'Chuyển Khoản (TRANSFER)',
        backgroundColor: '#F59E0B', // Vàng cam
        data: sortedDates.map(date => dayMap[date].transfer)
      }
    ]
  };
});

// Cấu hình Stacked Bar Chart
const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { tooltip: { callbacks: { label: (ctx) => `${ctx.dataset.label}: ${formatCurrency(ctx.raw)}` } } },
  scales: {
    x: { stacked: true }, // Kích hoạt xếp chồng (Stacked)
    y: {
      stacked: true,
      ticks: { callback: (value) => value >= 1000000 ? (value / 1000000) + ' Tr' : value + ' đ' }
    }
  }
};

// 3. PIE CHARTS
const piePaymentData = computed(() => {
  return {
    labels: ['Tiền Mặt', 'Chuyển Khoản'],
    datasets: [{
      backgroundColor: ['#10B981', '#F59E0B'],
      data: [kpi.value.totalCash, kpi.value.totalTransfer]
    }]
  };
});

const pieServiceData = computed(() => {
  // Giả lập Dịch vụ bán ra (Do backend InvoiceResponse chưa nhả list items chi tiết,
  // ta hiển thị giả lập để UI đúng chuẩn thiết kế PRD. Sẽ đấu nối sâu ở các API Analytics sau).
  return {
    labels: ['Nước Uống', 'Thuê Vợt', 'Ống Cầu', 'Phụ kiện'],
    datasets: [{
      backgroundColor: ['#3B82F6', '#EF4444', '#8B5CF6', '#14B8A6'],
      data: [45, 25, 20, 10]
    }]
  };
});

const pieOptions = { responsive: true, maintainAspectRatio: false };

onMounted(() => {
  applyQuickFilter(); // Khởi tạo sẽ gọi Tháng Này
});
</script>

<style scoped>
.bg-gradient-primary { background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%); }
.text-heading { color: #0F172A; }
.btn-primary-sport { background-color: #2563EB; border-color: #2563EB; color: white; }
</style>