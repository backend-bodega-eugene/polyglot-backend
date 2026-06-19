// ========== 首页交互逻辑 ==========

// API 基础配置
const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const RESULT_API_URL = `${API_BASE_URL}/api/soccer/matches/results/page`;
const BET_ORDER_API_URL = `${API_BASE_URL}/api/order/bet/orders/place`;
const CHAMPION_PAGE_API_URL = `${API_BASE_URL}/api/soccer/champion/page`;
const CHAMPION_BET_ORDER_API_URL = `${API_BASE_URL}/api/order/bet/orders/placechampion`;
const LEAGUES_API_URL = `${API_BASE_URL}/api/soccer/leagues`;
const FOLLOW_API_URL = `${API_BASE_URL}/api/soccer/follow`;
const MATCH_MARKET_API_URLS = {
    today: `${API_BASE_URL}/api/soccer/matchmarkets/today`,
    ball: `${API_BASE_URL}/api/soccer/matchmarkets/live`,
    early: `${API_BASE_URL}/api/soccer/matchmarkets/early`,
    combo: `${API_BASE_URL}/api/soccer/matchmarkets/parlay`
};
const DEFAULT_LANG_CODE = 'zh-CN';
const PAGE_SIZE = 100;
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
const matchKeywordInput = document.getElementById('matchKeywordInput');

// 当前状态
let currentTab = 'today';
let currentLeagueId = null;
let currentLangCode = DEFAULT_LANG_CODE;
let currentPageIndex = 1;
let selectedBet = null;
let showingFollowedMatches = false;
let matchSearchTimer = null;
let currentLeagueName = '';
let championTotalRecords = 0;
let championActiveRequestId = 0;
let championLoading = false;
const followedMatchIds = new Set();
const availableLeagueOptions = new Map();

/**
 * 兼容分页接口和普通数组接口
 */
