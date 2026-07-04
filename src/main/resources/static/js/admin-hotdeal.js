let currentSort = 'EXPIRE_ASC';

// 등록 폼 전송을 위한 선택한 상품 정보 추적
let selectedProducts = [];
let editingHotDealId = null; // 수정중인 핫딜 ID를 저장하는 전역변수

let currentPage = 1;

document.addEventListener('DOMContentLoaded', () => {
  searchProducts();

  // 핫딜 등록 폼 submit 이벤트 핸들러 등록
  const hotDealForm = document.getElementById('hotDealForm');
  if (hotDealForm) {
    let isHotDealSubmitting = false;
    hotDealForm.addEventListener('submit', (e) => {
      e.preventDefault();
      if (isHotDealSubmitting) return;

      if (selectedProducts.length === 0) {
        alert('핫딜에 등록할 상품을 최소 하나 이상 선택해야 합니다.');
        return;
      }

      const title = document.getElementById('dealTitle').value;
      const description = document.getElementById('dealDescription').value;
      const startsAt = document.getElementById('startsAt').value;
      const endsAt = document.getElementById('endsAt').value;
      const discountRate = parseInt(document.getElementById('discountRate').value, 10);

      // 날짜 구분자 변환 (yyyy-MM-dd -> yyyy.MM.dd)
      const formattedStartsAt = startsAt.replace(/-/g, '.');
      const formattedEndsAt = endsAt.replace(/-/g, '.');

      // 상품 목록 DTO 매핑
      const products = selectedProducts.map((p) => ({
        productOptionId: p.optionId,
        originalPrice: p.price,
      }));

      const requestData = {
        title,
        description,
        startsAt: formattedStartsAt,
        endsAt: formattedEndsAt,
        discountRate,
        products,
      };

      isHotDealSubmitting = true;
      const submitButton = hotDealForm.querySelector('button[type="submit"]');
      if (submitButton) submitButton.disabled = true;

      const isEdit = editingHotDealId !== null;
      const url = isEdit ? `/admin/hotdeals/${editingHotDealId}` : '/admin/hotdeals';
      const method = isEdit ? 'PUT' : 'POST';

      fetch(url, {
        method: method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData),
      })
        .then((res) => res.json())
        .then((response) => {
          if (response.isSuccess) {
            alert(
              isEdit ? '핫딜이 성공적으로 수정되었습니다.' : '핫딜이 성공적으로 등록되었습니다.',
            );
            location.reload();
          } else {
            alert(
              (isEdit ? '핫딜 수정에 실패했습니다: ' : '핫딜 등록에 실패했습니다: ') +
                (response.message || '알 수 없는 오류'),
            );
          }
        })
        .catch((err) => {
          console.error(isEdit ? 'Error updating hotdeal:' : 'Error creating hotdeal:', err);
          alert('서버와 통신하는 중 오류가 발생했습니다.');
        })
        .finally(() => {
          isHotDealSubmitting = false;
          if (submitButton) submitButton.disabled = false;
        });
    });
  }

  // 핫딜 종료 버튼 이벤트 핸들러 등록
  const btnEndHotDeal = document.getElementById('btnEndHotDeal');
  if (btnEndHotDeal) {
    btnEndHotDeal.addEventListener('click', () => {
      if (!editingHotDealId) {
        alert('종료할 핫딜을 하단 목록에서 선택(수정 버튼 클릭)한 뒤 진행해 주세요.');
        return;
      }

      if (
        confirm('정말로 이 핫딜을 종료하시겠습니까?\n종료 시 더 이상 고객에게 노출되지 않습니다.')
      ) {
        fetch(`/admin/hotdeals/${editingHotDealId}`, {
          method: 'DELETE',
        })
          .then((res) => res.json())
          .then((response) => {
            if (response.isSuccess) {
              alert('핫딜이 성공적으로 종료되었습니다.');
              location.reload();
            } else {
              alert('핫딜 종료에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
            }
          })
          .catch((err) => {
            console.error('Error deleting hotdeal:', err);
            alert('서버와 통신하는 중 오류가 발생했습니다.');
          });
      }
    });
  }
});

