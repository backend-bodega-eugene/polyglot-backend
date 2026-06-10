const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const PROFILE_URL = `${API_BASE_URL}/api/user/profile/me`;
const SET_FUND_PASSWORD_URL = `${API_BASE_URL}/api/user/security/fundpassword/set`;
const CHANGE_FUND_PASSWORD_URL = `${API_BASE_URL}/api/user/security/fundpassword/change`;

const fundPasswordForm = document.getElementById('fundPasswordForm');
const fundPasswordMode = document.getElementById('fundPasswordMode');
const oldFundPasswordField = document.getElementById('oldFundPasswordField');
const oldFundPasswordInput = document.getElementById('oldFundPassword');
const newFundPasswordInput = document.getElementById('newFundPassword');
const confirmFundPasswordInput = document.getElementById('confirmFundPassword');
const fundPasswordMessage = document.getElementById('fundPasswordMessage');
const fundPasswordSubmitBtn = document.getElementById('fundPasswordSubmitBtn');
const { apiFetch } = window.GoalHubApp;

let hasFundPassword = false;

function setFundPasswordMessage(message, type = '') {
    fundPasswordMessage.textContent = message;
    fundPasswordMessage.className = `password-message ${type}`.trim();
}

function applyFundPasswordMode(nextHasFundPassword) {
    hasFundPassword = nextHasFundPassword;
    oldFundPasswordField.style.display = hasFundPassword ? 'flex' : 'none';
    oldFundPasswordInput.required = hasFundPassword;
    fundPasswordMode.textContent = hasFundPassword ? '当前已设置资金密码，请输入原资金密码后修改' : '当前未设置资金密码，请设置新的资金密码';
    fundPasswordSubmitBtn.textContent = hasFundPassword ? '确认修改' : '确认设置';
    localStorage.setItem('hasFundPassword', String(hasFundPassword));
}

async function loadProfile() {
    try {
        fundPasswordSubmitBtn.disabled = true;

        const payload = await apiFetch(PROFILE_URL);

        applyFundPasswordMode(Boolean(payload.data?.hasFundPassword));
        setFundPasswordMessage('');
    } catch (error) {
        console.error('获取资金密码状态失败:', error);
        applyFundPasswordMode(localStorage.getItem('hasFundPassword') === 'true');
        setFundPasswordMessage(`获取状态失败: ${error.message}`, 'error');
    } finally {
        fundPasswordSubmitBtn.disabled = false;
    }
}

function validateFundPassword(oldFundPassword, newFundPassword, confirmFundPassword) {
    if (hasFundPassword && !oldFundPassword) {
        return '请输入原资金密码';
    }

    if (!newFundPassword) {
        return '请输入新资金密码';
    }

    if (newFundPassword.length < 6) {
        return '资金密码至少需要6个字符';
    }

    if (newFundPassword.length > 50) {
        return '资金密码不能超过50个字符';
    }

    if (newFundPassword !== confirmFundPassword) {
        return '两次输入的资金密码不一致';
    }

    if (hasFundPassword && oldFundPassword === newFundPassword) {
        return '新资金密码不能与原资金密码相同';
    }

    return '';
}

async function submitFundPassword(event) {
    event.preventDefault();

    const oldFundPassword = oldFundPasswordInput.value.trim();
    const newFundPassword = newFundPasswordInput.value.trim();
    const confirmFundPassword = confirmFundPasswordInput.value.trim();
    const errorMessage = validateFundPassword(oldFundPassword, newFundPassword, confirmFundPassword);

    if (errorMessage) {
        setFundPasswordMessage(errorMessage, 'error');
        return;
    }

    const requestUrl = hasFundPassword ? CHANGE_FUND_PASSWORD_URL : SET_FUND_PASSWORD_URL;
    const requestBody = hasFundPassword
        ? { oldFundPassword, newFundPassword }
        : { fundPassword: newFundPassword };

    try {
        fundPasswordSubmitBtn.disabled = true;
        fundPasswordSubmitBtn.textContent = '提交中...';
        setFundPasswordMessage('');

        const payload = await apiFetch(requestUrl, {
            method: 'POST',
            body: JSON.stringify(requestBody)
        });

        fundPasswordForm.reset();
        applyFundPasswordMode(true);
        setFundPasswordMessage(payload.message || '资金密码保存成功', 'success');
    } catch (error) {
        console.error('提交资金密码失败:', error);
        setFundPasswordMessage(`提交失败: ${error.message}`, 'error');
    } finally {
        fundPasswordSubmitBtn.disabled = false;
        fundPasswordSubmitBtn.textContent = hasFundPassword ? '确认修改' : '确认设置';
    }
}

fundPasswordForm?.addEventListener('submit', submitFundPassword);
loadProfile();
