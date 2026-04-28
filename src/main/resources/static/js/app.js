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

  // Produtos admin - frontend CRUD usando localStorage
  const productForm = document.getElementById('product-form');
  const productIdInput = document.getElementById('product-id');
  const productNameInput = document.getElementById('product-name');
  const productPriceInput = document.getElementById('product-price');
  const productDescriptionInput = document.getElementById('product-description');
  const productStockInput = document.getElementById('product-stock');
  const productImageInput = document.getElementById('product-image');
  const productPreviewImg = document.getElementById('product-preview');
  const productsTbody = document.getElementById('products-tbody');
  const refreshProductsBtn = document.getElementById('refresh-products');
  const productClearBtn = document.getElementById('product-clear');
  let currentImageData = null;

  function loadProducts() {
    try {
      return JSON.parse(localStorage.getItem('adminProducts') || '[]');
    } catch (e) { return []; }
  }

  function saveProducts(list) {
    try { localStorage.setItem('adminProducts', JSON.stringify(list)); } catch (e) { console.warn(e); }
  }

  function renderProducts() {
    const products = loadProducts();
    productsTbody.innerHTML = '';
    if (!products || !products.length) {
      productsTbody.innerHTML = '<tr><td colspan="6" class="muted">Nenhum produto cadastrado</td></tr>';
      return;
    }
    products.forEach((p) => {
      const tr = document.createElement('tr');
      const tdName = document.createElement('td'); tdName.textContent = p.name;
      const tdDescription = document.createElement('td'); tdDescription.textContent = p.description || '';
      const tdImage = document.createElement('td');
      if (p.image) {
        const img = document.createElement('img'); img.src = p.image; img.className = 'product-thumb'; img.alt = p.name;
        tdImage.appendChild(img);
      } else {
        tdImage.textContent = '—';
      }
      const tdPrice = document.createElement('td'); tdPrice.textContent = p.price;
      const tdStock = document.createElement('td'); tdStock.textContent = (typeof p.stock !== 'undefined') ? p.stock : '—';
      const tdActions = document.createElement('td');

      const editBtn = document.createElement('button'); editBtn.className = 'admin-action-btn'; editBtn.textContent = 'Editar';
      editBtn.addEventListener('click', () => {
        productIdInput.value = p.id;
        productNameInput.value = p.name;
        if (productDescriptionInput) productDescriptionInput.value = p.description || '';
        productPriceInput.value = p.price;
        if (productStockInput) productStockInput.value = (typeof p.stock !== 'undefined') ? p.stock : '';
        currentImageData = p.image || null;
        if (productPreviewImg) {
          if (currentImageData) { productPreviewImg.src = currentImageData; productPreviewImg.style.display = 'block'; }
          else { productPreviewImg.src = ''; productPreviewImg.style.display = 'none'; }
        }
        // switch to produtos tab if not active
        const produtosBtn = document.querySelector('.admin-nav-link[data-tab-target="produtos"]');
        if (produtosBtn) produtosBtn.click();
      });

      const delForm = document.createElement('form'); delForm.className = 'inline-delete-form';
      delForm.addEventListener('submit', (e) => { e.preventDefault(); });
      const delBtn = document.createElement('button'); delBtn.className = 'admin-action-btn danger'; delBtn.textContent = 'Remover';
      delBtn.addEventListener('click', () => {
        if (!confirm('Remover produto?')) return;
        const remaining = loadProducts().filter(x => x.id !== p.id);
        saveProducts(remaining);
        renderProducts();
      });

      delForm.appendChild(delBtn);
      tdActions.appendChild(editBtn);
      tdActions.appendChild(delForm);

      tr.appendChild(tdName); tr.appendChild(tdDescription); tr.appendChild(tdImage); tr.appendChild(tdPrice); tr.appendChild(tdStock); tr.appendChild(tdActions);
      productsTbody.appendChild(tr);
    });
  }

  if (productForm) {
    productForm.addEventListener('submit', function (e) {
      e.preventDefault();
      const id = productIdInput.value || '';
      const name = productNameInput.value.trim();
      const price = productPriceInput.value.trim();
      if (!name || !price) return alert('Preencha nome e preço');

      function upsertProduct(imageData) {
        const products = loadProducts();
        const description = productDescriptionInput ? productDescriptionInput.value.trim() : '';
        const stock = productStockInput ? parseInt(productStockInput.value, 10) || 0 : 0;
        if (id) {
          const idx = products.findIndex(p => p.id === id);
          if (idx > -1) {
            products[idx].name = name; products[idx].price = price; products[idx].image = imageData || null;
            products[idx].description = description; products[idx].stock = stock;
          }
        } else {
          products.push({ id: String(Date.now()), name, price, image: imageData || null, description, stock });
        }
        saveProducts(products);
        renderProducts();
        productForm.reset(); productIdInput.value = ''; currentImageData = null; if (productPreviewImg) { productPreviewImg.src = ''; productPreviewImg.style.display = 'none'; }
      }

      // if a new file was chosen, read it as data URL first
      if (productImageInput && productImageInput.files && productImageInput.files[0]) {
        const file = productImageInput.files[0];
        const reader = new FileReader();
        reader.onload = function (ev) { upsertProduct(ev.target.result); };
        reader.readAsDataURL(file);
      } else {
        // keep existing image if editing, or null when creating
        upsertProduct(currentImageData);
      }
    });
  }

  if (productClearBtn) {
    productClearBtn.addEventListener('click', () => { productForm.reset(); productIdInput.value = ''; currentImageData = null; if (productPreviewImg) { productPreviewImg.src = ''; productPreviewImg.style.display = 'none'; } if (productDescriptionInput) productDescriptionInput.value = ''; if (productStockInput) productStockInput.value = 0; });
  }

  if (productImageInput) {
    productImageInput.addEventListener('change', function () {
      const f = this.files && this.files[0];
      if (!f) { currentImageData = null; if (productPreviewImg) { productPreviewImg.src = ''; productPreviewImg.style.display = 'none'; } return; }
      const reader = new FileReader();
      reader.onload = function (ev) { currentImageData = ev.target.result; if (productPreviewImg) { productPreviewImg.src = currentImageData; productPreviewImg.style.display = 'block'; } };
      reader.readAsDataURL(f);
    });
  }

  if (refreshProductsBtn) refreshProductsBtn.addEventListener('click', renderProducts);

  // initial render for products tab
  try { renderProducts(); } catch (e) { /* ignore if elements missing */ }
});

function sendWhatsApp(productName, price) {
  const phone = "5511999999999";
  const text = `Olá, gostaria de pedir ${productName} por R$ ${price}`;
  const url = `https://wa.me/${phone}?text=${encodeURIComponent(text)}`;
  window.open(url, "_blank");
}

window.sendWhatsApp = sendWhatsApp;