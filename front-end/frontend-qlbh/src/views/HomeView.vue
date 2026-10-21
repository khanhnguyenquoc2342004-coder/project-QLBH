<script setup>
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  LogoutOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

const router = useRouter();
const store = useStore();
const packages = computed(() => store.getters.allPackages);
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);
const isLoggedIn = computed(() => store.getters.isLoggedIn);
const isCustomer = computed(() => store.getters.isCustomer);
const currentUser = computed(() => store.getters.currentUser);
const cartCount = computed(() => store.getters.cartCount);

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(value || 0);
}
async function addToCart(servicePackage) {
  if (!isLoggedIn.value) {
    message.info('Hãy đăng nhập để thêm gói dịch vụ vào giỏ hàng');
    router.push({ name: 'Login', query: { redirect: '/' } });
    return;
  }
  try {
    await store.dispatch('addCartItem', { packageId: servicePackage.id, quantity: 1 });
    message.success(`Đã thêm ${servicePackage.name} vào giỏ hàng`);
  } catch {
    message.error(store.getters.error || 'Không thể thêm vào giỏ hàng');
  }
}

async function buyNow(servicePackage) {
  if (!isLoggedIn.value) {
    message.info('Hãy đăng nhập để đặt hàng');
    router.push({ name: 'Login', query: { redirect: '/' } });
    return;
  }

  try {
    await store.dispatch('addCartItem', { packageId: servicePackage.id, quantity: 1 });
    router.push({ name: 'Cart' });
  } catch {
    message.error(store.getters.error || 'Không thể thêm vào giỏ hàng');
  }
}
async function logout() {
  await store.dispatch('logout');
  message.success('Đã đăng xuất');
  router.push({ name: 'Home' });
}

onMounted(async () => {
  await store.dispatch('fetchPackages', { status: 'ACTIVE', size: 100 }).catch(() => {});
  if (isLoggedIn.value) await store.dispatch('fetchCart').catch(() => {});
});
</script>

<template>
  <main class="storefront">
    <header class="site-header">
      <router-link class="brand" to="/"><span class="brand-icon"><shopping-outlined /></span><strong>QL Bán Hàng</strong></router-link>
      <nav class="site-nav">
        <a href="#catalog">Gói dịch vụ</a>
        <router-link v-if="isLoggedIn" to="/orders">Đơn mua</router-link>
        <router-link v-if="isLoggedIn" to="/profile">Hồ sơ</router-link>
        <router-link v-if="isLoggedIn" class="cart-link" to="/cart"><a-badge :count="cartCount" :offset="[5, 0]"><shopping-cart-outlined /></a-badge> Giỏ hàng</router-link>
        <template v-if="!isLoggedIn"><router-link to="/login">Đăng nhập</router-link><router-link class="register-link" to="/register">Đăng ký</router-link></template>
        <template v-else><span class="welcome">Chào, {{ currentUser?.fullName }}</span><a v-if="!isCustomer" @click="router.push('/admin/dashboard')">Quản trị</a><a @click="logout"><logout-outlined /> Đăng xuất</a></template>
        <router-link v-if="isLoggedIn" to="/activations">Kích hoạt dịch vụ</router-link>
      </nav>
    </header>

    <section class="hero">
      <div>
        <span class="eyebrow">DỊCH VỤ DÀNH CHO BẠN</span>
        <h1>Chọn gói phù hợp, đặt hàng trong vài bước.</h1>
        <p>Khám phá các gói dịch vụ đang mở bán, thêm vào giỏ hàng và gửi đơn đặt hàng trực tuyến.</p>
        <a-button type="primary" size="large" href="#catalog">Xem gói dịch vụ <arrow-right-outlined /></a-button>
      </div>
      <div class="hero-card"><appstore-outlined /><span>Danh mục linh hoạt</span><strong>{{ packages.length }} gói đang mở bán</strong></div>
    </section>   
    <section id="catalog" class="catalog">
      <div class="section-heading"><div><span class="eyebrow">DANH MỤC</span><h2>Gói dịch vụ đang mở bán</h2></div><span>{{ packages.length }} gói</span></div>
      <a-alert v-if="error" type="error" show-icon :message="error" class="catalog-error" />
      <a-spin :spinning="loading">
        <div v-if="packages.length" class="package-grid">
          <article v-for="servicePackage in packages" :key="servicePackage.id" class="package-card">
            <span class="package-code">{{ servicePackage.code }}</span>
            <h3>{{ servicePackage.name }}</h3>
            <p>{{ servicePackage.description || 'Gói dịch vụ được cung cấp theo nhu cầu của bạn.' }}</p>
            <div v-if="servicePackage.discountPercent" class="price-wrap"><del>{{ formatCurrency(servicePackage.price) }}</del><strong class="discount-price">{{ formatCurrency(Number(servicePackage.price) * (1 - servicePackage.discountPercent / 100)) }}</strong><a-tag color="red">-{{ servicePackage.discountPercent }}%</a-tag></div>
            <strong v-else class="price">{{ formatCurrency(servicePackage.price) }}</strong>
            <div class="package-actions"><a-button @click="addToCart(servicePackage)"><shopping-cart-outlined /> Thêm giỏ</a-button><a-button type="primary" @click="buyNow(servicePackage)">Mua ngay</a-button></div>
          </article>
        </div>
        <a-empty v-else-if="!loading" description="Chưa có gói dịch vụ đang mở bán" />
      </a-spin>
    </section>

    <footer>QL Bán Hàng · Đặt dịch vụ trực tuyến an toàn</footer>
  </main>
