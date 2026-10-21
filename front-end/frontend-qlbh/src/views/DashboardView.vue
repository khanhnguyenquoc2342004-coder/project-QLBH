<script setup>
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  CheckCircleOutlined,
  StopOutlined,
  WalletOutlined
} from '@ant-design/icons-vue';
import { computed, onMounted } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const packages = computed(() => store.getters.allPackages);
const error = computed(() => store.getters.error);
const summary = computed(() => store.getters.dashboardSummary);
const canManageUsers = computed(() => store.getters.canManageUsers);

const activePackages = computed(() => packages.value.filter((item) => item.status === 'ACTIVE').length);
const inactivePackages = computed(() => packages.value.filter((item) => item.status === 'INACTIVE').length);
const listedValue = computed(() => packages.value.reduce((sum, item) => sum + Number(item.price || 0), 0));

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(value);
}

onMounted(async () => {
  if (packages.value.length === 0) {
    try {
      await store.dispatch('fetchPackages');
    } catch {
    }
  }
  if (canManageUsers.value) {
    await store.dispatch('fetchDashboardSummary').catch(() => {});
  }
});
</script>

<template>
  <section class="dashboard-page">
    <div class="page-heading">
      <div>
        <span class="section-kicker">TỔNG QUAN</span>
        <h1>Xin chào!</h1>
        <p>Theo dõi nhanh dữ liệu gói dịch vụ trong hệ thống.</p>
      </div>
      <router-link class="manage-link" to="/packages">
        Quản lý gói dịch vụ <arrow-right-outlined />
      </router-link>
    </div>

    <a-alert
      v-if="error"
      class="dashboard-alert"
      type="warning"
      show-icon
      :message="error"
      description="Không thể tải số liệu mới. Hãy kiểm tra backend và kết nối MariaDB."
    />

    <a-row :gutter="[18, 18]" class="metric-grid">
      <a-col :xs="24" :sm="12" :xl="6">
        <a-card :bordered="false" class="metric-card">
          <a-avatar class="metric-icon icon-blue"><appstore-outlined /></a-avatar>
          <span>Tổng gói dịch vụ</span>
          <strong>{{ packages.length }}</strong>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :xl="6">
        <a-card :bordered="false" class="metric-card">
          <a-avatar class="metric-icon icon-green"><check-circle-outlined /></a-avatar>
          <span>Đang hoạt động</span>
          <strong>{{ activePackages }}</strong>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :xl="6">
        <a-card :bordered="false" class="metric-card">
          <a-avatar class="metric-icon icon-gray"><stop-outlined /></a-avatar>
          <span>Tổng đơn hàng</span>
          <strong>{{ summary?.totalOrders ?? 0 }}</strong>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :xl="6">
        <a-card :bordered="false" class="metric-card">
          <a-avatar class="metric-icon icon-purple"><wallet-outlined /></a-avatar>
          <span>Doanh thu hoàn tất</span>
          <strong class="currency-value">{{ formatCurrency(summary?.revenue ?? 0) }}</strong>
        </a-card>
      </a-col>
    </a-row>
    <a-card :bordered="false" class="getting-started-card">
      <span class="section-kicker">QUY TRÌNH</span>
      <h2>Sẵn sàng quản lý dữ liệu</h2>
      <p>
        Tạo gói dịch vụ, kiểm tra dữ liệu đầu vào và theo dõi mỗi request bằng mã trace ID từ Spring Boot.
      </p>
      <router-link to="/packages">
        Mở danh sách gói dịch vụ <arrow-right-outlined />
      </router-link>
      <div v-if="canManageUsers && summary?.topPackages?.length" class="top-packages"><strong>Gói được đặt nhiều</strong><span v-for="item in summary.topPackages" :key="item.name">{{ item.name }} · {{ item.quantity }} lượt</span></div>
    </a-card>
  </section>
</template>

<style scoped>
.page-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.section-kicker {
  color: #3f78c5;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
}

h1,
h2,
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

.manage-link,
.getting-started-card a {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #2268c7;
  font-weight: 650;
  text-decoration: none;
  white-space: nowrap;
}

.dashboard-alert {
  margin-bottom: 18px;
}

.metric-grid {
  margin-bottom: 18px;
}

.metric-card,
.getting-started-card {
  border: 1px solid #e7edf6;
  border-radius: 14px;
  box-shadow: none;
}

.metric-card :deep(.ant-card-body) {
  display: grid;
  grid-template-columns: 42px 1fr;
  column-gap: 13px;
  padding: 20px;
}

.metric-icon {
  grid-row: span 2;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 11px;
  font-size: 19px;
}

.icon-blue { color: #246fca; background: #eaf3ff; }
.icon-green { color: #158a58; background: #e4f8ee; }
.icon-gray { color: #657087; background: #edf0f5; }
.icon-purple { color: #7a51bd; background: #f0eafe; }

.metric-card span {
  align-self: end;
  color: #748097;
  font-size: 13px;
}

.metric-card strong {
  align-self: start;
  margin-top: 4px;
  color: #17243c;
  font-size: 24px;
  letter-spacing: -0.6px;
}

.metric-card .currency-value {
  font-size: 20px;
}

.getting-started-card :deep(.ant-card-body) {
  padding: 26px;
}

.getting-started-card h2 {
  margin-top: 7px;
  color: #16233c;
  font-size: 20px;
}

.getting-started-card p {
  max-width: 680px;
  margin: 8px 0 14px;
  color: #68758c;
}

.top-packages { display: grid; gap: 5px; margin-top: 20px; padding-top: 16px; color: #68758c; border-top: 1px solid #edf0f5; font-size: 13px; }
.top-packages strong { color: #253553; }

@media (max-width: 650px) {
  .page-heading {
    align-items: start;
    flex-direction: column;
  }
}
</style>
