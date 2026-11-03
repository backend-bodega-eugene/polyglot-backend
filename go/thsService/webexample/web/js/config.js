(function () {
  window.GoalHubConfig = {
    // 后端接口地址前缀，所有 /admin/... 请求都会基于这里拼接。
    // 例如：apiBase: 'https://eugenecoming.com'
    // 如果前端和后端网关同域部署，也可以改成空字符串：''
    apiBase: 'http://localhost:8000',
    loginPath: '/admin/auth/login',
    loginPage: 'login.html',
    nextUrl: 'index.html',
  };
})();
