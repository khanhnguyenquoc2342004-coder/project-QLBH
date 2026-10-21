import api from '../api';

function getErrorMessage(error) {
  return error.response?.data?.message || error.message || 'Có lỗi xảy ra.';
}

async function ensureCsrfCookie() {
  await api.get('/auth/csrf');
}

export default {
  async initializeAuth({ commit, state }) {
    if (state.authInitialized) {
      return state.currentUser;
    }

    try {
      await ensureCsrfCookie();
      const response = await api.get('/auth/session');

      if (response.status === 204) {
        commit('CLEAR_AUTH');
        return null;
      }

      commit('SET_AUTH', response.data);
      return response.data;
    } catch {
      commit('CLEAR_AUTH');
      return null;
    } finally {
      commit('SET_AUTH_INITIALIZED', true);
    }
  },

  async register({ commit }, registration) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      await ensureCsrfCookie();
      const response = await api.post('/auth/register', registration);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async login({ commit }, credentials) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      await ensureCsrfCookie();
      const response = await api.post('/auth/login', credentials);
      commit('SET_AUTH', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async logout({ commit }) {
    try {
      await api.post('/auth/logout');
    } finally {
      commit('CLEAR_AUTH');
    }
  },

  async updateProfile({ commit }, profile) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.put('/auth/profile', profile);
      commit('SET_AUTH', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async fetchPackages({ commit }, params = {}) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.get('/packages', { params });
      commit('SET_PACKAGES', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async createPackage({ commit }, packageData) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.post('/packages', packageData);
      commit('ADD_PACKAGE', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async updatePackage({ commit }, { id, data }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.put(`/packages/${id}`, data);
      commit('UPDATE_PACKAGE', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async deletePackage({ commit }, id) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      await api.delete(`/packages/${id}`);
      commit('DELETE_PACKAGE', id);
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async fetchPackageDetail({ commit }, id) {
    commit('SET_ERROR', null);
    try {
      const response = await api.get(`/packages/${id}`);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    }
  },

  async submitServiceRegistration({ commit }, registration) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      await ensureCsrfCookie();
      const response = await api.post('/registrations', registration);
      return response.data;
    } catch (error) {
      const workflowResult = error.response?.data;
      if (workflowResult?.status === 'FAILED') {
        return workflowResult;
      }
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async fetchMyRegistrations({ commit }) {
    const response = await api.get('/registrations/my');
    commit('SET_REGISTRATIONS', response.data);
    return response.data;
  },

  async fetchPurchasedServices({ commit }) {
    const response = await api.get('/registrations/purchased-services');
    commit('SET_PURCHASED_SERVICES', response.data);
    return response.data;
  },

  async fetchAdminRegistrations({ commit }) {
    const response = await api.get('/admin/registrations');
    commit('SET_ADMIN_REGISTRATIONS', response.data);
    return response.data;
  },

  async fetchIntegrationLogs({ commit }) {
    const response = await api.get('/integration-logs');
    commit('SET_INTEGRATION_LOGS', response.data);
    return response.data;
  },

  async fetchUsers({ commit }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.get('/users');
      commit('SET_USERS', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async createUser({ commit }, userData) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.post('/users', userData);
      commit('ADD_USER', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async updateUser({ commit }, { id, data }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.put(`/users/${id}`, data);
      commit('UPDATE_USER', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async createOrder({ commit }, orderData) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      await ensureCsrfCookie();
      const response = await api.post('/orders', orderData);
      commit('ADD_ORDER', response.data);
      commit('CLEAR_CART');
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async fetchCart({ commit }) {
    const response = await api.get('/cart');
    commit('SET_CART', response.data);
    return response.data;
  },

  async addCartItem({ commit }, item) {
    await ensureCsrfCookie();
    const response = await api.post('/cart', item);
    commit('SET_CART', response.data);
    return response.data;
  },

  async updateCartItem({ commit }, item) {
    await ensureCsrfCookie();
    const response = await api.put(`/cart/${item.packageId}`, item);
    commit('SET_CART', response.data);
    return response.data;
  },

  async removeCartItem({ commit }, packageId) {
    await ensureCsrfCookie();
    await api.delete(`/cart/${packageId}`);
    commit('REMOVE_FROM_CART', packageId);
  },

  async fetchMyOrders({ commit }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await api.get('/orders/my');
      commit('SET_ORDERS', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async cancelMyOrder({ commit }, id) {
    commit('SET_LOADING', true);
    try {
      await ensureCsrfCookie();
      const response = await api.post(`/orders/my/${id}/cancel`);
      return response.data;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async requestOrderRefund({ commit }, id) {
    commit('SET_LOADING', true);
    try {
      await ensureCsrfCookie();
      const response = await api.post(`/orders/my/${id}/refund-request`);
      return response.data;
    } finally { commit('SET_LOADING', false); }
  },

  async fetchAdminOrders({ commit }, params = {}) {
    commit('SET_LOADING', true);
    try {
      const response = await api.get('/admin/orders', { params });
      commit('SET_ADMIN_ORDERS', response.data);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally { commit('SET_LOADING', false); }
  },

  async updateAdminOrder({ commit }, { id, data }) {
    commit('SET_LOADING', true);
    try {
      await ensureCsrfCookie();
      const response = await api.put(`/admin/orders/${id}`, data);
      const orders = (await api.get('/admin/orders')).data;
      commit('SET_ADMIN_ORDERS', orders);
      return response.data;
    } catch (error) {
      commit('SET_ERROR', getErrorMessage(error));
      throw error;
    } finally { commit('SET_LOADING', false); }
  },

  async fetchDashboardSummary({ commit }) {
    const response = await api.get('/admin/dashboard');
    commit('SET_DASHBOARD_SUMMARY', response.data);
    return response.data;
  },

  async fetchAuditLogs({ commit }) {
    const response = await api.get('/audit-logs');
    commit('SET_AUDIT_LOGS', response.data);
    return response.data;
  }
};
