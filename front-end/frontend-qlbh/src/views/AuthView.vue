<script setup>
import { LockOutlined, MailOutlined, ShopOutlined, UserOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const router = useRouter();
const store = useStore();
const isRegister = computed(() => route.name === 'Register');
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);
const loginFormRef = ref();
const registerFormRef = ref();
const registrationSucceeded = ref(false);
const registrationFailed = ref(false);
const loginForm = reactive({ username: '', password: '' });
const registerForm = reactive({ fullName: '', username: '', email: '', password: '', confirmPassword: '' });

const loginRules = { username: [{ required: true, message: 'Nhập tên đăng nhập' }], password: [{ required: true, message: 'Nhập mật khẩu' }] };
const registerRules = {
  fullName: [{ required: true, message: 'Nhập họ và tên' }],
  username: [{ required: true, message: 'Nhập tên đăng nhập' }, { pattern: /^[a-zA-Z0-9._-]{3,50}$/, message: 'Dùng 3-50 ký tự chữ, số, ., _ hoặc -' }],
  email: [{ required: true, message: 'Nhập email' }, { type: 'email', message: 'Email không hợp lệ' }, { pattern: /^[A-Z0-9._%+-]+@gmail\.com$/i, message: 'Email phải có dạng ten@gmail.com' }],
  password: [{ required: true, message: 'Nhập mật khẩu' }, { min: 8, message: 'Mật khẩu tối thiểu 8 ký tự' }, { pattern: /[A-Za-z]/, message: 'Mật khẩu cần có chữ cái' }, { pattern: /[0-9]/, message: 'Mật khẩu cần có chữ số' }],
  confirmPassword: [{ required: true, message: 'Nhập lại mật khẩu' }, { validator: async (_rule, value) => value === registerForm.password ? Promise.resolve() : Promise.reject(new Error('Mật khẩu xác nhận không khớp')) }]
};

watch(isRegister, () => {
  registrationSucceeded.value = false;
  registrationFailed.value = false;
  loginFormRef.value?.clearValidate();
  registerFormRef.value?.clearValidate();
});

async function submitLogin() {
  try {
    await loginFormRef.value.validate();
    const user = await store.dispatch('login', loginForm);
    message.success('Đăng nhập thành công');
    const fallbackRoute = user.role === 'CUSTOMER' ? '/' : '/admin/dashboard';
    router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : fallbackRoute);
  } catch (submitError) {
    if (!submitError?.errorFields) message.error(store.getters.error || 'Không thể đăng nhập');
  }
}

async function submitRegister() {
  registrationFailed.value = false;
  try {
    await registerFormRef.value.validate();
    await store.dispatch('register', { fullName: registerForm.fullName, username: registerForm.username, email: registerForm.email, password: registerForm.password });
    registrationSucceeded.value = true;
    message.success('Đăng ký thành công');
  } catch (submitError) {
    registrationFailed.value = true;
    if (!submitError?.errorFields) {
      message.error(store.getters.error || 'Không thể đăng ký');
    }
  }
}

