import { createRouter, createWebHistory } from 'vue-router';
import store from '../store';
import AdminLayout from '../layouts/AdminLayout.vue';

const AuthView = () => import('../views/AuthView.vue');
const HomeView = () => import('../views/HomeView.vue');
const CartView = () => import('../views/CartView.vue');
const OrderView = () => import('../views/OrderView.vue');
const ProfileView = () => import('../views/ProfileView.vue');
const ActivationView = () => import('../views/ActivationView.vue');
const DashboardView = () => import('../views/DashboardView.vue');
const ProductView = () => import('../views/ProductView.vue');
const UserView = () => import('../views/UserView.vue');
const AdminOrderView = () => import('../views/AdminOrderView.vue');
const AuditLogView = () => import('../views/AuditLogView.vue');
const IntegrationMonitorView = () => import('../views/IntegrationMonitorView.vue');

const adminHome = () => ({ name: 'Dashboard' });

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: HomeView },
    { path: '/cart', name: 'Cart', component: CartView, meta: { requiresAuth: true } },
    { path: '/orders', name: 'Orders', component: OrderView, meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: ProfileView, meta: { requiresAuth: true } },
    { path: '/activations', name: 'Activations', component: ActivationView, meta: { requiresAuth: true } },
    { path: '/login', name: 'Login', component: AuthView, meta: { guestOnly: true } },
    { path: '/register', name: 'Register', component: AuthView, meta: { guestOnly: true } },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresBackOffice: true },
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', component: DashboardView },
        { path: 'packages', name: 'Packages', component: ProductView },
        { path: 'orders', name: 'AdminOrders', component: AdminOrderView, meta: { requiresAdmin: true } },
        { path: 'audit-logs', name: 'AuditLogs', component: AuditLogView, meta: { requiresAdmin: true } },
        { path: 'integrations', name: 'Integrations', component: IntegrationMonitorView, meta: { requiresAdmin: true } },
        { path: 'users', name: 'Users', component: UserView, meta: { requiresAdmin: true } }
      ]
    },
    { path: '/dashboard', redirect: adminHome },
    { path: '/packages', redirect: '/admin/packages' },
    { path: '/users', redirect: '/admin/users' },
    { path: '/products', redirect: '/admin/packages' },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
});

router.beforeEach(async (to) => {
  if (!store.getters.authInitialized) {
    await store.dispatch('initializeAuth');
  }

  if (to.meta.guestOnly && store.getters.isLoggedIn) {
    return store.getters.isCustomer ? { name: 'Home' } : adminHome();
  }
  if (to.meta.requiresAuth && !store.getters.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresBackOffice && store.getters.isCustomer) {
    return { name: 'Home' };
  }
  if (to.meta.requiresAdmin && !store.getters.canManageUsers) {
    return adminHome();
  }

  return true;
});

export default router;
