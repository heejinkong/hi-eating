/**
 * 관리자 프로모션 설정 화면에서 발생하는 UI 상호작용 및 API 호출을 제어하는 스크립트입니다.
 */

// 현재 사용자가 목록에서 선택한 기존 배너의 데이터베이스 ID (신규 등록 시에는 null)
let selectedBannerId = null;

// 사용자가 배너 이미지 추가 버튼을 클릭해 새롭게 선택한 로컬 이미지 파일 객체
let selectedFile = null;

// 기존 배너 상세 조회 상태에서 사용자가 기존 이미지를 삭제(x 클릭)했는지 여부
let isImageDeleted = false;

/**
 * DOM 콘텐츠가 완전히 로드되었을 때 초기 설정을 실행합니다.
 */
document.addEventListener('DOMContentLoaded', () => {
  // 오늘 날짜 계산 (KST 기준 yyyy-MM-dd)
  const today = new Date();
  const offset = today.getTimezoneOffset() * 60000;
  const localToday = new Date(today.getTime() - offset);
  const todayStr = localToday.toISOString().split('T')[0];

  const startsAtInput = document.getElementById('bannerStartsAtInput');
  const endsAtInput = document.getElementById('bannerEndsAtInput');

  // 시작 날짜 인풋이 존재하면, 과거 날짜는 선택할 수 없도록 최솟값(min)을 오늘 날짜로 고정
  if (startsAtInput) {
    startsAtInput.min = todayStr;

    // 노출 시작일이 변경될 때마다 종료일의 최솟값(min)을 시작일로 설정하여 종료일이 시작일보다 앞서지 못하게 유효성 제한
    startsAtInput.addEventListener('change', () => {
      if (endsAtInput) {
        endsAtInput.min = startsAtInput.value;
      }
    });
  }

  // 화면 진입 시 왼쪽 리스트의 첫 번째 배너를 디폴트로 자동 선택하여 상세 폼에 로드
  const firstBannerItem = document.querySelector('.banner-item');
  if (firstBannerItem) {
    selectBannerItem(firstBannerItem);
  } else {
    // 등록된 배너가 없다면 우측 상세 정보 패널을 비활성화 상태로 표시
    updateRightPanelState(null);
  }
});

/**
 * 왼쪽 배너 리스트에서 특정 배너 아이템을 클릭했을 때의 선택 이벤트를 처리합니다.
 *
 * @param {HTMLElement} element 클릭된 배너 리스트 아이템 엘리먼트
 */
function selectBannerItem(element) {
  if (!element) return;

  // 기존 배너 상세 조회 상태로 넘어가므로, 신규 등록을 위해 선택했던 로컬 이미지 파일 정보 및 삭제 플래그 초기화
  selectedFile = null;
  isImageDeleted = false;

  // 리스트 내 다른 모든 배너 아이템의 'active' 스타일 클래스 제거
  document.querySelectorAll('.banner-item').forEach((item) => {
    item.classList.remove('active');
  });

  // 사용자가 클릭한 배너 아이템에만 'active' 스타일을 입혀 선택 상태임을 강조
  element.classList.add('active');

  // HTML data-* 속성으로부터 배너 세부 데이터 추출
  selectedBannerId = parseInt(element.getAttribute('data-id'), 10);
  const title = element.getAttribute('data-title') || '';
  const link = element.getAttribute('data-link') || '';
  const img = element.getAttribute('data-img') || '';
  const start = element.getAttribute('data-start') || '';
  const end = element.getAttribute('data-end') || '';

  // 추출한 정보를 바탕으로 우측 상세 패널 정보 갱신
  updateRightPanelState({ id: selectedBannerId, title, link, img, start, end });
}

/**
 * 우측 상세 링크 패널의 입력 필드와 미리보기를 갱신 및 활성화/비활성화 처리합니다.
 *
 * @param {Object|null} bannerData 화면에 세팅할 배너 정보 객체 (null인 경우 전체 비활성화)
 */
