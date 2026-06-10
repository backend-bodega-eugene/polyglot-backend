const API_BASE_URL = window.GoalHubConfig?.API_BASE_URL || 'http://localhost:8000';
const COMMENTS_PAGE_URL = `${API_BASE_URL}/api/user/usercomments/page`;
const COMMENTS_ADD_URL = `${API_BASE_URL}/api/user/usercomments/add`;
const COMMENTS_PAGE_SIZE = 10;

const commentForm = document.getElementById('commentForm');
const commentContact = document.getElementById('commentContact');
const commentMessage = document.getElementById('commentMessage');
const commentSubmitBtn = document.getElementById('commentSubmitBtn');
const commentMessageTip = document.getElementById('commentMessageTip');
const commentList = document.getElementById('commentList');
const commentsLoadMoreBtn = document.getElementById('commentsLoadMoreBtn');
const commentRefreshBtn = document.getElementById('commentRefreshBtn');
const { apiFetch } = window.GoalHubApp;

let currentPageIndex = 1;
let totalComments = 0;
let loadedComments = 0;

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }

    const normalizedValue = typeof value === 'string' && value.includes(' ')
        ? value.replace(' ', 'T')
        : value;
    const date = new Date(normalizedValue);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function setTip(message, type = '') {
    if (!commentMessageTip) {
        return;
    }

    commentMessageTip.textContent = message;
    commentMessageTip.className = `comment-message ${type}`.trim();
}

function getRecords(payload) {
    if (Array.isArray(payload?.data?.records)) {
        return payload.data.records;
    }

    if (Array.isArray(payload?.records)) {
        return payload.records;
    }

    return [];
}

function createCommentCard(record) {
    const card = document.createElement('article');
    card.className = 'comment-card';

    const replyContent = record.replyContent || '';
    const replyHtml = replyContent
        ? `<div class="comment-reply">${escapeHtml(replyContent)}</div><div class="comment-reply-time">回复时间：${escapeHtml(formatDateTime(record.replyTime))}</div>`
        : '<div class="comment-pending">暂未回复</div>';

    card.innerHTML = `
        <div class="comment-card-head">
            <span>留言时间：${escapeHtml(formatDateTime(record.createdAt))}</span>
            <span>${record.contact ? `联系方式：${escapeHtml(record.contact)}` : ''}</span>
        </div>
        <div class="comment-body">${escapeHtml(record.message || '')}</div>
        ${replyHtml}
    `;

    return card;
}

function renderLoadMore(recordsCount) {
    if (!commentsLoadMoreBtn) {
        return;
    }

    loadedComments += recordsCount;
    commentsLoadMoreBtn.style.display = loadedComments < totalComments ? 'block' : 'none';
}

async function loadComments(reset = false) {
    if (!commentList) {
        return;
    }

    if (reset) {
        currentPageIndex = 1;
        totalComments = 0;
        loadedComments = 0;
        commentList.innerHTML = '<div class="comment-empty">留言加载中...</div>';
        if (commentsLoadMoreBtn) {
            commentsLoadMoreBtn.style.display = 'none';
        }
    }

    try {
        const payload = await apiFetch(COMMENTS_PAGE_URL, {
            method: 'POST',
            body: JSON.stringify({
                pageIndex: currentPageIndex,
                pageSize: COMMENTS_PAGE_SIZE
            })
        });
        const records = getRecords(payload);
        totalComments = Number(payload?.data?.total ?? records.length) || 0;

        if (currentPageIndex === 1) {
            commentList.innerHTML = '';
        }

        if (!records.length && currentPageIndex === 1) {
            commentList.innerHTML = '<div class="comment-empty">暂无留言</div>';
            renderLoadMore(0);
            return;
        }

        records.forEach(record => {
            commentList.appendChild(createCommentCard(record));
        });
        renderLoadMore(records.length);
    } catch (error) {
        console.error('加载留言失败:', error);
        commentList.innerHTML = `<div class="comment-empty comment-error">加载留言失败: ${escapeHtml(error.message)}</div>`;
    }
}

async function submitComment(event) {
    event.preventDefault();

    const contact = commentContact.value.trim();
    const message = commentMessage.value.trim();

    if (!contact) {
        setTip('请输入联系方式', 'error');
        commentContact.focus();
        return;
    }

    if (!message) {
        setTip('请输入留言内容', 'error');
        commentMessage.focus();
        return;
    }

    try {
        commentSubmitBtn.disabled = true;
        commentSubmitBtn.textContent = '提交中...';
        setTip('');

        const payload = await apiFetch(COMMENTS_ADD_URL, {
            method: 'POST',
            body: JSON.stringify({ contact, message })
        });

        commentForm.reset();
        setTip('留言提交成功', 'success');
        loadComments(true);
    } catch (error) {
        console.error('提交留言失败:', error);
        setTip(`提交留言失败: ${error.message}`, 'error');
    } finally {
        commentSubmitBtn.disabled = false;
        commentSubmitBtn.textContent = '提交留言';
    }
}

commentForm?.addEventListener('submit', submitComment);

commentRefreshBtn?.addEventListener('click', () => {
    loadComments(true);
});

commentsLoadMoreBtn?.addEventListener('click', () => {
    currentPageIndex += 1;
    loadComments();
});

window.addEventListener('load', () => {
    loadComments(true);
});
