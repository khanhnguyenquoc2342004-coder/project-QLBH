<script setup>
import { EditOutlined, PlusOutlined, ReloadOutlined, TeamOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const users = computed(() => store.getters.allUsers);
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);
const currentUser = computed(() => store.getters.currentUser);
const formRef = ref();
const modalOpen = ref(false);
const editingUser = ref(null);
const isEditingSelf = computed(() => editingUser.value?.id === currentUser.value?.id);
const form = reactive({ fullName: '', username: '', email: '', password: '', role: 'STAFF', enabled: true });

const columns = [
  { title: 'Người dùng', key: 'user', minWidth: 210 },
  { title: 'Email', dataIndex: 'email', key: 'email', minWidth: 220 },
  { title: 'Vai trò', key: 'role', width: 145, align: 'center' },
  { title: 'Trạng thái', key: 'enabled', width: 145, align: 'center' },
  { title: 'Thao tác', key: 'action', width: 100, align: 'right' }
];

const formRules = computed(() => ({
  fullName: [{ required: true, message: 'Nhập họ và tên' }],
  username: editingUser.value ? [] : [{ required: true, message: 'Nhập tên đăng nhập' }, { pattern: /^[a-zA-Z0-9._-]{3,50}$/, message: 'Tên đăng nhập không hợp lệ' }],
  email: [{ required: true, message: 'Nhập email' }, { type: 'email', message: 'Email không hợp lệ' }],
  password: editingUser.value ? [{ min: 8, message: 'Mật khẩu mới tối thiểu 8 ký tự' }] : [{ required: true, message: 'Nhập mật khẩu' }, { min: 8, message: 'Mật khẩu tối thiểu 8 ký tự' }],
  role: [{ required: true, message: 'Chọn vai trò' }]
}));

function resetForm() {
  editingUser.value = null;
  Object.assign(form, { fullName: '', username: '', email: '', password: '', role: 'STAFF', enabled: true });
  formRef.value?.clearValidate();
}

function openCreateModal() { resetForm(); modalOpen.value = true; }
function openEditModal(user) {
  editingUser.value = user;
  Object.assign(form, { fullName: user.fullName, username: user.username, email: user.email, password: '', role: user.role, enabled: user.enabled });
  modalOpen.value = true;
}
function closeModal() { modalOpen.value = false; resetForm(); }

async function loadUsers() {
  try { await store.dispatch('fetchUsers'); } catch { /* alert hiển thị bên dưới */ }
}

async function saveUser() {
  try {
    await formRef.value.validate();
    if (editingUser.value) {
      const newPassword = form.password.trim().length ? form.password : null;
      await store.dispatch('updateUser', { id: editingUser.value.id, data: { fullName: form.fullName.trim(), email: form.email.trim(), newPassword, role: form.role, enabled: form.enabled } });
      message.success('Đã cập nhật người dùng');
    } else {
      await store.dispatch('createUser', { fullName: form.fullName.trim(), username: form.username.trim(), email: form.email.trim(), password: form.password, role: form.role });
      message.success('Đã tạo tài khoản');
    }
    closeModal();
  } catch (submitError) {
    if (!submitError?.errorFields) message.error(store.getters.error || 'Không thể lưu người dùng');
  }
}

onMounted(loadUsers);
</script>

