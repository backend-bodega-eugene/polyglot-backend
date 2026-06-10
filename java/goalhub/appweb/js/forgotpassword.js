const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const SEND_CODE_URL = `${API_BASE_URL}/api/user/forgotpassword/sendcode`;
const RESET_PASSWORD_URL = `${API_BASE_URL}/api/user/forgotpassword/reset`;

const forgotPasswordForm = document.getElementById('forgotPasswordForm');
const emailStep = document.getElementById('emailStep');
const resetStep = document.getElementById('resetStep');
const emailInput = document.getElementById('email');
const resetEmailInput = document.getElementById('resetEmail');
const emailCodeInput = document.getElementById('emailCode');
const newPasswordInput = document.getElementById('newPassword');
const confirmPasswordInput = document.getElementById('confirmPassword');
const sendCodeBtn = document.getElementById('sendCodeBtn');
const resetPasswordBtn = document.getElementById('resetPasswordBtn');

const emailError = document.getElementById('emailError');
const codeError = document.getElementById('codeError');
const newPasswordError = document.getElementById('newPasswordError');
const confirmPasswordError = document.getElementById('confirmPasswordError');
const formError = document.getElementById('formError');

function isSuccessCode(code) {
    return code === 0 || code === 200 || code === '0' || code === '200';
}

function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type === 'error' ? 'error' : ''}`;
    messageDiv.textContent = message;
    document.body.appendChild(messageDiv);

    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

function clearErrors() {
    emailError.textContent = '';
    codeError.textContent = '';
    newPasswordError.textContent = '';
    confirmPasswordError.textContent = '';
    formError.textContent = '';
}

function validateEmail(email) {
    if (!email) {
        return '请输入邮箱';
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        return '请输入有效邮箱';
    }

    return '';
}

async function parseResponse(response) {
    const responseText = await response.text();
    const payload = responseText ? JSON.parse(responseText) : { code: response.ok ? 200 : response.status };

    if (!response.ok || !isSuccessCode(payload.code)) {
        throw new Error(payload.message || `请求失败 (HTTP ${response.status})`);
    }

    return payload;
}

async function sendCode() {
    clearErrors();

    const email = emailInput.value.trim();
    const emailMessage = validateEmail(email);
    if (emailMessage) {
        emailError.textContent = emailMessage;
        return;
    }

    try {
        sendCodeBtn.disabled = true;
        sendCodeBtn.classList.add('loading');

        const response = await fetch(SEND_CODE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email })
        });
        const payload = await parseResponse(response);

        resetEmailInput.value = email;
        emailStep.classList.add('forgot-step-hidden');
        resetStep.classList.remove('forgot-step-hidden');
        showMessage(payload.message || '验证码已发送，请查看邮箱');
    } catch (error) {
        console.error('发送邮件验证码失败:', error);
        formError.textContent = error.message;
        showMessage(error.message, 'error');
    } finally {
        sendCodeBtn.disabled = false;
        sendCodeBtn.classList.remove('loading');
    }
}

function validateResetForm() {
    const code = emailCodeInput.value.trim();
    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    let valid = true;

    clearErrors();

    if (!code) {
        codeError.textContent = '请输入邮件验证码';
        valid = false;
    }

    if (!newPassword) {
        newPasswordError.textContent = '请输入新密码';
        valid = false;
    } else if (newPassword.length < 6) {
        newPasswordError.textContent = '新密码至少需要6个字符';
        valid = false;
    }

    if (newPassword !== confirmPassword) {
        confirmPasswordError.textContent = '两次输入的新密码不一致';
        valid = false;
    }

    return valid;
}

async function resetPassword(event) {
    event.preventDefault();

    if (!validateResetForm()) {
        return;
    }

    try {
        resetPasswordBtn.disabled = true;
        resetPasswordBtn.classList.add('loading');

        const response = await fetch(RESET_PASSWORD_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: resetEmailInput.value.trim(),
                code: emailCodeInput.value.trim(),
                newPassword: newPasswordInput.value
            })
        });
        const payload = await parseResponse(response);

        showMessage(payload.message || '密码重置成功，请重新登录');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 1200);
    } catch (error) {
        console.error('重置密码失败:', error);
        formError.textContent = error.message;
        showMessage(error.message, 'error');
    } finally {
        resetPasswordBtn.disabled = false;
        resetPasswordBtn.classList.remove('loading');
    }
}

sendCodeBtn?.addEventListener('click', sendCode);
forgotPasswordForm?.addEventListener('submit', resetPassword);
