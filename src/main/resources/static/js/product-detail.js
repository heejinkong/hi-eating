document.addEventListener('DOMContentLoaded', () => {
  const mainImage = document.getElementById('mainProductImage');
  const mainImageFrame = mainImage
      ? mainImage.closest('.product-main-image')
      : null;
  const thumbnails = document.querySelectorAll('.product-thumbnail');
  const allImageUrls = Array.from(thumbnails).map(
      thumb => thumb.dataset.imageUrl);
  const quantityInput = document.getElementById('productQuantity');
  const quantityButtons = document.querySelectorAll('[data-quantity-action]');
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
  let imageChangeToken = 0;

  let currentImageIndex = mainImage ? allImageUrls.indexOf(mainImage.src) : 0;
  if (mainImage && currentImageIndex === -1 && allImageUrls.length > 0) {
    currentImageIndex = 0;
    mainImage.src = allImageUrls[0];
  }

  function showImage(index) {
    if (!mainImage || allImageUrls.length === 0) {
      return;
    }

    if (index < 0) {
      index = allImageUrls.length - 1;
    } else if (index >= allImageUrls.length) {
      index = 0;
    }

    const nextImageUrl = allImageUrls[index];
    const nextImageToken = imageChangeToken + 1;
    imageChangeToken = nextImageToken;

    if (mainImage.src === nextImageUrl) {
      updateActiveThumbnail(index);
      return;
    }

    const preloadedImage = new Image();
    preloadedImage.onload = function () {
      if (imageChangeToken !== nextImageToken) {
        return;
      }

      animateImageChange(index, nextImageUrl);
    };
    preloadedImage.onerror = function () {
      if (imageChangeToken !== nextImageToken) {
        return;
      }

      applyImage(index, nextImageUrl);
    };
    preloadedImage.src = nextImageUrl;
  }

  function animateImageChange(index, imageUrl) {
    if (!mainImageFrame || reduceMotion.matches) {
      applyImage(index, imageUrl);
      return;
    }

    const outgoingImage = mainImage.cloneNode(false);
    outgoingImage.removeAttribute('id');
    outgoingImage.className = 'product-main-image-ghost';
    outgoingImage.alt = '';
    outgoingImage.setAttribute('aria-hidden', 'true');
    mainImageFrame.appendChild(outgoingImage);

    mainImage.classList.remove('is-entering');
    applyImage(index, imageUrl);
    mainImage.offsetWidth;
    mainImage.classList.add('is-entering');
    outgoingImage.classList.add('is-leaving');

    const cleanup = function () {
      outgoingImage.remove();
      mainImage.classList.remove('is-entering');
    };

    outgoingImage.addEventListener('animationend', cleanup, {once: true});
    window.setTimeout(cleanup, 520);
  }

  function applyImage(index, imageUrl) {
    mainImage.src = imageUrl;
    currentImageIndex = index;
    updateActiveThumbnail(index);
  }

  function updateActiveThumbnail(index) {
    thumbnails.forEach((thumbnail, i) => {
      thumbnail.classList.toggle('active', i === index);
    });
  }

  window.showNextImage = function () {
    showImage(currentImageIndex + 1);
  }

  window.showPrevImage = function () {
    showImage(currentImageIndex - 1);
  }

  thumbnails.forEach((thumbnail, index) => {
    thumbnail.addEventListener('click', () => {
      showImage(index);
    });
  });

  quantityButtons.forEach(button => {
    button.addEventListener('click', () => {
      if (!quantityInput) {
        return;
      }

      const currentValue = Number.parseInt(quantityInput.value, 10) || 1;
      const nextValue = button.dataset.quantityAction === 'increase'
          ? currentValue + 1
          : Math.max(1, currentValue - 1);

      quantityInput.value = String(nextValue);
    });
  });

  if (quantityInput) {
    quantityInput.addEventListener('change', () => {
      const currentValue = Number.parseInt(quantityInput.value, 10) || 1;
      quantityInput.value = String(Math.max(1, currentValue));
    });
  }
});
