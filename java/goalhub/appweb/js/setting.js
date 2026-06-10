const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const PROFILE_URL = `${API_BASE_URL}/api/user/profile/me`;

const fundPasswordStatus = document.getElementById('fundPasswordStatus');
const { apiFetch } = window.GoalHubApp;

async function loadFundPasswordStatus() {
    if (!fundPasswordStatus) {
        return;
    }

    try {
        const payload = await apiFetch(PROFILE_URL);

        const hasFundPassword = Boolean(payload.data?.hasFundPassword);
        fundPasswordStatus.textContent = hasFundPassword ? '已设置' : '未设置';
        localStorage.setItem('hasFundPassword', String(hasFundPassword));
    } catch (error) {
        console.error('获取资金密码状态失败:', error);
        fundPasswordStatus.textContent = '未知';
    }
}

loadFundPasswordStatus();
