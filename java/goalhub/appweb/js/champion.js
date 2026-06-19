const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const CHAMPION_PAGE_API_URL = `${API_BASE_URL}/api/soccer/champion/page`;
const CHAMPION_BET_ORDER_API_URL = `${API_BASE_URL}/api/order/bet/orders/placechampion`;
const LEAGUES_API_URL = `${API_BASE_URL}/api/soccer/leagues`;
const DEFAULT_LANG_CODE = 'zh-CN';
const PAGE_SIZE = 20;
const BET_PRESET_AMOUNTS = [10, 50, 100, 500, 1000];
const { apiFetch, refreshBalance } = window.GoalHubApp;

const championLeagueList = document.getElementById('championLeagueList');
const championList = document.getElementById('championList');
const championTitle = document.getElementById('championTitle');
const championSummary = document.getElementById('championSummary');
const championEmpty = document.getElementById('championEmpty');
const championEmptyText = document.getElementById('championEmptyText');
const championRefreshBtn = document.getElementById('championRefreshBtn');
const championLoadMoreBtn = document.getElementById('championLoadMoreBtn');

let currentLangCode = localStorage.getItem('langCode') || DEFAULT_LANG_CODE;
let currentLeagueId = null;
let currentLeagueName = '';
let currentPageIndex = 1;
let totalRecords = 0;
let isLoading = false;
let activeRequestId = 0;
let selectedChampionBet = null;

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
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

function formatOdds(value) {
    const odds = Number(value);
    return Number.isFinite(odds) ? odds.toFixed(2).replace(/\.00$/, '') : '-';
}

function formatMoney(value) {
    const amount = Number(value);
    return Number.isFinite(amount) ? amount.toFixed(2) : '0.00';
}

function isBetOpen(record) {
    return !record.betStatus || record.betStatus === 'OPEN';
}

function buildImage(url, fallbackText, className) {
    if (url) {
        return `<img class="${className}" src="${escapeHtml(url)}" alt="${escapeHtml(fallbackText)}" loading="lazy">`;
    }

    return `<span class="${className} champion-logo-fallback">${escapeHtml(String(fallbackText || '?').slice(0, 1))}</span>`;
}

function setLoading(firstPage) {
    isLoading = true;
    championRefreshBtn.disabled = true;
    championLoadMoreBtn.disabled = true;

    if (firstPage) {
        championEmpty.style.display = 'none';
        championList.innerHTML = '<div class="app-section champion-state">冠军赔率加载中...</div>';
    }
}

function clearLoading() {
    isLoading = false;
    championRefreshBtn.disabled = false;
    championLoadMoreBtn.disabled = false;
    championLoadMoreBtn.textContent = '加载更多';
}

function renderError(message) {
    championList.innerHTML = `<div class="app-section champion-state champion-error">${escapeHtml(message)}</div>`;
    championEmpty.style.display = 'none';
    championLoadMoreBtn.style.display = 'none';
}

function renderEmpty(text = '暂无冠军赔率') {
    championList.innerHTML = '';
    championEmptyText.textContent = text;
    championEmpty.style.display = 'flex';
    championLoadMoreBtn.style.display = 'none';
}

function updateSummary(records) {
    const loadedCount = championList.querySelectorAll('.champion-team-row').length;
    const totalText = Number.isFinite(totalRecords) && totalRecords > 0 ? `共 ${totalRecords} 个选项` : `已加载 ${loadedCount} 个选项`;
    championTitle.textContent = currentLeagueName ? `${currentLeagueName} · 冠军玩法赛事` : '冠军玩法赛事';
    championSummary.textContent = records?.length || loadedCount ? totalText : '请选择联赛 / 杯赛';
}

function updateLoadMore(recordsLength) {
    const totalPages = Math.ceil(totalRecords / PAGE_SIZE);
    const hasMore = recordsLength === PAGE_SIZE && currentPageIndex <= totalPages;
    championLoadMoreBtn.style.display = hasMore ? 'block' : 'none';
}

