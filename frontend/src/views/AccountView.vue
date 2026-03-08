<template>
  <div class="account-view h-100 d-flex flex-column">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-heading m-0">Quản Lý Tài Khoản</h3>
        <p class="text-secondary-txt mt-1 mb-0">Hệ thống phân quyền Nhân viên & Khách hàng</p>
      </div>
    </div>

    <ul class="nav nav-tabs mb-3" id="accountTabs" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active fw-bold text-primary" id="staff-tab" data-bs-toggle="tab" data-bs-target="#staff" type="button" role="tab">
          <i class="bi bi-person-badge me-1"></i> NHÂN VIÊN (STAFF)
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link fw-bold text-success" id="customer-tab" data-bs-toggle="tab" data-bs-target="#customer" type="button" role="tab" @click="fetchCustomers">
          <i class="bi bi-people-fill me-1"></i> KHÁCH HÀNG (CUSTOMER)
        </button>
      </li>
    </ul>

    <div class="tab-content flex-grow-1" id="accountTabsContent">

      <div class="tab-pane fade show active" id="staff" role="tabpanel">
        <div class="card border-0 shadow-sm rounded-4 mb-4">
          <div class="card-header bg-white border-0 pt-3 d-flex justify-content-end">
            <button class="btn btn-primary fw-bold" @click="openStaffModal()">
              <i class="bi bi-plus-lg"></i> Thêm Nhân Viên Mới
            </button>
          </div>
          <div class="card-body p-0 overflow-hidden">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
              <tr>
                <th class="py-3 px-4">Tài khoản (ROLE_BS)</th>
                <th class="py-3 px-4 text-end">Lương Cơ Bản</th>
                <th class="py-3 px-4 text-end text-success">Thưởng (+)</th>
                <th class="py-3 px-4 text-end text-danger">Phạt (-)</th>
                <th class="py-3 px-4 text-end fw-bold text-primary">THỰC NHẬN</th>
                <th class="py-3 px-4 text-center">Thao tác</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="staffs.length === 0"><td colspan="6" class="text-center py-4">Chưa có nhân viên.</td></tr>
              <tr v-for="staff in staffs" :key="staff.id">
                <td class="px-4 fw-bold"><i class="bi bi-person-circle text-primary me-2"></i>{{ staff.username }}</td>
                <td class="px-4 text-end">{{ formatCurrency(staff.baseSalary || 3000000) }}</td>
                <td class="px-4 text-end text-success" style="cursor: pointer" @click="openSalaryModal(staff)" title="Sửa thưởng phạt">
                  + {{ formatCurrency(staff.bonusSalary || 0) }} <i class="bi bi-pencil ms-1 text-muted"></i>
                </td>
                <td class="px-4 text-end text-danger" style="cursor: pointer" @click="openSalaryModal(staff)" title="Sửa thưởng phạt">
                  - {{ formatCurrency(staff.penaltySalary || 0) }} <i class="bi bi-pencil ms-1 text-muted"></i>
                </td>
                <td class="px-4 text-end fw-bold text-primary fs-5 bg-light">{{ formatCurrency(calculateNetSalary(staff)) }}</td>
                <td class="px-4 text-center">
                  <button class="btn btn-sm btn-light text-primary me-2" @click="openStaffModal(staff)"><i class="bi bi-pencil-square"></i></button>
                  <button class="btn btn-sm btn-light text-danger" @click="deleteStaff(staff.id)"><i class="bi bi-trash"></i></button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="tab-pane fade" id="customer" role="tabpanel">
        <div class="card border-0 shadow-sm rounded-4">
          <div class="card-header bg-white border-0 pt-3 d-flex justify-content-end">
            <button class="btn btn-success fw-bold" @click="openCustomerModal()">
              <i class="bi bi-person-plus-fill"></i> Đăng Ký Khách Hàng
            </button>
          </div>
          <div class="card-body p-0 overflow-hidden">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
              <tr>
                <th class="py-3 px-4">Số Điện Thoại</th>
                <th class="py-3 px-4">Họ và Tên</th>
                <th class="py-3 px-4 text-center">Phân Hạng (Nút Switch)</th>
                <th class="py-3 px-4 text-center">Thao Tác</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="customers.length === 0"><td colspan="4" class="text-center py-4">Chưa có dữ liệu khách hàng.</td></tr>
              <tr v-for="cus in customers" :key="cus.id">
                <td class="px-4 fw-bold text-primary">{{ cus.phoneNumber }}</td>
                <td class="px-4 fw-medium">{{ cus.fullName }}</td>
                <td class="px-4 text-center">
                  <span v-if="cus.customerType === 'FIXED'" class="badge bg-warning text-dark px-3 py-2 rounded-pill"><i class="bi bi-calendar-check-fill me-1"></i> CỐ ĐỊNH</span>
                  <span v-else-if="cus.customerType === 'REGULAR'" class="badge bg-primary px-3 py-2 rounded-pill">THƯỜNG</span>
                  <span v-else class="badge bg-secondary px-3 py-2 rounded-pill">VÃNG LAI</span>
                </td>
                <td class="px-4 text-center">
                  <button class="btn btn-sm btn-light text-success me-2" @click="openCustomerModal(cus)"><i class="bi bi-pencil-square"></i></button>
                  <button class="btn btn-sm btn-light text-danger" @click="deleteCustomer(cus.id)"><i class="bi bi-trash"></i></button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>

    <div class="modal fade" id="staffCrudModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0">
          <div class="modal-header border-0 pb-0">
            <h5 class="fw-bold">{{ isEditStaff ? 'Sửa Nhân Viên' : 'Thêm Nhân Viên' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body p-4">
            <form @submit.prevent="saveStaff">
              <div class="mb-3">
                <label class="form-label fw-medium">Tên đăng nhập (Username) <span class="text-danger">*</span></label>
                <input type="text" v-model="staffForm.username" class="form-control" required :disabled="isEditStaff">
              </div>
              <div class="mb-3">
                <label class="form-label fw-medium">Mật khẩu {{ isEditStaff ? '(Để trống nếu không đổi)' : '*' }}</label>
                <input type="password" v-model="staffForm.password" class="form-control" :required="!isEditStaff">
              </div>
              <div class="mb-4">
                <label class="form-label fw-medium">Lương cơ bản mặc định (VNĐ)</label>
                <input type="number" v-model.number="staffForm.baseSalary" class="form-control text-primary fw-bold" required min="0">
              </div>
              <button type="submit" class="btn btn-primary w-100 fw-bold py-2" :disabled="isSaving">LƯU NHÂN VIÊN</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="customerCrudModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0">
          <div class="modal-header border-0 pb-0">
            <h5 class="fw-bold">{{ isEditCustomer ? 'Cập Nhật Khách Hàng' : 'Đăng Ký Khách Hàng Mới' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body p-4">
            <form @submit.prevent="saveCustomer">
              <div class="mb-3">
                <label class="form-label fw-medium">Số điện thoại (Dùng để đăng nhập) <span class="text-danger">*</span></label>
                <input type="text" v-model="customerForm.phoneNumber" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label fw-medium">Mật khẩu {{ isEditCustomer ? '(Để trống nếu không đổi)' : '*' }}</label>
                <input type="password" v-model="customerForm.password" class="form-control" :required="!isEditCustomer">
              </div>
              <div class="mb-3">
                <label class="form-label fw-medium">Họ và tên <span class="text-danger">*</span></label>
                <input type="text" v-model="customerForm.fullName" class="form-control" required>
              </div>

              <div class="mb-4">
                <label class="form-label fw-medium">Loại Khách Hàng</label>
                <select v-model="customerForm.customerType" class="form-select bg-light fw-bold" required>
                  <option value="REGULAR">👤 Khách Thường (Regular)</option>
                  <option value="FIXED">⭐ Khách Cố Định (Fixed/Monthly)</option>
                  <option value="WALK_IN">🚶 Khách Vãng Lai (Walk-in)</option>
                </select>
              </div>

              <button type="submit" class="btn btn-success w-100 fw-bold py-2" :disabled="isSaving">LƯU KHÁCH HÀNG</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="salaryModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content rounded-4 border-0">
          <div class="modal-body p-4">
            <h6 class="fw-bold mb-3 text-center">Thưởng/Phạt: <span class="text-primary">{{ salaryForm.username }}</span></h6>
            <form @submit.prevent="updateSalary">
              <div class="mb-3">
                <label class="form-label text-success fw-bold small">Thưởng (+)</label>
                <input type="number" v-model.number="salaryForm.bonusSalary" class="form-control form-control-sm" min="0">
              </div>
              <div class="mb-3">
                <label class="form-label text-danger fw-bold small">Phạt (-)</label>
                <input type="number" v-model.number="salaryForm.penaltySalary" class="form-control form-control-sm" min="0">
              </div>
              <button type="submit" class="btn btn-sm btn-primary w-100 fw-bold" :disabled="isSaving">Lưu</button>
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

const staffs = ref([]);
const customers = ref([]);
const isSaving = ref(false);

// Form States
const isEditStaff = ref(false);
const staffForm = ref({ id: null, username: '', password: '', baseSalary: 3000000 });

const isEditCustomer = ref(false);
const customerForm = ref({ id: null, fullName: '', phoneNumber: '', password: '', customerType: 'REGULAR' });

const salaryForm = ref({ id: null, username: '', bonusSalary: 0, penaltySalary: 0 });

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const calculateNetSalary = (staff) => (staff.baseSalary || 3000000) + (staff.bonusSalary || 0) - (staff.penaltySalary || 0);

// --- FETCH DATA ---
const fetchStaffs = async () => {
  const res = await api.get('/users/staff');
  staffs.value = res.data.data;
};
const fetchCustomers = async () => {
  const res = await api.get('/customers');
  customers.value = res.data.data;
};

// --- CRUD NHÂN VIÊN ---
const openStaffModal = (staff = null) => {
  isEditStaff.value = !!staff;
  if (staff) {
    staffForm.value = { id: staff.id, username: staff.username, password: '', baseSalary: staff.baseSalary };
  } else {
    staffForm.value = { id: null, username: '', password: '', baseSalary: 3000000 };
  }
  Modal.getOrCreateInstance(document.getElementById('staffCrudModal')).show();
};

const saveStaff = async () => {
  isSaving.value = true;
  try {
    if (isEditStaff.value) await api.put(`/users/staff/${staffForm.value.id}`, staffForm.value);
    else await api.post('/users/staff', staffForm.value);

    Modal.getInstance(document.getElementById('staffCrudModal')).hide();
    fetchStaffs();
    alert('Lưu nhân viên thành công!');
  } catch (error) { alert('Lỗi: Có thể do Username đã tồn tại.'); }
  finally { isSaving.value = false; }
};

const deleteStaff = async (id) => {
  if(!confirm('Bạn có chắc muốn xóa nhân viên này? (Nếu họ đang có ca làm việc hoặc hóa đơn sẽ không thể xóa)')) return;
  try {
    await api.delete(`/users/staff/${id}`);
    fetchStaffs();
  } catch (error) { alert('Không thể xóa! Nhân viên này đang có ràng buộc dữ liệu (hóa đơn, ca làm).'); }
};

// --- CRUD KHÁCH HÀNG ---
const openCustomerModal = (cus = null) => {
  isEditCustomer.value = !!cus;
  if (cus) {
    customerForm.value = { id: cus.id, fullName: cus.fullName, phoneNumber: cus.phoneNumber, password: '', customerType: cus.customerType };
  } else {
    customerForm.value = { id: null, fullName: '', phoneNumber: '', password: '', customerType: 'REGULAR' };
  }
  Modal.getOrCreateInstance(document.getElementById('customerCrudModal')).show();
};

const saveCustomer = async () => {
  isSaving.value = true;
  try {
    if (isEditCustomer.value) await api.put(`/customers/${customerForm.value.id}`, customerForm.value);
    else await api.post('/customers', customerForm.value);

    Modal.getInstance(document.getElementById('customerCrudModal')).hide();
    fetchCustomers();
    alert('Lưu khách hàng thành công!');
  } catch (error) { alert('Lỗi: Có thể do Số điện thoại đã bị trùng.'); }
  finally { isSaving.value = false; }
};

const deleteCustomer = async (id) => {
  if(!confirm('Xác nhận xóa khách hàng này?')) return;
  try {
    await api.delete(`/customers/${id}`);
    fetchCustomers();
  } catch (error) { alert('Không thể xóa! Khách này đã có lịch sử đặt sân.'); }
};

// --- UPDATE THƯỞNG PHẠT ---
const openSalaryModal = (staff) => {
  salaryForm.value = { id: staff.id, username: staff.username, bonusSalary: staff.bonusSalary, penaltySalary: staff.penaltySalary };
  Modal.getOrCreateInstance(document.getElementById('salaryModal')).show();
};
const updateSalary = async () => {
  isSaving.value = true;
  try {
    await api.put(`/users/staff/${salaryForm.value.id}/salary`, salaryForm.value);
    Modal.getInstance(document.getElementById('salaryModal')).hide();
    fetchStaffs();
  } catch (error) { alert('Lỗi cập nhật lương'); }
  finally { isSaving.value = false; }
};

onMounted(() => { fetchStaffs(); });
</script>

<style scoped>
.cursor-pointer { cursor: pointer; }
</style>