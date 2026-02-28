<template>
  <div class="dashboard-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Tổng Quan Hệ Thống</h3>
        <p class="text-secondary-txt mt-1 mb-0">Thống kê doanh thu 30 ngày gần nhất</p>
      </div>
      <button class="btn btn-outline-primary" @click="fetchRevenue">
        <i class="bi bi-arrow-clockwise me-1"></i> Cập nhật
      </button>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="card border-0 shadow-sm rounded-4 h-100 bg-gradient-primary text-white">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h6 class="fw-bold text-white-50 mb-2">TỔNG DOANH THU (30 NGÀY)</h6>
                <h2 class="fw-bold mb-0">{{ formatCurrency(totalRevenue30Days) }}</h2>
              </div>
              <div class="icon-box bg-white bg-opacity-25 rounded-circle d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                <i class="bi bi-cash-stack fs-4"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h6 class="fw-bold text-secondary mb-2">NGÀY DOANH THU CAO NHẤT</h6>
                <h3 class="fw-bold text-success mb-0">{{ formatCurrency(maxRevenueDay.revenue) }}</h3>
                <small class="text-muted">Ngày: {{ maxRevenueDay.date }}</small>
              </div>
              <div class="icon-box bg-light text-success rounded-circle d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                <i class="bi bi-graph-up-arrow fs-4"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h6 class="fw-bold text-secondary mb-2">TRẠNG THÁI HỆ THỐNG</h6>
                <h3 class="fw-bold text-primary mb-0">Hoạt động tốt</h3>
                <small class="text-muted">Đã kết nối Database & MinIO</small>
              </div>
              <div class="icon-box bg-light text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                <i class="bi bi-server fs-4"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm rounded-4">
      <div class="card-header bg-white border-bottom-0 pt-4 pb-0 px-4">
        <h5 class="fw-bold text-heading">
          <i class="bi bi-bar-chart-fill text-primary me-2"></i>Biểu Đồ Doanh Thu Theo Ngày
        </h5>
      </div>
      <div class="card-body p-4" style="height: 400px;">
        <div v-if="isLoading" class="h-100 d-flex align-items-center justify-content-center">
          <div class="spinner-border text-primary" role="status"></div>
        </div>
        <div v-else-if="!hasData" class="h-100 d-flex align-items-center justify-content-center text-muted">
          Chưa có dữ liệu doanh thu trong 30 ngày qua. Hãy chốt đơn để xem thống kê!
        </div>
        <Bar v-else :data="chartData" :options="chartOptions" />
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '../api/axios';
import { Bar } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';

// Đăng ký các module của ChartJS
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const rawData = ref([]);
const isLoading = ref(false);

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

// Computed tính tổng tiền
const totalRevenue30Days = computed(() => {
  return rawData.value.reduce((sum, item) => sum + item.revenue, 0);
});

// Computed tìm ngày doanh thu cao nhất
const maxRevenueDay = computed(() => {
  if (rawData.value.length === 0) return { date: 'N/A', revenue: 0 };
  return rawData.value.reduce((max, item) => item.revenue > max.revenue ? item : max, rawData.value[0]);
});

const hasData = computed(() => rawData.value.length > 0);

// Dữ liệu cho biểu đồ
const chartData = ref({
  labels: [],
  datasets: []
});

// Cấu hình hiển thị biểu đồ đẹp mắt
const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: function(context) {
          return 'Doanh thu: ' + formatCurrency(context.raw);
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function(value) {
          // Rút gọn số tiền trên trục Y (VD: 1,000,000 -> 1 Tr)
          if (value >= 1000000) return (value / 1000000) + ' Tr';
          if (value >= 1000) return (value / 1000) + ' K';
          return value;
        }
      }
    }
  }
};

// Hàm gọi API lấy dữ liệu
const fetchRevenue = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/dashboard/revenue/daily');
    const data = res.data.data; // Mảng: [{date: '2026-02-28', revenue: 150000}]

    // API trả về giảm dần (mới nhất đầu tiên), ta cần đảo ngược lại để vẽ biểu đồ theo chiều thời gian (cũ -> mới)
    const reversedData = [...data].reverse();
    rawData.value = reversedData;

    // Map dữ liệu vào ChartJS
    chartData.value = {
      labels: reversedData.map(item => item.date),
      datasets: [
        {
          label: 'Doanh Thu',
          backgroundColor: '#2563EB', // Màu Primary Sport
          borderRadius: 6,
          data: reversedData.map(item => item.revenue)
        }
      ]
    };
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu thống kê:', error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchRevenue();
});
</script>

<style scoped>
.text-heading { color: #0F172A; }
.text-secondary-txt { color: #64748B; font-size: 0.9rem; }
.bg-gradient-primary {
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
}
</style>