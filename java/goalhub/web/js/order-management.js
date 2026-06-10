(function () {
  const auth = window.GoalHubAuth;
  const config = window.GoalHubOrderConfig || {};
  if (!auth) return;

  const state = { pageIndex: 1, pageSize: 10, total: 0, list: [], currentOrder: null, action: null };
  const ORDER_COLSPAN = 25;
  const $ = (id) => document.getElementById(id);
  const esc = (s) => (s == null ? '' : String(s)).replace(/[&<>"']/g, (m) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
  const ok = (data) => data && (data.code === 200 || data.code === 0 || data.success === true);
  const msg = (data, fallback) => data?.message || data?.msg || fallback;

  const actionModal = $('action-modal') ? new bootstrap.Modal($('action-modal')) : null;
  const detailModal = new bootstrap.Modal($('detail-modal'));

  function unwrapPage(data) {
    if (Array.isArray(data?.data)) return { list: data.data, total: data.data.length, pageIndex: 1, pageSize: state.pageSize };
    if (Array.isArray(data)) return { list: data, total: data.length, pageIndex: 1, pageSize: state.pageSize };
    const page = data?.data || data || {};
    const records = page.records || page.list || page.rows || page.data || [];
    return {
      list: Array.isArray(records) ? records : [],
      total: Math.max(Number(page.total || page.totalCount || page.count || 0), Array.isArray(records) ? records.length : 0),
      pageIndex: Number(page.pageIndex || page.current || page.page || state.pageIndex),
      pageSize: Number(page.pageSize || page.size || state.pageSize),
    };
  }

  async function request(url, body) {
    const res = await auth.authFetch(url, { method: 'POST', body: JSON.stringify(body || {}) });
    const text = await res.text();
    const data = text ? JSON.parse(text) : {};
    if (!res.ok || (text && !ok(data))) throw new Error(msg(data, '请求失败'));
    return data;
  }

  function pick(row, keys, fallback = '-') {
    for (const key of keys) {
      if (row?.[key] != null && row[key] !== '') return row[key];
    }
    return fallback;
  }

  function money(value) {
    if (value == null || value === '') return '-';
    const n = Number(value);
    return Number.isFinite(n) ? n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : String(value);
  }

  function time(value) {
    if (!value) return '-';
    const date = new Date(String(value));
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  }

  function toIso(value) {
    if (!value) return '';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '' : date.toISOString();
  }

  function clampPageIndex() {
    const pages = Math.max(1, Math.ceil(state.total / state.pageSize));
    state.pageIndex = Math.min(Math.max(1, Number(state.pageIndex) || 1), pages);
    return pages;
  }

  function badge(value) {
    const labels = {
      PENDING: '待判定',
      REVIEWING: '审核中',
      WIN: '赢',
      LOSE: '输',
      CANCELLED: '取消',
      FROZEN: '冻结',
      REFUNDED: '退款',
      SETTLED: '已结算',
    };
    const raw = String(value || '-');
    const text = labels[raw] || raw;
    const cls = /win|success|settled|complete|approved|通过|赢/i.test(text)
      ? 'bg-success'
      : /lose|fail|reject|cancel|frozen|closed|拒绝|输|冻结/i.test(text)
        ? 'bg-secondary'
        : /pending|review|open|待|审核/i.test(text)
          ? 'bg-warning text-dark'
          : 'bg-info';
    return `<span class="badge ${cls}">${esc(text)}</span>`;
  }

  function orderCell(row, keys, type = 'text') {
    const value = pick(row, keys, '');
    if (type === 'money') return money(value);
    if (type === 'time') return esc(time(value));
    if (type === 'badge') return badge(value);
    return esc(value === '' ? '-' : value);
  }

  function readFilters() {
    const num = (id) => {
      const value = $(id)?.value.trim();
      return value ? Number(value) : '';
    };
    return {
      orderNo: $('f-orderNo')?.value.trim(),
      userId: num('f-userId'),
      currencyCode: $('f-currencyCode')?.value.trim(),
      status: $('f-status')?.value.trim(),
      systemResult: $('f-systemResult')?.value.trim(),
      reviewResult: $('f-reviewResult')?.value.trim(),
      createdStartTime: toIso($('f-createdStartTime')?.value),
      createdEndTime: toIso($('f-createdEndTime')?.value),
    };
  }

  function pageBody() {
    const body = { ...readFilters(), pageIndex: state.pageIndex, pageSize: state.pageSize };
    Object.keys(body).forEach((key) => {
      if (body[key] === '' || body[key] == null) delete body[key];
    });
    return body;
  }

  function renderRows() {
    const tbody = $('orders-tbody');
    if (!state.list.length) {
      tbody.innerHTML = `<tr><td colspan="${ORDER_COLSPAN}" class="text-center py-4 text-muted">暂无数据</td></tr>`;
      return;
    }
    tbody.innerHTML = state.list.map((row) => {
      const id = pick(row, ['id', 'orderId'], '');
      const orderNo = pick(row, ['orderNo', 'orderNumber'], '');
      const actions = config.history ? `
        <button class="btn btn-outline-primary btn-sm" data-op="detail">明细</button>
      ` : `
        <button class="btn btn-outline-primary btn-sm me-1" data-op="detail">明细</button>
        <button class="btn btn-outline-success btn-sm me-1" data-op="settle">结算</button>
        <button class="btn btn-outline-warning btn-sm me-1" data-op="review">审核</button>
        <button class="btn btn-outline-secondary btn-sm" data-op="freeze">冻结</button>
      `;
      return `
        <tr data-id="${esc(id)}" data-order-no="${esc(orderNo)}">
          <td>${orderCell(row, ['id', 'orderId'])}</td>
          <td>${orderCell(row, ['orderNo', 'orderNumber'])}</td>
          <td>${orderCell(row, ['userId', 'appUserId'])}</td>
          <td>${orderCell(row, ['accountId'])}</td>
          <td>${orderCell(row, ['status'], 'badge')}</td>
          <td>${orderCell(row, ['totalBetAmount', 'betAmount', 'stakeAmount', 'orderAmount', 'amount', 'totalAmount'], 'money')}</td>
          <td>${orderCell(row, ['totalExpectedProfit'], 'money')}</td>
          <td>${orderCell(row, ['totalExpectedReturn'], 'money')}</td>
          <td>${orderCell(row, ['currencyCode', 'currency'])}</td>
          <td>${orderCell(row, ['balanceBefore'], 'money')}</td>
          <td>${orderCell(row, ['balanceAfter'], 'money')}</td>
          <td>${orderCell(row, ['systemResult'], 'badge')}</td>
          <td>${orderCell(row, ['reviewResult'], 'badge')}</td>
          <td>${orderCell(row, ['reviewAdminId'])}</td>
          <td>${orderCell(row, ['reviewAdminName'])}</td>
          <td>${orderCell(row, ['reviewRemark'])}</td>
          <td>${orderCell(row, ['reviewedAt'], 'time')}</td>
          <td>${orderCell(row, ['settleAmount', 'payoutAmount', 'winAmount', 'profitAmount'], 'money')}</td>
          <td>${orderCell(row, ['settleAdminId'])}</td>
          <td>${orderCell(row, ['settleAdminName'])}</td>
          <td>${orderCell(row, ['settleRemark'])}</td>
          <td>${orderCell(row, ['settledAt'], 'time')}</td>
          <td>${orderCell(row, ['createdTime', 'createTime', 'createdAt'], 'time')}</td>
          <td>${orderCell(row, ['updatedAt', 'updateTime'], 'time')}</td>
          <td class="orders-action-cell text-end pe-3">${actions}</td>
        </tr>`;
    }).join('');
  }

  function renderPager() {
    const pages = clampPageIndex();
    const cur = state.pageIndex;
    const item = (p, txt, disabled = false, active = false) => `<li class="page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}"><a href="#" class="page-link" data-page="${p}">${txt}</a></li>`;
    const start = Math.max(1, cur - 3);
    const end = Math.min(pages, start + 6);
    const items = [item(cur - 1, '&laquo;', cur <= 1)];
    for (let p = start; p <= end; p++) items.push(item(p, p, false, p === cur));
    items.push(item(cur + 1, '&raquo;', cur >= pages));
    $('orders-pager').innerHTML = items.join('');
    $('orders-total').textContent = state.total;
  }

  async function reload() {
    try {
      $('orders-tbody').innerHTML = `<tr><td colspan="${ORDER_COLSPAN}" class="text-center py-4">加载中...</td></tr>`;
      const page = unwrapPage(await request(config.pageApi, pageBody()));
      state.list = page.list;
      state.total = page.total;
      state.pageIndex = page.pageIndex;
      state.pageSize = page.pageSize;
      clampPageIndex();
      renderRows();
      renderPager();
    } catch (error) {
      console.error(error);
      $('orders-tbody').innerHTML = `<tr><td colspan="${ORDER_COLSPAN}" class="text-center py-4 text-danger">${esc(error.message || '加载失败')}</td></tr>`;
    }
  }

  async function loadDetails(order) {
    $('detail-title').textContent = `订单明细 ${order.orderNo || ''}`;
    $('items-tbody').innerHTML = '<tr><td colspan="8" class="text-center py-4">加载中...</td></tr>';
    detailModal.show();
    try {
      const page = unwrapPage(await request('/admin/order/item/page', {
        orderId: Number(order.id),
        orderNo: order.orderNo,
        pageIndex: 1,
        pageSize: 100,
        langCode: $('f-langCode')?.value.trim() || 'zh-CN',
      }));
      $('items-tbody').innerHTML = page.list.length ? page.list.map((row) => `
        <tr>
          <td>${esc(pick(row, ['id', 'itemId']))}</td>
          <td>${esc(pick(row, ['matchName', 'matchCode', 'matchId']))}</td>
          <td>${esc(pick(row, ['marketName', 'marketCode', 'marketId']))}</td>
          <td>${esc(pick(row, ['marketOptionName', 'optionName', 'marketOptionId']))}</td>
          <td>${esc(pick(row, ['odds']))}</td>
          <td>${money(pick(row, ['betAmount', 'stakeAmount', 'amount'], ''))}</td>
          <td>${badge(pick(row, ['result', 'systemResult']))}</td>
          <td>${badge(pick(row, ['status']))}</td>
        </tr>`).join('') : '<tr><td colspan="8" class="text-center py-4 text-muted">暂无明细</td></tr>';
    } catch (error) {
      $('items-tbody').innerHTML = `<tr><td colspan="8" class="text-center py-4 text-danger">${esc(error.message || '加载失败')}</td></tr>`;
    }
  }

  function findOrder(tr) {
    const id = tr?.dataset.id;
    return state.list.find((x) => String(pick(x, ['id', 'orderId'], '')) === String(id));
  }

  function openAction(action, order) {
    state.action = action;
    state.currentOrder = order;
    const names = { settle: '订单结算', review: '审核订单', freeze: '冻结订单' };
    $('action-title').textContent = names[action] || '订单操作';
    $('action-order').value = order.orderNo || order.id;
    $('action-remark').value = '';
    $('review-result-wrap').classList.toggle('d-none', action !== 'review');
    $('action-reviewResult').value = 'WIN';
    actionModal.show();
  }

  async function submitAction() {
    const order = state.currentOrder;
    const action = state.action;
    if (!order || !action) return;
    const urls = { settle: '/admin/order/settle', review: '/admin/order/review', freeze: '/admin/order/freeze' };
    const body = { orderId: Number(order.id), remark: $('action-remark').value.trim() };
    if (action === 'review') body.reviewResult = $('action-reviewResult').value;
    await request(urls[action], body);
    actionModal.hide();
    await reload();
  }

  $('filter-form').addEventListener('submit', (e) => {
    e.preventDefault();
    state.pageSize = Number($('f-pageSize').value || 10);
    state.pageIndex = 1;
    reload();
  });
  $('btn-reset').addEventListener('click', () => {
    $('filter-form').reset();
    $('f-pageSize').value = String(state.pageSize);
    state.pageIndex = 1;
    reload();
  });
  $('btn-refresh').addEventListener('click', reload);
  $('orders-pager').addEventListener('click', (e) => {
    const a = e.target.closest('a[data-page]');
    if (!a) return;
    e.preventDefault();
    const page = Number(a.dataset.page);
    if (!Number.isFinite(page) || page < 1 || page === state.pageIndex) return;
    state.pageIndex = page;
    reload();
  });
  $('orders-tbody').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-op]');
    if (!btn) return;
    const order = findOrder(btn.closest('tr'));
    if (!order) return;
    if (btn.dataset.op === 'detail') loadDetails({ id: pick(order, ['id', 'orderId']), orderNo: pick(order, ['orderNo', 'orderNumber'], '') });
    else openAction(btn.dataset.op, { id: pick(order, ['id', 'orderId']), orderNo: pick(order, ['orderNo', 'orderNumber'], '') });
  });
  $('action-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      await submitAction();
    } catch (error) {
      alert(error.message || '操作失败');
    }
  });

  $('f-pageSize').value = String(state.pageSize);
  reload();
})();