function getRecords(payload) {
    if (Array.isArray(payload)) {
        return payload;
    }

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

function getCurrentTabLabel() {
    const labels = {
        today: '今日',
        ball: '滚球',
        early: '早盘',
        combo: '串关',
        champion: '冠军',
        results: '赛果'
    };

    return labels[currentTab] || '赛事';
}

function getMatchId(match) {
    return match?.matchId || match?.id || match?.soccerMatchId || match?.footballMatchId || '';
}

function getTotal(payload, records) {
    const total = Number(payload?.data?.total ?? payload?.total);
    return Number.isFinite(total) ? total : records.length;
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

function formatOdds(value) {
    const odds = Number(value);
    return Number.isFinite(odds) ? odds.toFixed(2).replace(/\.00$/, '') : '-';
}

function isChampionBetOpen(record) {
    return !record.betStatus || record.betStatus === 'OPEN';
}

function buildChampionLogo(url, fallbackText, className) {
    if (url) {
        return `<img class="${className}" src="${escapeHtml(url)}" alt="${escapeHtml(fallbackText)}" loading="lazy">`;
    }

    return `<span class="${className} champion-logo-fallback">${escapeHtml(String(fallbackText || '?').slice(0, 1))}</span>`;
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
    currentLeagueId = leagueId || null;
    currentLeagueName = element?.dataset?.leagueName || element?.textContent?.trim() || '';
    showingFollowedMatches = false;
    document.querySelectorAll('.sports-nav .nav-item').forEach(navItem => navItem.classList.remove('active'));

    // 重新加载对应联盟的赛事
    currentPageIndex = 1;
    if (currentTab === 'champion') {
        loadChampionOdds();
    } else {
        loadMatches();
    }
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
        if (!MATCH_MARKET_API_URLS[currentTab]) {
            currentTab = 'today';
            navTabs.forEach(tab => tab.classList.toggle('active', tab.dataset.tab === currentTab));
        }
        if (statusFilter) {
            statusFilter.textContent = '关注';
        }
        setLoadMoreVisible(false);
        matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">关注赛事加载中...</div>';

        const data = await apiFetch(`${FOLLOW_API_URL}/my`);
        const records = getRecords(data);
        followedMatchIds.clear();
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
        });

        const groups = await fetchMatchMarketGroups(1);
        const followedGroups = groups.map(group => ({
            ...group,
            matches: (Array.isArray(group.matches) ? group.matches : []).filter(match => followedMatchIds.has(String(getMatchId(match))))
        })).filter(group => group.matches.length > 0);

        if (!followedGroups.length) {
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">当前分类暂无关注赛事</div>';
            return;
        }

        renderLeagueFilter(groups);
        renderMatchMarketGroups(followedGroups);
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
        if (currentTab === 'champion') {
            await loadChampionView();
            return;
        }

        if (currentTab === 'results') {
            await loadMatchResults();
            return;
        }

        if (!MATCH_MARKET_API_URLS[currentTab]) {
            setLoadMoreVisible(false);
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">该分类暂未接入聚合赛事接口</div>';
            return;
        }

        if (statusFilter) {
            statusFilter.textContent = getCurrentTabLabel();
        }

        setLoadMoreVisible(false);
        matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">赛事赔率加载中...</div>';
        const groups = await fetchMatchMarketGroups(currentPageIndex);
        renderLeagueFilter(groups);

        if (groups.length > 0 && groups.some(group => Array.isArray(group.matches) && group.matches.length > 0)) {
            renderMatchMarketGroups(groups);
        } else {
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无赛事</div>';
        }
    } catch (error) {
        matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载赛事失败: ${error.message}</div>`;
    }
}

async function loadChampionView() {
    if (statusFilter) {
        statusFilter.textContent = '冠军';
    }

    setLoadMoreVisible(false);
    matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">冠军赔率加载中...</div>';
    await loadChampionLeagues();
}

function buildMatchMarketRequest(pageIndex) {
    const keyword = matchKeywordInput?.value.trim() || '';
    return {
        pageIndex,
        pageSize: PAGE_SIZE,
        leagueId: currentLeagueId,
        keyword,
        langCode: currentLangCode
    };
}

async function fetchMatchMarketGroups(pageIndex) {
    const data = await apiFetch(MATCH_MARKET_API_URLS[currentTab], {
        method: 'POST',
        body: JSON.stringify(buildMatchMarketRequest(pageIndex))
    });

    return getRecords(data);
}

function renderLeagueFilter(groups) {
    if (!currentLeagueId) {
        availableLeagueOptions.clear();
        groups.forEach(group => {
            const leagueId = group.leagueId;
            if (leagueId !== undefined && leagueId !== null && !availableLeagueOptions.has(String(leagueId))) {
                availableLeagueOptions.set(String(leagueId), {
                    leagueId,
                    leagueName: group.leagueName || '未知'
                });
            }
        });
    } else if (!availableLeagueOptions.size) {
        groups.forEach(group => {
            const leagueId = group.leagueId;
            if (leagueId !== undefined && leagueId !== null && !availableLeagueOptions.has(String(leagueId))) {
                availableLeagueOptions.set(String(leagueId), {
                    leagueId,
                    leagueName: group.leagueName || '未知'
                });
            }
        });
    }

    leaguesList.innerHTML = '';
    const allLeagueItem = document.createElement('div');
    allLeagueItem.className = `league-item${currentLeagueId ? '' : ' active'}`;
    allLeagueItem.textContent = '全部';
    allLeagueItem.dataset.leagueId = '';
    allLeagueItem.addEventListener('click', () => selectLeague(null, allLeagueItem));
    leaguesList.appendChild(allLeagueItem);

    availableLeagueOptions.forEach(league => {
        const leagueItem = document.createElement('div');
        const active = currentLeagueId && String(currentLeagueId) === String(league.leagueId);
        leagueItem.className = `league-item${active ? ' active' : ''}`;
        leagueItem.textContent = league.leagueName;
        leagueItem.dataset.leagueId = league.leagueId;
        leagueItem.dataset.leagueName = league.leagueName;
        leagueItem.addEventListener('click', () => selectLeague(league.leagueId, leagueItem));
        leaguesList.appendChild(leagueItem);
    });
}

async function loadChampionLeagues() {
    leaguesList.innerHTML = '<div class="league-item">加载中...</div>';

    try {
        const data = await apiFetch(`${LEAGUES_API_URL}?langCode=${encodeURIComponent(currentLangCode)}`);
        const leagues = getRecords(data);

        if (!leagues.length) {
            leaguesList.innerHTML = '<div class="league-item">暂无联赛</div>';
            matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无冠军赔率</div>';
            return;
        }

        leaguesList.innerHTML = '';
        const allLeagueItem = document.createElement('div');
        allLeagueItem.className = `league-item${currentLeagueId ? '' : ' active'}`;
        allLeagueItem.textContent = '全部';
        allLeagueItem.dataset.leagueId = '';
        allLeagueItem.dataset.leagueName = '全部';
        allLeagueItem.addEventListener('click', () => selectLeague(null, allLeagueItem));
        leaguesList.appendChild(allLeagueItem);

        leagues.forEach(league => {
            const leagueId = league.id ?? league.leagueId;
            const leagueName = league.name || league.leagueName || '未知联赛';
            const leagueItem = document.createElement('div');
            const active = currentLeagueId && String(currentLeagueId) === String(leagueId);
            leagueItem.className = `league-item${active ? ' active' : ''}`;
            leagueItem.textContent = leagueName;
            leagueItem.dataset.leagueId = leagueId;
            leagueItem.dataset.leagueName = leagueName;
            leagueItem.addEventListener('click', () => selectLeague(leagueId, leagueItem));
            leaguesList.appendChild(leagueItem);
        });

        await loadChampionOdds();
    } catch (error) {
        leaguesList.innerHTML = `<div class="league-item">加载失败</div>`;
        matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">加载冠军联赛失败: ${error.message}</div>`;
    }
}