function changeSort(sortType) {
  currentSort = sortType;
  document.getElementById('searchSort').value = sortType;
  document.getElementById('btnSortExpire').classList.toggle('active', sortType === 'EXPIRE_ASC');
  document.getElementById('btnSortStock').classList.toggle('active', sortType === 'STOCK_ASC');
  searchProducts(true);
}

// 셀렉트 박스 정렬 옵션 변경 시 조회 처리
if (document.getElementById('searchSort')) {
  document.getElementById('searchSort').addEventListener('change', (e) => {
    changeSort(e.target.value);
  });
}

function goToPage(pageNum) {
  currentPage = pageNum;
  searchProducts();
}

function resetSearch() {
  // 왼쪽 검색 필터 초기화
  document.getElementById('searchKeyword').value = '';
  document.getElementById('searchCategory').value = '';

  // 오른쪽 등록 폼 입력 값 전체 초기화
  document.getElementById('dealTitle').value = '';
  document.getElementById('dealDescription').value = '';
  document.getElementById('startsAt').value = '';
  document.getElementById('endsAt').value = '';
  document.getElementById('hotDealPrice').value = '';
  document.getElementById('exposureSwitch').checked = true;
  if (document.getElementById('charCount')) {
    document.getElementById('charCount').innerText = '0';
  }

  // 선택 상품 배열 및 요약 영역 초기화
  selectedProducts = [];
  updateSelectedBox();

  // 화면에 체크되어 있는 체크박스 물리적으로 전부 해제
  const checkboxes = document.querySelectorAll('.checkbox-custom');
  checkboxes.forEach((cb) => (cb.checked = false));

  changeSort('EXPIRE_ASC');
}

function searchProducts(resetPage = false) {
  if (resetPage) {
    currentPage = 1;
  }

  const keyword = document.getElementById('searchKeyword').value;
  const categoryId = document.getElementById('searchCategory').value;
  const resultContainer = document.getElementById('productSearchResult');
  const paginationContainer = document.getElementById('productPagination');

  if (!resultContainer) return;

  resultContainer.innerHTML = `
      <div class="text-center py-5 text-muted">
        <div class="spinner-border text-danger spinner-border-sm mb-2" role="status"></div>
        <p style="font-size: 0.8rem;">상품 목록을 불러오는 중...</p>
      </div>
    `;

  let url = `/admin/api/products?sortBy=${currentSort}&page=${currentPage}`;
  if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
  if (categoryId) url += `&categoryId=${categoryId}`;
  if (editingHotDealId !== null) url += `&hotDealId=${editingHotDealId}`;

  fetch(url)
    .then((res) => res.json())
    .then((response) => {
      if (response.isSuccess) {
        const pageData = response.result;
        const products = pageData.list;
        const totalCount = pageData.totalCount;
        const totalPages = pageData.totalPages;

        document.getElementById('totalCountDisplay').innerText = `${totalCount}개`;

        if (!products || products.length === 0) {
          resultContainer.innerHTML = `
                      <div class="text-center py-5 text-muted" style="font-size: 0.85rem;">
                        <i class="bi bi-database-exclamation fs-3 d-block mb-2"></i>
                        <p>검색 조건에 맞는 상품이 없습니다.</p>
                      </div>
                    `;
          paginationContainer.innerHTML = '';
          return;
        }

        let html = '';
        products.forEach((p) => {
          // 유통기한 디데이(D-Day) 잔여 일수 계산
          let dDayStr = 'N/A';
          let isUrgent = false;
          if (p.expireDate) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const exp = new Date(p.expireDate);
            exp.setHours(0, 0, 0, 0);
            const diffTime = exp - today;
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            dDayStr = diffDays >= 0 ? `D-${diffDays}` : `D+${Math.abs(diffDays)}`;
            if (diffDays <= 7) isUrgent = true;
          }

          const statusBadgeClass =
            p.status === '폐기'
              ? 'badge-red'
              : p.status === '유통임박'
                ? 'badge-orange'
                : 'badge-gray';
          const isDiscarded = p.status === '폐기';
          const isChecked = selectedProducts.some((item) => item.optionId === p.productOptionId);

          html += `
                      <div class="product-row">
                        <div class="product-info-cell">
                          <div class="product-icon-box" style="background-color: #f2f2f2; color: #888888;">
                            <i class="bi bi-box" style="color: #888888;"></i>
                          </div>
                          <div>
                            <h4 class="product-name-lbl">${p.name}</h4>
                            <span class="product-code-lbl">P-${p.productId} / O-${p.productOptionId}</span>
                          </div>
                        </div>
                        <div class="text-muted">${p.categoryName || '기타'}</div>
                        <div class="fw-bold">${p.price.toLocaleString()}원</div>
                        <div class="text-muted">${p.stock}개</div>
                        <div>
                          <div>${p.expireDate ? p.expireDate.replace(/-/g, '.') : 'N/A'}</div>
                          <div class="${isUrgent ? 'expire-lbl-soon' : 'expire-lbl-normal'}" style="font-size: 0.75rem;">${dDayStr}</div>
                        </div>
                        <div>
                          <span class="status-badge ${statusBadgeClass}">${p.status}</span>
                        </div>
                        <div class="text-center">
                          <input type="checkbox" class="checkbox-custom" data-option-id="${p.productOptionId}"
                                 onclick="toggleProductSelect(${p.productOptionId}, '${p.name.replace(/'/g, "\\'")}', ${p.price}, this.checked, ${isDiscarded}, this)"
                                 ${isDiscarded && !isChecked ? 'disabled' : ''}
                                 ${isChecked ? 'checked' : ''}>
                        </div>
                      </div>
                    `;
        });
        resultContainer.innerHTML = html;
        renderPagination(totalPages, pageData.currentPage);
      } else {
        resultContainer.innerHTML = `
                  <div class="alert alert-danger m-3" role="alert" style="font-size: 0.85rem;">
                    오류 발생: ${response.message}
                  </div>
                `;
        paginationContainer.innerHTML = '';
      }
    })
    .catch((err) => {
      resultContainer.innerHTML = `
              <div class="alert alert-danger m-3" role="alert" style="font-size: 0.85rem;">
                서버 통신 실패: ${err.message}
              </div>
            `;
      paginationContainer.innerHTML = '';
    });
}

