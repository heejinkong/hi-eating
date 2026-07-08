import {
  createPromotion,
  deletePromotion,
  reorderPromotions,
  updatePromotion,
  updatePromotionWithImage,
} from './admin-promotion/api.js';
import {
  endOfDay,
  nextMonthInputValue,
  startOfDay,
  todayInputValue,
  todayStartOfDay,
} from './admin-promotion/date-utils.js';
import {
  markImageDeleted,
  promotionState,
  resetPendingImageState,
  selectBanner,
  selectFile,
  startCreatingBanner,
} from './admin-promotion/state.js';

let draggedBannerItem = null;
let originalBannerOrder = [];

document.addEventListener('DOMContentLoaded', () => {
  initializeDateInputs();
  initializeInitialSelection();
  initializeBannerDragAndDrop();
});

function initializeDateInputs() {
  const todayStr = todayInputValue();
  const startsAtInput = document.getElementById('bannerStartsAtInput');
  const endsAtInput = document.getElementById('bannerEndsAtInput');

  if (!startsAtInput) return;

  startsAtInput.min = todayStr;
  startsAtInput.addEventListener('change', () => {
    if (endsAtInput) {
      endsAtInput.min = startsAtInput.value;
    }
  });
}

function initializeInitialSelection() {
  const firstBannerItem = document.querySelector('.banner-item');
  if (firstBannerItem) {
    selectBannerItem(firstBannerItem);
    return;
  }

  updateRightPanelState(null);
}

function initializeBannerDragAndDrop() {
  const bannerList = document.getElementById('bannerList');
  if (!bannerList) return;

  bannerList.querySelectorAll('.banner-item').forEach((item) => {
    item.draggable = true;
    item.addEventListener('dragstart', (event) => handleDragStart(event, item));
    item.addEventListener('dragend', () => handleDragEnd(item));
  });

  bannerList.addEventListener('dragover', (event) => {
    event.preventDefault();
    if (!draggedBannerItem) return;

    const target = event.target.closest('.banner-item');
    if (!target || target === draggedBannerItem) return;

    const targetRect = target.getBoundingClientRect();
    const insertAfter = event.clientY > targetRect.top + targetRect.height / 2;
    bannerList.insertBefore(draggedBannerItem, insertAfter ? target.nextSibling : target);
  });
}

function handleDragStart(event, item) {
  draggedBannerItem = item;
  originalBannerOrder = getOrderedPromotionIds();
  item.classList.add('dragging');
  event.dataTransfer.effectAllowed = 'move';
  event.dataTransfer.setData('text/plain', item.dataset.id);
}

async function handleDragEnd(item) {
  item.classList.remove('dragging');
  if (!draggedBannerItem) return;

  const movedItem = draggedBannerItem;
  draggedBannerItem = null;
  const changedOrder = getOrderedPromotionIds();

  if (originalBannerOrder.join(',') === changedOrder.join(',')) return;

  updateOrderBadges();
  await saveBannerOrder(movedItem, changedOrder);
}

function getOrderedPromotionIds() {
  return Array.from(document.querySelectorAll('#bannerList .banner-item')).map((item) =>
    parseInt(item.dataset.id, 10),
  );
}

function updateOrderBadges() {
  document.querySelectorAll('#bannerList .banner-item .order-badge').forEach((badge, index) => {
    badge.textContent = index + 1;
  });
}

async function saveBannerOrder(movedItem, orderedPromotionIds) {
  const items = Array.from(document.querySelectorAll('#bannerList .banner-item'));
  const movedIndex = items.indexOf(movedItem);

  try {
    await reorderPromotions({
      movedPromotionId: parseInt(movedItem.dataset.id, 10),
      previousPromotionId: movedIndex > 0 ? parseInt(items[movedIndex - 1].dataset.id, 10) : null,
      nextPromotionId:
        movedIndex < items.length - 1 ? parseInt(items[movedIndex + 1].dataset.id, 10) : null,
      orderedPromotionIds,
    });
  } catch (error) {
    alert(error.message);
    window.location.reload();
  }
}

