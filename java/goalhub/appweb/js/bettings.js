const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const ORDER_PAGE_API_URL = `${API_BASE_URL}/api/order/bet/orders/page`;
const DEFAULT_LANG_CODE = 'en-US';
const PAGE_SIZE = 10;

const pageTitle = document.getElementById('pageTitle');
const emptyText = document.getElementById('emptyText');
const ordersEmpty = document.getElementById('ordersEmpty');
const ordersList = document.getElementById('ordersList');
const orderFilterForm = document.getElementById('orderFilterForm');
const keywordsInput = document.getElementById('keywordsInput');
const startTimeInput = document.getElementById('startTimeInput');
const endTimeInput = document.getElementById('endTimeInput');
const resetFilterBtn = document.getElementById('resetFilterBtn');
const ordersLoadMoreBtn = document.getElementById('ordersLoadMoreBtn');
const { apiFetch } = window.GoalHubApp;

let currentPageIndex = 1;
let totalOrders = 0;
let loadedOrders = 0;
let isLoading = false;
let currentLangCode = localStorage.getItem('langCode') || DEFAULT_LANG_CODE;

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatAmount(value) {
    const amount = Number(value);
    return Number.isFinite(amount) ? amount.toFixed(2) : '0.00';
}

function formatDate(value) {
    if (!value) {
        return '-';
    }

    const normalizedValue = typeof value === 'string' && value.includes(' ') ? value.replace(' ', 'T') : value;
    const date = new Date(normalizedValue);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function toApiDateTime(value) {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '' : date.toISOString();
}

function getRecords(payload) {
    if (Array.isArray(payload?.data?.records)) {
        return payload.data.records;
    }

    if (Array.isArray(payload?.records)) {
        return payload.records;
    }

    if (Array.isArray(payload?.data)) {
        return payload.data;
    }

    return [];
}

function getTotal(payload, records) {
    const total = Number(payload?.data?.total ?? payload?.total);
    return Number.isFinite(total) ? total : records.length;
}

function buildRequestBody() {
    const body = {
        pageIndex: currentPageIndex,
        pageSize: PAGE_SIZE,
        langCode: currentLangCode
    };

    const keywords = keywordsInput.value.trim();
    const startTime = toApiDateTime(startTimeInput.value);
    const endTime = toApiDateTime(endTimeInput.value);

    if (keywords) {
        body.keywords = keywords;
    }

    if (startTime) {
        body.startTime = startTime;
    }

    if (endTime) {
        body.endTime = endTime;
    }

    return body;
}

function renderLoading(firstPage) {
    if (firstPage) {
        ordersList.innerHTML = '<div class="app-section" style="color:#999; text-align:center;">加载中...</div>';
        ordersEmpty.style.display = 'none';
    }

    ordersLoadMoreBtn.disabled = true;
    ordersLoadMoreBtn.textContent = '加载中...';
}

function renderEmpty() {
    ordersList.innerHTML = '';
    ordersEmpty.style.display = 'flex';
    ordersLoadMoreBtn.style.display = 'none';
}

function renderError(message) {
    ordersList.innerHTML = `<div class="app-section" style="color:#e74c3c; text-align:center;">${escapeHtml(message)}</div>`;
    ordersEmpty.style.display = 'none';
    ordersLoadMoreBtn.style.display = 'none';
}

function updateLoadMore() {
    const hasMore = loadedOrders < totalOrders;
    ordersLoadMoreBtn.style.display = hasMore ? 'block' : 'none';
    ordersLoadMoreBtn.disabled = false;
    ordersLoadMoreBtn.textContent = '加载更多';
}

function renderOrders(records, append = false) {
    if (!append && !records.length) {
        renderEmpty();
        return;
    }

    ordersEmpty.style.display = 'none';
    const html = records.map(createOrderHtml).join('');
    if (append) {
        ordersList.insertAdjacentHTML('beforeend', html);
    } else {
        ordersList.innerHTML = html;
    }

    updateLoadMore();
}

function createOrderHtml(order) {
    const items = Array.isArray(order.items) ? order.items : [];
    const itemHtml = items.length
        ? items.map(createOrderItemHtml).join('')
        : '<div class="order-item"><div class="order-item-row">暂无投注明细</div></div>';

    return `
        <article class="order-card">
            <div class="order-head">
                <div class="order-no">${escapeHtml(order.orderNo || `订单 ${order.orderId || '-'}`)}</div>
                <div class="order-status">${escapeHtml(getStatusText(order.status))}</div>
            </div>
            <div class="order-money">
                <span>投注 <strong>${formatAmount(order.totalBetAmount)}</strong> ${escapeHtml(order.currencyCode || 'USDT')}</span>
                <span>预计返还 <strong>${formatAmount(order.totalExpectedReturn)}</strong></span>
            </div>
            ${itemHtml}
            <div class="order-meta">创建时间 ${escapeHtml(formatDate(order.createdAt))}${order.settledAt ? ` ｜ 结算时间 ${escapeHtml(formatDate(order.settledAt))}` : ''}</div>
        </article>
    `;
}

function createOrderItemHtml(item) {
    const teams = `${item.homeTeamName || '主队'} VS ${item.awayTeamName || '客队'}`;
    const option = `${item.playName || item.playCode || '玩法'} / ${item.optionName || item.optionCode || '选项'}`;

    return `
        <div class="order-item">
            <div class="order-item-title">${escapeHtml(teams)}</div>
            <div class="order-item-row"><span>${escapeHtml(option)}</span><span>@${escapeHtml(item.odds ?? '-')}</span></div>
            <div class="order-item-row"><span>投注 ${formatAmount(item.betAmount)}</span><span>预计返还 ${formatAmount(item.expectedReturn)}</span></div>
            <div class="order-item-row"><span>开赛 ${escapeHtml(formatDate(item.matchStartTime))}</span><span>${escapeHtml(getResultText(item.systemResult || item.reviewResult))}</span></div>
        </div>
    `;
}

function getStatusText(status) {
    const statusMap = {
        PENDING: '待结算',
        UNSETTLED: '未结算',
        SETTLED: '已结算',
        CANCELLED: '已取消',
        VOID: '已作废'
    };

    return statusMap[status] || status || '-';
}

function getResultText(result) {
    const resultMap = {
        WIN: '赢',
        LOSE: '输',
        DRAW: '走水',
        HALF_WIN: '赢半',
        HALF_LOSE: '输半'
    };

    return resultMap[result] || result || '-';
}

async function loadOrders({ append = false } = {}) {
    if (isLoading) {
        return;
    }

    const firstPage = currentPageIndex === 1 && !append;
    isLoading = true;
    pageTitle.textContent = '投注记录';
    emptyText.textContent = '暂无投注记录';
    renderLoading(firstPage);

    try {
        const data = await apiFetch(ORDER_PAGE_API_URL, {
            method: 'POST',
            body: JSON.stringify(buildRequestBody())
        });

        const records = getRecords(data);
        totalOrders = getTotal(data, records);
        loadedOrders = append ? loadedOrders + records.length : records.length;
        renderOrders(records, append);
    } catch (error) {
        console.error('查询注单失败:', error);
        renderError(`查询失败: ${error.message}`);
    } finally {
        isLoading = false;
        if (ordersLoadMoreBtn.style.display !== 'none') {
            updateLoadMore();
        }
    }
}

orderFilterForm.addEventListener('submit', event => {
    event.preventDefault();
    currentPageIndex = 1;
    loadedOrders = 0;
    loadOrders();
});

resetFilterBtn.addEventListener('click', () => {
    keywordsInput.value = '';
    startTimeInput.value = '';
    endTimeInput.value = '';
    currentPageIndex = 1;
    loadedOrders = 0;
    loadOrders();
});

ordersLoadMoreBtn.addEventListener('click', () => {
    currentPageIndex += 1;
    loadOrders({ append: true });
});

loadOrders();