function renderPagination(totalPages, activePage) {
  const container = document.getElementById('productPagination');
  if (!container) return;
  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  let html = '';

  // 이전 페이지 이동
  const isFirst = activePage === 1;
  html += `
      <button class="page-btn" onclick="goToPage(${activePage - 1})" ${isFirst ? 'disabled style="opacity: 0.4; cursor: not-allowed;"' : ''}>
        <i class="bi bi-chevron-left"></i>
      </button>
    `;

  // 페이지 번호 리스트 출력 (최대 5개)
  let startPage = Math.max(1, activePage - 2);
  let endPage = Math.min(totalPages, startPage + 4);
  if (endPage - startPage < 4) {
    startPage = Math.max(1, endPage - 4);
  }

  for (let i = startPage; i <= endPage; i++) {
    const isActive = i === activePage;
    html += `
          <button class="page-btn ${isActive ? 'active' : ''}" onclick="goToPage(${i})">${i}</button>
        `;
  }

  // 총 페이지 수가 더 많을 경우 생략 기호(...) 표시
  if (endPage < totalPages) {
    html += `<button class="page-btn text-btn" disabled>...</button>`;
  }

  // 다음 페이지 이동 (오른쪽 화살표)
  const isLast = activePage === totalPages;
  html += `
      <button class="page-btn text-btn" onclick="goToPage(${activePage + 1})" ${isLast ? 'disabled style="opacity: 0.4; cursor: not-allowed;"' : ''}>
        다음 <i class="bi bi-chevron-right ms-1"></i>
      </button>
    `;

  container.innerHTML = html;
}

