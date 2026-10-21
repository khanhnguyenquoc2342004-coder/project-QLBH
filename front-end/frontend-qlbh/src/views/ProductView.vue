<script setup>
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const packages = computed(() => store.getters.allPackages);
const packagePage = computed(() => store.getters.packagePage);
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);
const canManagePackages = computed(() => store.getters.canManagePackages);

const search = reactive({ keyword: '', status: undefined });
const formRef = ref();
const modalOpen = ref(false);
const detailOpen = ref(false);
const selectedPackage = ref(null);
const editingId = ref(null);
const form = reactive({
  code: '',
  name: '',
  price: null,
  description: '',
  category: '',
  imageUrl: '',
  discountPercent: 0,
  status: 'ACTIVE'
});

const columns = computed(() => {
  const packageColumns = [
  { title: 'Mã gói', dataIndex: 'code', key: 'code', width: 130 },
  { title: 'Tên gói dịch vụ', dataIndex: 'name', key: 'name', minWidth: 220 },
  { title: 'Danh mục', dataIndex: 'category', key: 'category', width: 150 },
  { title: 'Giá niêm yết', key: 'price', width: 165, align: 'right' },
  { title: 'Khuyến mãi', key: 'discount', width: 120, align: 'center' },
  { title: 'Trạng thái', key: 'status', width: 145, align: 'center' },
  { title: 'Mô tả', dataIndex: 'description', key: 'description', ellipsis: true }
  ];

  if (canManagePackages.value) {
    packageColumns.push({ title: 'Thao tác', key: 'action', width: 150, align: 'right' });
  }
  return packageColumns;
});

const formRules = {
  code: [
    { required: true, message: 'Nhập mã gói dịch vụ' },
    { max: 30, message: 'Mã gói tối đa 30 ký tự' }
  ],
  name: [
    { required: true, message: 'Nhập tên gói dịch vụ' },
    { max: 150, message: 'Tên gói tối đa 150 ký tự' }
  ],
  price: [
    { required: true, message: 'Nhập giá niêm yết' },
    { type: 'number', min: 0.01, message: 'Giá phải lớn hơn 0' }
  ],
  status: [{ required: true, message: 'Chọn trạng thái' }]
};

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(value || 0);
}

function resetForm() {
  editingId.value = null;
  Object.assign(form, {
    code: '',
    name: '',
    price: null,
    description: '',
    category: '',
    imageUrl: '',
    discountPercent: 0,
    status: 'ACTIVE'
  });
  formRef.value?.clearValidate();
}

function openCreateModal() {
  resetForm();
  modalOpen.value = true;
}

function openEditModal(record) {
  editingId.value = record.id;
  Object.assign(form, {
    code: record.code,
    name: record.name,
    price: Number(record.price),
    description: record.description || '',
    category: record.category || '',
    imageUrl: record.imageUrl || '',
    discountPercent: Number(record.discountPercent || 0),
    status: record.status
  });
  modalOpen.value = true;
}

function closeModal() {
  modalOpen.value = false;
  resetForm();
}

async function loadPackages(page = 0, size = 8) {
  const params = {};

  params.page = page;
  params.size = size;

  if (search.keyword.trim()) {
    params.keyword = search.keyword.trim();
  }
  if (search.status) {
    params.status = search.status;
  }

  try {
    await store.dispatch('fetchPackages', params);
  } catch {
    // Lỗi đã hiển thị bằng alert bên dưới danh sách.
  }
}

async function openDetail(record) {
  try {
    selectedPackage.value = await store.dispatch('fetchPackageDetail', record.id);
    detailOpen.value = true;
  } catch {
    message.error(store.getters.error || 'Không thể tải chi tiết gói dịch vụ');
  }
}

function handleTableChange(pagination) {
  loadPackages(pagination.current - 1, pagination.pageSize);
}

async function savePackage() {
  try {
    await formRef.value.validate();

    const data = {
      ...form,
      code: form.code.trim(),
      name: form.name.trim(),
      description: form.description.trim() || null
    };

    if (editingId.value) {
      await store.dispatch('updatePackage', { id: editingId.value, data });
      message.success('Đã cập nhật gói dịch vụ');
    } else {
      await store.dispatch('createPackage', data);
      message.success('Đã thêm gói dịch vụ');
    }

    closeModal();
    await loadPackages();
  } catch (error) {
    if (error?.errorFields) {
      return;
    }
    message.error(store.getters.error || 'Không thể lưu gói dịch vụ');
  }
}

