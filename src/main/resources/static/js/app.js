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
});

function sendWhatsApp(productName, price) {
  const phone = "5511999999999";
  const text = `Olá, gostaria de pedir ${productName} por R$ ${price}`;
  const url = `https://wa.me/${phone}?text=${encodeURIComponent(text)}`;
  window.open(url, "_blank");
}

window.sendWhatsApp = sendWhatsApp;

document.addEventListener("DOMContentLoaded", function () {
  const loginForm = document.querySelector("#adminLoginForm");

  if (loginForm) {
    loginForm.addEventListener("submit", function (event) {
      event.preventDefault();
      const redirectUrl = loginForm.getAttribute("data-redirect-url") || "/admin";
      window.location.href = redirectUrl;
    });
  }

  const adminNavLinks = document.querySelectorAll(".admin-nav-link[data-tab-target]");
  const adminPanels = document.querySelectorAll(".admin-tab[data-tab-panel]");

  if (adminNavLinks.length && adminPanels.length) {
    adminNavLinks.forEach((button) => {
      button.addEventListener("click", function () {
        const target = button.getAttribute("data-tab-target");

        adminNavLinks.forEach((item) => item.classList.remove("active"));
        adminPanels.forEach((panel) => panel.classList.remove("active"));

        button.classList.add("active");
        const targetPanel = document.querySelector(`.admin-tab[data-tab-panel="${target}"]`);
        if (targetPanel) targetPanel.classList.add("active");
      });
    });
  }
});
