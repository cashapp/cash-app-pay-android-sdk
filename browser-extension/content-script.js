(function() {
  if (window.__cashAppPayInjected) return;
  window.__cashAppPayInjected = true;
  try {
    // Avoid injecting on extension or browser internal pages
    if (location.protocol.startsWith('chrome')) return;

    // Create button
    const btn = document.createElement('button');
    btn.id = 'cash-app-pay-injector-btn';
    btn.innerText = 'Pay with Cash App';
    btn.className = 'cap-injector-btn';

    btn.addEventListener('click', () => {
      // Demo: open Sandbox / developer page in a new tab
      const sandboxUrl = 'https://developers.cash.app/docs/api/technical-documentation/sandbox/sandbox-app';
      window.open(sandboxUrl, '_blank');
    });

    // Append to body
    const container = document.createElement('div');
    container.id = 'cash-app-pay-injector-container';
    container.appendChild(btn);
    document.documentElement.appendChild(container);
  } catch (e) {
    console.error('Cash App Pay injector error', e);
  }
})();
