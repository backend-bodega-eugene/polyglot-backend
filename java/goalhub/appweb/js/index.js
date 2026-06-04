// ========== 首页交互逻辑 ==========

// API 基础配置
const API_BASE_URL = 'http://localhost:8000';
const RESULT_API_URL = 'http://localhost:8000/api/soccer/matches/results/page';
const BET_ORDER_API_URL = 'http://localhost:8000/api/order/bet/orders/place';
const DEFAULT_LANG_CODE = 'en-US';
const PAGE_SIZE = 10;
const BET_PRESET_AMOUNTS = [10, 50, 100, 500, 1000];

// DOM 元素
const navTabs = document.querySelectorAll('.nav-tab');
const leagueItems = document.querySelectorAll('.league-item');
const footerItems = document.querySelectorAll('.footer-item');
const backBtn = document.querySelector('.back-btn');
const leaguesList = document.getElementById('leaguesList');
const matchesList = document.getElementById('matchesList');
const statusFilter = document.querySelector('.status-filter');
const balanceEl = document.querySelector('.balance');

// 当前状态
let currentTab = 'today';
let currentLeagueId = null;
let currentLangCode = DEFAULT_LANG_CODE;
let authToken = null;
let currentPageIndex = 1;
let selectedBet = null;

/**
 * 兼容分页接口和普通数组接口
 */
function getRecords(payload) {
    if (Array.isArray(payload?.data?.records)) {
        return payload.data.records;
    }

    if (Array.isArray(payload?.data)) {
        return payload.data;
    }

    if (Array.isArray(payload?.records)) {
        return payload.records;
    }

    return [];
}

/**
 * 获取认证header
 */
function getAuthHeaders() {
    authToken = localStorage.getItem('authToken') || '';
    console.log('当前token:', authToken ? '已获取' : '未找到');
    
    return {
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
    };
}

