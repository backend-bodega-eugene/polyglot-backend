(function () {
    const balanceText = localStorage.getItem('defaultBalanceText') || '💰 0.00';
    document.querySelectorAll('.balance').forEach(element => {
        element.textContent = balanceText;
    });
})();
