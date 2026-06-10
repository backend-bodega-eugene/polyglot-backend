(function () {
    const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
    const BALANCE_CACHE_KEY = 'defaultBalanceText';
    const FOOTER_ITEMS = [
        { key: 'tutorial', href: 'tutorial.html', icon: '📋', text: '盘口教程' },
        { key: 'service', href: 'service.html', icon: '⚙️', text: '客服页面' },
        { key: 'openOrders', href: 'bettings.html#open', icon: '📋', text: '未结注单' },
        { key: 'settledOrders', href: 'bettings.html#settled', icon: '✔️', text: '已结注单' },
        { key: 'my', href: 'myprofile.html', icon: '👤', text: '我的' }
    ];
    const FOOTER_ACTIVE_BY_PAGE = {
        'tutorial.html': 'tutorial',
        'service.html': 'service',
        'bettings.html': 'openOrders',
        'myprofile.html': 'my',
        'about.html': 'my',
        'setting.html': 'my',
        'editprofile.html': 'my',
        'fundpassword.html': 'my',
        'changepassword.html': 'my',
        'deposit.html': 'my',
        'withdraw.html': 'my',
        'deposithistory.html': 'my',
        'withdrawhistory.html': 'my',
        'transactions.html': 'my',
        'usercomments.html': 'service'
    };
    const PUBLIC_PAGES = new Set([
        'login.html',
        'register.html',
        'forgotpassword.html'
    ]);

    function getPageName() {
        const pathname = window.location.pathname || '';
        return pathname.substring(pathname.lastIndexOf('/') + 1) || 'register.html';
    }

    function isPublicPage() {
        return PUBLIC_PAGES.has(getPageName());
    }

    function getAuthToken() {
        return localStorage.getItem('authToken') || '';
    }

    function clearAuthState() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userId');
        localStorage.removeItem('defaultBalanceText');
        localStorage.removeItem('currentUsername');
        localStorage.removeItem('currentNickname');
        localStorage.removeItem('hasFundPassword');
    }

    function logout() {
        clearAuthState();
        window.location.href = '/login.html';
    }

    function redirectToLogin() {
        if (getPageName() === 'login.html') {
            return;
        }

        const next = `${window.location.pathname}${window.location.search}${window.location.hash}`;
        window.location.replace(`/login.html?next=${encodeURIComponent(next)}`);
    }

    function ensureAuthenticated() {
        if (isPublicPage()) {
            return true;
        }

        if (!getAuthToken()) {
            clearAuthState();
            redirectToLogin();
            return false;
        }

        return true;
    }

    function isSuccessCode(code) {
        return code === undefined || code === null || code === 0 || code === 200 || code === '0' || code === '200';
    }

    function buildUrl(input) {
        if (typeof input !== 'string') {
            return input;
        }

        if (/^https?:\/\//i.test(input)) {
            return input;
        }

        return `${API_BASE_URL}${input.startsWith('/') ? input : `/${input}`}`;
    }

    function getAuthHeaders(extraHeaders = {}) {
        return {
            Authorization: `Bearer ${getAuthToken()}`,
            ...extraHeaders
        };
    }

    async function apiFetch(input, options = {}) {
        const { auth = true, headers = {}, body, ...fetchOptions } = options;

        if (auth && !ensureAuthenticated()) {
            throw new Error('请先登录');
        }

        const requestHeaders = { ...headers };
        if (auth) {
            requestHeaders.Authorization = `Bearer ${getAuthToken()}`;
        }

        if (body !== undefined && !(body instanceof FormData) && !requestHeaders['Content-Type']) {
            requestHeaders['Content-Type'] = 'application/json';
        }

        const response = await fetch(buildUrl(input), {
            ...fetchOptions,
            headers: requestHeaders,
            body
        });

        const responseText = await response.text();
        let payload = null;
        if (responseText) {
            try {
                payload = JSON.parse(responseText);
            } catch (error) {
                payload = responseText;
            }
        }

        const code = typeof payload === 'object' && payload ? payload.code : undefined;
        if (response.status === 401 || code === 401 || code === '401') {
            clearAuthState();
            redirectToLogin();
            throw new Error((payload && payload.message) || '登录已过期，请重新登录');
        }

        if (!response.ok) {
            throw new Error((payload && payload.message) || responseText || `HTTP ${response.status}`);
        }

        if (payload && typeof payload === 'object' && !isSuccessCode(payload.code)) {
            throw new Error(payload.message || '请求失败');
        }

        return payload;
    }

    function renderCachedBalance() {
        const balanceText = localStorage.getItem(BALANCE_CACHE_KEY) || '💰 0.00';
        document.querySelectorAll('.balance').forEach(element => {
            element.textContent = balanceText;
        });
    }

    function getFooterActiveKey() {
        const pageName = getPageName();
        if (pageName === 'bettings.html' && window.location.hash === '#settled') {
            return 'settledOrders';
        }

        return FOOTER_ACTIVE_BY_PAGE[pageName] || '';
    }

    function renderFooter() {
        const activeKey = getFooterActiveKey();
        document.querySelectorAll('.index-footer').forEach(footer => {
            footer.innerHTML = FOOTER_ITEMS.map(item => (
                `<a class="footer-item${item.key === activeKey ? ' active' : ''}" href="${item.href}">` +
                `<span class="footer-icon">${item.icon}</span><span>${item.text}</span></a>`
            )).join('');
        });
    }

    function bindLogout() {
        document.querySelectorAll('#logoutLink, [data-logout="true"]').forEach(element => {
            element.addEventListener('click', event => {
                event.preventDefault();
                logout();
            });
        });
    }

    function markAppShellPage() {
        if (document.querySelector('.app-shell')) {
            document.body.classList.add('app-page');
        }
    }

    function formatBalance(value) {
        const amount = Number(value);
        return Number.isFinite(amount) ? amount.toFixed(2) : '0.00';
    }

    async function refreshBalance() {
        if (!document.querySelector('.balance') || !getAuthToken()) {
            renderCachedBalance();
            return;
        }

        try {
            const payload = await apiFetch('/api/user/account/me/defaultbalance');
            const balanceText = `💰 ${formatBalance(payload?.data?.availableBalance)}`;
            localStorage.setItem(BALANCE_CACHE_KEY, balanceText);
            document.querySelectorAll('.balance').forEach(element => {
                element.textContent = balanceText;
            });
        } catch (error) {
            renderCachedBalance();
        }
    }

    window.GoalHubApp = {
        API_BASE_URL,
        BALANCE_CACHE_KEY,
        apiFetch,
        clearAuthState,
        ensureAuthenticated,
        getAuthHeaders,
        isSuccessCode,
        logout,
        refreshBalance,
        renderFooter,
        renderCachedBalance
    };

    markAppShellPage();
    renderCachedBalance();
    renderFooter();
    bindLogout();

    if (!isPublicPage()) {
        ensureAuthenticated();
        window.addEventListener('load', refreshBalance);
    }
})();