function buildChampionRequestBody(pageIndex) {
    const keyword = matchKeywordInput?.value.trim() || '';
    const body = {
        pageIndex,
        pageSize: PAGE_SIZE,
        leagueId: currentLeagueId,
        keyword,
        langCode: currentLangCode
    };
    Object.keys(body).forEach(key => {
        if (body[key] === '' || body[key] == null) {
            delete body[key];
        }
    });
    return body;
}

async function loadChampionOdds({ append = false } = {}) {
    if (championLoading) {
        return;
    }

    const requestId = championActiveRequestId + 1;
    championActiveRequestId = requestId;
    championLoading = true;
    const pageIndex = append ? currentPageIndex : 1;

    if (!append) {
        matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">冠军赔率加载中...</div>';
    }

    try {
        const data = await apiFetch(CHAMPION_PAGE_API_URL, {
            method: 'POST',
            body: JSON.stringify(buildChampionRequestBody(pageIndex))
        });

        if (requestId !== championActiveRequestId) {
            return;
        }

        const records = getRecords(data);
        championTotalRecords = getTotal(data, records);
        currentPageIndex = pageIndex + 1;
        renderChampionRecords(records, append);
    } catch (error) {
        if (requestId === championActiveRequestId) {
            matchesList.innerHTML = `<div style="padding: 10px; color: #e74c3c;">查询冠军赔率失败: ${error.message}</div>`;
            setLoadMoreVisible(false);
        }
    } finally {
        if (requestId === championActiveRequestId) {
            championLoading = false;
        }
    }
}

function groupChampionByLeague(records) {
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
        matchesList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">暂无冠军赔率</div>';
        setLoadMoreVisible(false);
        return;
    }

    const groupedHtml = Array.from(groupChampionByLeague(records).values()).map(createChampionLeagueGroupHtml).join('');
    if (append) {
        matchesList.insertAdjacentHTML('beforeend', groupedHtml);
    } else {
        matchesList.innerHTML = groupedHtml;
    }

    const totalPages = Math.ceil(championTotalRecords / PAGE_SIZE);
    setLoadMoreVisible(records.length === PAGE_SIZE && currentPageIndex <= totalPages);
}

function createChampionLeagueGroupHtml(group) {
    return `
        <article class="champion-group">
            <div class="champion-group-head">
                ${buildChampionLogo(group.leagueLogoUrl, group.leagueName, 'champion-league-logo')}
                <div>
                    <div class="champion-group-title">${escapeHtml(group.leagueName)}</div>
                    <div class="champion-group-subtitle">冠军赔率</div>
                </div>
            </div>
            <div class="champion-team-list">${group.records.map(createChampionTeamHtml).join('')}</div>
        </article>
    `;
}

