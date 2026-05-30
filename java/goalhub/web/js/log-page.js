(function () {
  const auth = window.GoalHubAuth;
  const config = window.GoalHubLogPage || {};
  const state = {
    pageIndex: 1,
    pageSize: 10,
    total: 0,
    list: [],
    filters: {},
  };

  const esc = (value) => (value == null ? '' : String(value)).replace(/[&<>"']/g, (m) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[m]));

  const ok = (data) => data && (data.code === 200 || data.code === 0 || data.success === true);
  const messageOf = (data, fallback) => data?.message || data?.msg || fallback;

  function fmtTime(value) {
    if (!value) return '-';
    const date = new Date(String(value));
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  }

  function isoFromLocal(value) {
    if (!value) return '';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '' : date.toISOString();
  }

  async function readJson(res) {
    const text = await res.text();
    if (!text) return {};
    try {
      return JSON.parse(text);
    } catch (_) {
      if (res.ok) return {};
      throw new Error('接口返回格式错误');
    }
  }

  async function request(url, options = {}) {
    const res = await auth.authFetch(url, options);
    const data = await readJson(res);
    if (!ok(data)) throw new Error(messageOf(data, '请求失败'));
    return data;
  }

  async function loadSidebarMenus() {
    const res = await auth.authFetch('/admin/auth/menus');
    if (!res.ok) throw new Error('菜单加载失败');
    const data = await res.json();
    return Array.isArray(data) ? data : (data.data || []);
  }

  function renderSidebarMenus(menus) {
    const root = document.querySelector('aside.app-sidebar #navigation');
    if (!root) return;
    root.innerHTML = '';

    const createItem = (node) => {
      const hasChildren = Array.isArray(node.children) && node.children.length > 0;
      const li = document.createElement('li');
      li.className = 'nav-item';

      if (!hasChildren) {
        const path = node.path || '#';
        li.innerHTML = `
          <a href="${esc(path)}" class="nav-link${path !== '#' && location.pathname.endsWith('/' + path) ? ' active' : ''}">
            <i class="nav-icon ${esc(node.icon || 'bi bi-speedometer')}"></i>
            <p>${esc(node.name || '')}</p>
          </a>`;
        return li;
      }

      li.innerHTML = `
        <a href="#" class="nav-link">
          <i class="nav-icon ${esc(node.icon || 'bi bi-folder')}"></i>
          <p>${esc(node.name || '')} <i class="nav-arrow bi bi-chevron-right"></i></p>
        </a>`;
      const ul = document.createElement('ul');
      ul.className = 'nav nav-treeview';
      node.children.forEach((child) => ul.appendChild(createItem(child)));
      li.appendChild(ul);
      return li;
    };

    menus.forEach((menu) => root.appendChild(createItem(menu)));
  }

  function renderProfile() {
    const user = auth.getAdminUser() || {};
    const name = user.nickname || user.fullName || user.realName || user.username || '管理员';
    const role = Number(user.isSuperAdmin) === 1 ? 'Super Admin' : 'Admin';
    const sinceText = user.memberSince ? `登录时间 ${new Date(user.memberSince).toLocaleDateString()}` : '今日登录';
    const avatar = user.avatarUrl || user.avatar || 'assets/img/default-avatar.jpg';

    const $name = document.getElementById('user-name');
    const $title = document.getElementById('user-title');
    const $since = document.getElementById('user-since');
    const $avatar = document.getElementById('user-avatar');
    const $avatarLarge = document.getElementById('user-avatar-large');
    const $signout = document.getElementById('btn-signout');

    if ($name) $name.textContent = name;
    if ($title) $title.innerHTML = `${esc(name)} - ${role} <small id="user-since">${esc(sinceText)}</small>`;
    if ($since) $since.textContent = sinceText;
    if ($avatar) $avatar.src = avatar;
    if ($avatarLarge) $avatarLarge.src = avatar;
    $signout?.addEventListener('click', () => auth.toLogin());
  }

  function initSidebarScroll() {
    const wrapper = document.querySelector('.sidebar-wrapper');
    if (wrapper && window.OverlayScrollbarsGlobal?.OverlayScrollbars !== undefined) {
      window.OverlayScrollbarsGlobal.OverlayScrollbars(wrapper, {
        scrollbars: {
          theme: 'os-theme-light',
          autoHide: 'leave',
          clickScroll: true,
        },
      });
    }
  }

  function readFilters() {
    return {
      serviceName: document.getElementById('f-serviceName').value.trim(),
      moduleName: document.getElementById('f-moduleName').value.trim(),
      event: document.getElementById('f-event').value.trim(),
      operatorName: document.getElementById('f-operatorName').value.trim(),
      createdAtStart: isoFromLocal(document.getElementById('f-created-start').value),
      createdAtEnd: isoFromLocal(document.getElementById('f-created-end').value),
    };
  }

  function buildBody() {
    const body = {
      pageIndex: state.pageIndex,
      pageSize: state.pageSize,
    };
    Object.entries(state.filters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) body[key] = value;
    });
    return body;
  }

  async function apiList() {
    const data = await request(config.endpoint, {
      method: 'POST',
      body: JSON.stringify(buildBody()),
    });
    const page = data.data || {};
    const records = page.records || page.list || page.rows || page.data || [];
    return {
      list: Array.isArray(records) ? records : [],
      total: Math.max(Number(page.total || page.totalCount || 0), Array.isArray(records) ? records.length : 0),
      pageIndex: Number(page.pageIndex || page.current || state.pageIndex),
      pageSize: Number(page.pageSize || page.size || state.pageSize),
    };
  }

  function logMessage(row) {
    return row.message || row.msg || row.content || row.description || row.detail || row.stackTrace || row.errorMessage || row.exception || '';
  }

  function renderRows(list) {
    const tbody = document.getElementById('logs-tbody');
    if (!tbody) return;

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">暂无数据</td></tr>';
      return;
    }

    tbody.innerHTML = list.map((row) => `
      <tr>
        <td>${esc(row.id ?? row.logId ?? '')}</td>
        <td>${esc(row.serviceName ?? row.servicename ?? '')}</td>
        <td>${esc(row.moduleName ?? row.modulesName ?? row.modulesname ?? '')}</td>
        <td>${esc(row.event ?? '')}</td>
        <td>${esc(row.operatorName ?? '')}</td>
        <td class="text-break" style="min-width:260px;">${esc(logMessage(row))}</td>
        <td>${esc(fmtTime(row.createdAt || row.createTime))}</td>
        <td>${esc(row.level || row.status || row.result || '')}</td>
      </tr>`).join('');
  }

  function renderPager() {
    const pager = document.getElementById('logs-pager');
    const total = document.getElementById('logs-total');
    if (!pager || !total) return;

    const pages = Math.max(1, Math.ceil(state.total / state.pageSize));
    const cur = Math.min(state.pageIndex, pages);
    const item = (page, text, disabled = false, active = false) =>
      `<li class="page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}">
        <a href="#" class="page-link" data-page="${page}">${text}</a>
      </li>`;

    const items = [item(cur - 1, '&laquo;', cur <= 1)];
    const start = Math.max(1, cur - 3);
    const end = Math.min(pages, start + 6);
    for (let page = start; page <= end; page += 1) items.push(item(page, page, false, page === cur));
    items.push(item(cur + 1, '&raquo;', cur >= pages));
    pager.innerHTML = items.join('');
    total.textContent = state.total;
  }

  async function reload() {
    const tbody = document.getElementById('logs-tbody');
    if (tbody) tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4">加载中...</td></tr>';

    try {
      const page = await apiList();
      state.list = page.list;
      state.total = page.total;
      state.pageIndex = page.pageIndex;
      state.pageSize = page.pageSize;
      renderRows(state.list);
      renderPager();
    } catch (error) {
      console.error(error);
      if (tbody) tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-danger">${esc(error.message || '加载失败')}</td></tr>`;
    }
  }

  function bindLogEvents() {
    const form = document.getElementById('filter-form');
    const pageSize = document.getElementById('f-pageSize');
    const reset = document.getElementById('btn-reset');
    const refresh = document.getElementById('btn-refresh');
    const pager = document.getElementById('logs-pager');

    form?.addEventListener('submit', (event) => {
      event.preventDefault();
      state.filters = readFilters();
      state.pageSize = Number(pageSize.value || 10);
      state.pageIndex = 1;
      reload();
    });

    reset?.addEventListener('click', () => {
      form.reset();
      pageSize.value = String(state.pageSize);
      state.filters = {};
      state.pageIndex = 1;
      reload();
    });

    refresh?.addEventListener('click', reload);

    pager?.addEventListener('click', (event) => {
      const link = event.target.closest('a[data-page]');
      if (!link) return;
      event.preventDefault();
      const page = Number(link.dataset.page);
      if (!Number.isFinite(page) || page < 1 || page === state.pageIndex) return;
      state.pageIndex = page;
      reload();
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('page-title').textContent = config.title || '日志查询';
    document.title = `GoalHub | ${config.title || '日志查询'}`;
    initSidebarScroll();
    renderProfile();
    bindLogEvents();

    const pageSize = document.getElementById('f-pageSize');
    if (pageSize) pageSize.value = String(state.pageSize);
    state.filters = readFilters();

    loadSidebarMenus().then(renderSidebarMenus).catch((error) => console.warn(error));
    reload();
  });
})();