async function removePackage(id) {
  try {
    await store.dispatch('deletePackage', id);
    message.success('Đã xóa gói dịch vụ');
  } catch {
    message.error(store.getters.error || 'Không thể xóa gói dịch vụ');
  }
}

onMounted(loadPackages);
</script>

<template>
  <section class="packages-page">
    <div class="page-heading">
      <div>
        <span class="section-kicker">DANH MỤC</span>
        <h1>Gói dịch vụ</h1>
        <p>Quản lý giá niêm yết và trạng thái kinh doanh của từng gói.</p>
      </div>
      <a-button v-if="canManagePackages" type="primary" size="large" class="create-button" @click="openCreateModal">
        <plus-outlined /> Thêm gói dịch vụ
      </a-button>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline" class="filter-form" @submit.prevent="loadPackages">
        <a-form-item label="Tìm kiếm">
          <a-input
            v-model:value="search.keyword"
            allow-clear
            placeholder="Mã hoặc tên gói"
            @press-enter="loadPackages"
          >
            <template #prefix><search-outlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="Trạng thái">
          <a-select
            v-model:value="search.status"
            allow-clear
            placeholder="Tất cả"
            :options="[
              { value: 'ACTIVE', label: 'Đang hoạt động' },
              { value: 'INACTIVE', label: 'Ngừng hoạt động' }
            ]"
          />
        </a-form-item>
        <a-space>
          <a-button type="primary" :loading="loading" @click="loadPackages">Tìm kiếm</a-button>
          <a-button :disabled="loading" @click="Object.assign(search, { keyword: '', status: undefined }); loadPackages()">
            <reload-outlined /> Đặt lại
          </a-button>
        </a-space>
      </a-form>
    </a-card>

    <a-alert v-if="error" class="error-alert" type="error" show-icon :message="error" />

    <a-card :bordered="false" class="table-card">
      <template #title>
        <div class="table-title">
          <span>Danh sách gói dịch vụ</span>
          <a-tag>{{ packages.length }} gói</a-tag>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="packages"
        :loading="loading"
        :pagination="{
          current: packagePage.number + 1,
          pageSize: packagePage.size,
          total: packagePage.totalElements,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} gói`
        }"
        @change="handleTableChange"
        :scroll="{ x: 900 }"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <span class="package-code">{{ record.code }}</span>
          </template>
          <template v-else-if="column.key === 'price'">
            <span class="price">{{ formatCurrency(record.price) }}</span>
          </template>
          <template v-else-if="column.key === 'discount'">
            <a-tag :color="record.discountPercent ? 'red' : 'default'">{{ record.discountPercent ? `-${record.discountPercent}%` : 'Không' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">
              {{ record.status === 'ACTIVE' ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space :size="0">
              <a-button type="text" aria-label="Xem chi tiết gói dịch vụ" @click="openDetail(record)">
                <eye-outlined />
              </a-button>
              <a-button type="text" aria-label="Sửa gói dịch vụ" @click="openEditModal(record)">
                <edit-outlined />
              </a-button>
              <a-popconfirm
                title="Xóa gói dịch vụ này?"
                ok-text="Xóa"
                cancel-text="Hủy"
                @confirm="removePackage(record.id)"
              >
                <a-button type="text" danger aria-label="Xóa gói dịch vụ">
                  <delete-outlined />
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? 'Cập nhật gói dịch vụ' : 'Thêm gói dịch vụ'"
      :confirm-loading="loading"
      ok-text="Lưu"
      cancel-text="Hủy"
      @cancel="closeModal"
      @ok="savePackage"
    >
      <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
        <a-row :gutter="14">
          <a-col :span="10">
            <a-form-item label="Mã gói" name="code">
              <a-input v-model:value="form.code" placeholder="VD: PKG-001" :maxlength="30" />
            </a-form-item>
          </a-col>
          <a-col :span="14">
            <a-form-item label="Trạng thái" name="status">
              <a-select v-model:value="form.status">
                <a-select-option value="ACTIVE">Đang hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Ngừng hoạt động</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="Tên gói dịch vụ" name="name">
          <a-input v-model:value="form.name" placeholder="Nhập tên gói dịch vụ" :maxlength="150" />
        </a-form-item>
        <a-form-item label="Giá niêm yết" name="price">
          <a-input-number
            v-model:value="form.price"
            :min="0.01"
            :precision="2"
            class="price-input"
            placeholder="0"
          />
        </a-form-item>
        <a-form-item label="Mô tả" name="description">
          <a-textarea
            v-model:value="form.description"
            :maxlength="1000"
            :rows="3"
            placeholder="Mô tả ngắn về gói dịch vụ"
            show-count
          />
        </a-form-item>
        <a-row :gutter="14"><a-col :span="12"><a-form-item label="Danh mục" name="category"><a-input v-model:value="form.category" :maxlength="80" placeholder="VD: Gói doanh nghiệp" /></a-form-item></a-col><a-col :span="12"><a-form-item label="Giảm giá (%)" name="discountPercent"><a-input-number v-model:value="form.discountPercent" :min="0" :max="100" class="price-input" /></a-form-item></a-col></a-row>
        <a-form-item label="Đường dẫn ảnh" name="imageUrl"><a-input v-model:value="form.imageUrl" :maxlength="500" placeholder="https://..." /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailOpen" title="Chi tiết gói dịch vụ" :footer="null">
      <a-descriptions v-if="selectedPackage" bordered :column="1" size="small">
        <a-descriptions-item label="Mã gói">{{ selectedPackage.code }}</a-descriptions-item>
        <a-descriptions-item label="Tên gói">{{ selectedPackage.name }}</a-descriptions-item>
        <a-descriptions-item label="Danh mục">{{ selectedPackage.category || 'Chưa phân loại' }}</a-descriptions-item>
        <a-descriptions-item label="Giá niêm yết">{{ formatCurrency(selectedPackage.price) }}</a-descriptions-item>
        <a-descriptions-item label="Khuyến mãi">{{ selectedPackage.discountPercent || 0 }}%</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">{{ selectedPackage.status === 'ACTIVE' ? 'Đang hoạt động' : 'Ngừng hoạt động' }}</a-descriptions-item>
        <a-descriptions-item label="Mô tả">{{ selectedPackage.description || 'Chưa có mô tả' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </section>
</template>

<style scoped>
.page-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.section-kicker {
  color: #3f78c5;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
}

h1,
p {
  margin: 0;
}

h1 {
  margin-top: 5px;
  color: #14213a;
  font-size: 30px;
  font-weight: 750;
  letter-spacing: -0.7px;
}

.page-heading p {
  margin-top: 8px;
  color: #6c7890;
}

.create-button {
  border-radius: 8px;
  font-weight: 600;
}

.filter-card,
.table-card {
  border: 1px solid #e7edf6;
  border-radius: 14px;
  box-shadow: none;
}

.filter-card {
  margin-bottom: 18px;
}

.filter-card :deep(.ant-card-body) {
  padding: 18px 20px;
}

.filter-form {
  gap: 6px 10px;
}

.filter-form :deep(.ant-form-item) {
  margin-right: 0;
}

.filter-form :deep(.ant-input),
.filter-form :deep(.ant-select) {
  min-width: 220px;
}

.error-alert {
  margin-bottom: 18px;
}

.table-card :deep(.ant-card-head) {
  min-height: 60px;
  border-bottom: 1px solid #edf0f5;
}

.table-card :deep(.ant-card-body) {
  padding: 4px 20px 18px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1a2842;
  font-size: 16px;
  font-weight: 700;
}

.table-title :deep(.ant-tag) {
  margin: 0;
  color: #54637c;
  background: #f3f6fa;
  border: 0;
}

.package-code {
  color: #245fad;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
}

.price {
  color: #1f2f4d;
  font-weight: 650;
}

.price-input {
  width: 100%;
}

@media (max-width: 650px) {
  .page-heading {
    align-items: start;
    flex-direction: column;
  }

  .filter-form {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }

  .filter-form :deep(.ant-form-item) {
    display: block;
  }

  .filter-form :deep(.ant-input),
  .filter-form :deep(.ant-select) {
    width: 100%;
    min-width: 0;
  }
}
</style>
