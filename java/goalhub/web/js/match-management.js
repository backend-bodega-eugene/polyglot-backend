(function () {
  const auth = window.GoalHubAuth;
  const config = window.GoalHubEntityConfig;
  if (!auth || !config) return;

  const state = {
    pageIndex: 1,
    pageSize: 10,
    total: 0,
    list: [],
    filters: {},
    options: {},
  };

  const $ = (id) => document.getElementById(id);
  const $tbody = $('entity-tbody');
  const $pager = $('entity-pager');
  const $total = $('entity-total');
  const $form = $('filter-form');
  const $keyword = $('f-keyword');
  const $langCode = $('f-langCode');
  const $pageSize = $('f-pageSize');
  const $reset = $('btn-reset');
  const $refresh = $('btn-refresh');
  const $create = $('btn-create');
  const $modalTitle = $('entity-modal-title');
  const $entityForm = $('entity-form');
  const $entityId = $('entity-id');
  const entityModal = new bootstrap.Modal($('entity-modal'));

  const esc = (s) => (s == null ? '' : String(s)).replace(/[&<>"']/g, (m) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[m]));
  const ok = (data) => data && (data.code === 200 || data.code === 0 || data.success === true);
  const messageOf = (data, fallback) => data?.message || data?.msg || fallback;

  function unwrapListPayload(data) {
    if (Array.isArray(data?.data)) {
      return {
        list: data.data,
        total: data.data.length,
        pageIndex: 1,
        pageSize: data.data.length || state.pageSize,
      };
    }
    if (Array.isArray(data)) {
      return {
        list: data,
        total: data.length,
        pageIndex: 1,
        pageSize: data.length || state.pageSize,
      };
    }
    const page = data?.data || data || {};
    const records = page.records || page.list || page.rows || page.data || [];
    return {
      list: Array.isArray(records) ? records : [],
      total: Math.max(Number(page.total || page.totalCount || page.count || 0), Array.isArray(records) ? records.length : 0),
      pageIndex: Number(page.pageIndex || page.current || page.page || state.pageIndex),
      pageSize: Number(page.pageSize || page.size || state.pageSize),
    };
  }

  async function request(url, options = {}) {
    const res = await auth.authFetch(url, options);
    const text = await res.text();
    let data = {};
    if (text) {
      try {
        data = JSON.parse(text);
      } catch (_) {
        if (res.ok) return {};
        throw new Error('接口返回格式错误');
      }
    }
    if (!text && res.ok) return data;
    if (!res.ok || !ok(data)) throw new Error(messageOf(data, '请求失败'));
    return data;
  }

  function fmtTime(value) {
    if (!value) return '-';
    const date = new Date(String(value));
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  }

  function toInputDateTime(value) {
    if (!value) return '';
    const date = new Date(String(value));
    if (Number.isNaN(date.getTime())) return '';
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  function toIso(value) {
    if (!value) return '';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '' : date.toISOString();
  }

  function statusBadge(value) {
    if (value === 1 || value === '1') return '<span class="badge bg-success">启用</span>';
    if (value === 0 || value === '0') return '<span class="badge bg-secondary">禁用</span>';
    const text = String(value || '-');
    const cls = /live|进行|active|启用/i.test(text)
      ? 'bg-success'
      : /finish|结束|complete/i.test(text)
        ? 'bg-primary'
        : /cancel|postpone|取消|延期|禁用/i.test(text)
          ? 'bg-secondary'
          : 'bg-info';
    return `<span class="badge ${cls}">${esc(text)}</span>`;
  }

  function labelFor(field, value) {
    if (value == null || value === '') return '-';
    const opt = state.options[field.optionKey || field.key]?.find((x) => String(x.value) === String(value));
    return opt ? opt.label : value;
  }

  function renderCell(field, row) {
    if (field.displayKey && row[field.displayKey] != null && row[field.displayKey] !== '') {
      return esc(row[field.displayKey]);
    }
    const value = row[field.key];
    if (field.type === 'image') {
      const src = value || 'assets/img/default-avatar.jpg';
      return `<img src="${esc(src)}" alt="${esc(field.label)}" class="rounded-circle entity-logo" onerror="this.src='assets/img/default-avatar.jpg'">`;
    }
    if (field.type === 'status') return statusBadge(value);
    if (field.type === 'datetime') return esc(fmtTime(value));
    if (field.type === 'select') return esc(labelFor(field, value));
    return esc(value ?? '');
  }

  function renderRows(list) {
    if (!list || list.length === 0) {
      $tbody.innerHTML = `<tr><td colspan="${config.columns.length + 2}" class="text-center py-4 text-muted">暂无数据</td></tr>`;
      return;
    }

    $tbody.innerHTML = list.map((row) => {
      const id = row.id ?? '';
      return `
        <tr data-id="${esc(id)}">
          <td>${esc(id)}</td>
          ${config.columns.map((field) => `<td>${renderCell(field, row)}</td>`).join('')}
          <td class="text-end pe-3">
            <button class="btn btn-outline-primary btn-sm me-1" data-op="edit">编辑</button>
            <button class="btn btn-outline-danger btn-sm" data-op="del">删除</button>
          </td>
        </tr>`;
    }).join('');
  }

  function renderPager() {
    const pages = Math.max(1, Math.ceil(state.total / state.pageSize));
    const cur = Math.min(state.pageIndex, pages);
    const item = (p, txt, disabled = false, active = false) =>
      `<li class="page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}"><a href="#" class="page-link" data-page="${p}">${txt}</a></li>`;
    const items = [item(cur - 1, '&laquo;', cur <= 1)];
    const start = Math.max(1, cur - 3);
    const end = Math.min(pages, start + 6);
    for (let p = start; p <= end; p++) items.push(item(p, p, false, p === cur));
    items.push(item(cur + 1, '&raquo;', cur >= pages));
    $pager.innerHTML = items.join('');
    $total.textContent = state.total;
  }

  function readFilters() {
    const filters = {};
    if ($keyword) filters.keyword = $keyword.value.trim();
    if ($langCode) filters.langCode = $langCode.value.trim();
    if (config.listParam?.filterId) {
      const input = $(config.listParam.filterId);
      filters[config.listParam.key] = input?.value ? Number(input.value) : '';
    }
    return filters;
  }

  function buildPageBody() {
    const body = config.api.list
      ? { [config.listParam.key]: state.filters[config.listParam.key] }
      : {
          pageIndex: state.pageIndex,
          pageSize: state.pageSize,
          langCode: state.filters.langCode,
          keyword: state.filters.keyword,
        };
    Object.keys(body).forEach((key) => {
      if (body[key] === '' || body[key] == null) delete body[key];
    });
    return body;
  }

  async function apiList() {
    if (config.requireFilter && config.listParam?.key && !state.filters[config.listParam.key]) {
      return { list: [], total: 0, pageIndex: 1, pageSize: state.pageSize };
    }
    const data = await request(config.api.list || config.api.page, {
      method: 'POST',
      body: JSON.stringify(buildPageBody()),
    });
    return unwrapListPayload(data);
  }

  function apiCreate(body) {
    return request(config.api.add, { method: 'POST', body: JSON.stringify(body) });
  }

  function apiUpdate(body) {
    return request(config.api.update, { method: 'POST', body: JSON.stringify(body) });
  }

  function apiDelete(id) {
    return request(config.api.delete, { method: 'POST', body: JSON.stringify({ id: Number(id) }) });
  }

  async function reload() {
    try {
      $tbody.innerHTML = `<tr><td colspan="${config.columns.length + 2}" class="text-center py-4">加载中...</td></tr>`;
      const page = await apiList();
      state.list = page.list;
      state.total = page.total;
      state.pageIndex = page.pageIndex;
      state.pageSize = page.pageSize;
      renderRows(state.list);
      renderPager();
    } catch (error) {
      console.error(error);
      $tbody.innerHTML = `<tr><td colspan="${config.columns.length + 2}" class="text-center py-4 text-danger">${esc(error.message || '加载失败')}</td></tr>`;
    }
  }

  function findRow(id) {
    return state.list.find((x) => String(x.id) === String(id));
  }

  function setSelectValue(input, value) {
    const str = value == null ? '' : String(value);
    if (str && !Array.from(input.options).some((o) => o.value === str)) {
      input.add(new Option(str, str));
    }
    input.value = str;
  }

  function readFormBody(isCreate) {
    const body = {};
    if (!isCreate) body.id = Number($entityId.value);
    config.fields.forEach((field) => {
      const input = $(field.id);
      let value = input.value;
      if (field.type === 'number' || field.valueType === 'number') value = value === '' ? null : Number(value);
      if (field.type === 'datetime') value = toIso(value);
      if (!isCreate || config.createKeys.includes(field.key)) body[field.key] = value;
    });
    return body;
  }

  function fillForm(row) {
    config.fields.forEach((field) => {
      const input = $(field.id);
      const value = field.type === 'datetime' ? toInputDateTime(row[field.key]) : row[field.key];
      if (field.type === 'select') setSelectValue(input, value);
      else input.value = value ?? '';
    });
  }

  async function loadOptions() {
    const loaders = config.optionLoaders || {};
    for (const [key, loader] of Object.entries(loaders)) {
      try {
        const data = await request(loader.api, {
          method: 'POST',
          body: JSON.stringify({ pageIndex: 1, pageSize: loader.pageSize || 200 }),
        });
        const page = unwrapListPayload(data);
        state.options[key] = page.list.map((x) => ({
          value: x[loader.valueKey || 'id'],
          label: loader.label ? loader.label(x) : `${x.code || x.name || x.id}`,
        }));
        document.querySelectorAll(`select[data-option-key="${key}"]`).forEach((select) => {
          const current = select.value;
          select.innerHTML = '<option value="">请选择</option>' + state.options[key]
            .map((x) => `<option value="${esc(x.value)}">${esc(x.label)}</option>`)
            .join('');
          if (current) setSelectValue(select, current);
        });
      } catch (error) {
        console.warn(`${key} options load failed`, error);
      }
    }
  }

  $form.addEventListener('submit', (e) => {
    e.preventDefault();
    state.filters = readFilters();
    state.pageSize = Number($pageSize?.value || 10);
    state.pageIndex = 1;
    reload();
  });

  $reset.addEventListener('click', () => {
    $form.reset();
    if ($pageSize) $pageSize.value = String(state.pageSize);
    state.filters = {};
    state.pageIndex = 1;
    reload();
  });

  $refresh.addEventListener('click', reload);

  $pager.addEventListener('click', (e) => {
    const a = e.target.closest('a[data-page]');
    if (!a) return;
    e.preventDefault();
    const page = Number(a.dataset.page);
    if (!Number.isFinite(page) || page < 1 || page === state.pageIndex) return;
    state.pageIndex = page;
    reload();
  });

  $create.addEventListener('click', async () => {
    $entityForm.reset();
    $entityId.value = '';
    $modalTitle.textContent = config.createTitle;
    await loadOptions();
    config.defaults?.forEach(([id, value]) => {
      const input = $(id);
      if (input) input.value = value;
    });
    if (config.listParam?.formId && state.filters[config.listParam.key]) {
      const input = $(config.listParam.formId);
      if (input) setSelectValue(input, state.filters[config.listParam.key]);
    }
    entityModal.show();
  });

  $tbody.addEventListener('click', async (e) => {
    const btn = e.target.closest('button[data-op]');
    if (!btn) return;
    const id = btn.closest('tr')?.dataset?.id;
    const row = findRow(id);
    if (!row) return;

    if (btn.dataset.op === 'edit') {
      $entityForm.reset();
      $entityId.value = id;
      $modalTitle.textContent = config.editTitle;
      await loadOptions();
      fillForm(row);
      entityModal.show();
      return;
    }

    if (btn.dataset.op === 'del') {
      if (!confirm(`确定删除该${config.entityName}？此操作不可恢复。`)) return;
      try {
        await apiDelete(id);
        await reload();
      } catch (error) {
        alert(error.message || '删除失败');
      }
    }
  });

  $entityForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const isCreate = !$entityId.value;
    const body = readFormBody(isCreate);
    try {
      if (isCreate) await apiCreate(body);
      else await apiUpdate(body);
      entityModal.hide();
      await reload();
    } catch (error) {
      alert(error.message || '保存失败');
    }
  });

  if ($pageSize) $pageSize.value = String(state.pageSize);
  state.filters = readFilters();
  loadOptions().finally(reload);
})();
