const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const CHANGE_PASSWORD_URL = `${API_BASE_URL}/api/user/change-password`;

const changePasswordForm = document.getElementById('changePasswordForm');
const oldPasswordInput = document.getElementById('oldPassword');
const newPasswordInput = document.getElementById('newPassword');
const confirmPasswordInput = document.getElementById('confirmPassword');
const passwordMessage = document.getElementById('passwordMessage');
const changePasswordBtn = document.getElementById('changePasswordBtn');
const { apiFetch } = window.GoalHubApp;

function setPasswordMessage(message, type = '') {
    passwordMessage.textContent = message;
    passwordMessage.className = `password-message ${type}`.trim();
}

function validatePasswordForm(oldPassword, newPassword, confirmPassword) {
    if (!oldPassword) {
        return '请输入原密码';
    }

    if (!newPassword) {
        return '请输入新密码';
    }

    if (newPassword.length < 6) {
        return '新密码至少需要6个字符';
    }

    if (newPassword.length > 50) {
        return '新密码不能超过50个字符';
    }

    if (newPassword !== confirmPassword) {
        return '两次输入的新密码不一致';
    }

    if (oldPassword === newPassword) {
        return '新密码不能与原密码相同';
    }

    return '';
}

async function submitChangePassword(event) {
    event.preventDefault();

    const oldPassword = oldPasswordInput.value.trim();
    const newPassword = newPasswordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();
    const errorMessage = validatePasswordForm(oldPassword, newPassword, confirmPassword);

    if (errorMessage) {
        setPasswordMessage(errorMessage, 'error');
        return;
    }

    try {
        changePasswordBtn.disabled = true;
        changePasswordBtn.textContent = '提交中...';
        setPasswordMessage('');

        const payload = await apiFetch(CHANGE_PASSWORD_URL, {
            method: 'POST',
            body: JSON.stringify({
                oldPassword,
                newPassword
            })
        });

        changePasswordForm.reset();
        setPasswordMessage(payload.message || '密码修改成功', 'success');
    } catch (error) {
        console.error('修改密码失败:', error);
        setPasswordMessage(`修改密码失败: ${error.message}`, 'error');
    } finally {
        changePasswordBtn.disabled = false;
        changePasswordBtn.textContent = '确认修改';
    }
}

changePasswordForm?.addEventListener('submit', submitChangePassword);
