const API_BASE_URL = 'http://localhost:8000';
const FUND_TYPE = document.body.dataset.fundType || 'deposit';
const FUND_CREATE_URL = `${API_BASE_URL}/api/order/${FUND_TYPE === 'withdraw' ? 'withdraworder' : 'depositorder'}/create`;

const fundOrderForm = document.getElementById('fundOrderForm');
const amountInput = document.getElementById('amount');
const chainTypeInput = document.getElementById('chainType');
const txHashInput = document.getElementById('txHash');
const withdrawAddressInput = document.getElementById('withdrawAddress');
const remarkInput = document.getElementById('remark');
const fundMessage = document.getElementById('fundMessage');
const fundSubmitBtn = document.getElementById('fundSubmitBtn');

function getAuthHeaders() {
    return {
        Authorization: `Bearer ${localStorage.getItem('authToken') || ''}`,
        'Content-Type': 'application/json'
    };
}

function isSuccessCode(code) {
    return code === 0 || code === 200 || code === '0' || code === '200';
}

function setFundMessage(message, type = '') {
    fundMessage.textContent = message;
    fundMessage.className = `fund-message ${type}`.trim();
}

function validateFundForm() {
    const amount = Number(amountInput.value);
    const chainType = chainTypeInput.value.trim();

    if (!Number.isFinite(amount) || amount <= 0) {
        return '请输入有效金额';
    }

    if (!chainType) {
        return '请输入链类型';
    }

    if (FUND_TYPE === 'deposit' && !txHashInput.value.trim()) {
        return '请输入交易哈希';
    }

    if (FUND_TYPE === 'withdraw' && !withdrawAddressInput.value.trim()) {
        return '请输入提现地址';
    }

    return '';
}

function buildRequestBody() {
    const body = {
        amount: Number(amountInput.value),
        currencyCode: 'USDT',
        chainType: chainTypeInput.value.trim(),
        remark: remarkInput.value.trim()
    };

    if (FUND_TYPE === 'deposit') {
        body.txHash = txHashInput.value.trim();
    } else {
        body.withdrawAddress = withdrawAddressInput.value.trim();
    }

    return body;
}

async function submitFundOrder(event) {
    event.preventDefault();

    const errorMessage = validateFundForm();
    if (errorMessage) {
        setFundMessage(errorMessage, 'error');
        return;
    }

    try {
        fundSubmitBtn.disabled = true;
        fundSubmitBtn.textContent = '提交中...';
        setFundMessage('');

        const response = await fetch(FUND_CREATE_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(buildRequestBody())
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const payload = await response.json();
        if (!isSuccessCode(payload.code)) {
            throw new Error(payload.message || '提交失败');
        }

        const orderNo = payload?.data?.orderNo;
        fundOrderForm.reset();
        document.getElementById('currencyCode').value = 'USDT';
        setFundMessage(`${FUND_TYPE === 'withdraw' ? '提现申请' : '充值订单'}提交成功${orderNo ? `，订单号：${orderNo}` : ''}`, 'success');
    } catch (error) {
        console.error('提交资金订单失败:', error);
        setFundMessage(`提交失败: ${error.message}`, 'error');
    } finally {
        fundSubmitBtn.disabled = false;
        fundSubmitBtn.textContent = FUND_TYPE === 'withdraw' ? '提交提现' : '提交充值';
    }
}

fundOrderForm?.addEventListener('submit', submitFundOrder);