function groupByLeague(records) {
    return records.reduce((groups, record) => {
        const key = record.leagueId ?? 'unknown';
        if (!groups.has(key)) {
            groups.set(key, {
                leagueName: record.leagueName || currentLeagueName || '冠军玩法赛事',
                leagueLogoUrl: record.leagueLogoUrl || '',
                records: []
            });
        }

        groups.get(key).records.push(record);
        return groups;
    }, new Map());
}

function renderChampionRecords(records, append = false) {
    if (!append && !records.length) {
        renderEmpty();
        updateSummary(records);
        return;
    }

    championEmpty.style.display = 'none';
    const groupedHtml = Array.from(groupByLeague(records).values()).map(createLeagueGroupHtml).join('');

    if (append) {
        championList.insertAdjacentHTML('beforeend', groupedHtml);
    } else {
        championList.innerHTML = groupedHtml;
    }

    updateSummary(records);
    updateLoadMore(records.length);
}

function createLeagueGroupHtml(group) {
    const teamRows = group.records.map(createChampionTeamHtml).join('');

    return `
        <article class="champion-group">
            <div class="champion-group-head">
                ${buildImage(group.leagueLogoUrl, group.leagueName, 'champion-league-logo')}
                <div>
                    <div class="champion-group-title">${escapeHtml(group.leagueName)}</div>
                    <div class="champion-group-subtitle">冠军赔率</div>
                </div>
            </div>
            <div class="champion-team-list">${teamRows}</div>
        </article>
    `;
}

function createChampionTeamHtml(record) {
    const closed = !isBetOpen(record);
    const statusText = record.betStatus && record.betStatus !== 'OPEN'
        ? `<span class="champion-status">${escapeHtml(record.betStatus)}</span>`
        : '';

    return `
        <button class="champion-team-row${closed ? ' closed' : ''}" type="button"
            data-champion-odds-id="${escapeHtml(record.championOddsId)}"
            data-league-id="${escapeHtml(record.leagueId)}"
            data-league-name="${escapeHtml(record.leagueName)}"
            data-team-id="${escapeHtml(record.teamId)}"
            data-team-name="${escapeHtml(record.teamName)}"
            data-odds="${escapeHtml(record.odds)}"
            data-bet-status="${escapeHtml(record.betStatus || '')}">
            <span class="champion-team-main">
                ${buildImage(record.teamLogoUrl, record.teamName || record.teamCode, 'champion-team-logo')}
                <span class="champion-team-name">${escapeHtml(record.teamName || record.teamCode || '未知队伍')}</span>
            </span>
            <span class="champion-odds-box">
                <span class="champion-odds">${escapeHtml(formatOdds(record.odds))}</span>
                ${statusText}
            </span>
        </button>
    `;
}

async function loadLeagues() {
    championLeagueList.innerHTML = '<div class="champion-league-state">加载中...</div>';

    try {
        const data = await apiFetch(`${LEAGUES_API_URL}?langCode=${encodeURIComponent(currentLangCode)}`);
        const leagues = getRecords(data);

        if (!leagues.length) {
            championLeagueList.innerHTML = '<div class="champion-league-state">暂无联赛</div>';
            renderEmpty('暂无可查询的联赛 / 杯赛');
            return;
        }

        championLeagueList.innerHTML = leagues.map((league, index) => {
            const leagueId = league.id ?? league.leagueId;
            const leagueName = league.name || league.leagueName || '未知联赛';
            return `
                <button class="champion-league-item${index === 0 ? ' active' : ''}" type="button"
                    data-league-id="${escapeHtml(leagueId)}"
                    data-league-name="${escapeHtml(leagueName)}">
                    ${league.logoUrl || league.leagueLogoUrl ? buildImage(league.logoUrl || league.leagueLogoUrl, leagueName, 'champion-league-item-logo') : ''}
                    <span>${escapeHtml(leagueName)}</span>
                </button>
            `;
        }).join('');

        const firstLeague = championLeagueList.querySelector('.champion-league-item');
        selectLeague(firstLeague);
    } catch (error) {
        championLeagueList.innerHTML = `<div class="champion-league-state champion-error">加载失败: ${escapeHtml(error.message)}</div>`;
        renderError(`加载联赛失败: ${error.message}`);
    }
}

