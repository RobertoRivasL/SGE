const startDateInput = document.getElementById('startDate');
const endDateInput = document.getElementById('endDate');
const applyFilterButton = document.getElementById('applyFilter');
const ventasPorDiaCanvas = document.getElementById('ventasPorDiaChart');
const ventasPorProductoCanvas = document.getElementById('ventasPorProductoChart');
const totalAmountMetric = document.getElementById('totalAmountMetric');
const totalTransactionsMetric = document.getElementById('totalTransactionsMetric');
const totalQuantityMetric = document.getElementById('totalQuantityMetric');
const API_URL = '/api/ventas';

// --- FUNCIONES FETCH ---
async function fetchSalesData(startDate, endDate) {
    let url = API_URL;
    if (startDate && endDate) {
        url = `${API_URL}?startDate=${startDate}&endDate=${endDate}`;
    }
    try {
        const response = await fetch(url, {credentials: 'same-origin'});
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                window.location.href = '/login';
                throw new Error("No autorizado");
            }
            throw new Error(`Error HTTP! estado: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error("Error al obtener datos detallados de ventas:", error);
        throw error;
    }
}

async function fetchSalesSummary(startDate, endDate) {
    let url = `${API_URL}/summary`;
    if (startDate && endDate) {
        url = `${url}?startDate=${startDate}&endDate=${endDate}`;
    }
    try {
        const response = await fetch(url, {credentials: 'same-origin'});
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                window.location.href = '/login';
                throw new Error("No autorizado");
            }
            throw new Error(`Error HTTP fetching summary! estado: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error("Error al obtener resumen de métricas:", error);
        throw error;
    }
}

// --- FUNCIONES DE PROCESAMIENTO DE DATOS ---
function processSalesDataForDailyChart(salesData) {
    const salesByDate = salesData.reduce((acc, venta) => {
        const date = venta.fecha;
        const total = venta.cantidad * venta.precioUnitario;
        acc[date] = (acc[date] || 0) + total;
        return acc;
    }, {});
    const labels = Object.keys(salesByDate).sort();
    const data = labels.map(date => parseFloat(salesByDate[date].toFixed(2)));
    return {labels, data};
}

function processSalesDataForProductChart(salesData) {
    const salesByProduct = salesData.reduce((acc, venta) => {
        const producto = venta.producto;
        const total = venta.cantidad * venta.precioUnitario;
        acc[producto] = (acc[producto] || 0) + total;
        return acc;
    }, {});
    const labels = Object.keys(salesByProduct);
    const data = labels.map(prod => parseFloat(salesByProduct[prod].toFixed(2)));
    return {labels, data};
}

// --- VISUALIZACIÓN DE DATOS ---
function updateOrCreateChart(canvas, type, labels, data, labelText) {
    // Si el gráfico existe, actualizamos sus datos y opciones
    if (canvas.chart) {
        canvas.chart.data.labels = labels;
        canvas.chart.data.datasets[0].data = data;
        canvas.chart.data.datasets[0].label = labelText;
        canvas.chart.options.scales.x.title.text = labelText.includes('Día') ? 'Fecha' : 'Producto';
        canvas.chart.options.plugins.title.text = labelText;
        canvas.chart.update();
    } else {
        // Si no existe, se crea un nuevo gráfico
        canvas.chart = new Chart(canvas, {
            type: type,
            data: {
                labels: labels,
                datasets: [{
                    label: labelText,
                    data: data,
                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {display: true, text: 'Valor Total ($)'}
                    },
                    x: {
                        title: {display: true, text: labelText.includes('Día') ? 'Fecha' : 'Producto'}
                    }
                },
                plugins: {
                    legend: {display: true},
                    title: {display: true, text: labelText}
                }
            }
        });
    }
}

function displaySalesSummary(summaryData) {
    if (summaryData) {
        totalAmountMetric.textContent = `$${summaryData.totalAmount.toFixed(2)}`;
        totalTransactionsMetric.textContent = `${summaryData.totalTransactions}`;
        totalQuantityMetric.textContent = `${summaryData.totalQuantity}`;
    } else {
        totalAmountMetric.textContent = '-';
        totalTransactionsMetric.textContent = '-';
        totalQuantityMetric.textContent = '-';
    }
}

// --- FUNCIÓN PRINCIPAL DE CARGA ---
async function loadDashboardData(startDate, endDate) {
    try {
        // Ejecutamos ambas peticiones en paralelo
        const [salesData, summary] = await Promise.all([
            fetchSalesData(startDate, endDate),
            fetchSalesSummary(startDate, endDate)
        ]);
        if (salesData && salesData.length > 0) {
            const dailyChartData = processSalesDataForDailyChart(salesData);
            const productChartData = processSalesDataForProductChart(salesData);
            updateOrCreateChart(ventasPorDiaCanvas, 'bar', dailyChartData.labels, dailyChartData.data, 'Ventas por Día');
            updateOrCreateChart(ventasPorProductoCanvas, 'bar', productChartData.labels, productChartData.data, 'Ventas por Producto');
        } else {
            if (ventasPorDiaCanvas.chart) ventasPorDiaCanvas.chart.destroy();
            if (ventasPorProductoCanvas.chart) ventasPorProductoCanvas.chart.destroy();
        }
        displaySalesSummary(summary);
    } catch (error) {
        console.error("Error al cargar el panel:", error);
        displaySalesSummary(null);
        if (ventasPorDiaCanvas.chart) ventasPorDiaCanvas.chart.destroy();
        if (ventasPorProductoCanvas.chart) ventasPorProductoCanvas.chart.destroy();
    }
}

// --- EVENTOS ---
applyFilterButton.addEventListener('click', () => {
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;
    if ((startDate && endDate) || (!startDate && !endDate)) {
        loadDashboardData(startDate, endDate);
    } else {
        alert('Por favor, selecciona ambas fechas para aplicar el filtro.');
    }
});
window.addEventListener('DOMContentLoaded', () => loadDashboardData());