function updateRightPanelState(bannerData) {
  const previewImg = document.getElementById('bannerPreviewImg');
  const previewPlaceholder = document.getElementById('bannerPreviewPlaceholder');
  const deletePreviewBtn = document.getElementById('btnDeletePreviewImg');
  const titleInput = document.getElementById('bannerTitleInput');
  const linkInput = document.getElementById('bannerLinkInput');
  const startsAtInput = document.getElementById('bannerStartsAtInput');
  const endsAtInput = document.getElementById('bannerEndsAtInput');

  // 등록된 배너가 아예 없거나, 신규/수정 상태가 아닐 때의 패널 비활성화 처리
  if (!bannerData) {
    if (previewImg) previewImg.style.display = 'none';
    if (previewPlaceholder) previewPlaceholder.style.display = 'flex';
    if (deletePreviewBtn) deletePreviewBtn.style.display = 'none';

    // 입력 인풋 필드들 전부 비활성화(disabled) 및 값 초기화
    if (titleInput) {
      titleInput.value = '';
      titleInput.disabled = true;
    }
    if (linkInput) {
      linkInput.value = '';
      linkInput.disabled = true;
    }
    if (startsAtInput) {
      startsAtInput.value = '';
      startsAtInput.disabled = true;
    }
    if (endsAtInput) {
      endsAtInput.value = '';
      endsAtInput.disabled = true;
    }
    updateLinkPreview('');
    selectedBannerId = null;
    return;
  }

  // 배너 데이터가 존재하는 경우 인풋 창 활성화 및 데이터 주입
  if (titleInput) {
    titleInput.disabled = false;
    titleInput.value = bannerData.title;
  }
  if (linkInput) {
    linkInput.disabled = false;
    linkInput.value = bannerData.link;
  }
  if (startsAtInput) {
    startsAtInput.disabled = false;
    startsAtInput.value = bannerData.start;
  }
  if (endsAtInput) {
    endsAtInput.disabled = false;
    endsAtInput.value = bannerData.end;
    if (bannerData.start) {
      endsAtInput.min = bannerData.start;
    }
  }

  // 썸네일 이미지 파일이 있을 경우 이미지 박스에 노출하고, 없을 경우 디폴트 플레이스홀더 노출
  if (previewImg && bannerData.img && !isImageDeleted) {
    previewImg.src = bannerData.img;
    previewImg.style.display = 'block';
    if (previewPlaceholder) previewPlaceholder.style.display = 'none';
    if (deletePreviewBtn) deletePreviewBtn.style.display = 'flex'; // 이미지 삭제 버튼 표출
  } else {
    if (previewImg) previewImg.style.display = 'none';
    if (previewPlaceholder) previewPlaceholder.style.display = 'flex';
    if (deletePreviewBtn) deletePreviewBtn.style.display = 'none';
  }

  // 우측 하단 링크 실시간 미리보기 텍스트도 매칭
  updateLinkPreview(bannerData.link);
}

/**
 * 사용자가 상품 링크 URL 인풋에 값을 칠 때마다 실시간으로 미리보기 텍스트를 연동해 줍니다.
 *
 * @param {string} value 사용자가 입력한 상품 링크 URL 문자열
 */
function updateLinkPreview(value) {
  const previewText = document.getElementById('linkPreviewText');
  const previewAnchor = document.getElementById('linkPreviewAnchor');

  const displayValue =
    value.trim() || '링크를 등록하시면 배너 클릭 시 이동 경로가 미리 노출됩니다.';

  if (previewText) {
    previewText.innerText = displayValue;
    if (value.trim()) {
      previewText.classList.remove('text-muted');
    } else {
      previewText.classList.add('text-muted');
    }
  }

  if (previewAnchor) {
    if (value.trim()) {
      previewAnchor.href = value.trim();
      previewAnchor.style.pointerEvents = 'auto';
      previewAnchor.style.opacity = '1';
    } else {
      previewAnchor.removeAttribute('href');
      previewAnchor.style.pointerEvents = 'none';
      previewAnchor.style.opacity = '0.4';
    }
  }
}

/**
 * 숨겨진 배너 파일 <input type="file"> 태그를 마우스 클릭하도록 트리거합니다.
 */