<template>
  <section class="users-page">
    <div class="page-heading">
      <div><span class="section-kicker">QUẢN TRỊ</span><h1>Người dùng</h1><p>Phân quyền và quản lý trạng thái tài khoản trong hệ thống.</p></div>
      <a-space><a-button :loading="loading" @click="loadUsers"><reload-outlined /></a-button><a-button type="primary" size="large" class="create-button" @click="openCreateModal"><plus-outlined /> Thêm người dùng</a-button></a-space>
    </div>

    <a-alert v-if="error" class="error-alert" type="error" show-icon :message="error" />
    <a-card :bordered="false" class="table-card">
      <template #title><div class="table-title"><span>Danh sách tài khoản</span><a-tag>{{ users.length }} tài khoản</a-tag></div></template>
      <a-table :columns="columns" :data-source="users" :loading="loading" :pagination="{ pageSize: 8, showSizeChanger: false }" :scroll="{ x: 830 }" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'"><div class="user-cell"><a-avatar><template #icon><team-outlined /></template></a-avatar><span><strong>{{ record.fullName }}</strong><small>@{{ record.username }}</small></span></div></template>
          <template v-else-if="column.key === 'role'"><a-tag :color="record.role === 'ADMIN' ? 'blue' : record.role === 'CUSTOMER' ? 'purple' : 'default'">{{ record.role === 'ADMIN' ? 'Quản trị viên' : record.role === 'CUSTOMER' ? 'Khách hàng' : 'Nhân viên' }}</a-tag></template>
          <template v-else-if="column.key === 'enabled'"><a-tag :color="record.enabled ? 'green' : 'red'">{{ record.enabled ? 'Đang hoạt động' : 'Đã khóa' }}</a-tag></template>
          <template v-else-if="column.key === 'action'"><a-button type="text" aria-label="Sửa người dùng" @click="openEditModal(record)"><edit-outlined /></a-button></template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" :title="editingUser ? 'Cập nhật người dùng' : 'Thêm người dùng'" :confirm-loading="loading" ok-text="Lưu" cancel-text="Hủy" @cancel="closeModal" @ok="saveUser">
      <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
        <a-alert v-if="isEditingSelf" class="self-edit-alert" type="info" show-icon message="Bạn không thể tự đổi vai trò hoặc tự khóa tài khoản." description="Hãy đăng nhập bằng một quản trị viên khác để thay đổi hai thông tin này." />
        <a-form-item label="Họ và tên" name="fullName"><a-input v-model:value="form.fullName" :maxlength="120" /></a-form-item>
        <a-form-item label="Tên đăng nhập" name="username"><a-input v-model:value="form.username" :disabled="Boolean(editingUser)" :maxlength="50" /></a-form-item>
        <a-form-item label="Email" name="email"><a-input v-model:value="form.email" :maxlength="120" /></a-form-item>
        <a-form-item :label="editingUser ? 'Mật khẩu mới (để trống nếu giữ nguyên)' : 'Mật khẩu'" name="password"><a-input-password v-model:value="form.password" autocomplete="new-password" /></a-form-item>
        <a-row :gutter="14"><a-col :span="12"><a-form-item label="Vai trò" name="role"><a-select v-model:value="form.role" :disabled="isEditingSelf"><a-select-option value="ADMIN">Quản trị viên</a-select-option><a-select-option value="STAFF">Nhân viên</a-select-option><a-select-option value="CUSTOMER">Khách hàng</a-select-option></a-select></a-form-item></a-col><a-col :span="12"><a-form-item label="Trạng thái" name="enabled"><a-switch v-model:checked="form.enabled" :disabled="isEditingSelf" checked-children="Hoạt động" un-checked-children="Đã khóa" /></a-form-item></a-col></a-row>
      </a-form>
    </a-modal>
  </section>
</template>

<style scoped>
.page-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; margin-bottom: 24px; }.section-kicker { color: #3f78c5; font-size: 11px; font-weight: 700; letter-spacing: 1px; }h1, p { margin: 0; }h1 { margin-top: 5px; color: #14213a; font-size: 30px; font-weight: 750; letter-spacing: -0.7px; }.page-heading p { margin-top: 8px; color: #6c7890; }.create-button { border-radius: 8px; font-weight: 600; }.error-alert { margin-bottom: 18px; }.self-edit-alert { margin-bottom: 18px; }.table-card { border: 1px solid #e7edf6; border-radius: 14px; box-shadow: none; }.table-card :deep(.ant-card-head) { min-height: 60px; border-bottom: 1px solid #edf0f5; }.table-card :deep(.ant-card-body) { padding: 4px 20px 18px; }.table-title { display: flex; align-items: center; gap: 10px; color: #1a2842; font-size: 16px; font-weight: 700; }.table-title :deep(.ant-tag) { margin: 0; color: #54637c; background: #f3f6fa; border: 0; }.user-cell { display: flex; align-items: center; gap: 10px; }.user-cell :deep(.ant-avatar) { color: #236cc5; background: #e8f2ff; }.user-cell strong, .user-cell small { display: block; }.user-cell strong { color: #23314a; }.user-cell small { margin-top: 2px; color: #77849b; font-size: 12px; }@media (max-width: 650px) { .page-heading { align-items: start; flex-direction: column; } }
</style>
