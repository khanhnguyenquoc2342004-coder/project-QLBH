import axios from 'axios';

const defaultApiOrigin = typeof window === 'undefined'
  ? 'http://localhost:8080'
  : `${window.location.protocol}//${window.location.hostname}:8080`;

const apiOrigin = import.meta.env.VITE_API_ORIGIN || defaultApiOrigin;

const api = axios.create({
  baseURL: `${apiOrigin}/api`,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
});

function getCookie(name) {
  if (typeof document === 'undefined') {
    return null;
  }

  const prefix = `${name}=`;
  const cookie = document.cookie.split('; ').find((item) => item.startsWith(prefix));
  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : null;
}

api.interceptors.request.use((config) => {
  const method = config.method?.toLowerCase();

  if (['post', 'put', 'patch', 'delete'].includes(method)) {
    const csrfToken = getCookie('XSRF-TOKEN');

    if (csrfToken) {
      config.headers['X-XSRF-TOKEN'] = csrfToken;
    }
  }

  return config;
});

export default api;
