export default {
  SET_AUTH(state, user) {
    state.isLoggedIn = true;
    state.authInitialized = true;
    state.currentUser = user;
    state.userRole = user.role;
  },

  CLEAR_AUTH(state) {
    state.isLoggedIn = false;
    state.authInitialized = true;
    state.currentUser = null;
    state.userRole = '';
    state.packages = [];
    state.packagePage = { number: 0, size: 8, totalElements: 0, totalPages: 0 };
    state.users = [];
    state.cartItems = [];
    state.orders = [];
    state.adminOrders = [];
    state.dashboardSummary = null;
    state.auditLogs = [];
    state.registrations = [];
    state.purchasedServices = [];
    state.adminRegistrations = [];
    state.integrationLogs = [];
  },

  SET_AUTH_INITIALIZED(state, status) {
    state.authInitialized = status;
  },

  SET_PACKAGES(state, pageData) {
    if (Array.isArray(pageData)) {
      state.packages = pageData;
      state.packagePage = { number: 0, size: pageData.length, totalElements: pageData.length, totalPages: 1 };
      return;
    }
    state.packages = pageData.content || [];
    state.packagePage = {
      number: pageData.number || 0,
      size: pageData.size || 8,
      totalElements: pageData.totalElements || 0,
      totalPages: pageData.totalPages || 0
    };
  },

  SET_USERS(state, users) {
    state.users = users;
  },

  SET_ORDERS(state, orders) {
    state.orders = orders;
  },

  SET_ADMIN_ORDERS(state, orders) {
    state.adminOrders = orders;
  },

  SET_DASHBOARD_SUMMARY(state, summary) {
    state.dashboardSummary = summary;
  },

  SET_AUDIT_LOGS(state, logs) {
    state.auditLogs = logs;
  },

  SET_REGISTRATIONS(state, registrations) {
    state.registrations = registrations;
  },

  SET_PURCHASED_SERVICES(state, services) {
    state.purchasedServices = services;
  },

  SET_ADMIN_REGISTRATIONS(state, registrations) {
    state.adminRegistrations = registrations;
  },

  SET_INTEGRATION_LOGS(state, logs) {
    state.integrationLogs = logs;
  },

  SET_CART(state, items) {
    state.cartItems = items;
  },

  ADD_ORDER(state, order) {
    state.orders.unshift(order);
  },

  ADD_TO_CART(state, servicePackage) {
    const item = state.cartItems.find((cartItem) => cartItem.id === servicePackage.id);
    if (item) {
      item.quantity += 1;
      return;
    }
    state.cartItems.push({ ...servicePackage, quantity: 1 });
  },

  UPDATE_CART_QUANTITY(state, { id, quantity }) {
    const item = state.cartItems.find((cartItem) => cartItem.packageId === id);
    if (item) {
      item.quantity = Math.max(1, Math.min(100, Number(quantity) || 1));
    }
  },

  REMOVE_FROM_CART(state, id) {
    state.cartItems = state.cartItems.filter((cartItem) => cartItem.packageId !== id);
  },

  CLEAR_CART(state) {
    state.cartItems = [];
  },

  SET_LOADING(state, status) {
    state.loading = status;
  },

  SET_ERROR(state, error) {
    state.error = error;
  },

  ADD_PACKAGE(state, servicePackage) {
    state.packages.unshift(servicePackage);
  },

  UPDATE_PACKAGE(state, updatedPackage) {
    const index = state.packages.findIndex((servicePackage) => servicePackage.id === updatedPackage.id);
    if (index !== -1) {
      state.packages.splice(index, 1, updatedPackage);
    }
  },

  DELETE_PACKAGE(state, id) {
    state.packages = state.packages.filter((servicePackage) => servicePackage.id !== id);
  },

  ADD_USER(state, user) {
    state.users.push(user);
  },

  UPDATE_USER(state, updatedUser) {
    const index = state.users.findIndex((user) => user.id === updatedUser.id);
    if (index !== -1) {
      state.users.splice(index, 1, updatedUser);
    }

    if (state.currentUser?.id === updatedUser.id) {
      state.currentUser = {
        ...state.currentUser,
        fullName: updatedUser.fullName,
        role: updatedUser.role
      };
      state.userRole = updatedUser.role;
    }
  }
};