function selectBannerItem(element) {
  if (!element) return;

  resetPendingImageState();
  document.querySelectorAll('.banner-item').forEach((item) => item.classList.remove('active'));
  element.classList.add('active');

  const id = parseInt(element.getAttribute('data-id'), 10);
  selectBanner(id);
  updateRightPanelState({
    id,
    title: element.getAttribute('data-title') || '',
    link: element.getAttribute('data-link') || '',
    img: element.getAttribute('data-img') || '',
    start: element.getAttribute('data-start') || '',
    end: element.getAttribute('data-end') || '',
  });
}

function updateRightPanelState(bannerData) {
  const controls = getFormControls();

  if (!bannerData) {
    setPreviewImage('');
    setFormDisabled(controls, true);
    setFormValues(controls, { title: '', link: '', start: '', end: '' });
    updateLinkPreview('');
    selectBanner(null);
    return;
  }

  setFormDisabled(controls, false);
  setFormValues(controls, bannerData);
  if (controls.endsAtInput && bannerData.start) {
    controls.endsAtInput.min = bannerData.start;
  }

  setPreviewImage(promotionState.isImageDeleted ? '' : bannerData.img);
  updateLinkPreview(bannerData.link);
}

function getFormControls() {
  return {
    titleInput: document.getElementById('bannerTitleInput'),
    linkInput: document.getElementById('bannerLinkInput'),
    startsAtInput: document.getElementById('bannerStartsAtInput'),
    endsAtInput: document.getElementById('bannerEndsAtInput'),
  };
}

function setFormDisabled(controls, disabled) {
  Object.values(controls).forEach((control) => {
    if (control) {
      control.disabled = disabled;
    }
  });
}

function setFormValues(controls, values) {
  if (controls.titleInput) controls.titleInput.value = values.title;
  if (controls.linkInput) controls.linkInput.value = values.link;
  if (controls.startsAtInput) controls.startsAtInput.value = values.start;
  if (controls.endsAtInput) controls.endsAtInput.value = values.end;
}

function setPreviewImage(imageUrl) {
  const previewImg = document.getElementById('bannerPreviewImg');
  const previewPlaceholder = document.getElementById('bannerPreviewPlaceholder');
  const deletePreviewBtn = document.getElementById('btnDeletePreviewImg');
  const hasImage = Boolean(imageUrl);

  if (previewImg) {
    previewImg.src = imageUrl || '';
    previewImg.style.display = hasImage ? 'block' : 'none';
  }
  if (previewPlaceholder) {
    previewPlaceholder.style.display = hasImage ? 'none' : 'flex';
  }
  if (deletePreviewBtn) {
    deletePreviewBtn.style.display = hasImage ? 'flex' : 'none';
  }
}

function updateLinkPreview(value) {
  const previewText = document.getElementById('linkPreviewText');
  const previewAnchor = document.getElementById('linkPreviewAnchor');
  const trimmed = value.trim();
  const displayValue = trimmed || '링크를 등록하시면 배너 클릭 시 이동 경로가 미리 노출됩니다.';

  if (previewText) {
    previewText.innerText = displayValue;
    previewText.classList.toggle('text-muted', !trimmed);
  }

  if (!previewAnchor) return;

  if (trimmed) {
    previewAnchor.href = trimmed;
    previewAnchor.style.pointerEvents = 'auto';
    previewAnchor.style.opacity = '1';
  } else {
    previewAnchor.removeAttribute('href');
    previewAnchor.style.pointerEvents = 'none';
    previewAnchor.style.opacity = '0.4';
  }
}

function triggerFileUpload() {
  document.getElementById('bannerFileInput')?.click();
}

function startCreatePromotion(openFilePicker = false) {
  startCreatingBanner();
  document.querySelectorAll('.banner-item').forEach((item) => item.classList.remove('active'));

  const controls = getFormControls();
  setPreviewImage('');
  setFormDisabled(controls, false);
  setFormValues(controls, {
    title: '',
    link: '',
    start: todayInputValue(),
    end: nextMonthInputValue(),
  });

  if (controls.startsAtInput) controls.startsAtInput.min = todayInputValue();
  if (controls.endsAtInput) controls.endsAtInput.min = todayInputValue();

  const fileInput = document.getElementById('bannerFileInput');
  if (fileInput) fileInput.value = '';

  updateLinkPreview('');
  if (openFilePicker) {
    triggerFileUpload();
  } else {
    controls.titleInput?.focus();
  }
}