function triggerFileUpload() {
  const fileInput = document.getElementById('bannerFileInput');
  if (fileInput) {
    fileInput.click();
  }
}

/**
 * [배너이미지 추가] 기능: 사용자가 로컬 파일 탐색기에서 사진을 선택하면 실행됩니다.
 * 사진을 우측 프로모션 배너 미리보기 영역에 연동하고, 새 배너 등록 폼 입력란을 활성화시킵니다.
 *
 * @param {HTMLInputElement} input 파일 선택 이벤트를 발생시킨 file input 객체
 */
function handleBannerImageSelect(input) {
  if (!input.files || input.files.length === 0) return;

  // 1. 선택된 로컬 이미지 파일을 전역 변수에 적재
  selectedFile = input.files[0];
  isImageDeleted = false; // 새로 사진을 넣었으므로 삭제 상태 해제

  // 2. 만약 기존 배너를 수정한 게 아니라 아예 "신규 추가" 상태라면 왼쪽 리스트의 모든 아이템 선택 해제
  if (!selectedBannerId) {
    document.querySelectorAll('.banner-item').forEach((item) => {
      item.classList.remove('active');
    });
  }

  // 3. 오늘 날짜 및 기본 날짜 계산 (종료일은 기본 오늘 기준 30일 후로 제공)
  const today = new Date();
  const offset = today.getTimezoneOffset() * 60000;
  const localToday = new Date(today.getTime() - offset);
  const todayStr = localToday.toISOString().split('T')[0];

  const nextMonth = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
  const offsetNext = nextMonth.getTimezoneOffset() * 60000;
  const localNext = new Date(nextMonth.getTime() - offsetNext);
  const nextMonthStr = localNext.toISOString().split('T')[0];

  // 4. 우측 텍스트 입력창들의 disabled 속성을 풀어 입력을 허용하고 디폴트값 세팅 (신규 추가 시에만 빈 값 설정)
  const previewImg = document.getElementById('bannerPreviewImg');
  const previewPlaceholder = document.getElementById('bannerPreviewPlaceholder');
  const deletePreviewBtn = document.getElementById('btnDeletePreviewImg');
  const titleInput = document.getElementById('bannerTitleInput');
  const linkInput = document.getElementById('bannerLinkInput');
  const startsAtInput = document.getElementById('bannerStartsAtInput');
  const endsAtInput = document.getElementById('bannerEndsAtInput');

  if (titleInput) {
    titleInput.disabled = false;
    if (!selectedBannerId) {
      titleInput.value = ''; // 신규 등록 시에만 초기화
      titleInput.focus();
    }
  }
  if (linkInput) {
    linkInput.disabled = false;
    if (!selectedBannerId) linkInput.value = '';
  }
  if (startsAtInput) {
    startsAtInput.disabled = false;
    if (!selectedBannerId) startsAtInput.value = todayStr;
    startsAtInput.min = todayStr;
  }
  if (endsAtInput) {
    endsAtInput.disabled = false;
    if (!selectedBannerId) endsAtInput.value = nextMonthStr;
    endsAtInput.min = startsAtInput.value || todayStr;
  }

  // 5. FileReader를 활용하여 사용자가 로컬에서 고른 이미지 사진을 우측 미리보기 영역에 실시간 연동
  if (selectedFile) {
    const reader = new FileReader();
    reader.onload = function (e) {
      if (previewImg) {
        previewImg.src = e.target.result;
        previewImg.style.display = 'block';
      }
      if (previewPlaceholder) {
        previewPlaceholder.style.display = 'none';
      }
      if (deletePreviewBtn) {
        deletePreviewBtn.style.display = 'flex'; // 이미지 선택되었으므로 x 삭제 버튼 표시
      }
    };
    reader.readAsDataURL(selectedFile);
  }
}

/**
 * 우측 미리보기 상자에서 'x' 버튼을 클릭했을 때 호출됩니다.
 * 임시로 이미지 미리보기를 지우고 플러스(+) 모양 플레이스홀더를 노출합니다.
 * (저장을 누르기 전까지는 실제 서버 데이터에 영향을 주지 않으므로 복구 가능)
 *
 * @param {Event} event 클릭 이벤트
 */