// 목록에서 체크박스 토글 시 선택 배열 가감 처리
function toggleProductSelect(
  optionId,
  name,
  price,
  isChecked,
  isDiscarded = false,
  checkbox = null,
) {
  const wasSelected = selectedProducts.some((item) => item.optionId === optionId);

  if (isDiscarded && isChecked && !wasSelected) {
    if (checkbox) {
      checkbox.checked = false;
      checkbox.disabled = true;
    }
    return;
  }

  if (isChecked) {
    // Avoid duplicate
    if (!wasSelected) {
      selectedProducts.push({ optionId, name, price });
    }
  } else {
    selectedProducts = selectedProducts.filter((item) => item.optionId !== optionId);
    if (isDiscarded && checkbox) {
      checkbox.disabled = true;
    }
  }
  updateSelectedBox();
}

function updateSelectedBox() {
  const box = document.getElementById('selectedProductsBox');
  if (!box) return;
  if (selectedProducts.length === 0) {
    box.innerHTML = `
          <div class="selected-header">
            <span class="selected-lbl">
              <i class="bi bi-box-seam text-primary"></i> 선택된 상품 (0개)
            </span>
          </div>
          <div class="text-center py-3 text-muted" style="font-size: 0.8rem;">
            왼쪽 목록에서 핫딜에 지정할 상품을 선택해 주세요.
          </div>
        `;
    return;
  }

  // 선택된 상품들의 총 가격 계산 및 30% 권장 할인가 추천 적용
  let totalOriginalPrice = 0;
  const namesHtml = selectedProducts
    .map((item) => {
      totalOriginalPrice += item.price;
      return `
        <div class="selected-item-row">
          <span>${escapeHtml(item.name)}</span>
          ${item.discarded ? '<span class="selected-discarded-badge">폐기</span>' : ''}
        </div>
      `;
    })
    .join('');

  box.innerHTML = `
      <div class="selected-header">
        <div class="d-flex align-items-center gap-2">
          <div class="selected-icon-badge">
            <i class="bi bi-box"></i>
          </div>
          <div class="selected-details">
            <span class="selected-lbl m-0">선택된 상품 (${selectedProducts.length}개)</span>
            <span class="selected-badge-indicator mt-1">선택됨</span>
          </div>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </div>
      <div class="selected-item-names mt-2">
        ${namesHtml}
      </div>
    `;
}

// 글자 수 제한 카운터 감지 및 업데이트
function updateCharCount() {
  const textarea = document.getElementById('dealDescription');
  const counter = document.getElementById('charCount');
  if (textarea && counter) {
    counter.innerText = textarea.value.length;
  }
}

// 추천 할인율 버튼 클릭 시 30%를 강제로 세팅하는 함수
function applyRecommendedDiscount() {
  const discountInput = document.getElementById('discountRate');
  if (discountInput) {
    discountInput.value = 30;
  }
}

// 취소 버튼 클릭 시 전체 입력 및 선택 값 초기화
function resetHotDealForm() {
  const form = document.getElementById('hotDealForm');
  if (form) {
    form.reset();
  }
  const counter = document.getElementById('charCount');
  if (counter) {
    counter.innerText = '0';
  }
  selectedProducts = [];
  updateSelectedBox();
  const checkboxes = document.querySelectorAll('#productSearchResult .checkbox-custom');
  checkboxes.forEach((cb) => (cb.checked = false));

  // 수정 모드 상태 초기화
  editingHotDealId = null;
  const sidebarTitle = document.querySelector('.right-sidebar-card h2');
  if (sidebarTitle) sidebarTitle.innerText = '핫딜 등록';

  const sidebarDesc = document.querySelector('.right-sidebar-card > p');
  if (sidebarDesc) sidebarDesc.innerText = '선택한 상품으로 새 핫딜을 등록합니다.';

  const submitButton = document.querySelector('#hotDealForm button[type="submit"]');
  if (submitButton) {
    submitButton.innerText = '핫딜 등록';
  }

  const btnEndHotDeal = document.getElementById('btnEndHotDeal');
  if (btnEndHotDeal) {
    btnEndHotDeal.style.display = 'none';
  }

  searchProducts(true);
}

