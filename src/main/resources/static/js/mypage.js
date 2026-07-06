document.addEventListener('DOMContentLoaded', () => {
  const carousel = document.querySelector('#recommendCarousel');
  const carouselShell = carousel?.closest('.recommend-carousel');
  const prevButton = document.querySelector('.recommend-prev');
  const nextButton = document.querySelector('.recommend-next');

  const loadingEl = document.getElementById('recommendLoading');

  function escapeHtml(str) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
  }

  function renderProducts(products) {
    if (!carousel) return;
    if (loadingEl) loadingEl.classList.add('is-hidden');
    if (products.length === 0) return;
    carousel.innerHTML = products
      .map(
        (p) => `
      <article class="product-card recommend-card pb-2">
        <div class="product-image-frame position-relative overflow-hidden mb-2 rounded">
          <a class="product-image-link d-block" href="/product/${encodeURIComponent(p.id)}">
            <img class="d-block w-100" alt="${escapeHtml(p.name)}" src="${escapeHtml(p.imageUrl)}" loading="lazy" />
          </a>
          <button
            aria-label="관심 상품 즐겨찾기"
            class="product-favorite-button position-absolute d-inline-flex align-items-center justify-content-center rounded-circle${p.favorited ? ' is-active' : ''}"
            data-product-id="${p.id}"
            aria-pressed="${p.favorited}"
            type="button"
          >
            <i aria-hidden="true" class="bi ${p.favorited ? 'bi-star-fill' : 'bi-star'}"></i>
          </button>
        </div>
        <h3 class="mb-2">${escapeHtml(p.name)}</h3>
        <div class="product-price d-flex align-items-baseline gap-2 mb-2">
          <strong>${escapeHtml(p.formattedPrice)}</strong>
        </div>
      </article>
    `,
      )
      .join('');
  }

  fetch('/api/recommendation/products')
    .then((res) => res.json())
    .then((data) => {
      renderProducts(data.products);
      initCarousel();
    })
    .catch(() => {
      if (loadingEl) loadingEl.classList.add('is-hidden');
    });

  function initCarousel() {
    if (!carousel || !carouselShell || !prevButton || !nextButton) return;

    const edgeTolerance = 2;

    const getStep = () => {
      const firstCard = carousel.querySelector('.recommend-card');
      if (!firstCard) return carousel.clientWidth;
      const styles = window.getComputedStyle(carousel);
      const gap = Number.parseFloat(styles.columnGap || styles.gap) || 0;
      return firstCard.getBoundingClientRect().width + gap;
    };

    const updateButtons = () => {
      updateNavPosition();
      const maxScrollLeft = carousel.scrollWidth - carousel.clientWidth;
      const atStart = carousel.scrollLeft <= edgeTolerance;
      const atEnd = carousel.scrollLeft >= maxScrollLeft - edgeTolerance;
      prevButton.classList.toggle('is-hidden', atStart);
      nextButton.classList.toggle('is-hidden', atEnd || maxScrollLeft <= edgeTolerance);
    };

    const updateNavPosition = () => {
      const imageFrame = carousel.querySelector('.product-image-frame');
      if (!imageFrame) return;
      const shellRect = carouselShell.getBoundingClientRect();
      const imageRect = imageFrame.getBoundingClientRect();
      const imageCenterY = imageRect.top - shellRect.top + imageRect.height / 2;
      carouselShell.style.setProperty('--recommend-nav-top', `${imageCenterY}px`);
    };

    const scrollByDirection = (direction) => {
      carousel.scrollBy({ left: getStep() * direction, behavior: 'smooth' });
    };

    prevButton.addEventListener('click', () => scrollByDirection(-1));
    nextButton.addEventListener('click', () => scrollByDirection(1));
    carousel.addEventListener('scroll', updateButtons, { passive: true });
    window.addEventListener('resize', updateButtons);
    carousel.querySelectorAll('img').forEach((image) => {
      image.addEventListener('load', updateButtons, { once: true });
    });

    updateButtons();
  }
});