function clearPreviewImage(event) {
  if (event) event.stopPropagation();

  selectedFile = null; // 대기 중인 새 파일 정보 비움

  if (selectedBannerId) {
    // 기존 배너 수정 상황이라면 이미지가 삭제된 상태임을 기억
    isImageDeleted = true;
  }

  const previewImg = document.getElementById('bannerPreviewImg');
  const previewPlaceholder = document.getElementById('bannerPreviewPlaceholder');
  const deletePreviewBtn = document.getElementById('btnDeletePreviewImg');

  if (previewImg) {
    previewImg.src = '';
    previewImg.style.display = 'none';
  }
  if (previewPlaceholder) {
    previewPlaceholder.style.display = 'flex';
  }
  if (deletePreviewBtn) {
    deletePreviewBtn.style.display = 'none';
  }

  // 파일 <input> 태그 안의 값도 초기화하여 사용자가 방금 제거했던 이미지 파일을 똑같이 재선택할 수 있게 처리
  const fileInput = document.getElementById('bannerFileInput');
  if (fileInput) fileInput.value = '';
}

/**
 * 우측 상단의 저장 버튼을 누를 때 호출됩니다.
 * 유효성 검사(NotNull 체크)를 통과하면 신규 등록 또는 수정을 백엔드 REST API로 전송하여 저장합니다.
 */
