const getCsrfToken = () => {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : '';
};

async function parseApiResponse(response) {
  const result = await response.json();
  if (!response.ok || !result.isSuccess) {
    throw new Error(result.message || '요청 처리에 실패했습니다.');
  }
  return result;
}

export async function reorderPromotions(request) {
  const response = await fetch('/admin/api/promotions/order', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
    body: JSON.stringify(request),
  });
  return parseApiResponse(response);
}

export async function createPromotion(formData) {
  const response = await fetch('/admin/api/promotions', {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': getCsrfToken() },
    body: formData,
  });
  return parseApiResponse(response);
}

export async function updatePromotionWithImage(id, formData) {
  const response = await fetch(`/admin/api/promotions/${id}`, {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': getCsrfToken() },
    body: formData,
  });
  return parseApiResponse(response);
}

export async function updatePromotion(id, request) {
  const response = await fetch(`/admin/api/promotions/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
    body: JSON.stringify(request),
  });
  return parseApiResponse(response);
}

export async function deletePromotion(id) {
  const response = await fetch(`/admin/api/promotions/${id}`, {
    method: 'DELETE',
    headers: { 'X-XSRF-TOKEN': getCsrfToken() },
  });
  return parseApiResponse(response);
}
