<script setup>
import { computed, onMounted } from 'vue'; import { useStore } from 'vuex';
const store=useStore(); const logs=computed(()=>store.getters.auditLogs); const loading=computed(()=>store.getters.isLoading); onMounted(()=>store.dispatch('fetchAuditLogs').catch(()=>{}));
const columns=[{title:'Thời gian',key:'time'},{title:'Người thực hiện',dataIndex:'actor'},{title:'Hành động',dataIndex:'action'},{title:'Đối tượng',key:'target'},{title:'Chi tiết',dataIndex:'detail'}];
</script>
<template><section><span class="eyebrow">KIỂM SOÁT</span><h1>Nhật ký thao tác</h1><a-card :bordered="false" class="table"><a-table :columns="columns" :data-source="logs" :loading="loading" row-key="id"><template #bodyCell="{column,record}"><template v-if="column.key==='time'">{{ new Date(record.createdAt).toLocaleString('vi-VN') }}</template><template v-else-if="column.key==='target'">{{ record.targetType }} #{{ record.targetId }}</template></template></a-table></a-card></section></template>
<style scoped>.eyebrow{color:#3f78c5;font-size:11px;font-weight:700;letter-spacing:1px}h1{margin:5px 0 22px;font-size:30px}.table{border:1px solid #e6ebf4;border-radius:14px}</style>
