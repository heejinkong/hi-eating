document.addEventListener('DOMContentLoaded', () => {
  const page = document.querySelector('.admin-email-main');
  const editor = document.querySelector('[data-admin-email-editor]');
  const passPublishButton = document.querySelector('[data-admin-email-publish-pass-button]');
  const selectedPublishButton = document.querySelector(
    '[data-admin-email-publish-selected-button]',
  );
  const selectAllCheckbox = document.querySelector('[data-admin-email-select-all]');
  const tableBody = document.querySelector('[data-admin-email-table-body]');
  const message = editor?.querySelector('[data-admin-email-editor-message]');
  const saveButton = editor?.querySelector('[data-admin-email-save-button]');
  const currentPublishButton = editor?.querySelector('[data-admin-email-publish-current-button]');
  const detailStatus = document.querySelector('[data-admin-email-detail-validation-status]');
  const filterButtons = Array.from(document.querySelectorAll('[data-admin-email-filter]'));
  const subjectInput = editor?.querySelector('[name="subject"]');
  const recipientInput = editor?.querySelector('input[type="email"]');
  const contentInput = editor?.querySelector('[name="content"]');
  const validationNote = document.querySelector('[data-admin-email-validation-note]');
  const validationReason = document.querySelector('[data-admin-email-validation-reason]');

  let selectedEmailDraftId = editor?.dataset.emailDraftId
    ? Number(editor.dataset.emailDraftId)
    : null;
  let currentFilter = 'ALL';

  const validationMeta = {
    PASS: { label: '검증 통과', className: 'is-success' },
    FAIL: { label: '부적합', className: 'is-danger' },
    PENDING: { label: '검증 대기', className: 'is-warning' },
  };

  const publishMeta = {
    PENDING: { label: '관리자 검수 필요', className: 'is-muted' },
    READY: { label: '발행 대기', className: 'is-brand' },
    PUBLISHED: { label: '발행 완료', className: 'is-success' },
    SENDING: { label: '발송 중', className: 'is-brand' },
    SENT: { label: '발송 완료', className: 'is-success' },
    RETRYING: { label: '재시도 대기', className: 'is-warning' },
    FAILED: { label: '발행 실패', className: 'is-danger' },
  };

  const showMessage = (text, state) => {
    if (!message) {
      alert(text);
      return;
    }

    message.textContent = text;
    message.hidden = false;
    message.classList.remove('is-success', 'is-danger');

    if (state) {
      message.classList.add(state);
    }
  };

  const hideMessage = () => {
    if (message) {
      message.hidden = true;
      message.textContent = '';
      message.classList.remove('is-success', 'is-danger');
    }
  };

  const fetchApi = async (url, options) => {
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...(options?.headers || {}),
      },
      ...options,
    });

    const body = await response.json();
    if (!response.ok || body.isSuccess === false) {
      throw new Error(body.message || '요청을 처리하지 못했습니다.');
    }

    return body.result;
  };

  const createBatchPublishMessage = (result, successText) => {
    const publishedCount = result?.publishedCount ?? result?.publishedEmails?.length ?? 0;
    const failedCount = result?.failedCount ?? result?.failedEmails?.length ?? 0;

    if (failedCount === 0) {
      return { text: successText, state: 'is-success' };
    }

    const failedSummary = (result.failedEmails || [])
      .slice(0, 3)
      .map((failure) => `#${failure.emailDraftId || '-'} ${failure.reason}`)
      .join(' / ');
    const suffix = failedSummary ? ` (${failedSummary})` : '';
    const state = publishedCount > 0 ? 'is-success' : 'is-danger';

    return {
      text: `RabbitMQ 발행 ${publishedCount}건 완료, ${failedCount}건 실패${suffix}`,
      state,
    };
  };

  const getValidationMeta = (draft) =>
    validationMeta[draft.validationStatus] || {
      label: draft.validationStatusLabel || '검증 대기',
      className: draft.validationStatusClass || 'is-warning',
    };

  const getPublishMeta = (draft) =>
    publishMeta[draft.publishStatus] || {
      label: draft.publishStatusLabel || '관리자 검수 필요',
      className: draft.publishStatusClass || 'is-muted',
    };

  const resetStatusClass = (element) => {
    element.classList.remove('is-success', 'is-danger', 'is-warning', 'is-brand', 'is-muted');
  };

  const createStatus = (meta) => {
    const status = document.createElement('span');
    status.className = `admin-email-status ${meta.className}`;
    status.textContent = meta.label;
    return status;
  };

  const updateSummary = (summary) => {
    document.querySelectorAll('[data-admin-email-summary]').forEach((element) => {
      const key = element.dataset.adminEmailSummary;
      element.textContent = String(summary?.[key] ?? 0);
    });
  };

  const updateFilterButtons = () => {
    filterButtons.forEach((button) => {
      button.classList.toggle('is-active', button.dataset.adminEmailFilter === currentFilter);
    });
  };

  const filterDrafts = (drafts) => {
    if (currentFilter === 'VALIDATION_FAIL') {
      return drafts.filter((draft) => draft.validationStatus === 'FAIL');
    }

    if (currentFilter === 'PUBLISH_READY') {
      return drafts.filter((draft) => draft.publishStatus === 'READY');
    }

    return drafts;
  };

  const setSelectedRow = (emailDraftId) => {
    document.querySelectorAll('[data-admin-email-row]').forEach((row) => {
      row.classList.toggle(
        'is-selected',
        Number(row.dataset.emailDraftId) === Number(emailDraftId),
      );
    });
  };

  const createEmailRow = (draft) => {
    const row = document.createElement('tr');
    row.dataset.adminEmailRow = '';
    row.dataset.emailDraftId = String(draft.id);
    row.classList.toggle('is-selected', Number(draft.id) === Number(selectedEmailDraftId));

    const selectCell = document.createElement('td');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.value = String(draft.id);
    checkbox.disabled = !draft.publishable && !draft.validationFailed;
    checkbox.dataset.adminEmailSelect = '';
    checkbox.setAttribute('aria-label', `${draft.recipientName || '수신자'} 메일 선택`);
    selectCell.appendChild(checkbox);

    const recipientCell = document.createElement('td');
    const link = document.createElement('a');
    link.className = 'admin-email-row-link';
    link.href = `/admin/emails?emailId=${draft.id}`;
    link.dataset.adminEmailDetailLink = '';
    link.dataset.detailUrl = `/admin/api/emails/${draft.id}`;

    const name = document.createElement('strong');
    name.textContent = draft.recipientName || '수신자';
    const email = document.createElement('span');
    email.textContent = draft.recipientEmail || '';
    link.append(name, email);
    recipientCell.appendChild(link);

    const hotDealCell = document.createElement('td');
    hotDealCell.textContent = draft.hotDealTitle || '-';

    const validationCell = document.createElement('td');
    validationCell.appendChild(createStatus(getValidationMeta(draft)));
    if (draft.validationReason) {
      const reason = document.createElement('span');
      reason.className = 'admin-email-reason';
      reason.textContent = draft.validationReason;
      validationCell.appendChild(reason);
    }

    const publishCell = document.createElement('td');
    publishCell.appendChild(createStatus(getPublishMeta(draft)));

    row.append(selectCell, recipientCell, hotDealCell, validationCell, publishCell);
    return row;
  };

  const renderTable = (drafts) => {
    if (!tableBody) {
      return;
    }

    tableBody.replaceChildren();
    filterDrafts(drafts).forEach((draft) => tableBody.appendChild(createEmailRow(draft)));
    updateFilterButtons();

    if (selectAllCheckbox) {
      selectAllCheckbox.checked = false;
    }
  };

  const renderDetail = (draft, options = {}) => {
    if (!draft || !editor) {
      return;
    }

    selectedEmailDraftId = Number(draft.id);
    editor.dataset.emailDraftId = String(draft.id);
    editor.dataset.updateUrl = `/admin/api/emails/${draft.id}/content`;
    editor.dataset.publishUrl = `/admin/api/emails/${draft.id}/publish`;

    const editable = Boolean(draft.validationFailed);
    const publishable = Boolean(draft.publishable);
    const canPublishFromDetail = publishable || editable;

    if (subjectInput) {
      subjectInput.value = draft.subject || '';
      subjectInput.readOnly = !editable;
    }
    if (recipientInput) {
      recipientInput.value = draft.recipientEmail || '';
    }
    if (contentInput) {
      contentInput.value = draft.content || '';
      contentInput.readOnly = !editable;
    }
    if (saveButton) {
      saveButton.disabled = !editable;
    }
    if (currentPublishButton) {
      currentPublishButton.disabled = !canPublishFromDetail;
    }
    if (detailStatus) {
      const meta = getValidationMeta(draft);
      resetStatusClass(detailStatus);
      detailStatus.classList.add(meta.className);
      detailStatus.textContent = meta.label;
    }
    if (validationNote && validationReason) {
      validationNote.hidden = !draft.validationReason;
      validationReason.textContent = draft.validationReason || '';
    }

    setSelectedRow(draft.id);

    if (!options.keepMessage) {
      hideMessage();
    }
  };

  const renderDashboard = (dashboard, preferredEmailDraftId) => {
    updateSummary(dashboard.summary);

    const drafts = dashboard.emailDrafts || [];
    const preferredDraft =
      drafts.find((draft) => Number(draft.id) === Number(preferredEmailDraftId)) ||
      dashboard.selectedEmailDraft ||
      drafts[0];

    if (preferredDraft) {
      selectedEmailDraftId = Number(preferredDraft.id);
    }

    renderTable(drafts);
    renderDetail(preferredDraft, { keepMessage: true });
  };

  const refreshDashboard = async (preferredEmailDraftId) => {
    const dashboardUrl = page?.dataset.dashboardUrl;
    if (!dashboardUrl) {
      return null;
    }

    const url = preferredEmailDraftId
      ? `${dashboardUrl}?emailId=${encodeURIComponent(preferredEmailDraftId)}`
      : dashboardUrl;
    const dashboard = await fetchApi(url);
    renderDashboard(dashboard, preferredEmailDraftId);
    return dashboard;
  };

  const loadEmailDetail = async (emailDraftId, detailUrl) => {
    if (!emailDraftId && !detailUrl) {
      return;
    }

    try {
      const url = detailUrl || `/admin/api/emails/${emailDraftId}`;
      const draft = await fetchApi(url);
      renderDetail(draft);
    } catch (error) {
      showMessage(error.message, 'is-danger');
    }
  };

  const saveCurrentEmail = async () => {
    const updateUrl = editor.dataset.updateUrl;
    const subject = subjectInput?.value.trim() || '';
    const content = contentInput?.value.trim() || '';

    if (!updateUrl || !subject || !content) {
      showMessage('제목과 본문을 모두 입력해주세요.', 'is-danger');
      return null;
    }

    if (saveButton) {
      saveButton.disabled = true;
    }

    try {
      const updatedEmail = await fetchApi(updateUrl, {
        method: 'PATCH',
        body: JSON.stringify({ subject, content }),
      });

      renderDetail(updatedEmail, { keepMessage: true });
      return updatedEmail;
    } catch (error) {
      showMessage(error.message, 'is-danger');
      return null;
    } finally {
      if (saveButton) {
        saveButton.disabled = false;
      }
    }
  };

  const publishCurrentEmail = async () => {
    const publishUrl = editor?.dataset.publishUrl;
    if (!publishUrl) {
      showMessage('발송할 이메일을 선택해주세요.', 'is-danger');
      return;
    }

    if (currentPublishButton) {
      currentPublishButton.disabled = true;
    }

    try {
      let targetEmailDraftId = selectedEmailDraftId;
      if (subjectInput && contentInput && !subjectInput.readOnly && !contentInput.readOnly) {
        const savedEmail = await saveCurrentEmail();
        if (!savedEmail) {
          return;
        }
        targetEmailDraftId = savedEmail.id;
      }

      const publishedEmail = await fetchApi(publishUrl, { method: 'POST' });
      await refreshDashboard(publishedEmail.id || targetEmailDraftId);
      showMessage('RabbitMQ 발행이 완료되었습니다.', 'is-success');
    } catch (error) {
      await refreshDashboard(selectedEmailDraftId);
      showMessage(error.message, 'is-danger');
    } finally {
      if (currentPublishButton) {
        currentPublishButton.disabled = false;
      }
    }
  };

  const publishSelectedEmails = async () => {
    const publishUrl = selectedPublishButton?.dataset.publishUrl;
    const emailDraftIds = Array.from(document.querySelectorAll('[data-admin-email-select]'))
      .filter((checkbox) => checkbox.checked && !checkbox.disabled)
      .map((checkbox) => Number(checkbox.value))
      .filter((value) => Number.isFinite(value));

    if (!publishUrl || emailDraftIds.length === 0) {
      showMessage('발송할 이메일을 선택해주세요.', 'is-danger');
      return;
    }

    selectedPublishButton.disabled = true;

    try {
      const result = await fetchApi(publishUrl, {
        method: 'POST',
        body: JSON.stringify({ emailDraftIds }),
      });
      await refreshDashboard(selectedEmailDraftId);
      const batchMessage = createBatchPublishMessage(
        result,
        '선택한 이메일의 승인 및 RabbitMQ 발행이 완료되었습니다.',
      );
      showMessage(batchMessage.text, batchMessage.state);
    } catch (error) {
      await refreshDashboard(selectedEmailDraftId);
      showMessage(error.message, 'is-danger');
    } finally {
      selectedPublishButton.disabled = false;
    }
  };

  const publishValidationPassedEmails = async () => {
    const publishUrl = passPublishButton?.dataset.publishUrl;
    if (!publishUrl) {
      showMessage('자동 발송 API를 찾을 수 없습니다.', 'is-danger');
      return;
    }

    passPublishButton.disabled = true;

    try {
      const result = await fetchApi(publishUrl, { method: 'POST' });
      await refreshDashboard(selectedEmailDraftId);
      const batchMessage = createBatchPublishMessage(
        result,
        '검증 통과 이메일의 RabbitMQ 발행이 완료되었습니다.',
      );
      showMessage(batchMessage.text, batchMessage.state);
    } catch (error) {
      await refreshDashboard(selectedEmailDraftId);
      showMessage(error.message, 'is-danger');
    } finally {
      passPublishButton.disabled = false;
    }
  };

  editor?.addEventListener('submit', async (event) => {
    event.preventDefault();

    const savedEmail = await saveCurrentEmail();
    if (savedEmail) {
      await refreshDashboard(savedEmail.id);
      showMessage('이메일 내용이 저장되었습니다.', 'is-success');
    }
  });

  tableBody?.addEventListener('click', async (event) => {
    const row = event.target.closest('[data-admin-email-row]');
    if (!row) {
      return;
    }

    const checkbox = event.target.closest('[data-admin-email-select]');
    if (!checkbox) {
      event.preventDefault();
    }

    const link = row.querySelector('[data-admin-email-detail-link]');
    loadEmailDetail(row.dataset.emailDraftId, link?.dataset.detailUrl);
  });

  currentPublishButton?.addEventListener('click', publishCurrentEmail);
  selectedPublishButton?.addEventListener('click', publishSelectedEmails);
  passPublishButton?.addEventListener('click', publishValidationPassedEmails);

  filterButtons.forEach((button) => {
    button.addEventListener('click', () => {
      currentFilter = button.dataset.adminEmailFilter || 'ALL';
      refreshDashboard(selectedEmailDraftId);
    });
  });

  selectAllCheckbox?.addEventListener('change', () => {
    document.querySelectorAll('[data-admin-email-select]').forEach((checkbox) => {
      if (!checkbox.disabled) {
        checkbox.checked = selectAllCheckbox.checked;
      }
    });
  });
});
