(function initTheme() {
  const savedTheme = localStorage.getItem('epis-theme') || 'light';
  document.documentElement.dataset.theme = savedTheme === 'dark' ? 'dark' : 'light';
})();

document.addEventListener('DOMContentLoaded', () => {
  updateToggleButtons();
  startSystemClock();

  document.querySelectorAll('[data-ui-action]').forEach((button) => {
    button.addEventListener('click', () => {
      const action = button.dataset.uiAction;
      if (action === 'contrast') {
        const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        localStorage.setItem('epis-theme', next);
        updateToggleButtons();
      }
      if (action === 'settings') {
        const drawer = document.getElementById('settingsDrawer');
        if (drawer) {
          drawer.classList.toggle('open');
          drawer.setAttribute('aria-hidden', drawer.classList.contains('open') ? 'false' : 'true');
        }
      }
    });
  });

  const drawer = document.getElementById('settingsDrawer');
  if (drawer) {
    drawer.addEventListener('click', (event) => {
      if (event.target === drawer) {
        drawer.classList.remove('open');
        drawer.setAttribute('aria-hidden', 'true');
      }
    });
  }

});

function updateToggleButtons() {
  const isDark = document.documentElement.dataset.theme === 'dark';
  document.querySelectorAll('[data-ui-action="contrast"]').forEach(btn => {
    btn.textContent = isDark ? 'Modo claro' : 'Modo oscuro';
    btn.setAttribute('aria-label', isDark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
    btn.setAttribute('title', isDark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
  });
}

function startSystemClock() {
  const clock = document.getElementById('systemClock');
  const date = document.getElementById('systemDate');
  if (!clock || !date) { return; }

  const render = () => {
    const now = new Date();
    clock.textContent = now.toLocaleTimeString('es-PE', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    date.textContent = now.toLocaleDateString('es-PE', {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  render();
  setInterval(render, 1000);
}
