(function () {
    const routeSelect = document.getElementById('routeId');
    const dateInput = document.getElementById('travelDate');
    const priceSelect = document.getElementById('priceId');

    async function loadPrices() {
        const routeId = routeSelect.value;
        const travelDate = dateInput.value;
        priceSelect.innerHTML = '<option value="">Загрузка...</option>';

        if (!routeId || !travelDate) {
            priceSelect.innerHTML = '<option value="">— сначала выберите маршрут и дату —</option>';
            return;
        }

        try {
            const response = await fetch(`/tickets/sell/prices?routeId=${routeId}&travelDate=${travelDate}`);
            const html = await response.text();
            priceSelect.innerHTML = html;
        } catch (e) {
            priceSelect.innerHTML = '<option value="">Ошибка загрузки тарифов</option>';
        }
    }

    routeSelect.addEventListener('change', loadPrices);
    dateInput.addEventListener('change', loadPrices);
})();
