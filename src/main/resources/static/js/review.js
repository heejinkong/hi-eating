document.addEventListener('DOMContentLoaded', () => {
  const ratingInput = document.querySelector('#reviewRating');
  const stars = Array.from(document.querySelectorAll('.review-star'));
  const textarea = document.querySelector('#reviewContent');
  const contentCount = document.querySelector('#reviewContentCount');
  const imageInput = document.querySelector('#reviewImage');
  const imagePreview = document.querySelector('#reviewImagePreview');
  const imagePreviewImg = imagePreview?.querySelector('img');
  const imageRemoveButton = document.querySelector('#reviewImageRemove');
  let previewObjectUrl = null;

  const updateStars = (rating) => {
    stars.forEach((star) => {
      const starRating = Number(star.dataset.rating);
      const selected = starRating <= rating;
      star.classList.toggle('is-selected', selected);
      star.setAttribute('aria-checked', String(starRating === rating));
    });
  };

  stars.forEach((star) => {
    star.addEventListener('click', () => {
      const rating = Number(star.dataset.rating);
      ratingInput.value = String(rating);
      updateStars(rating);
    });
  });

  const updateContentCount = () => {
    if (!textarea || !contentCount) {
      return;
    }

    contentCount.textContent = String(textarea.value.length);
  };

  textarea?.addEventListener('input', updateContentCount);
  updateContentCount();
  updateStars(Number(ratingInput?.value || 0));

  const clearPreview = () => {
    if (previewObjectUrl) {
      URL.revokeObjectURL(previewObjectUrl);
      previewObjectUrl = null;
    }

    if (imageInput) {
      imageInput.value = '';
    }

    if (imagePreviewImg) {
      imagePreviewImg.removeAttribute('src');
    }

    imagePreview?.setAttribute('hidden', '');
  };

  imageInput?.addEventListener('change', () => {
    const file = imageInput.files?.[0];

    if (!file) {
      clearPreview();
      return;
    }

    if (!file.type.startsWith('image/')) {
      clearPreview();
      alert("이미지 파일만 삽입 가능합니다");
      return;
    }

    if (previewObjectUrl) {
      URL.revokeObjectURL(previewObjectUrl);
    }

    previewObjectUrl = URL.createObjectURL(file);
    imagePreviewImg.src = previewObjectUrl;
    imagePreview?.removeAttribute('hidden');
  });

  imageRemoveButton?.addEventListener('click', clearPreview);
});
