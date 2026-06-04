// 获取 DOM 元素
const registerForm = document.getElementById('registerForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const nicknameInput = document.getElementById('nickname');
const agreementCheckbox = document.getElementById('agreement');
const submitBtn = document.querySelector('.btn-register');

// 获取错误提示元素
const usernameError = document.getElementById('usernameError');
const passwordError = document.getElementById('passwordError');
const confirmPasswordError = document.getElementById('confirmPasswordError');
const nicknameError = document.getElementById('nicknameError');
const formError = document.getElementById('formError');

// 获取底部按钮
const loginBtn = document.getElementById('loginBtn');
const guestBtn = document.getElementById('guestBtn');
const serviceBtn = document.getElementById('serviceBtn');

// API 配置
const API_URL = 'http://localhost:8000/api/user/register';

// 验证函数
function validateUsername(username) {
    if (!username) {
        return { valid: false, message: '用户名不能为空' };
    }
    if (username.length < 3) {
        return { valid: false, message: '用户名至少需要3个字符' };
    }
    if (username.length > 20) {
        return { valid: false, message: '用户名不能超过20个字符' };
    }
    if (!/^[a-zA-Z0-9_]*$/.test(username)) {
        return { valid: false, message: '用户名只能包含字母、数字和下划线' };
    }
    return { valid: true, message: '' };
}

function validatePassword(password) {
    if (!password) {
        return { valid: false, message: '密码不能为空' };
    }
    if (password.length < 6) {
        return { valid: false, message: '密码至少需要6个字符' };
    }
    if (password.length > 50) {
        return { valid: false, message: '密码不能超过50个字符' };
    }
    return { valid: true, message: '' };
}

function validateConfirmPassword(password, confirmPassword) {
    if (!confirmPassword) {
        return { valid: false, message: '请确认密码' };
    }
    if (password !== confirmPassword) {
        return { valid: false, message: '两次输入的密码不一致' };
    }
    return { valid: true, message: '' };
}

function validateNickname(nickname) {
    if (!nickname) {
        return { valid: false, message: '昵称不能为空' };
    }
    if (nickname.length < 2) {
        return { valid: false, message: '昵称至少需要2个字符' };
    }
    if (nickname.length > 30) {
        return { valid: false, message: '昵称不能超过30个字符' };
    }
    return { valid: true, message: '' };
}

// 清除错误提示
function clearErrors() {
    usernameError.textContent = '';
    passwordError.textContent = '';
    confirmPasswordError.textContent = '';
    nicknameError.textContent = '';
    formError.textContent = '';
}

// 显示错误提示
function showError(errorElement, message) {
    if (errorElement) {
        errorElement.textContent = message;
    }
}

// 显示消息提示
function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type === 'error' ? 'error' : ''}`;
    messageDiv.textContent = message;
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// 实时验证
usernameInput.addEventListener('blur', () => {
    const validation = validateUsername(usernameInput.value.trim());
    showError(usernameError, validation.message);
});

passwordInput.addEventListener('blur', () => {
    const validation = validatePassword(passwordInput.value);
    showError(passwordError, validation.message);
});

confirmPasswordInput.addEventListener('blur', () => {
    const validation = validateConfirmPassword(passwordInput.value, confirmPasswordInput.value);
    showError(confirmPasswordError, validation.message);
});

nicknameInput.addEventListener('blur', () => {
    const validation = validateNickname(nicknameInput.value.trim());
    showError(nicknameError, validation.message);
});

// 表单提交
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // 清除之前的错误提示
    clearErrors();
    
    // 获取表单数据
    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    const nickname = nicknameInput.value.trim();
    const agreement = agreementCheckbox.checked;
    
    // 验证所有字段
    const usernameValidation = validateUsername(username);
    const passwordValidation = validatePassword(password);
    const confirmPasswordValidation = validateConfirmPassword(password, confirmPassword);
    const nicknameValidation = validateNickname(nickname);
    
    // 显示验证错误
    if (!usernameValidation.valid) {
        showError(usernameError, usernameValidation.message);
    }
    if (!passwordValidation.valid) {
        showError(passwordError, passwordValidation.message);
    }
    if (!confirmPasswordValidation.valid) {
        showError(confirmPasswordError, confirmPasswordValidation.message);
    }
    if (!nicknameValidation.valid) {
        showError(nicknameError, nicknameValidation.message);
    }
    
    // 检查是否同意条款
    if (!agreement) {
        showError(formError, '请阅读并同意相关条款和隐私政策');
    }
    
    // 如果有验证错误，返回
    if (!usernameValidation.valid || !passwordValidation.valid || 
        !confirmPasswordValidation.valid || !nicknameValidation.valid || !agreement) {
        return;
    }
    
    // 发送注册请求
    await submitRegister(username, password, nickname);
});

// 提交注册请求
async function submitRegister(username, password, nickname) {
    // 禁用提交按钮并显示加载状态
    submitBtn.disabled = true;
    submitBtn.classList.add('loading');
    
    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password,
                nickname: nickname
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // 注册成功
            showMessage('注册成功！正在跳转...', 'success');
            
            // 清空表单
            registerForm.reset();
            
            // 2秒后跳转到登录页面或首页
            setTimeout(() => {
                // 这里可以根据需求跳转到登录页或其他页面
                window.location.href = '/login.html';
            }, 2000);
        } else {
            // 注册失败
            const errorMessage = data.message || data.error || '注册失败，请重试';
            showError(formError, errorMessage);
            showMessage(errorMessage, 'error');
        }
    } catch (error) {
        console.error('注册请求失败:', error);
        const errorMessage = '网络错误，请检查服务器是否运行或网络连接';
        showError(formError, errorMessage);
        showMessage(errorMessage, 'error');
    } finally {
        // 恢复提交按钮状态
        submitBtn.disabled = false;
        submitBtn.classList.remove('loading');
    }
}

// 底部按钮事件处理
loginBtn.addEventListener('click', () => {
    // 跳转到登录页面
    window.location.href = '/login.html';
});

guestBtn.addEventListener('click', () => {
    // 游客进入
    showMessage('游客进入功能开发中...', 'success');
    // 可以根据需求跳转到首页或其他页面
    // window.location.href = '/index.html';
});

serviceBtn.addEventListener('click', () => {
    // 在线客服
    showMessage('客服已连接，稍后会有专员为您服务', 'success');
    // 可以集成在线客服SDK或显示客服信息
});

// 监听条款链接
document.querySelectorAll('.form-group.checkbox .link').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        if (link.textContent.includes('条款')) {
            showMessage('打开服务条款页面', 'success');
        } else if (link.textContent.includes('隐私')) {
            showMessage('打开隐私政策页面', 'success');
        }
    });
});
