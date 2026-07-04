document.addEventListener('DOMContentLoaded', () => {
  const carousel = document.querySelector('#recommendCarousel');
  const carouselShell = carousel?.closest('.recommend-carousel');
  const prevButton = document.querySelector('.recommend-prev');
  const nextButton = document.querySelector('.recommend-next');

  if (!carousel || !carouselShell || !prevButton || !nextButton) {
    return;
  }

  const edgeTolerance = 2;

  const getStep = () => {
    const firstCard = carousel.querySelector('.recommend-card');
    if (!firstCard) {
      return carousel.clientWidth;
    }

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
    if (!imageFrame) {
      return;
    }

    const shellRect = carouselShell.getBoundingClientRect();
    const imageRect = imageFrame.getBoundingClientRect();
    const imageCenterY = imageRect.top - shellRect.top + imageRect.height / 2;
    carouselShell.style.setProperty('--recommend-nav-top', `${imageCenterY}px`);
  };

  const scrollByDirection = (direction) => {
    carousel.scrollBy({
      left: getStep() * direction,
      behavior: 'smooth',
    });
  };

  prevButton.addEventListener('click', () => scrollByDirection(-1));
  nextButton.addEventListener('click', () => scrollByDirection(1));
  carousel.addEventListener('scroll', updateButtons, { passive: true });
  window.addEventListener('resize', updateButtons);
  carousel.querySelectorAll('img').forEach((image) => {
    image.addEventListener('load', updateButtons, { once: true });
  });

  updateButtons();
});
