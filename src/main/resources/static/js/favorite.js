(() => {
  const setFavoriteButtonState = (button, favorite) => {
    button.classList.toggle('is-active', favorite);
    button.setAttribute('aria-pressed', String(favorite));

    const icon = button.querySelector('i');
    if (icon) {
      icon.classList.toggle('bi-star', !favorite);
      icon.classList.toggle('bi-star-fill', favorite);
    }
  };

  const updateFavoritePageAfterRemove = (button) => {
    const page = document.querySelector('[data-favorite-page]');
    if (!page) {
      return;
    }

    const card = button.closest('[data-favorite-product-card]');
    if (card) {
      card.remove();
    }

    const count = page.querySelector('[data-favorite-total-count]');
    if (count) {
      const nextCount = Math.max(0, Number(count.textContent || '0') - 1);
      count.textContent = String(nextCount);
    }

    const grid = page.querySelector('[data-favorite-grid]');
    const empty = page.querySelector('[data-favorite-empty]');
    const hasCards = Boolean(grid?.querySelector('[data-favorite-product-card]'));

    if (grid) {
      grid.hidden = !hasCards;
    }

    if (empty) {
      empty.hidden = hasCards;
    }

    if (!hasCards && page.querySelector('.favorite-pagination')) {
      window.location.reload();
    }
  };

  const toggleFavorite = async (button) => {
    const productId = button.dataset.productId;
    if (!productId || button.classList.contains('is-loading')) {
      return;
    }

    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;
    const headers = {
      Accept: 'application/json',
    };

    if (csrfToken && csrfHeader) {
      headers[csrfHeader] = csrfToken;
    }

    button.classList.add('is-loading');
    button.disabled = true;

    try {
      const response = await fetch(`/api/favorites/${productId}/toggle`, {
        method: 'POST',
        headers,
      });

      if (!response.ok) {
        throw new Error('Failed to toggle favorite.');
      }

      const data = await response.json();
      setFavoriteButtonState(button, data.favorite);

      if (!data.favorite) {
        updateFavoritePageAfterRemove(button);
      }
    } catch (error) {
      console.error(error);
      window.alert(
        '\uC990\uACA8\uCC3E\uAE30 \uCC98\uB9AC\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
      );
    } finally {
      button.classList.remove('is-loading');
      button.disabled = false;
    }
  };

  document.addEventListener('click', (event) => {
    const button = event.target.closest('.product-favorite-button');
    if (!button) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();
    toggleFavorite(button);
  });
})();
