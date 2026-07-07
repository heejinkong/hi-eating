export const promotionState = {
  selectedBannerId: null,
  selectedFile: null,
  isImageDeleted: false,
};

export function selectBanner(id) {
  promotionState.selectedBannerId = id;
}

export function selectFile(file) {
  promotionState.selectedFile = file;
}

export function markImageDeleted(deleted) {
  promotionState.isImageDeleted = deleted;
}

export function resetPendingImageState() {
  promotionState.selectedFile = null;
  promotionState.isImageDeleted = false;
}

export function startCreatingBanner() {
  promotionState.selectedBannerId = null;
  resetPendingImageState();
}
