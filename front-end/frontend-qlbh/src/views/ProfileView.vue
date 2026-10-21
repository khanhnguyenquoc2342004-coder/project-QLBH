<script setup>
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

const store = useStore();
const router = useRouter();
const formRef = ref();
const user = store.getters.currentUser;
const form = reactive({ fullName: user?.fullName || '', phone: user?.phone || '', address: user?.address || '' });
const rules = { fullName: [{ required: true, message: 'Nhập họ và tên' }], phone: [{ required: true, message: 'Nhập số điện thoại để đặt hàng' }], address: [{ required: true, message: 'Nhập địa chỉ để đặt hàng' }] };
async function saveProfile() {
  try { await formRef.value.validate(); await store.dispatch('updateProfile', form); message.success('Đã cập nhật hồ sơ'); } catch (error) { if (!error?.errorFields) message.error(store.getters.error || 'Không thể cập nhật hồ sơ'); }
}
</script>
<template>
  <main class="profile-page"><header><router-link to="/"><arrow-left-outlined /> Về trang chủ</router-link></header><a-card class="profile-card" :bordered="false"><span class="eyebrow">THÔNG TIN KHÁCH HÀNG</span><h1>Hồ sơ của tôi</h1><p>Email: {{ user?.email }}</p><a-form ref="formRef" :model="form" :rules="rules" layout="vertical" @finish="saveProfile"><a-form-item label="Họ và tên" name="fullName"><a-input v-model:value="form.fullName" /></a-form-item><a-form-item label="Số điện thoại" name="phone"><a-input v-model:value="form.phone" placeholder="0901234567" /></a-form-item><a-form-item label="Địa chỉ" name="address"><a-textarea v-model:value="form.address" :rows="3" /></a-form-item><a-button html-type="submit" type="primary" :loading="store.getters.isLoading"><save-outlined /> Lưu hồ sơ</a-button></a-form></a-card></main>
</template>
<style scoped>.profile-page { min-height:100vh; background:#f7f9fd; }.profile-page header { max-width:650px; padding:24px; margin:auto; }.profile-page header a { color:#51617a; text-decoration:none; }.profile-card { width:min(100% - 32px, 560px); margin:20px auto; border:1px solid #e5ebf4; border-radius:15px; }.eyebrow { color:#4b7db9; font-size:11px; font-weight:700; letter-spacing:1px; }h1,p { margin:0; }h1 { margin-top:6px; font-size:28px; }p { margin:8px 0 24px; color:#6d7a90; }</style>
