document.addEventListener('DOMContentLoaded', () => {
  if (typeof Chart === 'undefined') {
    document.querySelectorAll('.chart-card').forEach((card) => {
      card.classList.add('chart-unavailable');
    });
    return;
  }

  Chart.register(centerTextPlugin);

  renderBarChart('ageSalesChart');
  renderDoughnutChart('categorySalesChart');
  renderDoughnutChart('genderSalesChart');
  renderBarChart('priceSalesChart');
});

const centerTextPlugin = {
  id: 'centerText',
  afterDraw(chart) {
    const value = chart.canvas.dataset.centerValue;
    if (!value) return;

    const { ctx, chartArea } = chart;
    const x = (chartArea.left + chartArea.right) / 2;
    const y = (chartArea.top + chartArea.bottom) / 2;

    ctx.save();
    ctx.textAlign = 'center';
    ctx.fillStyle = '#8b8b8b';
    ctx.font = '700 12px sans-serif';
    ctx.fillText('총 매출', x, y - 8);
    ctx.fillStyle = '#111111';
    ctx.font = '800 15px sans-serif';
    ctx.fillText(value, x, y + 14);
    ctx.restore();
  },
};

function renderBarChart(canvasId) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return;

  const chartData = readChartData(canvas);

  new Chart(canvas, {
    type: 'bar',
    data: {
      labels: chartData.labels,
      datasets: [
        {
          data: chartData.values,
          backgroundColor: chartData.colors,
          borderRadius: 8,
          borderSkipped: false,
          barThickness: 38,
          maxBarThickness: 44,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          displayColors: false,
          callbacks: {
            label(context) {
              return `매출 ${formatCurrency(context.parsed.y)}`;
            },
          },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: {
            color: '#777777',
            font: { size: 11, weight: '600' },
            maxRotation: 0,
            autoSkip: false,
          },
        },
        y: {
          beginAtZero: true,
          border: { display: false },
          grid: { color: '#eeeeee' },
          ticks: {
            color: '#888888',
            font: { size: 11 },
            callback(value) {
              return Number(value) / 1000;
            },
          },
        },
      },
    },
  });
}

function renderDoughnutChart(canvasId) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return;

  const chartData = readChartData(canvas);

  new Chart(canvas, {
    type: 'doughnut',
    data: {
      labels: chartData.labels,
      datasets: [
        {
          data: chartData.values,
          backgroundColor: chartData.colors,
          borderWidth: 0,
          hoverOffset: 4,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '66%',
      plugins: {
        legend: { display: false },
        tooltip: {
          displayColors: true,
          callbacks: {
            label(context) {
              return `${context.label}: ${formatCurrency(context.parsed)}`;
            },
          },
        },
      },
    },
  });
}

function readChartData(canvas) {
  const labels = splitData(canvas.dataset.labels);
  const values = splitData(canvas.dataset.values).map((value) => Number(value));
  const colors = splitData(canvas.dataset.colors);

  return { labels, values, colors };
}

function splitData(value) {
  if (!value) return [];
  const separator = value.includes('|') ? '|' : ',';
  return value.split(separator).map((item) => item.trim());
}

function formatCurrency(value) {
  return `${Number(value).toLocaleString('ko-KR')}원`;
}
