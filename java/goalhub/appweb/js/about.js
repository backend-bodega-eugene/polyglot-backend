const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const ABOUT_API_URL = `${API_BASE_URL}/api/soccer/contents/articles/about`;

const aboutTitle = document.getElementById('aboutTitle');
const aboutContent = document.getElementById('aboutContent');

async function loadAbout() {
    if (!aboutContent) {
        return;
    }

    try {
        const response = await fetch(ABOUT_API_URL);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const payload = await response.json();
        const article = payload?.data || {};
        const title = article.title || '关于';
        const contentHtml = article.contentHtml || article.content_html || '';

        document.title = `${title} - GoalHub`;
        aboutTitle.textContent = title;
        aboutContent.innerHTML = contentHtml || '<p>暂无关于内容</p>';
    } catch (error) {
        console.error('加载关于内容失败:', error);
        aboutContent.textContent = `加载关于内容失败: ${error.message}`;
        aboutContent.classList.add('tutorial-error');
    }
}

window.addEventListener('load', loadAbout);
