const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const PROFILE_URL = `${API_BASE_URL}/api/user/profile/me`;

const profileForm = document.getElementById('profileForm');
const emailInput = document.getElementById('email');
const phoneInput = document.getElementById('phone');
const nicknameInput = document.getElementById('nickname');
const avatarUrlInput = document.getElementById('avatarUrl');
const profileMessage = document.getElementById('profileMessage');
const profileSubmitBtn = document.getElementById('profileSubmitBtn');
const { apiFetch } = window.GoalHubApp;

function setProfileMessage(message, type = '') {
    profileMessage.textContent = message;
    profileMessage.className = `password-message ${type}`.trim();
}

function fillProfileForm(profile) {
    emailInput.value = profile.email || '';
    phoneInput.value = profile.phone || '';
    nicknameInput.value = profile.nickname || '';
    avatarUrlInput.value = profile.avatarUrl || '';

    if (profile.username) {
        localStorage.setItem('currentUsername', profile.username);
    }
    if (profile.nickname) {
        localStorage.setItem('currentNickname', profile.nickname);
    }
    localStorage.setItem('hasFundPassword', String(Boolean(profile.hasFundPassword)));
}

async function loadProfile() {
    try {
        profileSubmitBtn.disabled = true;
        setProfileMessage('加载中...');

        const payload = await apiFetch(PROFILE_URL);

        fillProfileForm(payload.data || {});
        setProfileMessage('');
    } catch (error) {
        console.error('获取资料失败:', error);
        setProfileMessage(`获取资料失败: ${error.message}`, 'error');
    } finally {
        profileSubmitBtn.disabled = false;
    }
}

async function submitProfile(event) {
    event.preventDefault();

    const profile = {
        email: emailInput.value.trim(),
        phone: phoneInput.value.trim(),
        nickname: nicknameInput.value.trim(),
        avatarUrl: avatarUrlInput.value.trim()
    };

    try {
        profileSubmitBtn.disabled = true;
        profileSubmitBtn.textContent = '保存中...';
        setProfileMessage('');

        const payload = await apiFetch(PROFILE_URL, {
            method: 'PUT',
            body: JSON.stringify(profile)
        });

        if (profile.nickname) {
            localStorage.setItem('currentNickname', profile.nickname);
        }
        setProfileMessage(payload.message || '资料保存成功', 'success');
    } catch (error) {
        console.error('保存资料失败:', error);
        setProfileMessage(`保存资料失败: ${error.message}`, 'error');
    } finally {
        profileSubmitBtn.disabled = false;
        profileSubmitBtn.textContent = '保存资料';
    }
}

profileForm?.addEventListener('submit', submitProfile);
loadProfile();
