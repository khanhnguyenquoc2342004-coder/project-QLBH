<script setup>
import { ArrowLeftOutlined, ReloadOutlined, SendOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const purchasedServices = computed(() => store.getters.purchasedServices);
const registrations = computed(() => store.getters.registrations);
const loading = computed(() => store.getters.isLoading);
const error = computed(() => store.getters.error);
const latestResult = ref(null);

const form = reactive({
  orderItemId: undefined,
  simulation: 'SUCCESS'
});
const requestId = ref(createRequestId());

function createRequestId() {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID();
  }
  return `activation-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function statusColor(status) {
  return ({ ACTIVATED: 'green', FAILED: 'red', PENDING: 'gold' })[status] || 'default';
}

function statusLabel(status) {
  return ({ ACTIVATED: 'Đã kích hoạt', FAILED: 'Thất bại', PENDING: 'Đang chờ' })[status] || status;
}

function formatDate(value) {
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'medium' }).format(new Date(value));
}

async function submit(useExistingRequestId = true) {
  if (!form.orderItemId) {
    message.warning('Hãy chọn gói dịch vụ đã mua cần kích hoạt');
    return;
  }
  if (!useExistingRequestId) {
    requestId.value = createRequestId();
  }

  try {
    const result = await store.dispatch('submitServiceRegistration', {
      orderItemId: form.orderItemId,
      requestId: requestId.value,
      simulation: form.simulation
    });
    latestResult.value = result;
    await store.dispatch('fetchMyRegistrations');

    if (result.status === 'ACTIVATED') {
      message.success(result.replayed ? 'Đã trả lại kết quả của yêu cầu trước' : 'Kích hoạt dịch vụ thành công');
    } else {
      message.error(result.message || 'Kích hoạt dịch vụ thất bại');
    }
  } catch {
    message.error(store.getters.error || 'Không thể gửi yêu cầu kích hoạt');
  }
}

function resendSameRequest() {
  if (latestResult.value) {
    form.orderItemId = latestResult.value.orderItemId;
    submit(true);
  }
}

function retryWithNewRequest(row) {
  form.orderItemId = row.orderItemId;
  form.simulation = 'SUCCESS';
  requestId.value = createRequestId();
  submit(true);
}

onMounted(async () => {
  await Promise.all([
    store.dispatch('fetchPurchasedServices'),
    store.dispatch('fetchMyRegistrations')
  ]).catch(() => {});
});
</script>

<template>
  <main class="activation-page">
    <header><router-link to="/"><arrow-left-outlined /> Về trang chủ</router-link><router-link to="/orders">Đơn mua</router-link></header>
    <section>
      <span class="eyebrow">TÍCH HỢP DỊCH VỤ</span>
      <h1>Đăng ký và kích hoạt dịch vụ</h1>
      <p>Chỉ các gói thuộc đơn hàng đã hoàn tất mới có thể kích hoạt. Mỗi mã yêu cầu chỉ được xử lý một lần.</p>

      <a-alert v-if="error" class="error-alert" type="error" show-icon :message="error" />
      <a-alert v-if="!loading && !purchasedServices.length" class="result-alert" type="info" show-icon message="Bạn chưa có gói nào đủ điều kiện kích hoạt" description="Hãy hoàn tất đơn hàng trước; sau đó gói đã mua sẽ xuất hiện tại đây." />

      <a-card class="form-card" :bordered="false">
        <a-form layout="vertical">
          <a-form-item label="Gói dịch vụ">
            <a-select v-model:value="form.orderItemId" placeholder="Chọn gói thuộc đơn đã hoàn tất" :loading="loading">
              <a-select-option v-for="item in purchasedServices" :key="item.orderItemId" :value="item.orderItemId" :disabled="!item.canActivate">
                {{ item.orderCode }} · {{ item.packageCode }} · {{ item.packageName }}{{ item.latestActivationStatus ? ` (${item.latestActivationStatus})` : '' }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="Kịch bản demo đối tác ngoài">
            <a-radio-group v-model:value="form.simulation">
              <a-radio value="SUCCESS">Thành công</a-radio>
              <a-radio value="BUSINESS_ERROR">Lỗi nghiệp vụ</a-radio>
              <a-radio value="TIMEOUT">Timeout</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="Request ID">
            <a-input v-model:value="requestId" readonly />
          </a-form-item>
          <a-space wrap>
            <a-button type="primary" :loading="loading" @click="submit(true)"><send-outlined /> Gửi kích hoạt</a-button>
            <a-button :disabled="loading" @click="requestId = createRequestId()"><reload-outlined /> Tạo request ID mới</a-button>
          </a-space>
        </a-form>
      </a-card>

      <a-alert v-if="latestResult" class="result-alert" :type="latestResult.status === 'ACTIVATED' ? 'success' : 'warning'" show-icon>
        <template #message>
          {{ latestResult.status === 'ACTIVATED' ? 'Kích hoạt thành công' : 'Kích hoạt chưa thành công' }} · {{ latestResult.registrationCode }}
        </template>
        <template #description>
          <div>{{ latestResult.message }}</div>
          <small>Trace ID: {{ latestResult.traceId || '—' }} · Request ID: {{ latestResult.requestId }}</small>
          <a-space class="result-actions">
            <a-button size="small" @click="resendSameRequest">Gửi lại cùng request ID</a-button>
            <a-button v-if="latestResult.retryable" size="small" type="primary" @click="retryWithNewRequest(latestResult)">Thử lại với request ID mới</a-button>
          </a-space>
        </template>
      </a-alert>

      <div class="history-heading"><div><span class="eyebrow">LỊCH SỬ</span><h2>Yêu cầu kích hoạt của tôi</h2></div><a-button @click="store.dispatch('fetchMyRegistrations')">Làm mới</a-button></div>
      <a-empty v-if="!registrations.length && !loading" description="Chưa có yêu cầu kích hoạt" />
      <a-table v-else row-key="id" :data-source="registrations" :pagination="{ pageSize: 6, showSizeChanger: false }" :scroll="{ x: 760 }">
        <a-table-column title="Mã đăng ký" data-index="registrationCode" />
        <a-table-column title="Gói dịch vụ" key="package">
          <template #default="{ record }"><strong>{{ record.packageName }}</strong><small>{{ record.packageCode }}</small></template>
        </a-table-column>
        <a-table-column title="Trạng thái" key="status" align="center">
          <template #default="{ record }"><a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag></template>
        </a-table-column>
        <a-table-column title="Kết quả" key="result">
          <template #default="{ record }"><span>{{ record.message }}</span><small v-if="record.errorCode">{{ record.errorCode }}</small></template>
        </a-table-column>
        <a-table-column title="Thời gian" key="createdAt">
          <template #default="{ record }">{{ formatDate(record.createdAt) }}</template>
        </a-table-column>
        <a-table-column title="" key="action" align="right">
          <template #default="{ record }"><a-button v-if="record.retryable" size="small" @click="retryWithNewRequest(record)">Thử lại</a-button></template>
        </a-table-column>
      </a-table>
    </section>
  </main>
</template>

<style scoped>
.activation-page { min-height: 100vh; background: #f7f9fd; color: #17233b; }.activation-page > header, .activation-page > section { max-width: 980px; margin: auto; }.activation-page > header { display: flex; justify-content: space-between; padding: 22px 24px; }.activation-page header a { color: #52617a; text-decoration: none; }.activation-page > section { padding: 30px 24px 70px; }.eyebrow { color: #3979bf; font-size: 11px; font-weight: 750; letter-spacing: 1px; }h1,h2,p { margin: 0; }h1 { margin-top: 6px; font-size: 34px; letter-spacing: -.8px; }.activation-page > section > p { margin-top: 8px; color: #6c7890; }.form-card { max-width: 680px; margin-top: 25px; border: 1px solid #e4ebf4; border-radius: 14px; }.form-card :deep(.ant-form-item) { margin-bottom: 18px; }.error-alert,.result-alert { margin-top: 20px; }.result-alert small { display: block; margin-top: 7px; color: #647189; }.result-actions { margin-top: 12px; }.history-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; margin: 42px 0 18px; }.history-heading h2 { margin-top: 5px; font-size: 23px; }.history-heading + :deep(.ant-empty) { padding: 36px; border: 1px dashed #dbe4f1; border-radius: 14px; background: #fff; }:deep(.ant-table-wrapper) { overflow: hidden; border: 1px solid #e4ebf4; border-radius: 14px; background: #fff; }:deep(.ant-table-cell small) { display: block; margin-top: 3px; color: #7a879b; font-size: 11px; }@media (max-width: 620px) { .activation-page > header { padding-inline: 18px; }.activation-page > section { padding-inline: 18px; }.history-heading { align-items: start; flex-direction: column; }.form-card :deep(.ant-radio-wrapper) { display: block; margin: 8px 0; } }
</style>
