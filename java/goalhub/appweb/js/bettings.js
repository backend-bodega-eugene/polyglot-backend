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

let nextPageIndex = 1;
let totalApiOrders = 0;
let loadedOrders = 0;
let isLoading = false;
let activeRequestId = 0;
let filteredOrderBuffer = [];
let currentLangCode = localStorage.getItem('langCode') || DEFAULT_LANG_CODE;

function isSettledView() {
    return window.location.hash === '#settled';
}

function updateViewText() {
    pageTitle.textContent = '投注记录';
    emptyText.textContent = isSettledView() ? '暂无已结注单' : '暂无未结注单';
}

function isOrderInCurrentView(order) {
    const isSettled = Boolean(order.settledAt);
    return isSettledView() ? isSettled : !isSettled;
}

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

function buildRequestBody(pageIndex) {
    const body = {
        pageIndex,
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
    const hasMore = filteredOrderBuffer.length > 0 || nextPageIndex <= Math.ceil(totalApiOrders / PAGE_SIZE);
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
    const view = getOrderItemView(item);
    const startRow = view.showStartTime
        ? `<div class="order-item-row"><span>开赛 ${escapeHtml(formatDate(item.matchStartTime))}</span><span>${escapeHtml(getResultText(item.systemResult || item.reviewResult))}</span></div>`
        : '';

    return `
        <div class="order-item">
            <div class="order-item-title">${escapeHtml(view.title)}</div>
            <div class="order-item-row"><span>${escapeHtml(view.subTitle)}</span><span>@${escapeHtml(item.odds ?? '-')}</span></div>
            <div class="order-item-row"><span>投注 ${formatAmount(item.betAmount)}</span><span>预计返还 ${formatAmount(item.expectedReturn)}</span></div>
            ${startRow}
        </div>
    `;
}

function getOrderItemView(item) {
    if (item.betType === 'CHAMPION') {
        return {
            title: item.leagueName || '冠军玩法赛事',
            subTitle: `${item.playName || item.playCode || '冠军'} / ${item.championTeamName || item.optionName || item.optionCode || '选项'}`,
            showStartTime: false
        };
    }

    return {
        title: `${item.homeTeamName || '主队'} VS ${item.awayTeamName || '客队'}`,
        subTitle: `${item.playName || item.playCode || '玩法'} / ${item.optionName || item.optionCode || '选项'}`,
        showStartTime: true
    };
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
    if (append && isLoading) {
        return;
    }

    const requestId = activeRequestId + 1;
    activeRequestId = requestId;
    const firstPage = !append;
    if (firstPage) {
        nextPageIndex = 1;
        totalApiOrders = 0;
        filteredOrderBuffer = [];
    }

    isLoading = true;
    updateViewText();
    renderLoading(firstPage);

    try {
        const filteredRecords = filteredOrderBuffer.splice(0, PAGE_SIZE);

        while (filteredRecords.length < PAGE_SIZE) {
            const data = await apiFetch(ORDER_PAGE_API_URL, {
                method: 'POST',
                body: JSON.stringify(buildRequestBody(nextPageIndex))
            });

            if (requestId !== activeRequestId) {
                return;
            }

            const records = getRecords(data);
            totalApiOrders = getTotal(data, records);
            nextPageIndex += 1;
            filteredRecords.push(...records.filter(isOrderInCurrentView));

            const totalApiPages = Math.ceil(totalApiOrders / PAGE_SIZE);
            if (!records.length || nextPageIndex > totalApiPages) {
                break;
            }
        }

        const recordsToRender = filteredRecords.slice(0, PAGE_SIZE);
        filteredOrderBuffer = filteredRecords.slice(PAGE_SIZE);
        loadedOrders = append ? loadedOrders + recordsToRender.length : recordsToRender.length;
        renderOrders(recordsToRender, append);
    } catch (error) {
        if (requestId !== activeRequestId) {
            return;
        }

        console.error('查询注单失败:', error);
        renderError(`查询失败: ${error.message}`);
    } finally {
        if (requestId !== activeRequestId) {
            return;
        }

        isLoading = false;
        if (ordersLoadMoreBtn.style.display !== 'none') {
            updateLoadMore();
        }
    }
}

orderFilterForm.addEventListener('submit', event => {
    event.preventDefault();
    nextPageIndex = 1;
    filteredOrderBuffer = [];
    loadedOrders = 0;
    loadOrders();
});

resetFilterBtn.addEventListener('click', () => {
    keywordsInput.value = '';
    startTimeInput.value = '';
    endTimeInput.value = '';
    nextPageIndex = 1;
    filteredOrderBuffer = [];
    loadedOrders = 0;
    loadOrders();
});

ordersLoadMoreBtn.addEventListener('click', () => {
    loadOrders({ append: true });
});

window.addEventListener('hashchange', () => {
    nextPageIndex = 1;
    loadedOrders = 0;
    totalApiOrders = 0;
    filteredOrderBuffer = [];
    if (window.GoalHubApp?.renderFooter) {
        window.GoalHubApp.renderFooter();
    }
    loadOrders();
});

if (!window.location.hash) {
    window.history.replaceState(null, '', `${window.location.pathname}${window.location.search}#open`);
}

loadOrders();
