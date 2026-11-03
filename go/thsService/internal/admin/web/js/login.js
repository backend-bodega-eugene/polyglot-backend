'use strict';
document.getElementById('loginForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const button = document.getElementById('login-btn');
  const message = document.getElementById('login-msg');
  message.classList.add('d-none');
  button.disabled = true;
  button.textContent = '登录中...';
  try {
    const res = await fetch('/api/admin/auth/login', {
      method: 'POST', credentials: 'same-origin', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({username: document.getElementById('username').value.trim(), password: document.getElementById('password').value})
    });
    const data = await res.json();
    if (!res.ok || data.code !== 0) throw new Error(data.message || '登录失败');
    location.replace('index.html');
  } catch (error) {
    message.textContent = error.message || '网络异常，请稍后再试';
    message.classList.remove('d-none');
  } finally { button.disabled = false; button.textContent = '登录'; }
});