// === 등록된 핫딜 페이징 및 정렬 기능 ===
let registeredDeals = [];
let currentDealPage = 1;
let currentDealSort = 'ASC'; // 기본값: 오래된순 (ASC)
let currentDealStatus = 'ALL';
const dealPageSize = 3;

// 정렬 상태 변경 및 active 클래스 토글
function changeDealSort(sortOrder) {
  currentDealSort = sortOrder;

  const btnDesc = document.getElementById('btnDealSortDesc');
  const btnAsc = document.getElementById('btnDealSortAsc');
  if (btnDesc && btnAsc) {
    if (sortOrder === 'DESC') {
      btnDesc.classList.add('active');
      btnAsc.classList.remove('active');
    } else {
      btnDesc.classList.remove('active');
      btnAsc.classList.add('active');
    }
  }

  currentDealPage = 1; // 정렬 변경 시 1페이지로 리셋
  renderRegisteredDeals();
}

function changeDealStatusFilter(status) {
  currentDealStatus = status;
  currentDealPage = 1;
  renderRegisteredDeals();
}

// 초기화 진입점
function initRegisteredDeals(dataList) {
  registeredDeals = dataList || [];
  renderRegisteredDeals();
}

// 날짜 형식 변환기 (yyyy.MM.dd)
function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  if (Array.isArray(dateStr)) {
    const y = dateStr[0];
    const m = String(dateStr[1]).padStart(2, '0');
    const d = String(dateStr[2]).padStart(2, '0');
    return `${y}.${m}.${d}`;
  }
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) {
    return dateStr.substring(0, 10).replace(/-/g, '.');
  }
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}.${m}.${d}`;
}

// 핫딜 목록 렌더링 (3개씩 정렬)
function renderRegisteredDeals() {
  const tbody = document.getElementById('registeredDealsBody');
  const totalCountLbl = document.getElementById('dealTotalCount');
  const paginationContainer = document.getElementById('registeredDealsPagination');
  if (!tbody) return;

  const filteredDeals =
    currentDealStatus === 'ALL'
      ? registeredDeals
      : registeredDeals.filter((deal) => deal.status === currentDealStatus);

  totalCountLbl.innerText = `${filteredDeals.length}개`;

  if (filteredDeals.length === 0) {
    tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-5 text-muted">
                    선택한 상태의 핫딜이 없습니다.
                </td>
            </tr>
        `;
    paginationContainer.innerHTML = '';
    return;
  }

  // 1. 상태 필터가 적용된 목록을 기간순으로 정렬
  const sortedList = [...filteredDeals].sort((a, b) => {
    const dateA = new Date(a.startsAt);
    const dateB = new Date(b.startsAt);
    return currentDealSort === 'ASC' ? dateA - dateB : dateB - dateA;
  });

  // 2. 페이징 인덱스 계산
  const totalPages = Math.ceil(sortedList.length / dealPageSize);
  if (currentDealPage > totalPages) currentDealPage = totalPages;
  if (currentDealPage < 1) currentDealPage = 1;

  const startIndex = (currentDealPage - 1) * dealPageSize;
  const pagedList = sortedList.slice(startIndex, startIndex + dealPageSize);

  // 3. 테이블 그리기
  let html = '';
  pagedList.forEach((deal) => {
    const startStr = formatDate(deal.startsAt);
    const endStr = formatDate(deal.endsAt);
    const safeTitle = escapeHtml(deal.title);

    let statusBadge = '';
    if (deal.status === 'ACTIVE') {
      statusBadge = '<span class="status-pill status-active">진행중</span>';
    } else if (deal.status === 'SCHEDULED') {
      statusBadge = '<span class="status-pill status-waiting">대기중</span>';
    } else if (deal.status === 'ENDED') {
      statusBadge = '<span class="status-pill status-closed">종료됨</span>';
    } else {
      statusBadge = `<span class="status-pill status-closed">${escapeHtml(deal.status)}</span>`;
    }

    html += `
        <tr>
            <td><strong>${safeTitle}</strong></td>
            <td>${startStr} ~ ${endStr}</td>
            <td>${deal.productCount ?? 0}개</td>
            <td class="fw-bold text-danger">${deal.discountRate ?? 0}%</td>
            <td>${statusBadge}</td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-outline-custom py-1 px-3" style="font-size: 0.8rem; font-weight: 700; border-radius: 6px;" onclick="loadHotDealForEdit(${deal.id})">수정</button>
            </td>
        </tr>
    `;
  });
  tbody.innerHTML = html;

  // 4. 페이지네이션 렌더링
  renderDealPagination(totalPages);
}

