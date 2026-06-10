(function () {
  const config = window.GoalHubConfig || {};
  const API_BASE = String(config.apiBase ?? '').replace(/\/+$/, '');
  const LOGIN_PAGE = config.loginPage || 'login.html';

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
    if (window.self !== window.top) {
      window.top.location.replace(LOGIN_PAGE);
      return;
    }
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

  (function routeStandalonePagesThroughShell() {
    const file = location.pathname.split('/').pop() || '';
    const embedded = new URLSearchParams(location.search).get('embedded') === '1' || window.self !== window.top;
    if (embedded || !file || file === 'index.html' || file === LOGIN_PAGE) return;
    if (!getToken()) return;
    if (!/\.html$/i.test(file)) return;

    const targetPage = `${file}${location.search || ''}${location.hash || ''}`;
    location.replace(`index.html?page=${encodeURIComponent(targetPage)}`);
  })();
})();
