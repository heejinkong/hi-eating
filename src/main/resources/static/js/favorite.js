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

    const csrfToken = (() => {
      const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
      return match ? decodeURIComponent(match[1]) : null;
    })();
    const headers = {
      Accept: 'application/json',
    };

    if (csrfToken) {
      headers['X-XSRF-TOKEN'] = csrfToken;
    }

    button.classList.add('is-loading');
    button.disabled = true;

    try {
      const response = await fetch(`/api/favorites/${productId}/toggle`, {
        method: 'POST',
        headers,
      });

      if (!response.ok) {
        const error = new Error('Failed to toggle favorite.');
        error.status = response.status;
        error.redirectedToLogin = response.redirected && response.url.includes('/login');
        throw error;
      }

      if (response.redirected && response.url.includes('/login')) {
        const error = new Error('Login required.');
        error.redirectedToLogin = true;
        throw error;
      }

      const contentType = response.headers.get('content-type') || '';
      if (!contentType.includes('application/json')) {
        const error = new Error('Unexpected favorite response.');
        error.redirectedToLogin = response.url.includes('/login');
        throw error;
      }

      const data = await response.json();
      setFavoriteButtonState(button, data.favorite);

      if (!data.favorite) {
        updateFavoritePageAfterRemove(button);
      }
    } catch (error) {
      console.error(error);
      const message =
        error.status === 401 || error.status === 403 || error.redirectedToLogin
          ? '즐겨찾기는 로그인한 사용자만 이용할 수 있습니다. 로그인 후 다시 시도해주세요.'
          : '즐겨찾기 처리에 실패했습니다. 잠시 후 다시 시도해주세요.';
      window.alert(message);
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
