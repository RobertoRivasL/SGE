document.addEventListener('DOMContentLoaded', function () {
    const ventasPorPeriodoData = window.ventasPorPeriodoData || [];
    const ventasPorCategoriaData = window.ventasPorCategoriaData || [];

    // Gráfico de ventas por período
    if (ventasPorPeriodoData.length > 0) {
        const ctxPeriodo = document.getElementById('ventasPorPeriodoChart').getContext('2d');
        new Chart(ctxPeriodo, {
            type: 'line',
            data: {
                labels: ventasPorPeriodoData.map(item => item.periodo),
                datasets: [{
                    label: 'Ventas',
                    data: ventasPorPeriodoData.map(item => item.total),
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function (value) {
                                return '$' + value.toLocaleString();
                            }
                        }
                    }
                }
            }
        });
    }

    // Gráfico de ventas por categoría
    if (ventasPorCategoriaData.length > 0) {
        const ctxCategoria = document.getElementById('ventasPorCategoriaChart').getContext('2d');
        new Chart(ctxCategoria, {
            type: 'doughnut',
            data: {
                labels: ventasPorCategoriaData.map(item => item.categoria),
                datasets: [{
                    data: ventasPorCategoriaData.map(item => item.total),
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.7)',
                        'rgba(54, 162, 235, 0.7)',
                        'rgba(255, 206, 86, 0.7)',
                        'rgba(75, 192, 192, 0.7)',
                        'rgba(153, 102, 255, 0.7)',
                        'rgba(255, 159, 64, 0.7)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {position: 'right'},
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.label || '';
                                let value = context.raw;
                                return `${label}: $${value.toLocaleString()}`;
                            }
                        }
                    }
                }
            }
        });
    }
});
