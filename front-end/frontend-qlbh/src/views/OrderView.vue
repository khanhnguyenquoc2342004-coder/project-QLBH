<script setup>
import { ArrowLeftOutlined, FileTextOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const orders = computed(() => store.getters.myOrders);
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);
}

function formatDate(value) {
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

function statusLabel(status) {
  return ({ PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn tất', CANCELLED: 'Đã hủy', REFUND_REQUESTED: 'Đang yêu cầu hoàn tiền', REFUNDED: 'Đã hoàn tiền' })[status] || status;
}

async function changeOrder(order, action) {
  try {
    await store.dispatch(action === 'cancel' ? 'cancelMyOrder' : 'requestOrderRefund', order.id);
    await store.dispatch('fetchMyOrders');
    message.success(action === 'cancel' ? 'Đã hủy đơn hàng' : 'Đã gửi yêu cầu hoàn tiền');
  } catch { message.error(store.getters.error || 'Không thể cập nhật đơn hàng'); }
}

onMounted(() => store.dispatch('fetchMyOrders').catch(() => {}));
</script>

<template>
  <main class="orders-page">
    <header><router-link to="/"><arrow-left-outlined /> Về trang chủ</router-link><router-link class="cart" to="/cart">Giỏ hàng</router-link></header>
    <section><span class="eyebrow">LỊCH SỬ MUA HÀNG</span><h1>Đơn mua của tôi</h1><p>Theo dõi những gói dịch vụ bạn đã đặt.</p><a-alert v-if="error" type="error" show-icon :message="error" class="error-alert" /><a-spin :spinning="loading"><a-empty v-if="!orders.length && !loading" description="Bạn chưa có đơn hàng nào" /><div v-else class="order-list"><a-card v-for="order in orders" :key="order.id" :bordered="false" class="order-card"><div class="order-head"><div><strong><file-text-outlined /> {{ order.orderCode || `Đơn #${order.id}` }}</strong><span>{{ formatDate(order.createdAt) }} · {{ order.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Chuyển khoản' }}</span></div><a-tag :color="order.status === 'CANCELLED' || order.status === 'REFUNDED' ? 'red' : order.status === 'COMPLETED' ? 'green' : 'gold'">{{ statusLabel(order.status) }}</a-tag></div><div v-for="item in order.items" :key="`${order.id}-${item.packageId}`" class="order-item"><span><strong>{{ item.packageName }}</strong><small>{{ item.packageCode }} · SL: {{ item.quantity }}</small></span><strong>{{ formatCurrency(item.lineTotal || Number(item.unitPrice) * item.quantity) }}</strong></div><div class="order-total"><span>Tổng thanh toán</span><strong>{{ formatCurrency(order.totalAmount) }}</strong></div><a-space><a-button v-if="order.status === 'PENDING'" size="small" danger @click="changeOrder(order, 'cancel')">Hủy đơn</a-button><a-button v-if="order.status === 'COMPLETED'" size="small" @click="changeOrder(order, 'refund')">Yêu cầu hoàn tiền</a-button></a-space></a-card></div></a-spin></section>
  </main>
</template>

<style scoped>
.orders-page { min-height: 100vh; background: #f7f9fd; }.orders-page > header, .orders-page > section { max-width: 860px; margin: auto; }.orders-page > header { display: flex; justify-content: space-between; padding: 22px 24px; }.orders-page header a { color: #52617a; text-decoration: none; }.orders-page > section { padding: 36px 24px 70px; }.eyebrow { color: #4b7db9; font-size: 11px; font-weight: 750; letter-spacing: 1px; }h1, p { margin: 0; }h1 { margin-top: 6px; font-size: 34px; letter-spacing: -.8px; }.orders-page > section > p { margin-top: 8px; color: #6c7890; }.error-alert { margin: 22px 0; }.order-list { display: grid; gap: 16px; margin-top: 26px; }.order-card { border: 1px solid #e5ebf4; border-radius: 14px; }.order-head, .order-item, .order-total { display: flex; align-items: center; justify-content: space-between; gap: 18px; }.order-head { padding-bottom: 16px; border-bottom: 1px solid #edf0f5; }.order-head strong, .order-head span, .order-item strong, .order-item small { display: block; }.order-head strong { color: #1e3556; }.order-head span, .order-item small { margin-top: 4px; color: #7a879b; font-size: 12px; }.order-item { padding: 15px 0; border-bottom: 1px solid #f0f2f6; }.order-item > strong { color: #253b5d; }.order-total { padding-top: 17px; color: #647189; }.order-total strong { color: #2067b7; font-size: 19px; }@media (max-width: 520px) { .order-head, .order-item, .order-total { align-items: start; flex-direction: column; gap: 6px; } }
</style>
