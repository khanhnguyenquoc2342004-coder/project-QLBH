<script setup>
import {
  AppstoreOutlined,
  DashboardOutlined,
  FileTextOutlined,
  LogoutOutlined,
  ShopOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const router = useRouter();
const store = useStore();
const selectedKeys = computed(() => [route.name || 'Dashboard']);
const currentUser = computed(() => store.getters.currentUser);
const canManageUsers = computed(() => store.getters.canManageUsers);

async function logout() {
  await store.dispatch('logout');
  message.success('Đã đăng xuất');
  router.replace('/login');
}
</script>

<template>
  <a-layout class="app-shell">
    <a-layout-sider class="sidebar" :width="248" breakpoint="lg" collapsed-width="0">
      <router-link class="brand" to="/admin/dashboard">
        <span class="brand-mark"><shop-outlined /></span>
        <span><strong>QL Bán Hàng</strong><small>Service package manager</small></span>
      </router-link>

      <a-menu class="nav-menu" theme="dark" mode="inline" :selected-keys="selectedKeys">
        <a-menu-item key="Dashboard"><dashboard-outlined /><router-link to="/admin/dashboard">Tổng quan</router-link></a-menu-item>
        <a-menu-item key="Packages"><appstore-outlined /><router-link to="/admin/packages">Gói dịch vụ</router-link></a-menu-item>
        <a-menu-item v-if="canManageUsers" key="AdminOrders"><file-text-outlined /><router-link to="/admin/orders">Đơn hàng</router-link></a-menu-item>
        <a-menu-item v-if="canManageUsers" key="Users"><team-outlined /><router-link to="/admin/users">Người dùng</router-link></a-menu-item>
        <a-menu-item v-if="canManageUsers" key="AuditLogs"><file-text-outlined /><router-link to="/admin/audit-logs">Nhật ký</router-link></a-menu-item>
        <a-menu-item v-if="canManageUsers" key="Integrations"><file-text-outlined /><router-link to="/admin/integrations">Tích hợp</router-link></a-menu-item>
      </a-menu>

      <div class="sidebar-note"><span class="note-dot" />Phiên đăng nhập bảo mật</div>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="topbar">
        <div><span class="eyebrow">HỆ THỐNG QUẢN LÝ</span><div class="topbar-title">Quản lý bán hàng</div></div>
        <a-dropdown placement="bottomRight">
          <a class="account" @click.prevent>
            <a-avatar class="account-avatar"><user-outlined /></a-avatar>
            <span><strong>{{ currentUser?.fullName }}</strong><small>{{ currentUser?.role === 'ADMIN' ? 'Quản trị viên' : 'Nhân viên' }}</small></span>
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" danger @click="logout"><logout-outlined /> Đăng xuất</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <a-layout-content class="page-content"><router-view /></a-layout-content>
      <a-layout-footer class="app-footer">QLBH · Vue 3 &amp; Spring Boot</a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.app-shell { min-height: 100vh; }.sidebar { position: sticky; top: 0; height: 100vh; background: #0b1f3a !important; }.brand { display: flex; align-items: center; gap: 12px; min-height: 84px; padding: 18px 20px; color: #fff; text-decoration: none; }.brand strong, .brand small { display: block; }.brand strong { font-size: 16px; letter-spacing: -0.2px; }.brand small { margin-top: 2px; color: #93a9c7; font-size: 11px; }.brand-mark { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 10px; background: linear-gradient(135deg, #2f80ed, #70c1ff); font-size: 19px; }.nav-menu { padding: 8px 12px; background: transparent; border-inline-end: 0; }.nav-menu :deep(.ant-menu-item) { width: 100%; margin-block: 6px; border-radius: 8px; }.nav-menu :deep(a) { color: inherit; }.sidebar-note { position: absolute; right: 20px; bottom: 24px; left: 20px; display: flex; align-items: center; gap: 8px; color: #93a9c7; font-size: 12px; }.note-dot { width: 7px; height: 7px; border-radius: 50%; background: #56d69b; box-shadow: 0 0 0 4px rgba(86, 214, 155, 0.12); }.topbar { display: flex; align-items: center; justify-content: space-between; height: 76px; padding: 0 34px; background: #fff; border-bottom: 1px solid #e8edf5; line-height: normal; }.eyebrow { color: #75829a; font-size: 10px; font-weight: 700; letter-spacing: 1.1px; }.topbar-title { margin-top: 3px; color: #13213b; font-size: 18px; font-weight: 700; }.account { display: flex; align-items: center; gap: 9px; color: #23304a; text-decoration: none; }.account strong, .account small { display: block; }.account strong { font-size: 13px; }.account small { margin-top: 2px; color: #77849a; font-size: 11px; }.account-avatar { color: #fff; background: #296fc9; }.page-content { min-height: 0; padding: 32px 34px; }.app-footer { padding: 16px 34px 22px; color: #8b97aa; font-size: 12px; text-align: left; background: transparent; }@media (max-width: 768px) { .topbar, .page-content { padding-right: 18px; padding-left: 18px; }.page-content { padding-top: 24px; }.account small { display: none; } }
</style>
