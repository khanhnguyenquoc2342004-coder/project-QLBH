<script setup>
import { ReloadOutlined } from '@ant-design/icons-vue';
import { computed, onMounted } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const registrations = computed(() => store.getters.adminRegistrations);
const logs = computed(() => store.getters.integrationLogs);
const loading = computed(() => store.getters.isLoading);

function statusColor(status) {
  return ({ ACTIVATED: 'green', FAILED: 'red', PENDING: 'gold', SENT: 'blue', TIMEOUT: 'orange' })[status] || 'default';
}

function formatDate(value) {
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'medium' }).format(new Date(value));
}

async function load() {
  await Promise.all([store.dispatch('fetchAdminRegistrations'), store.dispatch('fetchIntegrationLogs')]).catch(() => {});
}

onMounted(load);
</script>

<template>
  <section class="monitor-page">
    <div class="page-heading"><div><span class="kicker">TÍCH HỢP</span><h1>Theo dõi kích hoạt dịch vụ</h1><p>Đối chiếu trạng thái, request ID, trace ID và phản hồi từ MOCK-EWS.</p></div><a-button :loading="loading" @click="load"><reload-outlined /> Làm mới</a-button></div>
    <a-card class="table-card" :bordered="false" title="Đăng ký/kích hoạt gần đây">
      <a-table row-key="id" :data-source="registrations" :loading="loading" :pagination="{ pageSize: 8 }" :scroll="{ x: 920 }">
        <a-table-column title="Mã" data-index="registrationCode" width="150" />
        <a-table-column title="Gói dịch vụ" key="package"><template #default="{ record }"><strong>{{ record.packageName }}</strong><small>{{ record.packageCode }}</small></template></a-table-column>
        <a-table-column title="Trạng thái" key="status" align="center"><template #default="{ record }"><a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag></template></a-table-column>
        <a-table-column title="Kết quả" key="message"><template #default="{ record }"><span>{{ record.message }}</span><small v-if="record.errorCode">{{ record.errorCode }}</small></template></a-table-column>
        <a-table-column title="Request ID" data-index="requestId" width="210" />
        <a-table-column title="Trace ID" data-index="traceId" width="210" />
        <a-table-column title="Thời gian" key="createdAt" width="160"><template #default="{ record }">{{ formatDate(record.createdAt) }}</template></a-table-column>
      </a-table>
    </a-card>
    <a-card class="table-card" :bordered="false" title="Integration log (100 bản ghi gần nhất)">
      <a-table row-key="id" :data-source="logs" :loading="loading" :pagination="{ pageSize: 8 }" :scroll="{ x: 950 }">
        <a-table-column title="Thời gian" key="createdAt" width="160"><template #default="{ record }">{{ formatDate(record.createdAt) }}</template></a-table-column>
        <a-table-column title="Sự kiện" data-index="eventType" width="160" />
        <a-table-column title="Kết quả" key="outcome" width="130"><template #default="{ record }"><a-tag :color="statusColor(record.outcome)">{{ record.outcome }}</a-tag></template></a-table-column>
        <a-table-column title="Mã đăng ký" data-index="registrationCode" width="150" />
        <a-table-column title="Request ID" data-index="requestId" width="210" />
        <a-table-column title="Trace ID" data-index="traceId" width="210" />
        <a-table-column title="Chi tiết" data-index="detail" />
      </a-table>
    </a-card>
  </section>
</template>

<style scoped>
.page-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; margin-bottom: 24px; }.kicker { color: #3979bf; font-size: 11px; font-weight: 750; letter-spacing: 1px; }h1,p { margin: 0; }h1 { margin-top: 5px; color: #14213a; font-size: 30px; letter-spacing: -.7px; }.page-heading p { margin-top: 8px; color: #6c7890; }.table-card { margin-bottom: 20px; overflow: hidden; border: 1px solid #e7edf6; border-radius: 14px; }.table-card :deep(.ant-card-head) { border-bottom: 1px solid #edf0f5; }.table-card :deep(.ant-table-cell small) { display: block; margin-top: 3px; color: #7a879b; font-size: 11px; }@media (max-width: 650px) { .page-heading { align-items: start; flex-direction: column; } }
</style>
