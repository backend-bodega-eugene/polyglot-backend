(function () {
  const auth = window.GoalHubAuth;
  const config = window.GoalHubFundOrderConfig || {};
  const state = { pageIndex: 1, pageSize: 10, total: 0, records: [] };

  if (!auth || !config.apiBase) return;

  const $form = document.getElementById('filter-form');
  const $tbody = document.getElementById('orders-tbody');
  const $total = document.getElementById('orders-total');
  const $pager = document.getElementById('orders-pager');
  const $auditForm = document.getElementById('audit-form');
  const $auditSave = document.getElementById('btn-audit-save');
  const auditModal = new bootstrap.Modal(document.getElementById('audit-modal'));

  const statusText = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' };
  const statusClass = { PENDING: 'text-bg-secondary', APPROVED: 'text-bg-success', REJECTED: 'text-bg-danger' };

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatDate(value) {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return escapeHtml(value);
    return date.toLocaleString();
  }

  function formatMoney(value) {
    if (value === null || value === undefined || value === '') return '-';
    const num = Number(value);
    if (!Number.isFinite(num)) return escapeHtml(value);
    return num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 8 });
  }

  function clampPageIndex() {
    const pages = Math.max(1, Math.ceil(state.total / state.pageSize));
    state.pageIndex = Math.min(Math.max(1, Number(state.pageIndex) || 1), pages);
    return pages;
  }

  async function request(path, body) {
    const res = await auth.authFetch(path, { method: 'POST', body: JSON.stringify(body || {}) });
    const json = await res.json().catch(() => ({}));
    if (!res.ok || ![0, 200, undefined].includes(json.code)) {
      throw new Error(json.message || '请求失败');
    }
    return json.data;
  }

  function optionalNumber(id) {
    const value = Number(document.getElementById(id)?.value || 0);
    return value > 0 ? value : undefined;
  }

  function optionalText(id) {
    const value = document.getElementById(id)?.value.trim();
    return value || undefined;
  }

  function readFilters() {
    const body = {
      userId: optionalNumber('f-userId'),
      orderNo: optionalText('f-orderNo'),
      currencyCode: optionalText('f-currencyCode'),
      status: optionalText('f-status'),
      chainType: optionalText('f-chainType'),
      txHash: optionalText('f-txHash'),
      pageIndex: state.pageIndex,
      pageSize: Number(document.getElementById('f-pageSize').value || 10),
    };
    if (config.kind === 'withdraw') body.withdrawAddress = optionalText('f-withdrawAddress');
    return body;
  }

  async function loadOrders() {
    $tbody.innerHTML = `<tr><td colspan="${config.colspan}" class="text-center py-4">加载中...</td></tr>`;
    try {
      const page = await request(`${config.apiBase}/page`, readFilters());
      state.total = Number(page?.total || 0);
      state.pageIndex = Number(page?.pageIndex || state.pageIndex);
      state.pageSize = Number(page?.pageSize || readFilters().pageSize);
      clampPageIndex();
      state.records = Array.isArray(page?.records) ? page.records : [];
      renderTable();
      renderPager();
    } catch (error) {
      $tbody.innerHTML = `<tr><td colspan="${config.colspan}" class="text-center text-danger py-4">${escapeHtml(error.message || '加载失败')}</td></tr>`;
    }
  }

  function statusBadge(status) {
    return `<span class="badge ${escapeHtml(statusClass[status] || 'text-bg-light')}">${escapeHtml(statusText[status] || status || '-')}</span>`;
  }

  function renderTable() {
    $total.textContent = state.total;
    if (!state.records.length) {
      $tbody.innerHTML = `<tr><td colspan="${config.colspan}" class="text-center py-4">暂无数据</td></tr>`;
      return;
    }

    $tbody.innerHTML = state.records.map((item) => {
      const actionLabel = item.status === 'PENDING' ? '审核' : '查看';
      const withdrawCells = config.kind === 'withdraw'
        ? `<td>${formatMoney(item.feeAmount)}</td><td><div class="order-truncate" title="${escapeHtml(item.withdrawAddress || '')}">${escapeHtml(item.withdrawAddress || '-')}</div></td>`
        : '';
      return `
        <tr data-id="${escapeHtml(item.id)}">
          <td>${escapeHtml(item.id)}</td>
          <td><div class="order-no" title="${escapeHtml(item.orderNo || '')}">${escapeHtml(item.orderNo || '-')}</div></td>
          <td>${escapeHtml(item.userId ?? '-')}</td>
          <td>${escapeHtml(item.currencyCode || '-')}</td>
          <td>${formatMoney(item.amount)}</td>
          <td>${formatMoney(item.actualAmount)}</td>
          ${withdrawCells}
          <td>${statusBadge(item.status)}</td>
          <td>${escapeHtml(item.chainType || '-')}</td>
          <td><div class="order-truncate" title="${escapeHtml(item.txHash || '')}">${escapeHtml(item.txHash || '-')}</div></td>
          <td>${formatDate(item.auditTime)}</td>
          <td>${formatDate(item.createdAt)}</td>
          <td class="text-end pe-3"><button class="btn btn-outline-primary btn-sm" data-op="audit"><i class="bi bi-clipboard-check me-1"></i>${actionLabel}</button></td>
        </tr>`;
    }).join('');
  }

  function renderPager() {
    const pages = clampPageIndex();
    const cur = state.pageIndex;
    const items = [];
    const add = (page, label, disabled = false, active = false) => {
      items.push(`<li class="page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}"><a class="page-link" href="#" data-page="${page}">${label}</a></li>`);
    };
    add(cur - 1, '&laquo;', cur <= 1);
    const start = Math.max(1, cur - 2);
    const end = Math.min(pages, cur + 2);
    for (let p = start; p <= end; p += 1) add(p, p, false, p === cur);
    add(cur + 1, '&raquo;', cur >= pages);
    $pager.innerHTML = items.join('');
  }

  function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value ?? '-';
  }

  function fillDetail(item) {
    document.getElementById('audit-id').value = item.id ?? '';
    document.getElementById('audit-status').value = item.status === 'PENDING' ? '' : (item.status || '');
    document.getElementById('audit-remark').value = item.status === 'PENDING' ? '' : (item.auditRemark || '');
    document.getElementById('audit-status').disabled = item.status !== 'PENDING';
    document.getElementById('audit-remark').disabled = item.status !== 'PENDING';
    $auditSave.classList.toggle('d-none', item.status !== 'PENDING');

    setText('d-orderNo', item.orderNo);
    setText('d-userId', item.userId);
    setText('d-currencyCode', item.currencyCode);
    setText('d-amount', formatMoney(item.amount));
    setText('d-actualAmount', formatMoney(item.actualAmount));
    setText('d-status', statusText[item.status] || item.status || '-');
    setText('d-chainType', item.chainType);
    setText('d-txHash', item.txHash);
    setText('d-remark', item.remark);
    setText('d-auditRemark', item.auditRemark);
    setText('d-auditAdmin', item.auditAdminName || item.auditAdminId || '-');
    setText('d-auditTime', formatDate(item.auditTime));
    setText('d-createdAt', formatDate(item.createdAt));
    setText('d-updatedAt', formatDate(item.updatedAt));

    const withdrawOnly = document.querySelectorAll('[data-withdraw-only]');
    withdrawOnly.forEach((el) => el.classList.toggle('d-none', config.kind !== 'withdraw'));
    if (config.kind === 'withdraw') {
      setText('d-feeAmount', formatMoney(item.feeAmount));
      setText('d-withdrawAddress', item.withdrawAddress);
    }

    document.getElementById('audit-title').textContent = `${config.title} #${item.id ?? ''}`;
  }

  async function openAudit(id, button) {
    button.disabled = true;
    try {
      const item = await request(`${config.apiBase}/detail`, { id: Number(id) });
      fillDetail(item || {});
      auditModal.show();
      if (item?.status === 'PENDING') {
        setTimeout(() => document.getElementById('audit-status')?.focus(), 150);
      }
    } catch (error) {
      alert(error.message || '详情加载失败');
    } finally {
      button.disabled = false;
    }
  }

  $form.addEventListener('submit', (event) => {
    event.preventDefault();
    state.pageIndex = 1;
    loadOrders();
  });

  document.getElementById('btn-reset').addEventListener('click', () => {
    $form.reset();
    state.pageIndex = 1;
    loadOrders();
  });

  document.getElementById('btn-refresh').addEventListener('click', () => loadOrders());

  $pager.addEventListener('click', (event) => {
    const link = event.target.closest('[data-page]');
    if (!link || link.parentElement.classList.contains('disabled') || link.parentElement.classList.contains('active')) return;
    event.preventDefault();
    const page = Number(link.dataset.page);
    if (!Number.isFinite(page) || page < 1) return;
    state.pageIndex = page;
    loadOrders();
  });

  $tbody.addEventListener('click', (event) => {
    const button = event.target.closest('[data-op="audit"]');
    if (!button) return;
    const id = button.closest('tr')?.dataset.id;
    if (!id) return;
    openAudit(id, button);
  });

  $auditForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const admin = auth.getAdminUser() || {};
    const id = Number(document.getElementById('audit-id').value || 0);
    const auditStatus = document.getElementById('audit-status').value;
    const auditRemark = document.getElementById('audit-remark').value.trim();
    if (!id) return;
    if (!auditStatus) {
      alert('请选择审核结果');
      return;
    }
    if (auditStatus === 'REJECTED' && !auditRemark) {
      alert('拒绝时必须填写审核备注');
      return;
    }
    const auditStatusText = statusText[auditStatus] || auditStatus;
    if (!confirm(`确定要将该${config.title}提交为「${auditStatusText}」吗？`)) return;

    $auditSave.disabled = true;
    try {
      await request(`${config.apiBase}/audit`, {
        id,
        auditStatus,
        auditRemark,
        adminId: admin.id || 0,
        adminUsername: admin.username || admin.nickname || admin.realName || '',
      });
      auditModal.hide();
      loadOrders();
    } catch (error) {
      alert(error.message || '审核失败');
    } finally {
      $auditSave.disabled = false;
    }
  });

  loadOrders();
})();
