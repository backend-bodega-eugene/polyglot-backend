// ========== 首页交互逻辑 ==========

// API 基础配置
const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const RESULT_API_URL = `${API_BASE_URL}/api/soccer/matches/results/page`;
const BET_ORDER_API_URL = `${API_BASE_URL}/api/order/bet/orders/place`;
const FOLLOW_API_URL = `${API_BASE_URL}/api/soccer/follow`;
const DEFAULT_LANG_CODE = 'en-US';
const PAGE_SIZE = 10;
const BET_PRESET_AMOUNTS = [10, 50, 100, 500, 1000];
const BALANCE_CACHE_KEY = 'defaultBalanceText';
const { apiFetch, refreshBalance, renderCachedBalance } = window.GoalHubApp;

// DOM 元素
const navTabs = document.querySelectorAll('.nav-tab');
const leaguesList = document.getElementById('leaguesList');
const matchesList = document.getElementById('matchesList');
const statusFilter = document.querySelector('.status-filter');
const balanceEl = document.querySelector('.balance');
const sportsNav = document.querySelector('.sports-nav');

// 当前状态
let currentTab = 'today';
let currentLeagueId = null;
let currentLangCode = DEFAULT_LANG_CODE;
let currentPageIndex = 1;
let selectedBet = null;
let showingFollowedMatches = false;
const followedMatchIds = new Set();

/**
 * 兼容分页接口和普通数组接口
 */
function getRecords(payload) {
    if (Array.isArray(payload?.data?.records)) {
        return payload.data.records;
    }

    if (Array.isArray(payload?.data?.list)) {
        return payload.data.list;
    }

    if (Array.isArray(payload?.data)) {
        return payload.data;
    }

    if (Array.isArray(payload?.records)) {
        return payload.records;
    }

    return [];
}

function getMatchId(match) {
    return match?.matchId || match?.id || match?.soccerMatchId || match?.footballMatchId || '';
}

function normalizeFollowRecord(record) {
    return record?.match || record?.soccerMatch || record?.footballMatch || record?.matchInfo || record;
}

function getFollowRecordMatchId(record, match) {
    return getMatchId(match) || record?.matchId || record?.soccerMatchId || record?.footballMatchId || '';
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
        const data = await apiFetch(url);
        const availableBalance = data?.data?.availableBalance;
        const balanceText = `💰 ${formatBalance(availableBalance)}`;
        balanceEl.textContent = balanceText;
        localStorage.setItem(BALANCE_CACHE_KEY, balanceText);
    } catch (error) {
        renderCachedBalance();
        if (!balanceEl.textContent.trim()) {
            balanceEl.textContent = '💰 0.00';
        }
    }
}

/**
 * 检查认证状态
 */
function checkAuth() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        leaguesList.innerHTML = '<div style="padding: 10px; color: #e74c3c;">请先<a href="/login.html" style="color: #2196F3;">登录</a>继续</div>';
        matchesList.innerHTML = '<div style="padding: 10px; color: #e74c3c;">请先<a href="/login.html" style="color: #2196F3;">登录</a>继续</div>';
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
        const data = await apiFetch(url);

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
    showingFollowedMatches = false;
    document.querySelectorAll('.sports-nav .nav-item').forEach(navItem => navItem.classList.remove('active'));

    // 重新加载对应联盟的赛事
    currentPageIndex = 1;
    loadMatches();
}

function setLoadMoreVisible(visible) {
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (loadMoreBtn) {
        loadMoreBtn.style.display = visible ? 'block' : 'none';
    }
}

async function loadFollowedMatches() {
    try {
        showingFollowedMatches = true;
        currentPageIndex = 1;
        if (statusFilter) {
            statusFilter.textContent = '关注';
        }
        setLoadMoreVisible(false);
        matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">关注赛事加载中...</div>';

        const data = await apiFetch(`${FOLLOW_API_URL}/my`);
        const records = getRecords(data);
        followedMatchIds.clear();
        matchesList.innerHTML = '';

        if (!records.length) {
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无关注赛事</div>';
            return;
        }

        records.forEach(record => {
            const match = normalizeFollowRecord(record);
            const matchId = getFollowRecordMatchId(record, match);
            if (matchId) {
                followedMatchIds.add(String(matchId));
            }

            const matchItem = createMatchElement(match, matchId);
            matchesList.appendChild(matchItem);
            loadMatchOdds(match, matchItem);
        });
    } catch (error) {
        matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载关注赛事失败: ${error.message}</div>`;
    }
}

/**
 * 加载赛事列表
 */
async function loadMatches() {
    try {
        showingFollowedMatches = false;
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

        const data = await apiFetch(url);
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
            if (currentTab !== 'hot' && records.length === PAGE_SIZE) {
                setLoadMoreVisible(true);
            } else {
                setLoadMoreVisible(false);
            }
        } else {
            setLoadMoreVisible(false);
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无赛事</div>';
        }
    } catch (error) {
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

        const data = await apiFetch(RESULT_API_URL, {
            method: 'POST',
            body: JSON.stringify(requestBody)
        });
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
        matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载赛果失败: ${error.message}</div>`;
    }
}

/**
 * 创建赛事DOM元素
 */
