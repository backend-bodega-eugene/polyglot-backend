'use strict';
(async () => {
  try {
    const res = await fetch('/api/admin/auth/me', {credentials: 'same-origin', cache: 'no-store'});
    if (res.status === 401) { location.replace('login.html'); return; }
    const data = await res.json();
    if (!res.ok || data.code !== 0) throw new Error(data.message || '登录验证失败');
    const admin = data.data;
    document.getElementById('user-name').textContent = admin.username;
    document.getElementById('user-title').textContent = admin.username + (admin.isSuperAdmin ? ' · 超级管理员' : ' · 管理员');
    document.getElementById('welcome').textContent = '欢迎回来，' + admin.username;
    document.body.style.visibility = 'visible';
  } catch (error) {
    document.body.style.visibility = 'visible';
    document.getElementById('welcome').textContent = error.message || '网络异常，请刷新重试';
  }
})();
document.getElementById('btn-signout').addEventListener('click', async () => {
  try {
    const res = await fetch('/api/admin/auth/logout', {method: 'POST', credentials: 'same-origin'});
    if (!res.ok) throw new Error('退出失败，请重试');
    location.replace('login.html');
  } catch (error) { alert(error.message || '网络异常'); }
});
