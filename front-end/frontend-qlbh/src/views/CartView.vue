<script setup>
import { ArrowLeftOutlined, DeleteOutlined, ShoppingCartOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

const router = useRouter();
const store = useStore();
const cartItems = computed(() => store.getters.cartItems);
const cartTotal = computed(() => store.getters.cartTotal);
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);
const checkoutForm = reactive({ paymentMethod: 'BANK_TRANSFER', note: '' });
const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);
const saleTotal = (item) => Number(item.price) * (1 - Number(item.discountPercent || 0) / 100) * item.quantity;

async function updateQuantity(item, quantity) {
  try { await store.dispatch('updateCartItem', { packageId: item.packageId, quantity: Number(quantity) || 1 }); } catch { message.error(store.getters.error || 'Không thể cập nhật giỏ hàng'); }
}
async function removeItem(packageId) {
  try { await store.dispatch('removeCartItem', packageId); } catch { message.error(store.getters.error || 'Không thể xóa gói dịch vụ'); }
}
async function checkout() {
  try {
    await store.dispatch('createOrder', { items: cartItems.value.map((item) => ({ packageId: item.packageId, quantity: item.quantity })), paymentMethod: checkoutForm.paymentMethod, note: checkoutForm.note || null });
    message.success('Đặt hàng thành công. Đơn hàng đang chờ xác nhận.');
    router.push({ name: 'Orders' });
  } catch { message.error(store.getters.error || 'Không thể đặt hàng'); }
}
onMounted(() => store.dispatch('fetchCart').catch(() => {}));
</script>

<template>
  <main class="cart-page"><header><router-link to="/"><arrow-left-outlined /> Tiếp tục mua sắm</router-link><strong><shopping-cart-outlined /> Giỏ hàng</strong></header><section><span class="eyebrow">ĐƠN HÀNG CỦA BẠN</span><h1>Giỏ hàng</h1><p>Giỏ hàng được lưu theo tài khoản của bạn.</p><a-alert v-if="error" type="error" show-icon :message="error" class="error" /><a-empty v-if="!cartItems.length" description="Giỏ hàng đang trống"><a-button type="primary" @click="router.push('/')">Xem gói dịch vụ</a-button></a-empty><div v-else class="layout"><a-card :bordered="false" class="items"><a-list :data-source="cartItems"><template #renderItem="{ item }"><a-list-item><template #actions><a-button type="text" danger @click="removeItem(item.packageId)"><delete-outlined /></a-button></template><a-list-item-meta :title="item.name" :description="item.description || item.code" /><div class="controls"><a-input-number :value="item.quantity" :min="1" :max="100" @change="value => updateQuantity(item, value)" /><strong>{{ formatCurrency(saleTotal(item)) }}</strong></div></a-list-item></template></a-list></a-card><a-card :bordered="false" class="summary"><span class="eyebrow">THANH TOÁN</span><h2>Tóm tắt đơn hàng</h2><a-select v-model:value="checkoutForm.paymentMethod"><a-select-option value="BANK_TRANSFER">Chuyển khoản ngân hàng</a-select-option><a-select-option value="CASH">Thanh toán tiền mặt</a-select-option></a-select><a-textarea v-model:value="checkoutForm.note" :maxlength="500" :rows="2" placeholder="Ghi chú đơn hàng" /><div class="total"><span>Tổng thanh toán</span><strong>{{ formatCurrency(cartTotal) }}</strong></div><a-button block type="primary" size="large" :loading="loading" @click="checkout">Đặt hàng</a-button><p>Hãy cập nhật số điện thoại và địa chỉ tại Hồ sơ trước khi đặt hàng.</p></a-card></div></section></main>
</template>

<style scoped>
.cart-page { min-height: 100vh; background: #f7f9fd; }.cart-page > header, section { max-width: 1050px; margin: auto; }.cart-page > header { display: flex; justify-content: space-between; padding: 22px 24px; }.cart-page header a { color: #53627a; text-decoration: none; }section { padding: 35px 24px 70px; }.eyebrow { color: #477ab5; font-size: 11px; font-weight: 700; letter-spacing: 1px; }h1,h2,p { margin: 0; }h1 { margin-top: 6px; font-size: 34px; }section > p { margin-top: 8px; color: #6d7990; }.error { margin: 20px 0; }.layout { display: grid; grid-template-columns: minmax(0, 1fr) 310px; gap: 22px; margin-top: 25px; }.items,.summary { border: 1px solid #e5ebf4; border-radius: 14px; }.controls { display: flex; align-items: center; gap: 16px; }.controls strong { min-width: 120px; color: #23416a; text-align: right; }.summary :deep(.ant-select),.summary :deep(textarea) { width: 100%; margin: 12px 0; }.total { display: flex; justify-content: space-between; padding: 18px 0; margin: 8px 0 20px; border-top: 1px solid #e8edf4; }.total strong { color: #1e65b1; font-size: 20px; }.summary p { margin-top: 12px; color: #78869a; font-size: 12px; line-height: 1.5; }@media (max-width: 720px) { .layout { grid-template-columns: 1fr; }.controls { align-items: end; flex-direction: column; } }
</style>
