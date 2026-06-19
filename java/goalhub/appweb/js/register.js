// 获取 DOM 元素
const registerForm = document.getElementById('registerForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const nicknameInput = document.getElementById('nickname');
const emailInput = document.getElementById('email');
const phoneInput = document.getElementById('phone');
const captchaCodeInput = document.getElementById('captchaCode');
const captchaImage = document.getElementById('captchaImage');
const refreshCaptchaBtn = document.getElementById('refreshCaptcha');
const agreementCheckbox = document.getElementById('agreement');
const submitBtn = document.querySelector('.btn-submit');

// 获取错误提示元素
const usernameError = document.getElementById('usernameError');
const passwordError = document.getElementById('passwordError');
const confirmPasswordError = document.getElementById('confirmPasswordError');
const nicknameError = document.getElementById('nicknameError');
const emailError = document.getElementById('emailError');
const phoneError = document.getElementById('phoneError');
const captchaCodeError = document.getElementById('captchaCodeError');
const formError = document.getElementById('formError');

// 存储验证码信息
let captchaKey = '';

// API 配置
const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const CAPTCHA_URL = `${API_BASE_URL}/api/user/captcha`;
const REGISTER_URL = `${API_BASE_URL}/api/user/register`;

function isSuccessCode(code) {
    return code === 0 || code === 200 || code === '0' || code === '200';
}

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

function validateEmail(email) {
    if (!email) {
        return { valid: true, message: '' };
    }
    if (email.length > 100) {
        return { valid: false, message: '邮箱不能超过100个字符' };
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        return { valid: false, message: '邮箱格式不正确' };
    }
    return { valid: true, message: '' };
}

function validatePhone(phone) {
    if (!phone) {
        return { valid: true, message: '' };
    }
    if (phone.length > 30) {
        return { valid: false, message: '手机号码不能超过30个字符' };
    }
    if (!/^[0-9+\-\s()]+$/.test(phone)) {
        return { valid: false, message: '手机号码格式不正确' };
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
    usernameError.textContent = '';
    passwordError.textContent = '';
    confirmPasswordError.textContent = '';
    nicknameError.textContent = '';
    emailError.textContent = '';
    phoneError.textContent = '';
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

// 页面加载时获取验证码
window.addEventListener('load', () => {
    loadCaptcha();
});

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

emailInput.addEventListener('blur', () => {
    const validation = validateEmail(emailInput.value.trim());
    showError(emailError, validation.message);
});

phoneInput.addEventListener('blur', () => {
    const validation = validatePhone(phoneInput.value.trim());
    showError(phoneError, validation.message);
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
    const email = emailInput.value.trim();
    const phone = phoneInput.value.trim();
    const captchaCode = captchaCodeInput.value.trim();
    const agreement = agreementCheckbox.checked;
    
    // 验证所有字段
    const usernameValidation = validateUsername(username);
    const passwordValidation = validatePassword(password);
    const confirmPasswordValidation = validateConfirmPassword(password, confirmPassword);
    const nicknameValidation = validateNickname(nickname);
    const emailValidation = validateEmail(email);
    const phoneValidation = validatePhone(phone);
    const captchaValidation = validateCaptchaCode(captchaCode);
    
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
    if (!emailValidation.valid) {
        showError(emailError, emailValidation.message);
    }
    if (!phoneValidation.valid) {
        showError(phoneError, phoneValidation.message);
    }
    if (!captchaValidation.valid) {
        showError(captchaCodeError, captchaValidation.message);
    }
    
    // 检查是否同意条款
    if (!agreement) {
        showError(formError, '请阅读并同意相关条款和隐私政策');
    }
    
    // 如果有验证错误，返回
    if (!usernameValidation.valid || !passwordValidation.valid || 
        !confirmPasswordValidation.valid || !nicknameValidation.valid || 
        !emailValidation.valid || !phoneValidation.valid ||
        !captchaValidation.valid || !agreement) {
        return;
    }
    
    // 发送注册请求
    await submitRegister(username, password, nickname, email, phone, captchaCode);
});

// 提交注册请求
async function submitRegister(username, password, nickname, email, phone, captchaCode) {
    // 禁用提交按钮并显示加载状态
    submitBtn.disabled = true;
    submitBtn.classList.add('loading');
    
    try {
        const response = await fetch(REGISTER_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password,
                nickname: nickname,
                email: email,
                phone: phone,
                captchaKey: captchaKey,
                captchaCode: captchaCode
            })
        });
        
        const data = await response.json();
        
        if (response.ok && isSuccessCode(data.code)) {
            // 注册成功
            showMessage('注册成功，正在跳转登录页...', 'success');
            
            // 清空表单
            registerForm.reset();
            
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 800);
        } else {
            // 注册失败
            const errorMessage = data.message || data.error || `注册失败 (HTTP ${response.status})`;
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