function handleBannerImageSelect(input) {
  if (!input.files || input.files.length === 0) return;

  selectFile(input.files[0]);
  markImageDeleted(false);

  if (!promotionState.selectedBannerId) {
    document.querySelectorAll('.banner-item').forEach((item) => item.classList.remove('active'));
  }

  const controls = getFormControls();
  setFormDisabled(controls, false);
  if (!promotionState.selectedBannerId) {
    setFormValues(controls, {
      title: '',
      link: '',
      start: todayInputValue(),
      end: nextMonthInputValue(),
    });
    controls.titleInput?.focus();
  }

  if (controls.startsAtInput) controls.startsAtInput.min = todayInputValue();
  if (controls.endsAtInput)
    controls.endsAtInput.min = controls.startsAtInput?.value || todayInputValue();

  const reader = new FileReader();
  reader.onload = (event) => setPreviewImage(event.target.result);
  reader.readAsDataURL(promotionState.selectedFile);
}

function clearPreviewImage(event) {
  event?.stopPropagation();
  selectFile(null);
  if (promotionState.selectedBannerId) {
    markImageDeleted(true);
  }
  setPreviewImage('');

  const fileInput = document.getElementById('bannerFileInput');
  if (fileInput) fileInput.value = '';
}

async function saveBannerLink() {
  const controls = getFormControls();
  const values = readFormValues(controls);
  if (!validateFormValues(values, controls)) return;

  const saveBtn = document.querySelector('.btn-save');
  const originalText = setSaveButtonLoading(saveBtn, true);

  try {
    if (promotionState.selectedFile && !promotionState.selectedBannerId) {
      await createPromotion(buildPromotionFormData(values, promotionState.selectedFile));
      alert('배너가 성공적으로 등록되었습니다.');
      window.location.reload();
      return;
    }

    if (!promotionState.selectedBannerId) {
      alert('배너 이미지를 새로 추가하거나, 목록에서 수정할 배너를 먼저 선택해 주세요.');
      return;
    }

    if (promotionState.selectedFile) {
      await updatePromotionWithImage(
        promotionState.selectedBannerId,
        buildPromotionFormData(values, promotionState.selectedFile),
      );
      alert('배너 이미지와 정보가 성공적으로 수정되었습니다.');
      window.location.reload();
      return;
    }

    await updatePromotion(promotionState.selectedBannerId, buildPromotionRequest(values));
    syncActiveBanner(values);
    alert('배너 정보가 성공적으로 수정되었습니다.');
  } catch (error) {
    alert(error.message || '서버와 통신하는 중 오류가 발생했습니다.');
  } finally {
    setSaveButtonLoading(saveBtn, false, originalText);
  }
}

function readFormValues(controls) {
  return {
    title: controls.titleInput ? controls.titleInput.value.trim() : '',
    link: controls.linkInput ? controls.linkInput.value.trim() : '',
    start: controls.startsAtInput ? controls.startsAtInput.value : '',
    end: controls.endsAtInput ? controls.endsAtInput.value : '',
  };
}

function validateFormValues(values, controls) {
  if (!values.title) {
    alert('배너 제목은 필수 입력 항목입니다.');
    controls.titleInput?.focus();
    return false;
  }
  if (values.title.length > 20) {
    alert('배너 제목은 20자 이하로 입력해 주세요.');
    controls.titleInput?.focus();
    return false;
  }
  if (!values.start) {
    alert('시작일자를 선택해 주세요.');
    controls.startsAtInput?.focus();
    return false;
  }
  if (!values.end) {
    alert('종료일자를 선택해 주세요.');
    controls.endsAtInput?.focus();
    return false;
  }
  if (!promotionState.selectedBannerId && !promotionState.selectedFile) {
    alert('배너 이미지를 필수로 추가해 주세요.');
    return false;
  }
  if (
    promotionState.selectedBannerId &&
    promotionState.isImageDeleted &&
    !promotionState.selectedFile
  ) {
    alert('수정 시에도 배너 이미지는 필수로 존재해야 합니다. 이미지를 다시 추가해 주세요.');
    return false;
  }
  if (startOfDay(values.start) < todayStartOfDay()) {
    alert('시작일은 오늘 이후로 설정해야 합니다.');
    return false;
  }
  if (startOfDay(values.end) < startOfDay(values.start)) {
    alert('종료일은 시작일 이후로 설정해야 합니다.');
    return false;
  }
  return true;
}

