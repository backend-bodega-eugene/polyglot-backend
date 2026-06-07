const API_BASE_URL = 'http://localhost:8000';
const HISTORY_TYPE = document.body.dataset.historyType || 'deposit';
const HISTORY_PAGE_URL = `${API_BASE_URL}/api/order/${HISTORY_TYPE === 'withdraw' ? 'withdraworder' : 'depositorder'}/page`;
const PAGE_SIZE = 10;

const historySummary = document.getElementById('historySummary');
const historyList = document.getElementById('historyList');
const historyEmpty = document.getElementById('historyEmpty');
const historyLoadMoreBtn = document.getElementById('historyLoadMoreBtn');
const historyRefreshBtn = document.getElementById('historyRefreshBtn');

let currentPageIndex = 1;
let totalOrders = 0;
let loadedOrders = 0;
let isLoading = false;

function getAuthHeaders() {
    return {
        Authorization: `Bearer ${localStorage.getItem('authToken') || ''}`,
        'Content-Type': 'application/json'
    };
}

function isSuccessCode(code) {
    return code === 0 || code === 200 || code === '0' || code === '200';
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

function getRecords(payload) {
    if (Array.isArray(payload?.data?.records)) {
        return payload.data.records;
    }

    if (Array.isArray(payload?.records)) {
        return payload.records;
    }

    return [];
}

function getTotal(payload, records) {
    const total = Number(payload?.data?.total ?? payload?.total);
    return Number.isFinite(total) ? total : records.length;
}

function getStatusText(status) {
    const statusMap = {
        PENDING: '待审核',
        APPROVED: '已通过',
        REJECTED: '已拒绝',
        COMPLETED: '已完成',
        CANCELLED: '已取消',
        FAILED: '失败'
    };

    return statusMap[status] || status || '-';
}

function buildRequestBody() {
    return {
        currencyCode: 'USDT',
        pageIndex: currentPageIndex,
        pageSize: PAGE_SIZE
    };
}

function renderLoading(firstPage) {
    if (firstPage) {
        historyList.innerHTML = '<div class="app-section" style="color:#999; text-align:center;">订单加载中...</div>';
        historyEmpty.style.display = 'none';
    }

    historyLoadMoreBtn.disabled = true;
    historyLoadMoreBtn.textContent = '加载中...';
}

function renderEmpty() {
    historyList.innerHTML = '';
    historyEmpty.style.display = 'flex';
    historyLoadMoreBtn.style.display = 'none';
}

function renderError(message) {
    historyList.innerHTML = `<div class="app-section" style="color:#e74c3c; text-align:center;">${escapeHtml(message)}</div>`;
    historyEmpty.style.display = 'none';
    historyLoadMoreBtn.style.display = 'none';
}

function updateSummary() {
    historySummary.textContent = `USDT ${HISTORY_TYPE === 'withdraw' ? '提现' : '充值'}订单 · 共 ${totalOrders} 条`;
}

function updateLoadMore() {
    const hasMore = loadedOrders < totalOrders;
    historyLoadMoreBtn.style.display = hasMore ? 'block' : 'none';
    historyLoadMoreBtn.disabled = false;
    historyLoadMoreBtn.textContent = '加载更多';
}

function createDetailRows(order) {
    const rows = [
        ['订单号', order.orderNo],
        ['币种', order.currencyCode],
        ['申请金额', `${formatAmount(order.amount)} ${order.currencyCode || 'USDT'}`],
        ['实际金额', `${formatAmount(order.actualAmount)} ${order.currencyCode || 'USDT'}`],
        ['状态', getStatusText(order.status)],
        ['链类型', order.chainType],
        ['交易哈希', order.txHash],
        ['备注', order.remark],
        ['审核备注', order.auditRemark],
        ['审核时间', formatDate(order.auditTime)],
        ['创建时间', formatDate(order.createdAt)],
        ['更新时间', formatDate(order.updatedAt)]
    ];

    if (HISTORY_TYPE === 'withdraw') {
        rows.splice(4, 0, ['手续费', `${formatAmount(order.feeAmount)} ${order.currencyCode || 'USDT'}`]);
        rows.splice(7, 0, ['提现地址', order.withdrawAddress]);
    }

    return rows
        .filter(([, value]) => value !== undefined && value !== null && value !== '')
        .map(([label, value]) => `
            <div class="fund-history-row">
                <span>${escapeHtml(label)}</span>
                <strong>${escapeHtml(value)}</strong>
            </div>
        `)
        .join('');
}

function createOrderHtml(order) {
    const currencyCode = order.currencyCode || 'USDT';
    const createdAt = formatDate(order.createdAt);

    return `
        <article class="fund-history-card">
            <button class="fund-history-toggle" type="button" aria-expanded="false">
                <span>
                    <strong>${formatAmount(order.amount)} ${escapeHtml(currencyCode)}</strong>
                    <small>${escapeHtml(createdAt)}</small>
                </span>
                <span class="fund-history-status">${escapeHtml(getStatusText(order.status))}</span>
            </button>
            <div class="fund-history-detail">
                ${createDetailRows(order)}
            </div>
        </article>
    `;
}

function renderOrders(records, append = false) {
    if (!append && !records.length) {
        renderEmpty();
        return;
    }

    historyEmpty.style.display = 'none';
    const html = records.map(createOrderHtml).join('');
    if (append) {
        historyList.insertAdjacentHTML('beforeend', html);
    } else {
        historyList.innerHTML = html;
    }

    updateLoadMore();
}

async function loadOrders({ append = false } = {}) {
    if (isLoading) {
        return;
    }

    const firstPage = currentPageIndex === 1 && !append;
    isLoading = true;
    renderLoading(firstPage);

    try {
        const response = await fetch(HISTORY_PAGE_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(buildRequestBody())
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const payload = await response.json();
        if (!isSuccessCode(payload.code)) {
            throw new Error(payload.message || '查询失败');
        }

        const records = getRecords(payload);
        totalOrders = getTotal(payload, records);
        loadedOrders = append ? loadedOrders + records.length : records.length;
        updateSummary();
        renderOrders(records, append);
    } catch (error) {
        console.error('查询资金订单失败:', error);
        renderError(`查询失败: ${error.message}`);
    } finally {
        isLoading = false;
        if (historyLoadMoreBtn.style.display !== 'none') {
            updateLoadMore();
        }
    }
}

function resetAndLoad() {
    currentPageIndex = 1;
    totalOrders = 0;
    loadedOrders = 0;
    updateSummary();
    loadOrders();
}

historyList.addEventListener('click', event => {
    const button = event.target.closest('.fund-history-toggle');
    if (!button || !historyList.contains(button)) {
        return;
    }

    const card = button.closest('.fund-history-card');
    const expanded = card.classList.toggle('expanded');
    button.setAttribute('aria-expanded', String(expanded));
});

historyLoadMoreBtn.addEventListener('click', () => {
    currentPageIndex += 1;
    loadOrders({ append: true });
});

historyRefreshBtn.addEventListener('click', resetAndLoad);

updateSummary();
loadOrders();
