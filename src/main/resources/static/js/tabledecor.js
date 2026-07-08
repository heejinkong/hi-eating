document.addEventListener('DOMContentLoaded', () => {
  const page = document.querySelector('.table-decor-page');
  const previewModal = document.querySelector('[data-table-decor-modal]');
  const authModal = document.querySelector('[data-table-decor-auth-modal]');

  if (!page || !previewModal || !authModal) {
    return;
  }

  const isAuthenticated = page.dataset.tableDecorAuthenticated === 'true';
  const previewImage = previewModal.querySelector('[data-table-decor-modal-image]');
  const previewOwner = previewModal.querySelector('[data-table-decor-modal-owner]');
  const previewDate = previewModal.querySelector('[data-table-decor-modal-date]');
  const previewLikeCount = previewModal.querySelector('[data-table-decor-modal-like]');
  const previewLikeButton = previewModal.querySelector('[data-table-decor-modal-like-button]');
  const authMessage = authModal.querySelector('[data-table-decor-auth-message]');
  const feed = document.querySelector('[data-table-decor-feed]');
  const loader = document.querySelector('[data-table-decor-loader]');
  const sentinel = document.querySelector('[data-table-decor-sentinel]');
  let lastFocusedElement = null;
  let currentPage = Number(page.dataset.tableDecorPage || '1');
  let totalPages = Number(page.dataset.tableDecorTotalPages || '1');
  let loading = false;

  const getCsrfToken = () => {
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
    return match ? decodeURIComponent(match[1]) : '';
  };

  const setBodyLocked = () => {
    document.body.style.overflow = 'hidden';
  };

  const releaseBodyLock = () => {
    if (previewModal.hidden && authModal.hidden) {
      document.body.style.overflow = '';
    }
  };

  const openPreviewModal = (trigger) => {
    lastFocusedElement = document.activeElement;

    previewImage.src = trigger.dataset.imageSrc || window.__IMAGE_FALLBACK_SRC__;
    previewImage.alt = trigger.dataset.imageAlt || '식탁 미리보기';
    previewOwner.textContent = trigger.dataset.owner || '이용자님의 식탁';
    previewDate.textContent = trigger.dataset.createdAt || '';
    previewLikeCount.textContent = trigger.dataset.likeCount || '0';

    previewLikeButton.dataset.postId = trigger.dataset.postId || '';
    previewLikeButton.setAttribute('aria-pressed', String(trigger.dataset.liked === 'true'));
    previewLikeButton.classList.toggle('is-active', trigger.dataset.liked === 'true');

    previewModal.hidden = false;
    previewModal.setAttribute('aria-hidden', 'false');
    setBodyLocked();
    previewModal.querySelector('[data-table-decor-modal-close]')?.focus();
  };

  const closePreviewModal = () => {
    previewModal.hidden = true;
    previewModal.setAttribute('aria-hidden', 'true');
    releaseBodyLock();

    if (lastFocusedElement instanceof HTMLElement) {
      lastFocusedElement.focus();
    }
  };

  const openAuthModal = (message) => {
    lastFocusedElement = document.activeElement;

    if (authMessage) {
      authMessage.textContent = message;
    }

    authModal.hidden = false;
    authModal.setAttribute('aria-hidden', 'false');
    setBodyLocked();
    authModal.querySelector('[data-table-decor-auth-close]')?.focus();
  };

  const closeAuthModal = () => {
    authModal.hidden = true;
    authModal.setAttribute('aria-hidden', 'true');
    releaseBodyLock();

    if (lastFocusedElement instanceof HTMLElement) {
      lastFocusedElement.focus();
    }
  };

  const setLikeState = (postId, liked, likeCount) => {
    document
      .querySelectorAll(`[data-table-decor-like][data-post-id="${postId}"]`)
      .forEach((button) => {
        button.classList.toggle('is-active', liked);
        button.setAttribute('aria-pressed', String(liked));

        const count = button.querySelector('[data-table-decor-like-count]');
        if (count) {
          count.textContent = String(likeCount);
        }
      });

    document
      .querySelectorAll(`[data-table-decor-trigger][data-post-id="${postId}"]`)
      .forEach((trigger) => {
        trigger.dataset.likeCount = String(likeCount);
        trigger.dataset.liked = String(liked);
      });

    if (previewLikeButton.dataset.postId === postId) {
      previewLikeButton.classList.toggle('is-active', liked);
      previewLikeButton.setAttribute('aria-pressed', String(liked));
      previewLikeCount.textContent = String(likeCount);
    }
  };

  const escapeHtml = (value) =>
    String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');

  const formatDate = (value) => {
    if (!value) {
      return '';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value).slice(0, 10).replaceAll('-', '.');
    }

    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    })
      .format(date)
      .replace(/\.\s?/g, '.')
      .replace(/\.$/, '');
  };

  const postCardTemplate = (post) => {
    const imageSrc = post.imageSrc || window.__IMAGE_FALLBACK_SRC__;
    const owner = `${post.userName || '이용자'}님의 식탁`;
    const createdAt = formatDate(post.createdAt);
    const liked = Boolean(post.liked);

    return `
      <article class="table-post-card">
        <header class="table-post-card__header">
          <strong>${escapeHtml(owner)}</strong>
          <span>${escapeHtml(createdAt)}</span>
        </header>
        <div class="table-post-card__media">
          <button
            class="table-post-card__image"
            type="button"
            data-table-decor-trigger
            data-post-id="${post.postId}"
            data-image-src="${escapeHtml(imageSrc)}"
            data-image-alt="${escapeHtml(owner)}"
            data-owner="${escapeHtml(owner)}"
            data-created-at="${escapeHtml(createdAt)}"
            data-like-count="${post.likeCount || 0}"
            data-liked="${liked}"
          >
            <img
              data-fallback-src="/images/logo-hi-eating.png"
              alt="${escapeHtml(owner)}"
              src="${escapeHtml(imageSrc)}"
            >
          </button>
        </div>
        <div class="table-post-card__actions">
          <button
            class="table-post-card__like${liked ? ' is-active' : ''}"
            type="button"
            aria-label="식탁 좋아요"
            data-table-decor-like
            data-post-id="${post.postId}"
            aria-pressed="${liked}"
          >
            <i aria-hidden="true" class="bi bi-heart-fill"></i>
            <span data-table-decor-like-count>${post.likeCount || 0}</span>
          </button>
        </div>
      </article>
    `;
  };

  const setLoading = (nextLoading) => {
    loading = nextLoading;

    if (loader) {
      loader.hidden = !nextLoading;
    }
  };

  const loadMorePosts = async () => {
    if (!feed || loading || currentPage >= totalPages) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`/api/table-decorations/posts?page=${currentPage + 1}`, {
        headers: {
          Accept: 'application/json',
        },
      });

      if (!response.ok || response.redirected) {
        throw new Error('Failed to load table decoration posts.');
      }

      const postPage = await response.json();
      feed.insertAdjacentHTML('beforeend', (postPage.posts || []).map(postCardTemplate).join(''));
      currentPage = postPage.page;
      totalPages = postPage.totalPages;
      page.dataset.tableDecorPage = String(currentPage);
      page.dataset.tableDecorTotalPages = String(totalPages);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const toggleLike = async (button) => {
    const postId = button.dataset.postId;

    if (!postId || button.classList.contains('is-loading')) {
      return;
    }

    if (!isAuthenticated) {
      openAuthModal('좋아요는 로그인한 사용자만 이용할 수 있습니다. 로그인 후 다시 시도해주세요.');
      return;
    }

    button.classList.add('is-loading');
    button.disabled = true;

    try {
      const response = await fetch(`/api/table-decorations/${postId}/likes/toggle`, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'X-XSRF-TOKEN': getCsrfToken(),
        },
      });

      if (!response.ok || response.redirected) {
        const error = new Error('Failed to toggle table decoration like.');
        error.status = response.status;
        throw error;
      }

      const data = await response.json();
      setLikeState(String(data.postId), data.liked, data.likeCount);
    } catch (error) {
      console.error(error);
      openAuthModal(
        error.status === 401 || error.status === 403
          ? '좋아요는 로그인한 사용자만 이용할 수 있습니다. 로그인 후 다시 시도해주세요.'
          : '좋아요 처리에 실패했습니다. 잠시 후 다시 시도해주세요.',
      );
    } finally {
      button.classList.remove('is-loading');
      button.disabled = false;
    }
  };

  document.addEventListener('click', (event) => {
    const loginRequiredLink = event.target.closest('[data-table-decor-login-required]');
    if (loginRequiredLink && !isAuthenticated) {
      event.preventDefault();
      openAuthModal(
        '내 식탁 등록은 로그인한 사용자만 이용할 수 있습니다. 로그인 후 다시 시도해주세요.',
      );
      return;
    }

    const likeButton = event.target.closest('[data-table-decor-like]');
    if (likeButton) {
      event.preventDefault();
      event.stopPropagation();
      toggleLike(likeButton);
      return;
    }

    const trigger = event.target.closest('[data-table-decor-trigger]');
    if (trigger) {
      event.preventDefault();
      openPreviewModal(trigger);
    }
  });

  previewModal.querySelectorAll('[data-table-decor-modal-close]').forEach((button) => {
    button.addEventListener('click', closePreviewModal);
  });

  authModal.querySelectorAll('[data-table-decor-auth-close]').forEach((button) => {
    button.addEventListener('click', closeAuthModal);
  });

  document.addEventListener('keydown', (event) => {
    if (event.key !== 'Escape') {
      return;
    }

    if (!authModal.hidden) {
      closeAuthModal();
      return;
    }

    if (!previewModal.hidden) {
      closePreviewModal();
    }
  });

  if (sentinel && 'IntersectionObserver' in window) {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          loadMorePosts();
        }
      },
      { rootMargin: '480px 0px' },
    );

    observer.observe(sentinel);
  }
});