function createMatchElement(match, fallbackMatchId = '') {
    const matchItem = document.createElement('div');
    matchItem.className = 'match-item';
    const matchId = getMatchId(match) || fallbackMatchId;
    matchItem.dataset.matchId = matchId || '';
    const isFollowed = followedMatchIds.has(String(matchId));

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
        <button class="follow-match-btn${isFollowed ? ' followed' : ''}" type="button" data-match-id="${escapeHtml(matchId)}" aria-label="${isFollowed ? '取消关注赛事' : '关注赛事'}" title="${isFollowed ? '取消关注' : '关注'}">
            <span class="follow-star">${isFollowed ? '★' : '☆'}</span>
            <span class="follow-text">${isFollowed ? '已关注' : '关注'}</span>
        </button>
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
    const matchId = matchItem?.dataset?.matchId || getMatchId(match);
    const oddsContainer = matchItem.querySelector('.odds-options');

    if (!matchId) {
        oddsContainer.innerHTML = '<div class="odds-empty">暂无赛事ID</div>';
        return;
    }

    try {
        const url = `${API_BASE_URL}/api/soccer/matches/${matchId}/odds`;
        const data = await apiFetch(url);
        const markets = Array.isArray(data?.data?.markets) ? data.data.markets : [];
        renderMatchOdds(oddsContainer, markets);
    } catch (error) {
        oddsContainer.innerHTML = '<div class="odds-empty odds-error">赔率加载失败</div>';
    }
}

async function toggleFollowMatch(button) {
    const matchId = button.dataset.matchId;
    if (!matchId) {
        return;
    }

    const isFollowed = followedMatchIds.has(String(matchId)) || button.classList.contains('followed');
    const method = isFollowed ? 'DELETE' : 'POST';

    try {
        button.disabled = true;
        await apiFetch(`${FOLLOW_API_URL}/${encodeURIComponent(matchId)}`, { method });

        const nextFollowed = !isFollowed;
        updateFollowButton(button, nextFollowed);
        if (nextFollowed) {
            followedMatchIds.add(String(matchId));
        } else {
            followedMatchIds.delete(String(matchId));
            if (showingFollowedMatches) {
                button.closest('.match-item')?.remove();
                if (!matchesList.querySelector('.match-item')) {
                    matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无关注赛事</div>';
                }
            }
        }
    } catch (error) {
        alert(`关注操作失败: ${error.message}`);
    } finally {
        button.disabled = false;
    }
}

function updateFollowButton(button, isFollowed) {
    button.classList.toggle('followed', isFollowed);
    button.title = isFollowed ? '取消关注' : '关注';
    button.setAttribute('aria-label', isFollowed ? '取消关注赛事' : '关注赛事');
    button.querySelector('.follow-star').textContent = isFollowed ? '★' : '☆';
    button.querySelector('.follow-text').textContent = isFollowed ? '已关注' : '关注';
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

        const data = await apiFetch(BET_ORDER_API_URL, {
            method: 'POST',
            body: JSON.stringify({
                matchMarketOptionId: selectedBet.matchMarketOptionId,
                amount
            })
        });
        if (data.code !== 0 && data.code !== 200 && data.code !== '0' && data.code !== '200') {
            throw new Error(data.message || '下单失败');
        }

        const order = data.data || {};
        messageEl.className = 'bet-message success';
        messageEl.innerHTML = `
            下单成功 ${order.orderNo ? `｜订单号 ${escapeHtml(order.orderNo)}` : ''}
            ${order.expectedReturn != null ? `｜预计返还 ${formatBalance(order.expectedReturn)} USDT` : ''}
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
        document.querySelectorAll('.sports-nav .nav-item').forEach(navItem => navItem.classList.remove('active'));
        showingFollowedMatches = false;
        
        currentTab = tab.dataset.tab;
        currentPageIndex = 1;
        if (statusFilter) {
            statusFilter.textContent = currentTab === 'results' ? '赛果' : '进行中';
        }
        
        loadMatches();
    });
});

if (sportsNav) {
    sportsNav.addEventListener('click', event => {
        const item = event.target.closest('.nav-item');
        if (!item || !sportsNav.contains(item)) {
            return;
        }

        if (item.dataset.action === 'followed') {
            document.querySelectorAll('.sports-nav .nav-item').forEach(navItem => navItem.classList.remove('active'));
            item.classList.add('active');
            loadFollowedMatches();
            return;
        }

        if (item.dataset.action === 'hot') {
            document.querySelectorAll('.sports-nav .nav-item').forEach(navItem => navItem.classList.remove('active'));
            navTabs.forEach(tab => tab.classList.remove('active'));
            item.classList.add('active');
            currentTab = 'hot';
            currentPageIndex = 1;
            showingFollowedMatches = false;
            if (statusFilter) {
                statusFilter.textContent = '热门';
            }
            loadMatches();
        }
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
        const followButton = event.target.closest('.follow-match-btn');
        if (followButton && matchesList.contains(followButton)) {
            event.preventDefault();
            event.stopPropagation();
            toggleFollowMatch(followButton);
            return;
        }

        const oddElement = event.target.closest('.odd');
        if (!oddElement || !matchesList.contains(oddElement)) {
            return;
        }

        openBetModal(oddElement);
    });
}

// 页面加载时初始化
window.addEventListener('load', () => {
    renderCachedBalance();
    
    // 检查认证状态
    if (!checkAuth()) {
        return;
    }

    // 获取语言设置（可选）
    const savedLangCode = localStorage.getItem('langCode');
    if (savedLangCode) {
        currentLangCode = savedLangCode;
    }

    initBetModal();

    // 加载联盟列表
    loadLeagues();

    // 加载赛事列表
    loadMatches();
});