function formatBalance(value) {
    const amount = Number(value);
    if (Number.isNaN(amount)) {
        return '0.00';
    }

    return amount.toFixed(2);
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/**
 * 加载用户默认账户可用余额
 */
async function loadDefaultBalance() {
    if (!balanceEl) {
        return;
    }

    try {
        const url = `${API_BASE_URL}/api/user/account/me/defaultbalance`;
        console.log('请求余额URL:', url);

        const response = await fetch(url, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('余额响应错误:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        const availableBalance = data?.data?.availableBalance;
        balanceEl.textContent = `💰 ${formatBalance(availableBalance)}`;
    } catch (error) {
        console.error('加载余额失败:', error);
        balanceEl.textContent = '💰 0.00';
    }
}

/**
 * 检查认证状态
 */
function checkAuth() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        console.error('未找到认证token，请先登陆');
        leaguesList.innerHTML = '<div style="padding: 10px; color: #e74c3c;">请先<a href="/login.html" style="color: #2196F3;">登陆</a>继续</div>';
        matchesList.innerHTML = '<div style="padding: 10px; color: #e74c3c;">请先<a href="/login.html" style="color: #2196F3;">登陆</a>继续</div>';
        return false;
    }
    return true;
}

/**
 * 获取联盟列表
 */
async function loadLeagues() {
    try {
        const url = `${API_BASE_URL}/api/soccer/leagues?langCode=${currentLangCode}`;
        console.log('请求URL:', url);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        console.log('响应状态:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('响应错误:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        console.log('联盟列表:', data);

        // 清空现有联盟列表
        leaguesList.innerHTML = '';

        // 添加"全部"选项
        const allLeagueItem = document.createElement('div');
        allLeagueItem.className = 'league-item active';
        allLeagueItem.textContent = '全部';
        allLeagueItem.dataset.leagueId = '';
        allLeagueItem.addEventListener('click', () => selectLeague(null, allLeagueItem));
        leaguesList.appendChild(allLeagueItem);

        // 添加真实联盟数据
        getRecords(data).forEach(league => {
            const leagueItem = document.createElement('div');
            leagueItem.className = 'league-item';
            leagueItem.textContent = league.name || league.leagueName || '未知';
            leagueItem.dataset.leagueId = league.id;
            leagueItem.addEventListener('click', () => selectLeague(league.id, leagueItem));
            leaguesList.appendChild(leagueItem);
        });
    } catch (error) {
        console.error('获取联盟列表失败:', error);
        // 显示错误提示
        leaguesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载失败: ${error.message}</div>`;
    }
}

/**
 * 选择联盟
 */
function selectLeague(leagueId, element) {
    // 移除其他项的active类
    document.querySelectorAll('.league-item').forEach(item => {
        item.classList.remove('active');
    });

    // 给当前项添加active类
    element.classList.add('active');

    // 更新当前联盟ID
    currentLeagueId = leagueId;

    console.log('选择联盟:', leagueId);

    // 重新加载对应联盟的赛事
    currentPageIndex = 1;
    loadMatches();
}

/**
 * 加载赛事列表
 */
async function loadMatches() {
    try {
        if (currentTab === 'results') {
            await loadMatchResults();
            return;
        }

        // 获取当前日期范围（当月）
        const now = new Date();
        const startDate = new Date(now.getFullYear(), now.getMonth(), 1);
        const endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59);

        const startTimeUtc = startDate.toISOString();
        const endTimeUtc = endDate.toISOString();

        let url;
        const baseParams = `pageIndex=${currentPageIndex}&pageSize=${PAGE_SIZE}&langCode=${currentLangCode}&startTimeUtc=${encodeURIComponent(startTimeUtc)}&endTimeUtc=${encodeURIComponent(endTimeUtc)}`;

        // 根据当前tab选择不同的API
        switch(currentTab) {
            case 'today':
                url = `${API_BASE_URL}/api/soccer/matches/today?${baseParams}`;
                if (currentLeagueId) {
                    url += `&leagueId=${currentLeagueId}`;
                }
                break;
            case 'early':
                url = `${API_BASE_URL}/api/soccer/matches/upcoming?${baseParams}`;
                if (currentLeagueId) {
                    url += `&leagueId=${currentLeagueId}`;
                }
                break;
            case 'hot':
                // 热门没有分页和日期限制
                url = `${API_BASE_URL}/api/soccer/matches/hot?langCode=${currentLangCode}&limit=${PAGE_SIZE}`;
                break;
            case 'ball':
            default:
                url = `${API_BASE_URL}/api/soccer/matches?${baseParams}`;
                if (currentLeagueId) {
                    url += `&leagueId=${currentLeagueId}`;
                }
                break;
        }

        console.log('请求赛事URL:', url);

        const response = await fetch(url, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        console.log('赛事响应状态:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('赛事响应错误:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        console.log('赛事列表:', data);
        const records = getRecords(data);

        // 清空赛事列表（第一页时）
        if (currentPageIndex === 1) {
            matchesList.innerHTML = '';
        }

        // 渲染赛事数据
        if (records.length > 0) {
            records.forEach(match => {
                const matchItem = createMatchElement(match);
                matchesList.appendChild(matchItem);
                loadMatchOdds(match, matchItem);
            });

            // 显示/隐藏加载更多按钮
            const loadMoreBtn = document.getElementById('loadMoreBtn');
            if (records.length === PAGE_SIZE) {
                loadMoreBtn.style.display = 'block';
            } else {
                loadMoreBtn.style.display = 'none';
            }
        } else {
            document.getElementById('loadMoreBtn').style.display = 'none';
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无赛事</div>';
        }
    } catch (error) {
        console.error('加载赛事失败:', error);
        matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载赛事失败: ${error.message}</div>`;
    }
}

/**
 * 加载赛果列表
 */
async function loadMatchResults() {
    try {
        if (statusFilter) {
            statusFilter.textContent = '赛果';
        }

        const requestBody = {
            pageIndex: currentPageIndex,
            pageSize: PAGE_SIZE,
            langCode: currentLangCode
        };

        if (currentLeagueId) {
            requestBody.leagueId = currentLeagueId;
        }

        console.log('请求赛果URL:', RESULT_API_URL, requestBody);

        const response = await fetch(RESULT_API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(requestBody)
        });

        console.log('赛果响应状态:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('赛果响应错误:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        console.log('赛果列表:', data);
        const records = getRecords(data);

        if (currentPageIndex === 1) {
            matchesList.innerHTML = '';
        }

        const loadMoreBtn = document.getElementById('loadMoreBtn');
        if (records.length > 0) {
            records.forEach(result => {
                matchesList.appendChild(createResultElement(result));
            });

            loadMoreBtn.style.display = records.length === PAGE_SIZE ? 'block' : 'none';
        } else {
            loadMoreBtn.style.display = 'none';
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无赛果</div>';
        }
    } catch (error) {
        console.error('加载赛果失败:', error);
        matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载赛果失败: ${error.message}</div>`;
    }
}

/**
 * 创建赛事DOM元素
 */
function createMatchElement(match) {
    const matchItem = document.createElement('div');
    matchItem.className = 'match-item';
    const matchId = match.matchId || match.id;
    matchItem.dataset.matchId = matchId || '';

    // 获取队伍信息
    const homeTeam = match.homeTeam?.name || match.homeTeamName || '主队';
    const awayTeam = match.awayTeam?.name || match.awayTeamName || '客队';
    const matchName = match.matchName || match.leagueName || '';
    const score = match.score || (match.status === 'NOT_STARTED' ? 'VS' : '-');

    // 格式化时间
    let timeText = '待定';
    const matchTime = match.matchTime || match.scheduledStartTimeUtc || match.startTimeUtc;
    if (matchTime) {
        const normalizedTime = typeof matchTime === 'string' && matchTime.includes(' ')
            ? `${matchTime.replace(' ', 'T')}Z`
            : matchTime;
        const date = new Date(normalizedTime);
        if (!Number.isNaN(date.getTime())) {
            timeText = date.toLocaleString('zh-CN', {
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        }
    }

    matchItem.innerHTML = `
        <div class="match-time">${escapeHtml(timeText)}${matchName ? ` · ${escapeHtml(matchName)}` : ''}</div>
        <div class="match-teams">
            <div class="team">${escapeHtml(homeTeam)}</div>
            <div class="score">${escapeHtml(score)}</div>
            <div class="team">${escapeHtml(awayTeam)}</div>
        </div>
        <div class="odds-options" data-odds-for="${matchId || ''}">
            <div class="odds-empty">赔率加载中...</div>
        </div>
        <div class="match-icons">
            <span class="icon">📊</span>
            <span class="icon">▶️</span>
            <span class="icon">🚩</span>
        </div>
    `;

    return matchItem;
}

/**
 * 加载单场赛事玩法和赔率
 */
async function loadMatchOdds(match, matchItem) {
    const matchId = match.matchId || match.id;
    const oddsContainer = matchItem.querySelector('.odds-options');

    if (!matchId) {
        oddsContainer.innerHTML = '<div class="odds-empty">暂无赛事ID</div>';
        return;
    }

    try {
        const url = `${API_BASE_URL}/api/soccer/matches/${matchId}/odds`;
        console.log('请求赔率URL:', url);

        const response = await fetch(url, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('赔率响应错误:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        const markets = Array.isArray(data?.data?.markets) ? data.data.markets : [];
        renderMatchOdds(oddsContainer, markets);
    } catch (error) {
        console.error('加载赔率失败:', error);
        oddsContainer.innerHTML = '<div class="odds-empty odds-error">赔率加载失败</div>';
    }
}

function renderMatchOdds(container, markets) {
    container.innerHTML = '';

    if (!markets.length) {
        container.innerHTML = '<div class="odds-empty">暂无玩法赔率</div>';
        return;
    }

    markets.forEach(market => {
        const marketBlock = document.createElement('div');
        marketBlock.className = 'odds-market';

        const marketName = market.marketName || market.marketCode || '玩法';
        const options = Array.isArray(market.options) ? market.options : [];
        const optionsHtml = options.length
            ? options.map(option => createOddOptionHtml(option)).join('')
            : '<div class="odds-empty">暂无选项</div>';

        marketBlock.innerHTML = `
            <div class="odds-market-title">${escapeHtml(marketName)}</div>
            <div class="odds-market-options">${optionsHtml}</div>
        `;

        container.appendChild(marketBlock);
    });
}

function createOddOptionHtml(option) {
    const optionName = option.marketOptionName || option.marketOptionCode || '选项';
    const oddsText = option.odds ?? '-';
    const isClosed = option.betStatus && option.betStatus !== 'OPEN';
    const statusClass = isClosed ? ' closed' : '';
    const statusText = isClosed ? `<span class="odd-status">${escapeHtml(option.betStatus)}</span>` : '';

    return `
        <div class="odd${statusClass}" data-option-id="${option.id || ''}" data-market-option-id="${option.marketOptionId || ''}" data-option-name="${escapeHtml(optionName)}" data-odds="${escapeHtml(oddsText)}" data-bet-status="${escapeHtml(option.betStatus || '')}">
            <span class="label">${escapeHtml(optionName)}</span>
            <span class="rate">${escapeHtml(oddsText)}</span>
            ${statusText}
        </div>
    `;
}

function initBetModal() {
    if (document.getElementById('betModal')) {
        return;
    }

    const modal = document.createElement('div');
    modal.className = 'bet-modal';
    modal.id = 'betModal';
    modal.innerHTML = `
        <div class="bet-modal-mask" data-bet-close="true"></div>
        <div class="bet-sheet">
            <button class="bet-close" type="button" data-bet-close="true">×</button>
            <div class="bet-sheet-header">
                <span class="bet-type">单</span>
                <span class="bet-title">投注单</span>
                <span class="bet-balance">USDT</span>
            </div>
            <div class="bet-info">
                <div class="bet-option-row">
                    <div>
                        <div class="bet-option-name" id="betOptionName">-</div>
                        <div class="bet-market-name" id="betMarketName">-</div>
                    </div>
                    <div class="bet-odds" id="betOdds">@-</div>
                </div>
                <div class="bet-match" id="betMatchName">-</div>
                <div class="bet-time" id="betMatchTime">-</div>
                <div class="bet-amount-row">
                    <input class="bet-amount-input" id="betAmountInput" type="number" min="0" step="0.01" placeholder="请输入下注金额">
                    <span class="bet-currency">USDT</span>
                </div>
            </div>
            <div class="bet-presets" id="betPresets"></div>
            <div class="bet-summary">
                <span>预计盈利 <strong id="betExpectedProfit">0.00</strong> USDT</span>
                <span>预计返还 <strong id="betExpectedReturn">0.00</strong> USDT</span>
            </div>
            <div class="bet-message" id="betMessage"></div>
            <button class="bet-submit" id="betSubmitBtn" type="button">确认投注</button>
        </div>
    `;

    document.body.appendChild(modal);

    const presets = modal.querySelector('#betPresets');
    presets.innerHTML = BET_PRESET_AMOUNTS.map(amount => (
        `<button type="button" class="bet-preset" data-amount="${amount}">${amount}</button>`
    )).join('');

    modal.addEventListener('click', event => {
        if (event.target.dataset.betClose === 'true') {
            closeBetModal();
        }

        const presetBtn = event.target.closest('.bet-preset');
        if (presetBtn) {
            setBetAmount(presetBtn.dataset.amount);
        }
    });

    modal.querySelector('#betAmountInput').addEventListener('input', updateBetSummary);
    modal.querySelector('#betSubmitBtn').addEventListener('click', submitBetOrder);
}

function openBetModal(oddElement) {
    if (oddElement.classList.contains('closed') || (oddElement.dataset.betStatus && oddElement.dataset.betStatus !== 'OPEN')) {
        return;
    }

    const matchItem = oddElement.closest('.match-item');
    const marketBlock = oddElement.closest('.odds-market');
    const teams = Array.from(matchItem.querySelectorAll('.team')).map(team => team.textContent.trim());
    const score = matchItem.querySelector('.score')?.textContent.trim() || 'VS';
    const matchTime = matchItem.querySelector('.match-time')?.textContent.trim() || '';
    const marketName = marketBlock.querySelector('.odds-market-title')?.textContent.trim() || '玩法';
    const optionName = oddElement.dataset.optionName || oddElement.querySelector('.label')?.textContent.trim() || '选项';
    const odds = Number(oddElement.dataset.odds);

    selectedBet = {
        matchMarketOptionId: Number(oddElement.dataset.optionId),
        odds,
        optionName,
        marketName,
        matchName: `${teams[0] || '主队'} ${score} ${teams[1] || '客队'}`,
        matchTime
    };

    const modal = document.getElementById('betModal');
    modal.querySelector('#betOptionName').textContent = optionName;
    modal.querySelector('#betMarketName').textContent = marketName;
    modal.querySelector('#betOdds').textContent = `@${Number.isNaN(odds) ? '-' : odds}`;
    modal.querySelector('#betMatchName').textContent = selectedBet.matchName;
    modal.querySelector('#betMatchTime').textContent = matchTime;
    modal.querySelector('#betAmountInput').value = '';
    modal.querySelector('#betMessage').textContent = '';
    modal.querySelector('#betSubmitBtn').disabled = false;
    modal.querySelector('#betSubmitBtn').textContent = '确认投注';

    updateBetSummary();
    modal.classList.add('active');
}

function closeBetModal() {
    const modal = document.getElementById('betModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

function setBetAmount(amount) {
    const input = document.getElementById('betAmountInput');
    input.value = amount;
    updateBetSummary();
}

function getBetAmount() {
    const input = document.getElementById('betAmountInput');
    const amount = Number(input.value);
    return Number.isNaN(amount) ? 0 : amount;
}

function updateBetSummary() {
    const amount = getBetAmount();
    const odds = Number(selectedBet?.odds);
    const expectedReturn = amount > 0 && odds > 0 ? amount * odds : 0;
    const expectedProfit = Math.max(expectedReturn - amount, 0);

    document.getElementById('betExpectedProfit').textContent = expectedProfit.toFixed(2);
    document.getElementById('betExpectedReturn').textContent = expectedReturn.toFixed(2);
}

async function submitBetOrder() {
    const messageEl = document.getElementById('betMessage');
    const submitBtn = document.getElementById('betSubmitBtn');
    const amount = getBetAmount();

    if (!selectedBet || !Number.isFinite(selectedBet.matchMarketOptionId)) {
        messageEl.textContent = '未选择有效赔率';
        messageEl.className = 'bet-message error';
        return;
    }

    if (amount <= 0) {
        messageEl.textContent = '请输入下注金额';
        messageEl.className = 'bet-message error';
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.textContent = '提交中...';
        messageEl.textContent = '';

        const response = await fetch(BET_ORDER_API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                matchMarketOptionId: selectedBet.matchMarketOptionId,
                amount
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('下单响应错误:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const data = await response.json();
        if (data.code !== 200 && data.code !== 10061) {
            throw new Error(data.message || '下单失败');
        }

        const order = data.data || {};
        messageEl.className = 'bet-message success';
        messageEl.innerHTML = `
            下单成功 ${order.orderNo ? `｜订单号 ${escapeHtml(order.orderNo)}` : ''}
            ${order.expectedReturn != null ? `｜预计返还 ${formatBalance(order.expectedReturn)} USDT` : ''}
        `;
        submitBtn.textContent = '已提交';
        loadDefaultBalance();
    } catch (error) {
        console.error('提交订单失败:', error);
        messageEl.textContent = `下单失败: ${error.message}`;
        messageEl.className = 'bet-message error';
        submitBtn.disabled = false;
        submitBtn.textContent = '确认投注';
    }
}

function formatMatchDate(value) {
    if (!value) {
        return '时间待定';
    }

    const normalizedTime = typeof value === 'string' && value.includes(' ')
        ? value.replace(' ', 'T')
        : value;
    const date = new Date(normalizedTime);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatScore(homeScore, awayScore) {
    return `${homeScore ?? '-'} : ${awayScore ?? '-'}`;
}

function createStatPill(label, homeValue, awayValue) {
    if (homeValue == null && awayValue == null) {
        return '';
    }

    return `
        <div class="result-stat">
            <span class="result-stat-label">${label}</span>
            <span class="result-stat-score">${homeValue ?? '-'}-${awayValue ?? '-'}</span>
        </div>
    `;
}

/**
 * 创建赛果DOM元素
 */
function createResultElement(result) {
    const resultItem = document.createElement('div');
    resultItem.className = 'match-item result-item';
    resultItem.dataset.matchId = result.matchId || '';

    const homeTeam = result.homeTeamName || '主队';
    const awayTeam = result.awayTeamName || '客队';
    const leagueName = result.leagueName || result.matchName || '赛事';
    const endedAt = formatMatchDate(result.matchEndedAt || result.matchStartTime);
    const regularScore = formatScore(result.regularHomeScore, result.regularAwayScore);
    const statusText = result.matchStatus || '已完赛';

    const extraScore = createStatPill('加时', result.extraHomeScore, result.extraAwayScore);
    const penaltyScore = createStatPill('点球', result.penaltyHomeScore, result.penaltyAwayScore);
    const corners = createStatPill('角球', result.homeCornerCount, result.awayCornerCount);
    const redCards = createStatPill('红牌', result.homeRedCardCount, result.awayRedCardCount);
    const yellowCards = createStatPill('黄牌', result.homeYellowCardCount, result.awayYellowCardCount);

    resultItem.innerHTML = `
        <div class="result-meta">
            <span class="result-league">${leagueName}</span>
            <span class="result-status">${statusText}</span>
        </div>
        <div class="match-time">结束时间 ${endedAt}</div>
        <div class="result-scoreboard">
            <div class="result-team result-team-home">${homeTeam}</div>
            <div class="result-score">${regularScore}</div>
            <div class="result-team result-team-away">${awayTeam}</div>
        </div>
        <div class="result-stats">
            ${extraScore}
            ${penaltyScore}
            ${corners}
            ${redCards}
            ${yellowCards}
        </div>
    `;

    return resultItem;
}

// 主导航标签切换
navTabs.forEach(tab => {
    tab.addEventListener('click', () => {
        navTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        
        currentTab = tab.dataset.tab;
        currentPageIndex = 1;
        if (statusFilter) {
            statusFilter.textContent = currentTab === 'results' ? '赛果' : '进行中';
        }
        
        console.log('切换到:', currentTab);
        loadMatches();
    });
});

// 底部菜单项切换
footerItems.forEach(item => {
    item.addEventListener('click', () => {
        const text = item.querySelector('span:last-child').textContent;
        console.log('点击:', text);
        
        // 处理各个菜单项
        switch(text) {
            case '盘口教程':
                alert('盘口教程页面（待开发）');
                break;
            case '设置菜单':
                alert('设置页面（待开发）');
                break;
            case '未结注单':
                alert('未结注单列表（待开发）');
                break;
            case '已结注单':
                alert('已结注单列表（待开发）');
                break;
            case '刷新':
                loadDefaultBalance();
                currentPageIndex = 1;
                loadMatches();
                break;
        }
    });
});

// 返回按钮
if (backBtn) {
    backBtn.addEventListener('click', () => {
        window.history.back();
    });
}

// 加载更多按钮
const loadMoreBtn = document.getElementById('loadMoreBtn');
if (loadMoreBtn) {
    loadMoreBtn.addEventListener('click', () => {
        currentPageIndex++;
        loadMatches();
    });
}

if (matchesList) {
    matchesList.addEventListener('click', event => {
        const oddElement = event.target.closest('.odd');
        if (!oddElement || !matchesList.contains(oddElement)) {
            return;
        }

        openBetModal(oddElement);
    });
}

// 页面加载时初始化
window.addEventListener('load', () => {
    console.log('页面加载中...');
    
    // 检查认证状态
    if (!checkAuth()) {
        console.error('需要先登陆');
        return;
    }

    // 获取语言设置（可选）
    const savedLangCode = localStorage.getItem('langCode');
    if (savedLangCode) {
        currentLangCode = savedLangCode;
    }

    console.log('使用语言编码:', currentLangCode);
    console.log('使用token:', localStorage.getItem('authToken') ? '已设置' : '未设置');

    initBetModal();

    // 加载用户余额
    loadDefaultBalance();

    // 加载联盟列表
    loadLeagues();

    // 加载赛事列表
    loadMatches();
});
