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
  const reviewPanel = document.getElementById('product-reviews');
  const reviewLoading = document.getElementById('reviewLoading');
  const reviewList = document.getElementById('reviewList');
  const reviewEmpty = document.getElementById('reviewEmpty');
  const reviewError = document.getElementById('reviewError');
  const reviewPagination = document.getElementById('reviewPagination');
  const reviewCount = document.getElementById('reviewCount');
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
  const reviewPageSize = 5;
  let imageChangeToken = 0;
  let currentReviewPage = 1;
  let reviewRequestToken = 0;

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

  if (reviewPanel && reviewList && reviewPagination) {
    loadReviews(currentReviewPage);
  }

  async function loadReviews(page) {
    const reviewApiUrl = reviewPanel.dataset.reviewApiUrl;

    if (!reviewApiUrl) {
      return;
    }

    const requestToken = reviewRequestToken + 1;
    reviewRequestToken = requestToken;
    setReviewState('loading');

    try {
      const requestUrl = new URL(reviewApiUrl, window.location.origin);
      requestUrl.searchParams.set('page', String(page));
      requestUrl.searchParams.set('size', String(reviewPageSize));

      const response = await fetch(requestUrl);
      if (!response.ok) {
        throw new Error(`Review request failed: ${response.status}`);
      }

      const reviewPage = await response.json();
      if (requestToken !== reviewRequestToken) {
        return;
      }

      currentReviewPage = reviewPage.page || 1;
      renderReviews(reviewPage.items || []);
      renderReviewPagination(reviewPage);
      updateReviewCount(reviewPage.totalCount || 0);
      setReviewState((reviewPage.items || []).length > 0 ? 'loaded' : 'empty');
    } catch (error) {
      if (requestToken !== reviewRequestToken) {
        return;
      }

      console.error(error);
      reviewList.replaceChildren();
      reviewPagination.replaceChildren();
      setReviewState('error');
    }
  }

  function setReviewState(state) {
    reviewLoading.classList.toggle('d-none', state !== 'loading');
    reviewEmpty.classList.toggle('d-none', state !== 'empty');
    reviewError.classList.toggle('d-none', state !== 'error');
    reviewPagination.classList.toggle(
        'd-none', state !== 'loaded' || reviewPagination.children.length === 0);
  }

  function renderReviews(reviews) {
    const reviewItems = reviews.map(createReviewItem);
    reviewList.replaceChildren(...reviewItems);
  }

  function createReviewItem(review) {
    const item = document.createElement('article');
    item.className = 'review-item';

    const meta = document.createElement('div');
    meta.className = 'review-meta d-flex flex-wrap align-items-center '
        + 'justify-content-between gap-2';

    const writer = document.createElement('strong');
    writer.className = 'review-writer';
    writer.textContent = review.reviewerName || '구매자';

    const date = document.createElement('time');
    date.dateTime = normalizeDateTime(review.createdAt);
    date.textContent = formatReviewDate(review.createdAt);

    const stars = document.createElement('span');
    stars.className = 'review-stars';
    stars.textContent = createStarText(review.rating);

    const metaLeft = document.createElement('div');
    metaLeft.className = 'review-meta-left d-flex flex-wrap align-items-center gap-2';
    metaLeft.append(writer, date);
    meta.append(metaLeft, stars);

    const content = document.createElement('p');
    content.className = 'review-content mb-0 mt-3';
    content.textContent = review.content || '';

    item.append(meta, content);

    if (review.imgSrc) {
      const image = document.createElement('img');
      image.className = 'review-image mt-3';
      image.alt = review.content || '리뷰 이미지';
      image.src = review.imgSrc;
      item.append(image);
    }

    return item;
  }

  function renderReviewPagination(reviewPage) {
    reviewPagination.replaceChildren();

    if (!reviewPage.totalPages || reviewPage.totalPages <= 1) {
      reviewPagination.classList.add('d-none');
      return;
    }

    const page = reviewPage.page || 1;
    const totalPages = reviewPage.totalPages;
    const startPage = Math.max(1, Math.min(page - 2, totalPages - 4));
    const endPage = Math.min(totalPages, startPage + 4);

    reviewPagination.append(
        createPageButton('이전', page - 1, page <= 1, false));

    for (let pageNumber = startPage; pageNumber <= endPage; pageNumber += 1) {
      reviewPagination.append(
          createPageButton(
              String(pageNumber),
              pageNumber,
              false,
              pageNumber === page));
    }

    reviewPagination.append(
        createPageButton('다음', page + 1, page >= totalPages, false));
  }

  function createPageButton(label, page, disabled, active) {
    const button = document.createElement('button');
    button.className = 'review-page-button';
    button.type = 'button';
    button.textContent = label;
    button.disabled = disabled;
    button.classList.toggle('active', active);
    button.setAttribute('aria-current', active ? 'page' : 'false');

    if (!disabled && !active) {
      button.addEventListener('click', () => {
        loadReviews(page);
      });
    }

    return button;
  }

  function updateReviewCount(totalCount) {
    if (!reviewCount) {
      return;
    }

    reviewCount.textContent = totalCount.toLocaleString('ko-KR');
  }

  function createStarText(rating) {
    const safeRating = Math.max(0, Math.min(5, Number.parseInt(rating, 10) || 0));
    return '★'.repeat(safeRating) + '☆'.repeat(5 - safeRating);
  }

  function normalizeDateTime(value) {
    if (Array.isArray(value)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = value;
      return `${year}-${padDatePart(month)}-${padDatePart(day)}T`
          + `${padDatePart(hour)}:${padDatePart(minute)}:${padDatePart(second)}`;
    }

    return value || '';
  }

  function formatReviewDate(value) {
    if (Array.isArray(value)) {
      const [year, month, day] = value;
      return `${year}.${padDatePart(month)}.${padDatePart(day)}`;
    }

    if (typeof value === 'string' && value.length >= 10) {
      return value.slice(0, 10).replaceAll('-', '.');
    }

    return '';
  }

  function padDatePart(value) {
    return String(value).padStart(2, '0');
  }
});
