import React from 'react';
import { Bar, Pie } from 'react-chartjs-2';
import { translateFeature } from '../utils/featureLabels';
import { 
  Chart as ChartJS, 
  BarElement, 
  CategoryScale, 
  LinearScale, 
  ArcElement, 
  Tooltip, 
  Legend 
} from 'chart.js';

// Registro obrigatório dos componentes do Chart.js
ChartJS.register(
  BarElement, 
  CategoryScale, 
  LinearScale, 
  ArcElement, 
  Tooltip, 
  Legend
);

// --- GRÁFICO DE PIZZA (CHURN) ---
export function ChurnDistributionChart({
  data,
  labels = ['Fidelizados', 'Churn'],
  colors = ['#1DB954', '#717171ff'],
  hoverColors = ['#1ed760', '#919090ff'],
}) {
  if (!data || !Array.isArray(data)) return null;
  const total = data.reduce((a, b) => a + b, 0);

  const chartData = {
    labels,
    datasets: [{
      data: data,
      backgroundColor: colors,
      borderColor: 'transparent',
      borderWidth: 0,
      // Efeito de fatia separada (Explodido)
      offset: [0, 30], 
      // Destaque adicional ao passar o mouse
      hoverOffset: 50,
      hoverBackgroundColor: hoverColors,
    }],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { 
        position: 'bottom',
        labels: { 
          color: '#ffffff', 
          usePointStyle: true,
          padding: 40, 
          font: { size: 12, weight: 'normal' } 
        } 
      },
      tooltip: {
        backgroundColor: '#191414',
        titleColor: '#1DB954',
        bodyColor: '#ffffff',
        displayColors: false,
        callbacks: {
          label: (context) => {
            const value = context.raw;
            const percentage = ((value / total) * 100).toFixed(1) + '%';
            return `${context.label}: ${value} (${percentage})`;
          }
        }
      }
    },
    layout: { padding: 30 }
  };

  return (
    <div style={{ height: '350px', width: '100%', position: 'relative' }}>
      <Pie data={chartData} options={options} />
    </div>
  );
}

// --- GRÁFICO DE BARRAS (IMPORTÂNCIA COM EFEITO DE HOVER) ---
export function FeatureImportanceChart({ data }) {
  if (!data || !Array.isArray(data)) return null;

  const barPalette = ['#38bdf8', '#60a5fa', '#f59e0b', '#f97316', '#ef4444', '#eab308', '#14b8a6', '#a855f7', '#fb7185', '#84cc16'];

  const chartData = {
    labels: data.map(f => {
      const nomeLimpo = f.name.replace(/^num__/, "").replace(/^cat__/, "");
      return translateFeature(nomeLimpo, nomeLimpo);
    }),
    datasets: [{
      label: 'Impacto no Modelo',
      data: data.map(f => f.value),
      backgroundColor: data.map((_, index) => barPalette[index % barPalette.length]),
      borderRadius: 4,
      hoverBackgroundColor: data.map((_, index) => barPalette[index % barPalette.length]),
      hoverBorderColor: '#ffffff',
      hoverBorderWidth: 1,
    }],
  };

  const options = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
    animation: {
      duration: 400,
      easing: 'easeOutQuart'
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#191414',
        titleColor: '#1DB954',
        bodyColor: '#ffffff',
        callbacks: {
          label: (context) => ` Importância: ${context.raw.toFixed(4)}`
        }
      }
    },
    scales: {
      x: { 
        grid: { color: '#343333ff' },
        ticks: { color: '#b3b3b3', font: { weight: 'normal' } } 
      },
      y: { 
        ticks: { 
          color: '#ffffff', 
          font: { size: 12, weight: 'normal' },
          padding: 10
        } 
      }
    }
  };

  return (
    <div style={{ height: '350px', width: '100%', position: 'relative' }}>
      <Bar data={chartData} options={options} />
    </div>
  );
}