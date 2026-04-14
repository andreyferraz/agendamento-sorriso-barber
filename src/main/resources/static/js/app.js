document.addEventListener("DOMContentLoaded", function () {
  const toggler = document.querySelector(".nav-toggle");
  const links = document.querySelector(".nav-links");

  if (toggler && links) {
    toggler.addEventListener("click", function () {
      const isOpen = links.classList.toggle("open");
      toggler.setAttribute("aria-expanded", String(isOpen));
      // toggle hamburger icon to X when open
      toggler.textContent = isOpen ? '✕' : '☰';
      toggler.setAttribute('aria-label', isOpen ? 'Fechar menu' : 'Abrir menu');
    });

    // close menu when a nav link is clicked (mobile behavior)
    links.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => {
        if (links.classList.contains('open')) {
          links.classList.remove('open');
          toggler.setAttribute('aria-expanded', 'false');
          toggler.textContent = '☰';
          toggler.setAttribute('aria-label', 'Abrir menu');
        }
      });
    });
  }
  // Header scroll behavior: toggle 'scrolled' class when page is scrolled
  const header = document.querySelector('.site-header');
  const scrollHandler = () => {
    if (!header) return;
    if (window.scrollY > 20) header.classList.add('scrolled');
    else header.classList.remove('scrolled');
  };
  scrollHandler();
  window.addEventListener('scroll', scrollHandler, { passive: true });
});

function sendWhatsApp(productName, price) {
  const phone = "5511999999999";
  const text = `Olá, gostaria de pedir ${productName} por R$ ${price}`;
  const url = `https://wa.me/${phone}?text=${encodeURIComponent(text)}`;
  window.open(url, "_blank");
}

window.sendWhatsApp = sendWhatsApp;