function createChampionTeamHtml(record) {
    const closed = !isChampionBetOpen(record);
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
                ${buildChampionLogo(record.teamLogoUrl, record.teamName || record.teamCode, 'champion-team-logo')}
                <span class="champion-team-name">${escapeHtml(record.teamName || record.teamCode || '未知队伍')}</span>
            </span>
            <span class="champion-odds-box">
                <span class="champion-odds">${escapeHtml(formatOdds(record.odds))}</span>
                ${statusText}
            </span>
        </button>
    `;
}

function renderMatchMarketGroups(groups) {
    matchesList.innerHTML = '';
    groups.forEach(group => {
        const matches = Array.isArray(group.matches) ? group.matches : [];
        if (!matches.length) {
            return;
        }

        const leagueBlock = document.createElement('div');
        leagueBlock.className = 'match-league-group';
        leagueBlock.innerHTML = `
            <div class="match-league-title">
                ${group.leagueLogoUrl ? `<img class="match-league-logo" src="${escapeHtml(group.leagueLogoUrl)}" alt="${escapeHtml(group.leagueName || '联赛')}" loading="lazy">` : ''}
                <span>${escapeHtml(group.leagueName || '未知联赛')}</span>
            </div>
        `;

        matches.forEach(match => {
            const matchItem = createMatchElement({
                ...match,
                leagueName: match.leagueName || group.leagueName,
                leagueLogoUrl: match.leagueLogoUrl || group.leagueLogoUrl
            });
            renderMatchOdds(matchItem.querySelector('.odds-options'), Array.isArray(match.markets) ? match.markets : []);
            leagueBlock.appendChild(matchItem);
        });

        matchesList.appendChild(leagueBlock);
    });
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
    const matchStatus = match.matchStatus || match.status || '';
    const matchContext = match.leagueName || matchStatus || '';
    const score = match.score || 'VS';

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
        <div class="match-time">${escapeHtml(timeText)}${matchContext ? ` · ${escapeHtml(matchContext)}` : ''}</div>
        <div class="match-teams">
            <div class="team">${escapeHtml(homeTeam)}</div>
            <div class="score">${escapeHtml(score)}</div>
            <div class="team">${escapeHtml(awayTeam)}</div>
        </div>
        <div class="odds-options" data-odds-for="${matchId || ''}">
            <div class="odds-empty">暂无玩法赔率</div>
        </div>
    `;

    return matchItem;
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
    const optionId = option.id ?? option.matchMarketOptionId ?? '';

    return `
        <div class="odd${statusClass}" data-option-id="${escapeHtml(optionId)}" data-market-option-id="${escapeHtml(option.marketOptionId ?? '')}" data-option-name="${escapeHtml(optionName)}" data-odds="${escapeHtml(oddsText)}" data-bet-status="${escapeHtml(option.betStatus || '')}">
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

function openChampionBetModal(championElement) {
    if (championElement.classList.contains('closed') || (championElement.dataset.betStatus && championElement.dataset.betStatus !== 'OPEN')) {
        return;
    }

    const odds = Number(championElement.dataset.odds);
    selectedBet = {
        betType: 'CHAMPION',
        championOddsId: Number(championElement.dataset.championOddsId),
        odds,
        optionName: championElement.dataset.teamName || '-',
        marketName: '冠军',
        matchName: championElement.dataset.leagueName || '冠军玩法',
        matchTime: ''
    };

    const modal = document.getElementById('betModal');
    modal.querySelector('#betOptionName').textContent = selectedBet.optionName;
    modal.querySelector('#betMarketName').textContent = selectedBet.marketName;
    modal.querySelector('#betOdds').textContent = `@${Number.isNaN(odds) ? '-' : formatOdds(odds)}`;
    modal.querySelector('#betMatchName').textContent = selectedBet.matchName;
    modal.querySelector('#betMatchTime').textContent = '';
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

    const isChampionBet = selectedBet?.betType === 'CHAMPION';
    const hasValidOdds = isChampionBet
        ? Number.isFinite(selectedBet?.championOddsId) && selectedBet.championOddsId > 0
        : Number.isFinite(selectedBet?.matchMarketOptionId) && selectedBet.matchMarketOptionId > 0;

    if (!selectedBet || !hasValidOdds) {
        messageEl.textContent = isChampionBet ? '未选择有效冠军赔率' : '未选择有效赔率';
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

        const data = await apiFetch(isChampionBet ? CHAMPION_BET_ORDER_API_URL : BET_ORDER_API_URL, {
            method: 'POST',
            body: JSON.stringify(isChampionBet
                ? { championOddsId: selectedBet.championOddsId, amount }
                : { matchMarketOptionId: selectedBet.matchMarketOptionId, amount })
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
        currentLeagueId = null;
        availableLeagueOptions.clear();
        if (statusFilter) {
            statusFilter.textContent = getCurrentTabLabel();
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
            currentLeagueId = null;
            availableLeagueOptions.clear();
            showingFollowedMatches = false;
            if (statusFilter) {
                statusFilter.textContent = '热门';
            }
            loadMatches();
        }
    });
}

if (matchKeywordInput) {
    matchKeywordInput.addEventListener('input', () => {
        window.clearTimeout(matchSearchTimer);
        matchSearchTimer = window.setTimeout(() => {
            currentPageIndex = 1;
            loadMatches();
        }, 300);
    });

    matchKeywordInput.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            window.clearTimeout(matchSearchTimer);
            currentPageIndex = 1;
            loadMatches();
        }
    });
}

// 加载更多按钮
const loadMoreBtn = document.getElementById('loadMoreBtn');
if (loadMoreBtn) {
    loadMoreBtn.addEventListener('click', () => {
        if (currentTab === 'champion') {
            loadChampionOdds({ append: true });
            return;
        }

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
        if (oddElement && matchesList.contains(oddElement)) {
            openBetModal(oddElement);
            return;
        }

        const championElement = event.target.closest('.champion-team-row');
        if (championElement && matchesList.contains(championElement)) {
            openChampionBetModal(championElement);
        }
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

    // 加载赛事列表
    loadMatches();
});
