// 获取 DOM 元素
const loginForm = document.getElementById('loginForm');
const accountInput = document.getElementById('account');
const passwordInput = document.getElementById('password');
const captchaCodeInput = document.getElementById('captchaCode');
const captchaImage = document.getElementById('captchaImage');
const refreshCaptchaBtn = document.getElementById('refreshCaptcha');
const rememberMeCheckbox = document.getElementById('rememberMe');
const submitBtn = document.querySelector('.btn-submit');

// 获取错误提示元素
const accountError = document.getElementById('accountError');
const passwordError = document.getElementById('passwordError');
const captchaCodeError = document.getElementById('captchaCodeError');
const formError = document.getElementById('formError');

// 存储验证码信息
let captchaKey = '';

// API 配置
const CAPTCHA_URL = 'http://localhost:8000/api/user/captcha';
const LOGIN_URL = 'http://localhost:8000/api/user/login';

// 验证函数
function validateAccount(account) {
    if (!account) {
        return { valid: false, message: '账号不能为空' };
    }
    if (account.length < 3) {
        return { valid: false, message: '账号至少需要3个字符' };
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
    return { valid: true, message: '' };
}

function validateCaptchaCode(captchaCode) {
    if (!captchaCode) {
        return { valid: false, message: '验证码不能为空' };
    }
    if (captchaCode.length !== 4) {
        return { valid: false, message: '验证码长度为4位' };
    }
    return { valid: true, message: '' };
}

// 清除错误提示
function clearErrors() {
    accountError.textContent = '';
    passwordError.textContent = '';
    captchaCodeError.textContent = '';
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

// 获取验证码
async function loadCaptcha() {
    try {
        const response = await fetch(CAPTCHA_URL, {
            method: 'GET'
        });
        
        const data = await response.json();
        
        if (data.code === 200 && data.data) {
            captchaKey = data.data.captchaKey;
            const imageData = data.data.captchaImage;
            
            // 显示验证码图片
            if (imageData) {
                captchaImage.src = `data:image/png;base64,${imageData}`;
            }
            
            // 清除验证码输入框
            captchaCodeInput.value = '';
            captchaCodeError.textContent = '';
        } else {
            showMessage('获取验证码失败，请重试', 'error');
        }
    } catch (error) {
        console.error('获取验证码失败:', error);
        showMessage('网络错误，无法获取验证码', 'error');
    }
}

// 刷新验证码按钮
refreshCaptchaBtn.addEventListener('click', (e) => {
    e.preventDefault();
    refreshCaptchaBtn.classList.add('loading');
    refreshCaptchaBtn.disabled = true;
    
    loadCaptcha().then(() => {
        refreshCaptchaBtn.classList.remove('loading');
        refreshCaptchaBtn.disabled = false;
    });
});

// 点击验证码图片也可以刷新
captchaImage.addEventListener('click', () => {
    if (!refreshCaptchaBtn.disabled) {
        refreshCaptchaBtn.click();
    }
});

// 页面加载时获取验证码并恢复记住的账号
window.addEventListener('load', () => {
    // 加载验证码
    loadCaptcha();
    
    // 如果有保存的账号，自动填充
    const rememberedAccount = localStorage.getItem('rememberedAccount');
    if (rememberedAccount) {
        accountInput.value = rememberedAccount;
        rememberMeCheckbox.checked = true;
    }
});

// 实时验证
accountInput.addEventListener('blur', () => {
    const validation = validateAccount(accountInput.value.trim());
    showError(accountError, validation.message);
});

passwordInput.addEventListener('blur', () => {
    const validation = validatePassword(passwordInput.value);
    showError(passwordError, validation.message);
});

// 表单提交
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // 清除之前的错误提示
    clearErrors();
    
    // 获取表单数据
    const account = accountInput.value.trim();
    const password = passwordInput.value;
    const captchaCode = captchaCodeInput.value.trim();
    const rememberMe = rememberMeCheckbox.checked;
    
    // 验证所有字段
    const accountValidation = validateAccount(account);
    const passwordValidation = validatePassword(password);
    const captchaValidation = validateCaptchaCode(captchaCode);
    
    // 显示验证错误
    if (!accountValidation.valid) {
        showError(accountError, accountValidation.message);
    }
    if (!passwordValidation.valid) {
        showError(passwordError, passwordValidation.message);
    }
    if (!captchaValidation.valid) {
        showError(captchaCodeError, captchaValidation.message);
    }
    
    // 如果有验证错误，返回
    if (!accountValidation.valid || !passwordValidation.valid || !captchaValidation.valid) {
        return;
    }
    
    // 发送登录请求
    await submitLogin(account, password, captchaCode, rememberMe);
});

// 提交登录请求
async function submitLogin(account, password, captchaCode, rememberMe) {
    // 禁用提交按钮并显示加载状态
    submitBtn.disabled = true;
    submitBtn.classList.add('loading');
    
    try {
        console.log('发送登录请求...');
        const response = await fetch(LOGIN_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                account: account,
                password: password,
                captchaKey: captchaKey,
                captchaCode: captchaCode
            })
        });
        
        console.log('响应状态:', response.status);
        const data = await response.json();
        console.log('响应数据:', data);
        
        if (response.ok) {
            // 登录成功
            showMessage('登录成功！正在跳转...', 'success');
            
            // 如果勾选了"记住我"，保存账号到 localStorage
            if (rememberMe) {
                localStorage.setItem('rememberedAccount', account);
            } else {
                localStorage.removeItem('rememberedAccount');
            }
            
            // 保存登录信息到 localStorage
            // 注意：需要确认后端返回的字段名
            const token = data.token || data.data?.token;
            const userId = data.userId || data.data?.userId;
            
            console.log('保存token:', token);
            console.log('保存userId:', userId);
            
            if (token) {
                localStorage.setItem('authToken', token);
            } else {
                console.warn('未获取到token');
            }
            
            if (userId) {
                localStorage.setItem('userId', userId);
            }
            
            // 清空表单
            loginForm.reset();
            
            // 重新加载验证码
            loadCaptcha();
            
            // 2秒后跳转到首页
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 2000);
        } else {
            // 登录失败
            const errorMessage = data.message || data.error || `登录失败 (HTTP ${response.status})`;
            console.error('登录错误:', errorMessage);
            showError(formError, errorMessage);
            showMessage(errorMessage, 'error');
            
            // 刷新验证码
            loadCaptcha();
        }
    } catch (error) {
        console.error('登录请求失败:', error);
        const errorMessage = '网络错误，请检查服务器是否运行或网络连接';
        showError(formError, errorMessage);
        showMessage(errorMessage, 'error');
    } finally {
        // 恢复提交按钮状态
        submitBtn.disabled = false;
        submitBtn.classList.remove('loading');
    }
}

// 页面加载时，如果有保存的账号，自动填充
window.addEventListener('load', () => {
    const rememberedAccount = localStorage.getItem('rememberedAccount');
    if (rememberedAccount) {
        accountInput.value = rememberedAccount;
        rememberMeCheckbox.checked = true;
    }
});
