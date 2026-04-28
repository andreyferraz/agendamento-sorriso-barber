document.addEventListener("DOMContentLoaded", function () {
  const toggler = document.querySelector(".nav-toggle");
  const links = document.querySelector(".nav-links");

  if (toggler && links) {
    toggler.addEventListener("click", function () {
      const isOpen = links.classList.toggle("open");
      toggler.setAttribute("aria-expanded", String(isOpen));
      toggler.textContent = isOpen ? "✕" : "☰";
      toggler.setAttribute("aria-label", isOpen ? "Fechar menu" : "Abrir menu");
    });

    links.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", () => {
        if (links.classList.contains("open")) {
          links.classList.remove("open");
          toggler.setAttribute("aria-expanded", "false");
          toggler.textContent = "☰";
          toggler.setAttribute("aria-label", "Abrir menu");
        }
      });
    });
  }

  const header = document.querySelector(".site-header");
  const scrollHandler = () => {
    if (!header) return;
    if (window.scrollY > 20) header.classList.add("scrolled");
    else header.classList.remove("scrolled");
  };

  scrollHandler();
  window.addEventListener("scroll", scrollHandler, { passive: true });

  const currentPath = window.location.pathname.replace(/\/$/, "") || "/";
  document.querySelectorAll(".nav-links a:not(.nav-cta)").forEach((link) => {
    const linkPath =
      new URL(link.href, window.location.origin).pathname.replace(/\/$/, "") || "/";
    if (linkPath === currentPath) {
      link.classList.add("active");
    }
  });

  const adminNavLinks = document.querySelectorAll(".admin-nav-link[data-tab-target]");
  const adminPanels = document.querySelectorAll(".admin-tab[data-tab-panel]");

  if (adminNavLinks.length && adminPanels.length) {
    // restore previously active admin tab from localStorage
    const savedTab = localStorage.getItem('adminActiveTab');
    if (savedTab) {
      const savedBtn = document.querySelector(`.admin-nav-link[data-tab-target="${savedTab}"]`);
      const savedPanel = document.querySelector(`.admin-tab[data-tab-panel="${savedTab}"]`);
      if (savedBtn && savedPanel) {
        adminNavLinks.forEach((item) => item.classList.remove("active"));
        adminPanels.forEach((panel) => panel.classList.remove("active"));
        savedBtn.classList.add("active");
        savedPanel.classList.add("active");
      }
    }

    adminNavLinks.forEach((button) => {
      button.addEventListener("click", function () {
        const target = button.getAttribute("data-tab-target");

        adminNavLinks.forEach((item) => item.classList.remove("active"));
        adminPanels.forEach((panel) => panel.classList.remove("active"));

        button.classList.add("active");
        const targetPanel = document.querySelector(`.admin-tab[data-tab-panel="${target}"]`);
        if (targetPanel) targetPanel.classList.add("active");

        // persist selected tab so it remains after page reload
        try {
          localStorage.setItem('adminActiveTab', target);
        } catch (e) {
          // storage might be unavailable in some privacy modes
          console.warn('Could not persist admin tab selection', e);
        }
      });
    });
  }

  // clear persisted admin tab when the user clicks logout
  const logoutLink = document.getElementById('admin-logout');
  if (logoutLink) {
    logoutLink.addEventListener('click', function () {
      try { localStorage.removeItem('adminActiveTab'); } catch (e) { /* ignore */ }
    });
  }
});

function sendWhatsApp(productName, price) {
  const phone = "5511999999999";
  const text = `Olá, gostaria de pedir ${productName} por R$ ${price}`;
  const url = `https://wa.me/${phone}?text=${encodeURIComponent(text)}`;
  window.open(url, "_blank");
}

window.sendWhatsApp = sendWhatsApp;