</template>
khanhchec
<style scoped>
.storefront { min-height: 100vh; color: #17233b; background: #f7f9fd; }.site-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; max-width: 1180px; min-height: 72px; padding: 0 24px; margin: auto; }.brand { display: flex; align-items: center; gap: 10px; color: #14233d; font-size: 17px; text-decoration: none; }.brand-icon { display: grid; width: 36px; height: 36px; place-items: center; color: #fff; border-radius: 10px; background: #246cc4; }.site-nav { display: flex; align-items: center; flex-wrap: wrap; justify-content: end; gap: 18px; font-size: 14px; }.site-nav a { color: #43516a; text-decoration: none; cursor: pointer; }.site-nav a:hover { color: #246cc4; }.register-link { padding: 8px 13px; color: #fff !important; border-radius: 8px; background: #246cc4; }.cart-link { display: flex; align-items: center; gap: 5px; }.welcome { color: #65728a; font-size: 13px; }.hero { display: grid; grid-template-columns: minmax(0, 1fr) 270px; gap: 50px; align-items: center; max-width: 1180px; padding: 88px 48px; margin: 0 auto 44px; color: #fff; border-radius: 28px; background: radial-gradient(circle at 85% 20%, #397ed8 0, transparent 28%), linear-gradient(135deg, #071c39, #164e90); }.eyebrow { color: #5d92cf; font-size: 11px; font-weight: 750; letter-spacing: 1.1px; }.hero .eyebrow { color: #a5cdfb; }.hero h1 { max-width: 700px; margin: 10px 0 18px; font-size: clamp(36px, 5vw, 58px); line-height: 1.08; letter-spacing: -1.8px; }.hero p { max-width: 600px; margin: 0 0 28px; color: #c7d9f0; font-size: 17px; line-height: 1.65; }.hero-card { display: flex; flex-direction: column; gap: 8px; padding: 28px; border: 1px solid rgba(197, 222, 255, .24); border-radius: 18px; background: rgba(255,255,255,.09); }.hero-card :deep(svg) { font-size: 34px; }.hero-card span { color: #c4daf5; font-size: 13px; }.hero-card strong { font-size: 19px; }.catalog { max-width: 1180px; padding: 8px 24px 76px; margin: auto; }.section-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; margin-bottom: 22px; }.section-heading h2 { margin: 5px 0 0; font-size: 28px; letter-spacing: -.7px; }.section-heading > span { color: #65728a; font-size: 13px; }.catalog-error { margin-bottom: 18px; }.package-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }.package-card { display: flex; flex-direction: column; min-height: 270px; padding: 24px; border: 1px solid #e4ebf5; border-radius: 16px; background: #fff; box-shadow: 0 10px 28px rgba(24, 51, 91, .05); }.package-code { color: #3979bf; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 11px; font-weight: 700; }.package-card h3 { margin: 14px 0 10px; font-size: 19px; }.package-card p { flex: 1; margin: 0; color: #67758d; line-height: 1.55; }.price { margin: 20px 0 15px; color: #173660; font-size: 21px; }.price-wrap { display: flex; align-items: center; flex-wrap: wrap; gap: 7px; margin: 20px 0 15px; }.price-wrap del { color: #8995a8; font-size: 13px; }.price-wrap :deep(.ant-tag) { margin: 0; }.discount-price { color: #cf3d3d; font-size: 21px; }.package-actions { display: flex; gap: 8px; }.package-actions :deep(.ant-btn) { flex: 1; }.package-actions :deep(.ant-btn-primary) { background: #246cc4; }footer { padding: 25px; color: #77849a; font-size: 12px; text-align: center; border-top: 1px solid #e7ecf4; }@media (max-width: 820px) { .hero { grid-template-columns: 1fr; padding: 48px 30px; margin: 0 16px 40px; }.hero-card { display: none; }.package-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }@media (max-width: 560px) { .site-header { align-items: start; flex-direction: column; padding-block: 16px; }.site-nav { justify-content: start; gap: 12px; }.hero { padding: 40px 25px; }.package-grid { grid-template-columns: 1fr; }.package-actions { flex-direction: column; } }
</style>
