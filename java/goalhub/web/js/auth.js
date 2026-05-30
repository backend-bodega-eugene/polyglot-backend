(function () {
  const API_BASE = localStorage.getItem('API_BASE') || 'http://localhost:8000';
  const LOGIN_PAGE = 'login.html';

  function getToken() {
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  }

  function getAdminUser() {
    try {
      return JSON.parse(localStorage.getItem('adminUser') || sessionStorage.getItem('adminUser') || 'null');
    } catch (e) {
      return null;
    }
  }

  function clearLogin() {
    localStorage.removeItem('authToken');
    sessionStorage.removeItem('authToken');
    localStorage.removeItem('adminUser');
    sessionStorage.removeItem('adminUser');
  }

  function toLogin() {
    clearLogin();
    location.replace(LOGIN_PAGE);
  }

  async function authFetch(path, options = {}) {
    const token = getToken();
    if (!token) {
      toLogin();
      throw new Error('未登录');
    }

    const headers = new Headers(options.headers || {});
    headers.set('Authorization', 'Bearer ' + token);
    const adminUser = getAdminUser();
    if (adminUser?.id) headers.set('X-Admin-Id', String(adminUser.id));
    if (adminUser?.username) headers.set('X-Admin-Username', adminUser.username);
    if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    const url = /^https?:\/\//i.test(path) ? path : API_BASE + path;
    const res = await fetch(url, { ...options, headers });
    if (res.status === 401 || res.status === 403) {
      toLogin();
      throw new Error('登录已失效');
    }
    return res;
  }

  window.GoalHubAuth = {
    API_BASE,
    LOGIN_PAGE,
    getToken,
    getAdminUser,
    clearLogin,
    toLogin,
    authFetch,
    requireLogin() {
      if (!getToken()) toLogin();
    },
  };

  if (!/\/?login\.html$/i.test(location.pathname)) {
    window.GoalHubAuth.requireLogin();
  }
})();