// 핫딜 목록 페이지네이션 그리기
function renderDealPagination(totalPages) {
  const container = document.getElementById('registeredDealsPagination');
  if (!container) return;
  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  let html = '';
  html += `
        <button type="button" class="page-btn" ${currentDealPage === 1 ? 'disabled' : ''} onclick="changeDealPage(${currentDealPage - 1})">
            <i class="bi bi-chevron-left"></i>
        </button>
    `;
  for (let i = 1; i <= totalPages; i++) {
    html += `
            <button type="button" class="page-btn ${currentDealPage === i ? 'active' : ''}" onclick="changeDealPage(${i})">${i}</button>
        `;
  }
  html += `
        <button type="button" class="page-btn" ${currentDealPage === totalPages ? 'disabled' : ''} onclick="changeDealPage(${currentDealPage + 1})">
            <i class="bi bi-chevron-right"></i>
        </button>
    `;
  container.innerHTML = html;
}

function changeDealPage(page) {
  currentDealPage = page;
  renderRegisteredDeals();
}

// 이스케이프 함수 (XSS 방지)
function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// 수정 대상 데이터를 서버에서 가져와 폼에 채워넣는 함수
function loadHotDealForEdit(id) {
  fetch(`/admin/hotdeals/${id}`)
    .then((res) => res.json())
    .then((response) => {
      if (response.isSuccess) {
        const deal = response.result;

        // 수정 모드 활성화
        editingHotDealId = deal.id;

        // 우측 사이드바 제목 및 서브 타이틀 변경
        const sidebarTitle = document.querySelector('.right-sidebar-card h2');
        if (sidebarTitle) sidebarTitle.innerText = '핫딜 수정';

        const sidebarDesc = document.querySelector('.right-sidebar-card p');
        if (sidebarDesc) sidebarDesc.innerText = '선택한 상품으로 핫딜 정보를 수정합니다.';

        // 등록 버튼의 글자만 핫딜 수정으로 변경 (CSS는 동일하게 유지)
        const submitButton = document.querySelector('#hotDealForm button[type="submit"]');
        if (submitButton) {
          submitButton.innerText = '핫딜 수정';
        }

        // 핫딜 종료 버튼 노출
        const btnEndHotDeal = document.getElementById('btnEndHotDeal');
        if (btnEndHotDeal) {
          btnEndHotDeal.style.display = 'block';
        }

        // 입력 필드 바인딩
        document.getElementById('dealTitle').value = deal.title;
        document.getElementById('dealDescription').value = deal.description || '';
        updateCharCount();

        if (deal.startsAt) {
          document.getElementById('startsAt').value = deal.startsAt.substring(0, 10);
        }
        if (deal.endsAt) {
          document.getElementById('endsAt').value = deal.endsAt.substring(0, 10);
        }

        document.getElementById('discountRate').value = deal.discountRate || 30;

        // 선택된 상품 정보 복원
        selectedProducts = deal.products.map((p) => ({
          optionId: p.productOptionId,
          name: p.productName,
          price: p.originalPrice,
          expireDate: p.expireDate,
          discarded: p.discarded,
        }));

        updateSelectedBox();
        searchProducts(true);

        // 스크롤 포커싱
        document.querySelector('.right-sidebar-card').scrollIntoView({ behavior: 'smooth' });
      } else {
        alert('핫딜 정보를 불러오지 못했습니다: ' + (response.message || '알 수 없는 오류'));
      }
    })
    .catch((err) => {
      console.error('Error loading hotdeal detail:', err);
      alert('서버와 통신하는 중 오류가 발생했습니다.');
    });
}
