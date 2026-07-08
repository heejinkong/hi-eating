document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('#reviewForm');
  const ratingInput = document.querySelector('#reviewRating');
  const stars = Array.from(document.querySelectorAll('.review-star'));
  const textarea = document.querySelector('#reviewContent');
  const contentCount = document.querySelector('#reviewContentCount');
  const imageInput = document.querySelector('#reviewImage');
  const imagePreview = document.querySelector('#reviewImagePreview');
  const imagePreviewImg = imagePreview?.querySelector('img');
  const imageRemoveButton = document.querySelector('#reviewImageRemove');
  const submitButton = form?.querySelector('.review-submit-button');
  const submitMessage = document.querySelector('#reviewSubmitMessage');
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
      alert('이미지 파일만 삽입 가능합니다');
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

  const showSubmitMessage = (message) => {
    if (!submitMessage) {
      alert(message);
      return;
    }

    submitMessage.textContent = message;
    submitMessage.removeAttribute('hidden');
  };

  const clearSubmitMessage = () => {
    submitMessage?.setAttribute('hidden', '');
    if (submitMessage) {
      submitMessage.textContent = '';
    }
  };

  form?.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearSubmitMessage();

    const rating = Number(ratingInput?.value || 0);
    if (rating < 1 || rating > 5) {
      showSubmitMessage('별점을 입력해주세요.');
      return;
    }

    if (!textarea?.value.trim()) {
      showSubmitMessage('리뷰 내용을 입력해주세요.');
      textarea?.focus();
      return;
    }

    const formData = new FormData(form);

    if (submitButton) {
      submitButton.disabled = true;
      submitButton.textContent = '등록 중...';
    }

    const xsrfToken = (() => {
      const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
      return match ? decodeURIComponent(match[1]) : '';
    })();

    try {
      const response = await fetch(form.action, {
        method: 'POST',
        headers: {
          'X-XSRF-TOKEN': xsrfToken,
        },
        body: formData,
      });
      const apiResponse = await response.json();

      if (!response.ok || !apiResponse.isSuccess) {
        throw new Error(apiResponse.message || '리뷰 등록에 실패했습니다.');
      }

      const redirectUrl = apiResponse.result?.redirectUrl || '/mypage';
      window.location.href = `${redirectUrl}#product-reviews`;
    } catch (error) {
      showSubmitMessage(error.message || '리뷰 등록에 실패했습니다.');
    } finally {
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = '리뷰 등록하기';
      }
    }
  });
});