function saveBannerLink() {
  const titleInput = document.getElementById('bannerTitleInput');
  const titleValue = titleInput ? titleInput.value.trim() : '';

  const linkInput = document.getElementById('bannerLinkInput');
  const linkValue = linkInput ? linkInput.value.trim() : '';

  const startsAtInput = document.getElementById('bannerStartsAtInput');
  const endsAtInput = document.getElementById('bannerEndsAtInput');
  const startsAtValue = startsAtInput ? startsAtInput.value : '';
  const endsAtValue = endsAtInput ? endsAtInput.value : '';

  // [유효성 검사 - NotNull] 배너 제목 검증
  if (!titleValue) {
    alert('배너 제목은 필수 입력 항목입니다.');
    if (titleInput) titleInput.focus();
    return;
  }

  // [유효성 검사 - NotNull] 시작일자 검증
  if (!startsAtValue) {
    alert('시작일자를 선택해 주세요.');
    if (startsAtInput) startsAtInput.focus();
    return;
  }

  // [유효성 검사 - NotNull] 종료일자 검증
  if (!endsAtValue) {
    alert('종료일자를 선택해 주세요.');
    if (endsAtInput) endsAtInput.focus();
    return;
  }

  // [유효성 검사 - NotNull] 배너 이미지 파일 검증 (신규 추가 및 기존 배너 이미지 삭제 시)
  // 1) 신규 추가인 경우: selectedFile이 필수
  if (!selectedBannerId && !selectedFile) {
    alert('배너 이미지를 필수로 추가해 주세요.');
    return;
  }
  // 2) 기존 수정인 경우: 기존 이미지를 삭제(x)해놓고 새로운 이미지를 올리지 않은 상태
  if (selectedBannerId && isImageDeleted && !selectedFile) {
    alert('수정 시에도 배너 이미지는 필수로 존재해야 합니다. 이미지를 다시 추가해 주세요.');
    return;
  }

  // 날짜 관계 논리성 유효성 체크
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const startDate = new Date(startsAtValue);
  startDate.setHours(0, 0, 0, 0);

  const endDate = new Date(endsAtValue);
  endDate.setHours(0, 0, 0, 0);

  if (startDate < today) {
    alert('시작일은 오늘 이후로 설정해야 합니다.');
    return;
  }

  if (endDate < startDate) {
    alert('종료일은 시작일 이후로 설정해야 합니다.');
    return;
  }

  const saveBtn = document.querySelector('.btn-save');
  const originalText = saveBtn ? saveBtn.innerHTML : '';

  // 1. [신규 등록 상태] 이미지 파일이 업로드 예약 상태인 경우
  if (selectedFile && !selectedBannerId) {
    const formData = new FormData();
    formData.append('file', selectedFile); // 이미지 파일 필수 전송 (NotNull)
    formData.append('title', titleValue);
    formData.append('link', linkValue);
    formData.append('startsAt', startsAtValue.replace(/-/g, '.'));
    formData.append('endsAt', endsAtValue.replace(/-/g, '.'));

    // 버튼 로딩 상태 UI 갱신 및 비활성화 (중복 제출 방지)
    if (saveBtn) {
      saveBtn.disabled = true;
      saveBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>저장 중...';
    }

    // 신규 등록 API 호출 (POST multipart)
    fetch('/admin/api/promotions', {
      method: 'POST',
      body: formData,
    })
      .then((res) => res.json())
      .then((response) => {
        if (response.isSuccess) {
          alert('배너가 성공적으로 등록되었습니다.');
          location.reload(); // 새로고침하여 목록에 반영
        } else {
          alert('배너 등록에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
          if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = originalText;
          }
        }
      })
      .catch((err) => {
        console.error('Error saving new banner:', err);
        alert('서버와 통신하는 중 오류가 발생했습니다.');
        if (saveBtn) {
          saveBtn.disabled = false;
          saveBtn.innerHTML = originalText;
        }
      });
    return;
  }

  // 2. [기존 배너 수정 상태]
  if (!selectedBannerId) {
    // 이미지 파일도 없고, 선택한 배너도 없다면 등록 불가능하므로 NotNull 에러 안내
    alert('배너 이미지를 새로 추가하거나, 목록에서 수정할 배너를 먼저 선택해 주세요.');
    return;
  }

  // 2-A) 기존 배너 수정 시, 새롭게 이미지를 선택하여 이미지를 교체하고자 하는 경우
  if (selectedFile) {
    const formData = new FormData();
    formData.append('file', selectedFile); // 교체할 새로운 이미지 파일 객체
    formData.append('title', titleValue);
    formData.append('link', linkValue);
    formData.append('startsAt', startsAtValue.replace(/-/g, '.'));
    formData.append('endsAt', endsAtValue.replace(/-/g, '.'));

    if (saveBtn) {
      saveBtn.disabled = true;
      saveBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>저장 중...';
    }

    // multipart 수정 API 호출 (POST /admin/api/promotions/{id})
    fetch(`/admin/api/promotions/${selectedBannerId}`, {
      method: 'POST',
      body: formData,
    })
      .then((res) => res.json())
      .then((response) => {
        if (response.isSuccess) {
          alert('배너 이미지와 정보가 성공적으로 수정되었습니다.');
          location.reload(); // 이미지 경로 변경이 일어났으므로 안전하게 화면 리로드하여 목록 갱신
        } else {
          alert('수정에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
          if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = originalText;
          }
        }
      })
      .catch((err) => {
        console.error('Error updating banner with image:', err);
        alert('서버와 통신하는 중 오류가 발생했습니다.');
        if (saveBtn) {
          saveBtn.disabled = false;
          saveBtn.innerHTML = originalText;
        }
      });
    return;
  }

  // 2-B) 이미지는 변경하지 않고, 기존 텍스트 및 기간 데이터만 수정하는 경우
  if (saveBtn) {
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>저장 중...';
  }

  // 기존 텍스트 수정 API 호출 (PUT json)
  fetch(`/admin/api/promotions/${selectedBannerId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      title: titleValue,
      link: linkValue,
      startsAt: startsAtValue.replace(/-/g, '.'),
      endsAt: endsAtValue.replace(/-/g, '.'),
    }),
  })
    .then((res) => res.json())
    .then((response) => {
      if (response.isSuccess) {
        alert('배너 정보가 성공적으로 수정되었습니다.');

        // DOM 데이터를 갱신하여 즉각적인 반응성 확보 (페이지 새로고침 없이 정보 유지)
        const activeItem = document.querySelector(`.banner-item[data-id="${selectedBannerId}"]`);
        if (activeItem) {
          activeItem.setAttribute('data-title', titleValue);
          activeItem.setAttribute('data-link', linkValue);
          activeItem.setAttribute('data-start', startsAtValue);
          activeItem.setAttribute('data-end', endsAtValue);

          // 왼쪽 목록의 제목 텍스트 변경
          const titleSpan = activeItem.querySelector('.banner-title');
          if (titleSpan) {
            titleSpan.innerText = titleValue;
          }

          // 왼쪽 목록의 노출 기간 포맷에 맞게 텍스트 변경
          const dateSpan = activeItem.querySelector('.banner-date');
          if (dateSpan) {
            const formattedStart = startsAtValue.replace(/-/g, '.');
            const formattedEnd = endsAtValue.replace(/-/g, '.');
            dateSpan.innerText = `${formattedStart} ~ ${formattedEnd}`;
          }

          // 실시간 종료 상태 체크하여 뱃지 동적 관리
          let statusBadge = activeItem.querySelector('.status-badge.expired');
          const todayDate = new Date();
          todayDate.setHours(0, 0, 0, 0);
          const endDateTime = new Date(endsAtValue);
          endDateTime.setHours(23, 59, 59, 999);

          if (endDateTime < todayDate) {
            // 날짜가 만료되었는데 화면에 뱃지가 없으면 생성
            if (!statusBadge) {
              const titleWrapper = activeItem.querySelector('.d-flex.align-items-center');
              if (titleWrapper) {
                statusBadge = document.createElement('span');
                statusBadge.className = 'status-badge expired';
                statusBadge.innerText = '종료';
                titleWrapper.appendChild(statusBadge);
              }
            }
          } else {
            // 날짜가 아직 종료되지 않았는데 종료 뱃지가 붙어 있으면 삭제
            if (statusBadge) {
              statusBadge.remove();
            }
          }
        }
        if (saveBtn) {
          saveBtn.disabled = false;
          saveBtn.innerHTML = originalText;
        }
      } else {
        alert('저장에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
        if (saveBtn) {
          saveBtn.disabled = false;
          saveBtn.innerHTML = originalText;
        }
      }
    })
    .catch((err) => {
      console.error('Error saving banner info:', err);
      alert('서버와 통신하는 중 오류가 발생했습니다.');
      if (saveBtn) {
        saveBtn.disabled = false;
        saveBtn.innerHTML = originalText;
      }
    });
}

/**
 * 삭제 버튼 클릭 시 프로모션을 삭제하는 API를 호출합니다.
 * (하드디스크 원본 이미지도 백엔드에서 물리적으로 지워집니다.)
 *
 * @param {number} id 삭제할 배너의 식별자 ID
 * @param {Event} event 클릭 이벤트 객체 (부모 로우 상세조회 클릭 전파 방지용)
 */
function deleteBanner(id, event) {
  if (event) {
    event.stopPropagation(); // 부모 아이템 클릭(상세조회) 이벤트 전파 방지
  }

  if (
    !confirm(
      '정말로 이 프로모션 배너를 삭제하시겠습니까?\n삭제 시 메인 화면에 더 이상 노출되지 않으며, 서버의 원본 이미지도 함께 영구 삭제됩니다.',
    )
  ) {
    return;
  }

  // 삭제 API 호출 (DELETE)
  fetch(`/admin/api/promotions/${id}`, {
    method: 'DELETE',
  })
    .then((res) => res.json())
    .then((response) => {
      if (response.isSuccess) {
        alert('배너가 성공적으로 삭제되었습니다.');
        location.reload(); // 성공 시 리스트 갱신을 위해 새로고침
      } else {
        alert('배너 삭제에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
      }
    })
    .catch((err) => {
      console.error('Error deleting banner:', err);
      alert('서버와 통신하는 중 오류가 발생했습니다.');
    });
}

/**
 * 입력 내용 초기화 (리셋 버튼용)
 */
function resetForm() {
  selectedFile = null;
  isImageDeleted = false;
  const activeItem = document.querySelector(`.banner-item[data-id="${selectedBannerId}"]`);
  if (activeItem) {
    selectBannerItem(activeItem);
  } else {
    updateRightPanelState(null);
  }
}
