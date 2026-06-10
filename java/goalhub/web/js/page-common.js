(function () {
  const SELECTOR_SIDEBAR_WRAPPER = '.sidebar-wrapper';
  const Default = { scrollbarTheme: 'os-theme-light', scrollbarAutoHide: 'leave', scrollbarClickScroll: true };
  const embedded = new URLSearchParams(location.search).get('embedded') === '1' || window.self !== window.top;

  function applyEmbeddedLayout() {
    document.body.classList.add('gh-embedded-page');
    if (document.getElementById('gh-embedded-style')) return;

    const style = document.createElement('style');
    style.id = 'gh-embedded-style';
    style.textContent = `
      html, body { min-height: 0 !important; background: var(--bs-tertiary-bg, #f8f9fa) !important; }
      body.gh-embedded-page { overflow: auto !important; }
      body.gh-embedded-page > .app-wrapper { display: block !important; min-height: 0 !important; }
      body.gh-embedded-page .app-header,
      body.gh-embedded-page .app-sidebar,
      body.gh-embedded-page .app-footer { display: none !important; }
      body.gh-embedded-page .app-main {
        display: block !important;
        min-height: 0 !important;
        width: 100% !important;
        margin-left: 0 !important;
        padding: 0 !important;
      }
    `;
    document.head.appendChild(style);
  }

  if (embedded) {
    if (document.body) applyEmbeddedLayout();
    else document.addEventListener('DOMContentLoaded', applyEmbeddedLayout);
    return;
  }

  document.addEventListener('DOMContentLoaded', function () {
    const sidebarWrapper = document.querySelector(SELECTOR_SIDEBAR_WRAPPER);
    if (sidebarWrapper && window.OverlayScrollbarsGlobal?.OverlayScrollbars !== undefined) {
      window.OverlayScrollbarsGlobal.OverlayScrollbars(sidebarWrapper, {
        scrollbars: { theme: Default.scrollbarTheme, autoHide: Default.scrollbarAutoHide, clickScroll: Default.scrollbarClickScroll },
      });
    }
  });

  const auth = window.GoalHubAuth;
  if (!auth) return;

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  async function fetchMenus() {
    const res = await auth.authFetch('/admin/auth/menus');
    if (!res.ok) throw new Error('菜单加载失败');
    const data = await res.json();
    return Array.isArray(data) ? data : (data.data || []);
  }

  function renderMenus(menus) {
    const root = document.querySelector('aside.app-sidebar #navigation');
    if (!root) return;
    root.innerHTML = '';

    const createItem = (node) => {
      const hasChildren = Array.isArray(node.children) && node.children.length > 0;
      const li = document.createElement('li');
      li.className = 'nav-item';

      if (!hasChildren) {
        const page = node.path || '#';
        const href = page === '#' ? '#' : `index.html?page=${encodeURIComponent(page)}`;
        li.innerHTML = `
          <a href="${escapeHtml(href)}" class="nav-link${location.pathname.endsWith('/' + page) ? ' active' : ''}">
            <i class="nav-icon ${escapeHtml(node.icon || 'bi bi-speedometer')}"></i>
            <p>${escapeHtml(node.name || '')}</p>
          </a>`;
        return li;
      }

      li.innerHTML = `
        <a href="#" class="nav-link">
          <i class="nav-icon ${escapeHtml(node.icon || 'bi bi-folder')}"></i>
          <p>${escapeHtml(node.name || '')} <i class="nav-arrow bi bi-chevron-right"></i></p>
        </a>`;
      const ul = document.createElement('ul');
      ul.className = 'nav nav-treeview';
      node.children.forEach((ch) => ul.appendChild(createItem(ch)));
      li.appendChild(ul);
      return li;
    };

    menus.forEach((menu) => root.appendChild(createItem(menu)));
  }

  fetchMenus()
    .then((menus) => renderMenus(menus || []))
    .catch((error) => console.warn(error));

  const $name = document.getElementById('user-name');
  const $title = document.getElementById('user-title');
  const $av1 = document.getElementById('user-avatar');
  const $av2 = document.getElementById('user-avatar-large');
  const $out = document.getElementById('btn-signout');

  function setProfile(p = {}) {
    const n = p.nickname || p.fullName || p.realName || p.username || '管理员';
    const role = Number(p.isSuperAdmin) === 1 ? 'Super Admin' : 'Admin';
    const sinceTxt = p.memberSince
      ? `登录时间 ${new Date(p.memberSince).toLocaleDateString()}`
      : '今日登录';

    if ($name) $name.textContent = n;
    if ($title) $title.innerHTML = `${escapeHtml(n)} - ${escapeHtml(role)} <small id="user-since">${escapeHtml(sinceTxt)}</small>`;

    const avatar = p.avatarUrl || p.avatar || 'assets/img/default-avatar.jpg';
    if ($av1) $av1.src = avatar;
    if ($av2) $av2.src = avatar;
  }

  $out?.addEventListener('click', () => auth.toLogin());
  setProfile(auth.getAdminUser() || {});
})();