function buildPromotionFormData(values, file) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('title', values.title);
  formData.append('link', values.link);
  formData.append('startsAt', values.start.replace(/-/g, '.'));
  formData.append('endsAt', values.end.replace(/-/g, '.'));
  return formData;
}

function buildPromotionRequest(values) {
  return {
    title: values.title,
    link: values.link,
    startsAt: values.start.replace(/-/g, '.'),
    endsAt: values.end.replace(/-/g, '.'),
  };
}

function setSaveButtonLoading(saveBtn, loading, originalText = '') {
  if (!saveBtn) return originalText;

  if (loading) {
    const currentText = saveBtn.innerHTML;
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>저장 중...';
    return currentText;
  }

  saveBtn.disabled = false;
  saveBtn.innerHTML = originalText;
  return originalText;
}

function syncActiveBanner(values) {
  const activeItem = document.querySelector(
    `.banner-item[data-id="${promotionState.selectedBannerId}"]`,
  );
  if (!activeItem) return;

  activeItem.setAttribute('data-title', values.title);
  activeItem.setAttribute('data-link', values.link);
  activeItem.setAttribute('data-start', values.start);
  activeItem.setAttribute('data-end', values.end);

  const titleSpan = activeItem.querySelector('.banner-title');
  if (titleSpan) titleSpan.innerText = values.title;

  const dateSpan = activeItem.querySelector('.banner-date');
  if (dateSpan) {
    dateSpan.innerText = `${values.start.replace(/-/g, '.')} ~ ${values.end.replace(/-/g, '.')}`;
  }

  syncExpiredBadge(activeItem, values.end);
}

function syncExpiredBadge(activeItem, endValue) {
  let statusBadge = activeItem.querySelector('.status-badge.expired');
  if (endOfDay(endValue) < todayStartOfDay()) {
    if (!statusBadge) {
      const titleWrapper = activeItem.querySelector('.d-flex.align-items-center');
      statusBadge = document.createElement('span');
      statusBadge.className = 'status-badge expired';
      statusBadge.innerText = '종료';
      titleWrapper?.appendChild(statusBadge);
    }
    return;
  }

  statusBadge?.remove();
}

async function deleteBanner(id, event) {
  event?.stopPropagation();
  if (
    !confirm(
      '정말로 이 프로모션 배너를 삭제하시겠습니까?\n삭제 시 메인 화면에 더 이상 노출되지 않습니다.',
    )
  ) {
    return;
  }

  try {
    await deletePromotion(id);
    alert('배너가 성공적으로 삭제되었습니다.');
    window.location.reload();
  } catch (error) {
    alert(error.message || '배너 삭제에 실패했습니다.');
  }
}

function resetForm() {
  resetPendingImageState();
  if (!promotionState.selectedBannerId) {
    startCreatePromotion(false);
    return;
  }

  const activeItem = document.querySelector(
    `.banner-item[data-id="${promotionState.selectedBannerId}"]`,
  );
  if (activeItem) {
    selectBannerItem(activeItem);
  } else {
    updateRightPanelState(null);
  }
}

window.selectBannerItem = selectBannerItem;
window.updateLinkPreview = updateLinkPreview;
window.triggerFileUpload = triggerFileUpload;
window.startCreatePromotion = startCreatePromotion;
window.handleBannerImageSelect = handleBannerImageSelect;
window.clearPreviewImage = clearPreviewImage;
window.saveBannerLink = saveBannerLink;
window.deleteBanner = deleteBanner;
window.resetForm = resetForm;
