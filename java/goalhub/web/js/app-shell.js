(function () {
  const SELECTOR_SIDEBAR_WRAPPER = '.sidebar-wrapper';
  const Default = { scrollbarTheme: 'os-theme-light', scrollbarAutoHide: 'leave', scrollbarClickScroll: true };
  const auth = window.GoalHubAuth;
  const frame = document.getElementById('app-frame');
  const frameWrap = document.getElementById('app-frame-wrap');
  const navigation = document.querySelector('aside.app-sidebar #navigation');
  const DEFAULT_PAGE = 'menus.html';

  if (!auth || !frame || !navigation) return;

  document.addEventListener('DOMContentLoaded', function () {
    const sidebarWrapper = document.querySelector(SELECTOR_SIDEBAR_WRAPPER);
    if (sidebarWrapper && window.OverlayScrollbarsGlobal?.OverlayScrollbars !== undefined) {
      window.OverlayScrollbarsGlobal.OverlayScrollbars(sidebarWrapper, {
        scrollbars: { theme: Default.scrollbarTheme, autoHide: Default.scrollbarAutoHide, clickScroll: Default.scrollbarClickScroll },
      });
    }
  });

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function normalizePage(page) {
    const raw = String(page || '').trim();
    if (!raw || raw === '#') return '';
    if (/^(https?:)?\/\//i.test(raw)) return '';
    return raw.replace(/^\.?\//, '');
  }

  function pageUrl(page) {
    const normalized = normalizePage(page);
    if (!normalized) return '';
    return `${normalized}${normalized.includes('?') ? '&' : '?'}embedded=1`;
  }

  function getInitialPage() {
    const params = new URLSearchParams(location.search);
    return normalizePage(params.get('page')) || DEFAULT_PAGE;
  }

  function setActive(page) {
    const normalized = normalizePage(page);
    navigation.querySelectorAll('.nav-link.active').forEach((a) => a.classList.remove('active'));
    navigation.querySelectorAll('.nav-item.menu-open').forEach((li) => li.classList.remove('menu-open'));

    const link = Array.from(navigation.querySelectorAll('[data-page]')).find((item) => item.dataset.page === normalized);
    if (!link) return;

    link.classList.add('active');
    let parent = link.closest('.nav-treeview')?.closest('.nav-item');
    while (parent) {
      parent.classList.add('menu-open');
      parent = parent.parentElement?.closest('.nav-treeview')?.closest('.nav-item');
    }
  }

  function injectEmbeddedStyle() {
    try {
      const doc = frame.contentDocument;
      if (!doc) return;
      doc.body?.classList.add('gh-embedded-page');
      if (doc.getElementById('gh-embedded-style')) return;

      const style = doc.createElement('style');
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
      doc.head.appendChild(style);
    } catch (error) {
      console.warn('内嵌页面样式注入失败', error);
    }
  }

  function loadPage(page, options = {}) {
    const normalized = normalizePage(page);
    const url = pageUrl(normalized);
    if (!url) return;

    frameWrap?.classList.add('is-loading');
    frame.src = url;
    setActive(normalized);

    if (options.push !== false) {
      const nextUrl = `index.html?page=${encodeURIComponent(normalized)}`;
      history.pushState({ page: normalized }, '', nextUrl);
    }
  }

  function firstPage(menus) {
    for (const item of menus || []) {
      const own = normalizePage(item.path);
      if (own) return own;
      const child = firstPage(item.children || []);
      if (child) return child;
    }
    return DEFAULT_PAGE;
  }

  async function fetchMenus() {
    const res = await auth.authFetch('/admin/auth/menus');
    if (!res.ok) throw new Error('菜单加载失败');
    const data = await res.json();
    return Array.isArray(data) ? data : (data.data || []);
  }

  function renderMenus(menus) {
    navigation.innerHTML = '';

    const createItem = (node) => {
      const hasChildren = Array.isArray(node.children) && node.children.length > 0;
      const page = normalizePage(node.path);
      const li = document.createElement('li');
      li.className = 'nav-item';

      if (!hasChildren) {
        li.innerHTML = `
          <a href="${escapeHtml(page ? `index.html?page=${encodeURIComponent(page)}` : '#')}" class="nav-link" data-page="${escapeHtml(page)}">
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
      node.children.forEach((child) => ul.appendChild(createItem(child)));
      li.appendChild(ul);
      return li;
    };

    menus.forEach((menu) => navigation.appendChild(createItem(menu)));
  }

  navigation.addEventListener('click', (event) => {
    const link = event.target.closest('a[data-page]');
    if (!link) return;

    const page = link.dataset.page;
    if (!page) return;

    event.preventDefault();
    loadPage(page);
  });

  frame.addEventListener('load', () => {
    injectEmbeddedStyle();
    frameWrap?.classList.remove('is-loading');
  });

  window.addEventListener('popstate', () => {
    loadPage(getInitialPage(), { push: false });
  });

  const $name = document.getElementById('user-name');
  const $title = document.getElementById('user-title');
  const $av1 = document.getElementById('user-avatar');
  const $av2 = document.getElementById('user-avatar-large');
  const $out = document.getElementById('btn-signout');
  const $profile = document.getElementById('btn-profile');

  function setProfile(p = {}) {
    const n = p.nickname || p.fullName || p.realName || p.username || '管理员';
    const role = Number(p.isSuperAdmin) === 1 ? 'Super Admin' : 'Admin';
    const sinceTxt = p.memberSince ? `登录时间 ${new Date(p.memberSince).toLocaleDateString()}` : '今日登录';

    if ($name) $name.textContent = n;
    if ($title) $title.innerHTML = `${escapeHtml(n)} - ${escapeHtml(role)} <small id="user-since">${escapeHtml(sinceTxt)}</small>`;

    const avatar = p.avatarUrl || p.avatar || 'assets/img/default-avatar.jpg';
    if ($av1) $av1.src = avatar;
    if ($av2) $av2.src = avatar;
  }

  $out?.addEventListener('click', () => auth.toLogin());
  $profile?.addEventListener('click', (event) => {
    event.preventDefault();
    loadPage('profile.html');
  });
  setProfile(auth.getAdminUser() || {});

  fetchMenus()
    .then((menus) => {
      renderMenus(menus || []);
      const requested = getInitialPage();
      loadPage(requested || firstPage(menus), { push: false });
    })
    .catch((error) => {
      console.warn(error);
      loadPage(DEFAULT_PAGE, { push: false });
    });
})();
