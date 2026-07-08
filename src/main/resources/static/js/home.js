(() => {
  const homePage = document.querySelector('.home-page');
  const tableDecorModal = document.querySelector('[data-home-table-decor-modal]');
  const tableDecorAuthModal = document.querySelector('[data-home-table-decor-auth-modal]');
  if (tableDecorModal) {
    const image = tableDecorModal.querySelector('[data-home-table-decor-modal-image]');
    const owner = tableDecorModal.querySelector('[data-home-table-decor-modal-owner]');
    const date = tableDecorModal.querySelector('[data-home-table-decor-modal-date]');
    const like = tableDecorModal.querySelector('[data-home-table-decor-modal-like]');
    const likeButton = tableDecorModal.querySelector('[data-home-table-decor-modal-like-button]');
    const authMessage = tableDecorAuthModal?.querySelector('[data-home-table-decor-auth-message]');
    const isAuthenticated = homePage?.dataset.homeTableDecorAuthenticated === 'true';
    const likeLoginRequiredMessage =
      '좋아요는 로그인한 사용자만 이용할 수 있습니다.\n로그인 후 다시 시도해주세요.';
    let lastFocusedElement = null;

    const getCsrfToken = () => {
      const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
      return match ? decodeURIComponent(match[1]) : '';
    };

    const setLikeIcon = (button, liked) => {
      const icon = button.querySelector('i');
      if (!icon) {
        return;
      }

      icon.classList.toggle('bi-heart-fill', liked);
      icon.classList.toggle('bi-heart', !liked);
    };

    const setBodyLocked = () => {
      document.body.style.overflow = 'hidden';
    };

    const releaseBodyLock = () => {
      if (tableDecorModal.hidden && (!tableDecorAuthModal || tableDecorAuthModal.hidden)) {
        document.body.style.overflow = '';
      }
    };

    const setLikeState = (postId, liked, likeCount) => {
      document
        .querySelectorAll(`[data-home-table-decor-trigger][data-post-id="${postId}"]`)
        .forEach((trigger) => {
          trigger.dataset.likeCount = String(likeCount);
          trigger.dataset.liked = String(liked);

          const count = trigger.querySelector('[data-home-table-decor-like-count]');
          if (count) {
            count.textContent = String(likeCount);
          }

          setLikeIcon(trigger, liked);
        });

      if (likeButton?.dataset.postId === String(postId)) {
        likeButton.classList.toggle('is-active', liked);
        likeButton.setAttribute('aria-pressed', String(liked));
        setLikeIcon(likeButton, liked);
        like.textContent = String(likeCount);
      }
    };

    const closeTableDecorModal = () => {
      tableDecorModal.hidden = true;
      tableDecorModal.setAttribute('aria-hidden', 'true');
      releaseBodyLock();

      if (lastFocusedElement instanceof HTMLElement) {
        lastFocusedElement.focus();
      }
    };

    const openAuthModal = (message) => {
      lastFocusedElement = document.activeElement;

      if (!tableDecorAuthModal) {
        return;
      }

      if (authMessage) {
        authMessage.textContent = message;
      }

      tableDecorAuthModal.hidden = false;
      tableDecorAuthModal.setAttribute('aria-hidden', 'false');
      setBodyLocked();
      tableDecorAuthModal.querySelector('[data-home-table-decor-auth-close]')?.focus();
    };

    const closeAuthModal = () => {
      if (!tableDecorAuthModal) {
        return;
      }

      tableDecorAuthModal.hidden = true;
      tableDecorAuthModal.setAttribute('aria-hidden', 'true');
      releaseBodyLock();

      if (lastFocusedElement instanceof HTMLElement) {
        lastFocusedElement.focus();
      }
    };

    const openTableDecorModal = (trigger) => {
      lastFocusedElement = document.activeElement;

      image.src = trigger.dataset.imageSrc || window.__IMAGE_FALLBACK_SRC__;
      image.alt = trigger.dataset.imageAlt || '\uc2dd\ud0c1 \ubbf8\ub9ac\ubcf4\uae30';
      owner.textContent = trigger.dataset.owner || '\uc774\uc6a9\uc790\ub2d8\uc758 \uc2dd\ud0c1';
      date.textContent = trigger.dataset.createdAt || '';
      like.textContent = trigger.dataset.likeCount || '0';
      if (likeButton) {
        likeButton.dataset.postId = trigger.dataset.postId || '';
        likeButton.classList.toggle('is-active', trigger.dataset.liked === 'true');
        likeButton.setAttribute('aria-pressed', String(trigger.dataset.liked === 'true'));
        setLikeIcon(likeButton, trigger.dataset.liked === 'true');
      }

      tableDecorModal.hidden = false;
      tableDecorModal.setAttribute('aria-hidden', 'false');
      setBodyLocked();
      tableDecorModal.querySelector('[data-home-table-decor-modal-close]')?.focus();
    };

    const toggleLike = async (button) => {
      const postId = button.dataset.postId;

      if (!postId || button.classList.contains('is-loading')) {
        return;
      }

      if (!isAuthenticated) {
        openAuthModal(likeLoginRequiredMessage);
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
            ? likeLoginRequiredMessage
            : '좋아요 처리에 실패했습니다. 잠시 후 다시 시도해주세요.',
        );
      } finally {
        button.classList.remove('is-loading');
        button.disabled = false;
      }
    };

    document.addEventListener('click', (event) => {
      const likeButton = event.target.closest('[data-home-table-decor-like]');
      if (likeButton) {
        event.preventDefault();
        event.stopPropagation();
        toggleLike(likeButton);
        return;
      }

      const trigger = event.target.closest('[data-home-table-decor-trigger]');
      if (!trigger) {
        return;
      }

      event.preventDefault();
      openTableDecorModal(trigger);
    });

    tableDecorModal.querySelectorAll('[data-home-table-decor-modal-close]').forEach((button) => {
      button.addEventListener('click', closeTableDecorModal);
    });

    tableDecorAuthModal
      ?.querySelectorAll('[data-home-table-decor-auth-close]')
      .forEach((button) => {
        button.addEventListener('click', closeAuthModal);
      });

    document.addEventListener('keydown', (event) => {
      if (event.key !== 'Escape') {
        return;
      }

      if (tableDecorAuthModal && !tableDecorAuthModal.hidden) {
        closeAuthModal();
        return;
      }

      if (!tableDecorModal.hidden) {
        closeTableDecorModal();
      }
    });
  }

  const revealSections = document.querySelectorAll('.reveal-section');
  if ('IntersectionObserver' in window) {
    const revealObserver = new IntersectionObserver(
      (entries, observer) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            return;
          }

          entry.target.classList.add('is-visible');
          observer.unobserve(entry.target);
        });
      },
      { threshold: 0.16 },
    );

    revealSections.forEach((section) => revealObserver.observe(section));
  } else {
    revealSections.forEach((section) => section.classList.add('is-visible'));
  }

  const formatTime = (milliseconds) => {
    const totalSeconds = Math.max(0, Math.floor(milliseconds / 1000));
    const hours = String(Math.floor(totalSeconds / 3600)).padStart(2, '0');
    const minutes = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0');
    const seconds = String(totalSeconds % 60).padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
  };

  document.querySelectorAll('[data-end-at]').forEach((timer) => {
    const endAt = new Date(timer.dataset.endAt).getTime();

    const tick = () => {
      timer.textContent = Number.isNaN(endAt) ? '00:00:00' : formatTime(endAt - Date.now());
    };

    tick();
    window.setInterval(tick, 1000);
  });

  document.querySelectorAll('[data-carousel]').forEach((carousel) => {
    const track = carousel.querySelector('[data-carousel-track]');
    const viewport = carousel.querySelector('[data-carousel-viewport]');
    const cards = Array.from(track.children);
    const current = carousel.querySelector('[data-carousel-current]');
    const prevButton = carousel.querySelector('[data-carousel-prev]');
    const nextButton = carousel.querySelector('[data-carousel-next]');
    const toggleButton = carousel.querySelector('[data-carousel-toggle]');

    if (cards.length === 0) {
      return;
    }

    const configuredVisible = Number(carousel.dataset.carouselVisible || '1');
    const visibleCount = () =>
      window.matchMedia('(max-width: 991.98px)').matches ? 1 : configuredVisible;
    const maxIndex = () => Math.max(0, cards.length - visibleCount());
    let index = 0;
    let paused = carousel.dataset.carouselAutoplay !== 'true';
    const draggable = carousel.dataset.carouselDrag === 'true';
    let intervalId = null;
    let dragStartX = 0;
    let dragDeltaX = 0;
    let dragging = false;
    let suppressClick = false;

    const slideStep = () => {
      const cardWidth = cards[0].getBoundingClientRect().width;
      const style = getComputedStyle(track);
      const parsedGap = parseFloat(style.columnGap || style.gap || '0');
      const gap = Number.isNaN(parsedGap) ? 0 : parsedGap;
      return cardWidth + gap;
    };

    const setTrackOffset = (offset) => {
      track.style.transform = `translateX(${offset}px)`;
    };

    const updateToggleIcon = () => {
      const icon = toggleButton?.querySelector('i');
      if (!icon) {
        return;
      }

      icon.className = paused ? 'bi bi-play-fill' : 'bi bi-pause-fill';
      toggleButton.setAttribute(
        'aria-label',
        paused
          ? '\uc790\ub3d9 \uc2ac\ub77c\uc774\ub4dc \uc7ac\uc0dd'
          : '\uc790\ub3d9 \uc2ac\ub77c\uc774\ub4dc \uc77c\uc2dc\uc815\uc9c0',
      );
    };

    const moveTo = (nextIndex) => {
      index = Math.max(0, Math.min(nextIndex, maxIndex()));
      setTrackOffset(-index * slideStep());

      if (current) {
        current.textContent = String(index + 1);
      }
    };

    const moveNext = () => {
      if (index >= maxIndex()) {
        moveTo(0);
        return;
      }

      moveTo(Math.min(index + visibleCount(), maxIndex()));
    };

    const start = () => {
      if (paused || intervalId) {
        return;
      }

      intervalId = window.setInterval(moveNext, 5000);
    };

    const stop = () => {
      window.clearInterval(intervalId);
      intervalId = null;
    };

    prevButton?.addEventListener('click', () => moveTo(index - visibleCount()));
    nextButton?.addEventListener('click', moveNext);
    toggleButton?.addEventListener('click', () => {
      paused = !paused;
      updateToggleIcon();
      if (paused) {
        stop();
      } else {
        start();
      }
    });
    window.addEventListener('resize', () => moveTo(index));

    if (draggable && viewport) {
      const endDrag = () => {
        if (!dragging) {
          return;
        }

        dragging = false;
        viewport.classList.remove('is-dragging');
        track.classList.remove('is-dragging');

        const threshold = Math.max(42, slideStep() * 0.18);
        if (Math.abs(dragDeltaX) > threshold) {
          moveTo(index + (dragDeltaX < 0 ? 1 : -1));
        } else {
          moveTo(index);
        }

        if (Math.abs(dragDeltaX) > 8) {
          suppressClick = true;
          window.setTimeout(() => {
            suppressClick = false;
          }, 0);
        }

        dragDeltaX = 0;
      };

      viewport.addEventListener('pointerdown', (event) => {
        if (event.button !== undefined && event.button !== 0) {
          return;
        }

        stop();
        dragging = true;
        dragStartX = event.clientX;
        dragDeltaX = 0;
        viewport.classList.add('is-dragging');
        track.classList.add('is-dragging');
        viewport.setPointerCapture?.(event.pointerId);
      });

      viewport.addEventListener('pointermove', (event) => {
        if (!dragging) {
          return;
        }

        dragDeltaX = event.clientX - dragStartX;
        setTrackOffset(-index * slideStep() + dragDeltaX);
      });

      viewport.addEventListener('pointerup', endDrag);
      viewport.addEventListener('pointercancel', endDrag);
      viewport.addEventListener('lostpointercapture', endDrag);
      viewport.addEventListener(
        'click',
        (event) => {
          if (!suppressClick) {
            return;
          }

          event.preventDefault();
          event.stopPropagation();
        },
        true,
      );
    }

    moveTo(0);
    updateToggleIcon();
    start();
  });
})();
