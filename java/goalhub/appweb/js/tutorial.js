const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const TUTORIAL_API_URL = `${API_BASE_URL}/api/soccer/contents/articles/handicaptutorial`;

const tutorialContent = document.getElementById('tutorialContent');

async function loadTutorial() {
    if (!tutorialContent) {
        return;
    }

    try {
        const response = await fetch(TUTORIAL_API_URL);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const payload = await response.json();
        const contentHtml = payload?.data?.contentHtml || payload?.data?.content_html || '';

        tutorialContent.innerHTML = contentHtml || '<p>暂无教程内容</p>';
    } catch (error) {
        console.error('加载盘口教程失败:', error);
        tutorialContent.textContent = `加载盘口教程失败: ${error.message}`;
        tutorialContent.classList.add('tutorial-error');
    }
}

window.addEventListener('load', loadTutorial);