function handleRegisterValidationFailed() {
  registrationFailed.value = true;
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-intro">
      <div class="auth-brand"><span><shop-outlined /></span>QL Bán Hàng</div>
      <div><span class="eyebrow">QUẢN LÝ TẬP TRUNG</span><h1>Đơn giản hóa việc quản lý gói dịch vụ.</h1><p>Kiểm soát dữ liệu, quyền truy cập và mọi thao tác quản trị ở một nơi.</p></div>
      <div class="auth-caption">Vue 3 · Spring Boot · MariaDB</div>
    </section>
    <section class="auth-panel">
      <a-card :bordered="false" class="auth-card">
        <template v-if="!isRegister">
          <span class="eyebrow">CHÀO MỪNG TRỞ LẠI</span><h2>Đăng nhập</h2><p class="subtitle">Sử dụng tài khoản đã được cấp để tiếp tục.</p>
          <a-alert v-if="error" class="auth-alert" type="error" show-icon :message="error" />
          <a-form ref="loginFormRef" :model="loginForm" :rules="loginRules" layout="vertical" @finish="submitLogin">
            <a-form-item label="Tên đăng nhập" name="username"><a-input v-model:value="loginForm.username" autocomplete="username" placeholder="Nhập tên đăng nhập"><template #prefix><user-outlined /></template></a-input></a-form-item>
            <a-form-item label="Mật khẩu" name="password"><a-input-password v-model:value="loginForm.password" autocomplete="current-password" placeholder="Nhập mật khẩu"><template #prefix><lock-outlined /></template></a-input-password></a-form-item>
            <a-button block type="primary" html-type="submit" size="large" :loading="loading">Đăng nhập</a-button>
          </a-form>
          <p class="switch-mode">Chưa có tài khoản? <router-link to="/register">Đăng ký ngay</router-link></p>
        </template>
        <template v-else>
          <a-result v-if="registrationSucceeded" status="success" title="Đăng ký thành công" sub-title="Tài khoản của bạn đã được tạo. Hãy đăng nhập để tiếp tục.">
            <template #extra><a-button type="primary" @click="router.push({ name: 'Login' })">Đi tới đăng nhập</a-button></template>
          </a-result>
          <template v-else>
            <span class="eyebrow">TẠO TÀI KHOẢN</span><h2>Đăng ký</h2><p class="subtitle">Tài khoản đầu tiên sẽ được cấp quyền quản trị viên.</p>
            <a-alert
              v-if="registrationFailed"
              class="auth-alert"
              type="error"
              show-icon
              message="Đăng ký thất bại"
              :description="error || 'Không thể tạo tài khoản. Vui lòng thử lại.'"
            />
            <a-form ref="registerFormRef" :model="registerForm" :rules="registerRules" layout="vertical" @finish="submitRegister" @finishFailed="handleRegisterValidationFailed">
              <a-form-item label="Họ và tên" name="fullName"><a-input v-model:value="registerForm.fullName" placeholder="Nguyễn Văn A" /></a-form-item>
              <a-form-item label="Tên đăng nhập" name="username"><a-input v-model:value="registerForm.username" autocomplete="username" placeholder="vd: nguyenvana"><template #prefix><user-outlined /></template></a-input></a-form-item>
              <a-form-item label="Email" name="email"><a-input v-model:value="registerForm.email" autocomplete="email" placeholder="ten@gmail.com"><template #prefix><mail-outlined /></template></a-input></a-form-item>
              <a-form-item label="Mật khẩu" name="password"><a-input-password v-model:value="registerForm.password" autocomplete="new-password" placeholder="Ít nhất 8 ký tự, gồm chữ và số"><template #prefix><lock-outlined /></template></a-input-password></a-form-item>
              <a-form-item label="Xác nhận mật khẩu" name="confirmPassword"><a-input-password v-model:value="registerForm.confirmPassword" autocomplete="new-password" placeholder="Nhập lại mật khẩu"><template #prefix><lock-outlined /></template></a-input-password></a-form-item>
              <a-button block type="primary" html-type="submit" size="large" :loading="loading">Tạo tài khoản</a-button>
            </a-form>
            <p class="switch-mode">Đã có tài khoản? <router-link to="/login">Đăng nhập</router-link></p>
          </template>
        </template>
      </a-card>
    </section>
  </main>
</template>

<style scoped>
.auth-page { display: grid; min-height: 100vh; grid-template-columns: minmax(360px, 1fr) minmax(420px, 0.9fr); background: #f7f9fd; }
.auth-intro { display: flex; min-height: 100%; flex-direction: column; justify-content: space-between; padding: 52px clamp(32px, 8vw, 128px); color: #fff; background: radial-gradient(circle at 70% 12%, #397cd3 0, transparent 26%), linear-gradient(145deg, #071a35, #123d74); }
.auth-brand { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 750; }.auth-brand span { display: grid; width: 34px; height: 34px; place-items: center; border-radius: 10px; background: #4e9df7; font-size: 18px; }.eyebrow { color: #5e8bc4; font-size: 11px; font-weight: 750; letter-spacing: 1.1px; }.auth-intro .eyebrow { color: #9cc6f7; }h1, h2, p { margin: 0; }h1 { max-width: 560px; margin-top: 12px; font-size: clamp(34px, 4vw, 58px); line-height: 1.08; letter-spacing: -1.7px; }.auth-intro p { max-width: 480px; margin-top: 18px; color: #c0d5ee; font-size: 17px; line-height: 1.65; }.auth-caption { color: #8eadd1; font-size: 12px; letter-spacing: 0.4px; }.auth-panel { display: grid; padding: 32px; place-items: center; }.auth-card { width: min(100%, 430px); padding: 12px; border: 1px solid #e5ebf4; border-radius: 16px; box-shadow: 0 18px 52px rgba(25, 52, 92, 0.08); }.auth-card h2 { margin-top: 8px; color: #15233c; font-size: 28px; letter-spacing: -0.6px; }.subtitle { margin: 8px 0 24px; color: #6c7890; }.auth-alert { margin-bottom: 16px; }.auth-card :deep(.ant-btn-primary) { height: 42px; border-radius: 8px; font-weight: 650; }.switch-mode { margin-top: 20px; color: #6e7a91; text-align: center; }.switch-mode a { color: #236bc7; font-weight: 650; text-decoration: none; }@media (max-width: 800px) { .auth-page { grid-template-columns: 1fr; }.auth-intro { min-height: 280px; padding: 30px; }.auth-caption { display: none; }h1 { font-size: 34px; }.auth-panel { padding: 24px 16px; } }
</style>
