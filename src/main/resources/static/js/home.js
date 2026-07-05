(() => {
  const tableDecorModal = document.querySelector('[data-home-table-decor-modal]');
  if (tableDecorModal) {
    const image = tableDecorModal.querySelector('[data-home-table-decor-modal-image]');
    const owner = tableDecorModal.querySelector('[data-home-table-decor-modal-owner]');
    const date = tableDecorModal.querySelector('[data-home-table-decor-modal-date]');
    const like = tableDecorModal.querySelector('[data-home-table-decor-modal-like]');
    let lastFocusedElement = null;

    const closeTableDecorModal = () => {
      tableDecorModal.hidden = true;
      tableDecorModal.setAttribute('aria-hidden', 'true');
      document.body.style.overflow = '';

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

      tableDecorModal.hidden = false;
      tableDecorModal.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
      tableDecorModal.querySelector('[data-home-table-decor-modal-close]')?.focus();
    };

    document.addEventListener('click', (event) => {
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

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && !tableDecorModal.hidden) {
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
    let intervalId = null;

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
      const cardWidth = cards[0].getBoundingClientRect().width;
      const style = getComputedStyle(track);
      const parsedGap = parseFloat(style.columnGap || style.gap || '0');
      const gap = Number.isNaN(parsedGap) ? 0 : parsedGap;
      track.style.transform = `translateX(-${index * (cardWidth + gap)}px)`;

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

    moveTo(0);
    updateToggleIcon();
    start();
  });
})();