function selectLeague(button) {
    if (!button) {
        return;
    }

    championLeagueList.querySelectorAll('.champion-league-item').forEach(item => {
        item.classList.toggle('active', item === button);
    });

    currentLeagueId = Number(button.dataset.leagueId);
    currentLeagueName = button.dataset.leagueName || '';
    currentPageIndex = 1;
    totalRecords = 0;
    loadChampionOdds();
}

function buildRequestBody(pageIndex) {
    return {
        pageIndex,
        pageSize: PAGE_SIZE,
        leagueId: currentLeagueId,
        langCode: currentLangCode
    };
}

async function loadChampionOdds({ append = false } = {}) {
    if (isLoading || currentLeagueId === null || currentLeagueId === undefined || currentLeagueId === '') {
        return;
    }

    const requestId = activeRequestId + 1;
    activeRequestId = requestId;
    const pageIndex = append ? currentPageIndex : 1;
    setLoading(!append);

    try {
        const data = await apiFetch(CHAMPION_PAGE_API_URL, {
            method: 'POST',
            body: JSON.stringify(buildRequestBody(pageIndex))
        });

        if (requestId !== activeRequestId) {
            return;
        }

        const records = getRecords(data);
        totalRecords = getTotal(data, records);
        currentPageIndex = pageIndex + 1;
        renderChampionRecords(records, append);
    } catch (error) {
        if (requestId !== activeRequestId) {
            return;
        }

        console.error('查询冠军赔率失败:', error);
        renderError(`查询冠军赔率失败: ${error.message}`);
    } finally {
        if (requestId === activeRequestId) {
            clearLoading();
        }
    }
}

function initChampionBetModal() {
    const modal = document.createElement('div');
    modal.className = 'champion-bet-modal';
    modal.id = 'championBetModal';
    modal.innerHTML = `
        <div class="champion-bet-mask" data-champion-bet-close="true"></div>
        <div class="champion-bet-sheet">
            <button class="champion-bet-close" type="button" data-champion-bet-close="true">×</button>
            <div class="bet-sheet-header">
                <span class="bet-type">冠</span>
                <span class="bet-title">冠军投注单</span>
                <span class="bet-balance">USDT</span>
            </div>
            <div class="bet-info">
                <div class="bet-option-row">
                    <div>
                        <div class="bet-option-name" id="championBetTeam">-</div>
                        <div class="bet-market-name" id="championBetLeague">冠军玩法</div>
                    </div>
                    <div class="bet-odds" id="championBetOdds">@-</div>
                </div>
                <div class="bet-amount-row">
                    <input class="bet-amount-input" id="championBetAmountInput" type="number" min="0" step="0.01" placeholder="请输入下注金额">
                    <span class="bet-currency">USDT</span>
                </div>
            </div>
            <div class="bet-presets" id="championBetPresets"></div>
            <div class="bet-summary">
                <span>预计盈利 <strong id="championExpectedProfit">0.00</strong> USDT</span>
                <span>预计返还 <strong id="championExpectedReturn">0.00</strong> USDT</span>
            </div>
            <div class="bet-message" id="championBetMessage"></div>
            <button class="bet-submit" id="championBetSubmitBtn" type="button">确认投注</button>
        </div>
    `;

    document.body.appendChild(modal);
    document.getElementById('championBetPresets').innerHTML = BET_PRESET_AMOUNTS.map(amount => (
        `<button type="button" class="bet-preset" data-amount="${amount}">${amount}</button>`
    )).join('');

    modal.addEventListener('click', event => {
        if (event.target.dataset.championBetClose === 'true') {
            closeChampionBetModal();
            return;
        }

        const preset = event.target.closest('.bet-preset');
        if (preset) {
            document.getElementById('championBetAmountInput').value = preset.dataset.amount;
            updateChampionBetSummary();
        }
    });

    document.getElementById('championBetAmountInput').addEventListener('input', updateChampionBetSummary);
    document.getElementById('championBetSubmitBtn').addEventListener('click', submitChampionBet);
}

