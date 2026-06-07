const API_BASE_URL = 'http://localhost:8000';
const TRANSACTIONS_API_URL = `${API_BASE_URL}/api/user/account/me/transactions`;
const PAGE_SIZE = 20;
const DEFAULT_CURRENCY_CODE = 'USDT';

const transactionTabs = document.getElementById('transactionTabs');
const transactionSummary = document.getElementById('transactionSummary');
const transactionTotal = document.getElementById('transactionTotal');
const transactionList = document.getElementById('transactionList');
const transactionEmpty = document.getElementById('transactionEmpty');
const transactionEmptyText = document.getElementById('transactionEmptyText');
const transactionsLoadMoreBtn = document.getElementById('transactionsLoadMoreBtn');
const transactionRefreshBtn = document.getElementById('transactionRefreshBtn');

let currentPageIndex = 1;
let currentFlow = 'all';
let totalTransactions = 0;
let loadedTransactions = 0;
let loadedRawTransactions = 0;
let isLoading = false;

function getAuthHeaders() {
    return {
        Authorization: `Bearer ${localStorage.getItem('authToken') || ''}`,
        'Content-Type': 'application/json'
    };
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

function isSuccessCode(code) {
    return code === 0 || code === 200 || code === '0' || code === '200';
}

function buildRequestBody() {
    return {
        currencyCode: DEFAULT_CURRENCY_CODE,
        pageIndex: currentPageIndex,
        pageSize: PAGE_SIZE
    };
}

function getChangeAmount(record) {
    return Number(record.changeAmount ?? record.change_amount ?? 0);
}

function filterByFlow(records) {
    if (currentFlow === 'income') {
        return records.filter(record => getChangeAmount(record) > 0);
    }

    if (currentFlow === 'expense') {
        return records.filter(record => getChangeAmount(record) < 0);
    }

    return records;
}

function getBizTypeText(type) {
    const typeMap = {
        BET: '投注',
        BET_SETTLE: '注单结算',
        DEPOSIT: '存款',
        WITHDRAW: '取款',
        TRANSFER: '转账',
        ADJUST: '调账'
    };

    return typeMap[type] || type || '流水';
}

function renderLoading(firstPage) {
    if (firstPage) {
        transactionList.innerHTML = '<div class="app-section" style="color:#999; text-align:center;">流水加载中...</div>';
        transactionEmpty.style.display = 'none';
    }

    transactionsLoadMoreBtn.disabled = true;
    transactionsLoadMoreBtn.textContent = '加载中...';
}

function renderEmpty() {
    transactionList.innerHTML = '';
    transactionEmpty.style.display = 'flex';
    transactionEmptyText.textContent = currentFlow === 'income'
        ? '暂无进账记录'
        : currentFlow === 'expense'
            ? '暂无出账记录'
            : '暂无流水记录';
    transactionsLoadMoreBtn.style.display = 'none';
}

function renderError(message) {
    transactionList.innerHTML = `<div class="app-section" style="color:#e74c3c; text-align:center;">${escapeHtml(message)}</div>`;
    transactionEmpty.style.display = 'none';
    transactionsLoadMoreBtn.style.display = 'none';
}

function updateSummary() {
    const labelMap = {
        all: 'USDT 全部流水',
        income: 'USDT 进账流水',
        expense: 'USDT 出账流水'
    };

    transactionSummary.textContent = labelMap[currentFlow] || 'USDT 流水';
    transactionTotal.textContent = currentFlow === 'all'
        ? `共 ${totalTransactions} 条`
        : `已显示 ${loadedTransactions} 条`;
}

function updateLoadMore() {
    const hasMore = loadedRawTransactions < totalTransactions;
    transactionsLoadMoreBtn.style.display = hasMore ? 'block' : 'none';
    transactionsLoadMoreBtn.disabled = false;
    transactionsLoadMoreBtn.textContent = '加载更多';
}

function createTransactionHtml(record) {
    const amount = getChangeAmount(record);
    const isIncome = amount > 0;
    const amountClass = isIncome ? 'income' : amount < 0 ? 'expense' : '';
    const sign = amount > 0 ? '+' : '';
    const currencyCode = record.currencyCode || record.currency_code || DEFAULT_CURRENCY_CODE;
    const bizType = record.bizType || record.biz_type || '';
    const remark = record.remark || '';

    return `
        <article class="transaction-card">
            <div class="transaction-head">
                <div>
                    <div class="transaction-title">${escapeHtml(getBizTypeText(bizType))}</div>
                    <div class="transaction-time">${escapeHtml(formatDate(record.createdAt || record.created_at))}</div>
                </div>
                <div class="transaction-amount ${amountClass}">${sign}${formatAmount(amount)} ${escapeHtml(currencyCode)}</div>
            </div>
            <div class="transaction-balances">
                <span>变动前 ${formatAmount(record.beforeBalance ?? record.before_balance)}</span>
                <span>变动后 ${formatAmount(record.afterBalance ?? record.after_balance)}</span>
            </div>
            ${remark ? `<div class="transaction-remark">${escapeHtml(remark)}</div>` : ''}
        </article>
    `;
}

function renderTransactions(records, append = false) {
    if (!append && !records.length) {
        renderEmpty();
        return;
    }

    transactionEmpty.style.display = 'none';
    const html = records.map(createTransactionHtml).join('');
    if (append) {
        transactionList.insertAdjacentHTML('beforeend', html);
    } else {
        transactionList.innerHTML = html;
    }

    updateLoadMore();
}

async function loadTransactions({ append = false } = {}) {
    if (isLoading) {
        return;
    }

    const firstPage = currentPageIndex === 1 && !append;
    isLoading = true;
    renderLoading(firstPage);

    try {
        const response = await fetch(TRANSACTIONS_API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(buildRequestBody())
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        if (!isSuccessCode(data.code)) {
            throw new Error(data.message || '查询流水失败');
        }

        const rawRecords = getRecords(data);
        const records = filterByFlow(rawRecords);
        totalTransactions = getTotal(data, rawRecords);
        loadedTransactions = append ? loadedTransactions + records.length : records.length;
        loadedRawTransactions = append ? loadedRawTransactions + rawRecords.length : rawRecords.length;
        updateSummary();
        renderTransactions(records, append);
        updateLoadMore();
    } catch (error) {
        console.error('查询流水失败:', error);
        renderError(`查询流水失败: ${error.message}`);
    } finally {
        isLoading = false;
        if (transactionsLoadMoreBtn.style.display !== 'none') {
            updateLoadMore();
        }
    }
}

function resetAndLoad() {
    currentPageIndex = 1;
    totalTransactions = 0;
    loadedTransactions = 0;
    loadedRawTransactions = 0;
    updateSummary();
    loadTransactions();
}

transactionTabs.addEventListener('click', event => {
    const tab = event.target.closest('.app-tab');
    if (!tab || !transactionTabs.contains(tab)) {
        return;
    }

    transactionTabs.querySelectorAll('.app-tab').forEach(item => item.classList.remove('active'));
    tab.classList.add('active');
    currentFlow = tab.dataset.flow || 'all';
    resetAndLoad();
});

transactionsLoadMoreBtn.addEventListener('click', () => {
    currentPageIndex += 1;
    loadTransactions({ append: true });
});

transactionRefreshBtn.addEventListener('click', resetAndLoad);

updateSummary();
loadTransactions();