function openChampionBetModal(button) {
    if (button.classList.contains('closed') || (button.dataset.betStatus && button.dataset.betStatus !== 'OPEN')) {
        return;
    }

    selectedChampionBet = {
        championOddsId: Number(button.dataset.championOddsId),
        leagueId: Number(button.dataset.leagueId),
        leagueName: button.dataset.leagueName,
        teamId: Number(button.dataset.teamId),
        teamName: button.dataset.teamName,
        odds: Number(button.dataset.odds)
    };

    document.getElementById('championBetTeam').textContent = selectedChampionBet.teamName || '-';
    document.getElementById('championBetLeague').textContent = `${selectedChampionBet.leagueName || '冠军玩法'} / 冠军`;
    document.getElementById('championBetOdds').textContent = `@${formatOdds(selectedChampionBet.odds)}`;
    document.getElementById('championBetAmountInput').value = '';
    document.getElementById('championBetMessage').textContent = '';
    document.getElementById('championBetMessage').className = 'bet-message';
    document.getElementById('championBetSubmitBtn').disabled = false;
    document.getElementById('championBetSubmitBtn').textContent = '确认投注';
    updateChampionBetSummary();
    document.getElementById('championBetModal').classList.add('active');
}

function closeChampionBetModal() {
    document.getElementById('championBetModal')?.classList.remove('active');
}

function updateChampionBetSummary() {
    const amount = Number(document.getElementById('championBetAmountInput').value);
    const odds = Number(selectedChampionBet?.odds);
    const expectedReturn = amount > 0 && odds > 0 ? amount * odds : 0;
    const expectedProfit = Math.max(expectedReturn - amount, 0);

    document.getElementById('championExpectedProfit').textContent = expectedProfit.toFixed(2);
    document.getElementById('championExpectedReturn').textContent = expectedReturn.toFixed(2);
}

async function submitChampionBet() {
    const messageEl = document.getElementById('championBetMessage');
    const amount = Number(document.getElementById('championBetAmountInput').value);

    if (!selectedChampionBet || !Number.isFinite(selectedChampionBet.championOddsId)) {
        messageEl.textContent = '未选择有效冠军赔率';
        messageEl.className = 'bet-message error';
        return;
    }

    if (!Number.isFinite(amount) || amount <= 0) {
        messageEl.textContent = '请输入下注金额';
        messageEl.className = 'bet-message error';
        return;
    }

    const submitBtn = document.getElementById('championBetSubmitBtn');
    const requestBody = {
        championOddsId: selectedChampionBet.championOddsId,
        amount
    };

    try {
        submitBtn.disabled = true;
        submitBtn.textContent = '提交中...';
        messageEl.textContent = '';
        messageEl.className = 'bet-message';

        const data = await apiFetch(CHAMPION_BET_ORDER_API_URL, {
            method: 'POST',
            body: JSON.stringify(requestBody)
        });

        const order = data.data || {};
        messageEl.className = 'bet-message success';
        messageEl.innerHTML = `
            下单成功${order.orderNo ? ` ｜ 订单号 ${escapeHtml(order.orderNo)}` : ''}
            ${order.expectedReturn != null ? ` ｜ 预计返还 ${formatMoney(order.expectedReturn)} USDT` : ''}
            ${order.balanceAfter != null ? ` ｜ 余额 ${formatMoney(order.balanceAfter)} USDT` : ''}
        `;
        submitBtn.textContent = '已提交';
        refreshBalance();
    } catch (error) {
        messageEl.textContent = `下单失败: ${error.message}`;
        messageEl.className = 'bet-message error';
        submitBtn.disabled = false;
        submitBtn.textContent = '确认投注';
    }
}

championLeagueList.addEventListener('click', event => {
    const item = event.target.closest('.champion-league-item');
    if (item) {
        selectLeague(item);
    }
});

championList.addEventListener('click', event => {
    const row = event.target.closest('.champion-team-row');
    if (row) {
        openChampionBetModal(row);
    }
});

championRefreshBtn.addEventListener('click', () => {
    currentPageIndex = 1;
    loadChampionOdds();
});

championLoadMoreBtn.addEventListener('click', () => {
    championLoadMoreBtn.textContent = '加载中...';
    loadChampionOdds({ append: true });
});

initChampionBetModal();
loadLeagues();
