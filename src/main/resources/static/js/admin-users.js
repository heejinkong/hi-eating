document.addEventListener('DOMContentLoaded', () => {
  const page = document.querySelector('.admin-users-main');
  const signupForm = document.querySelector('.signup-form');
  const emailLocalInput = document.getElementById('signup-email-local');
  const emailDomainSelect = document.getElementById('signup-email-domain');
  const emailCheckButton = document.getElementById('signup-email-check-button');
  const passwordInput = document.getElementById('signup-password');
  const passwordConfirmInput = document.getElementById('signup-password-confirm');
  const modalElement = document.getElementById('signup-email-check-modal');
  const modalTitle = document.getElementById('signup-email-check-modal-title');
  const modalMessage = document.getElementById('signup-email-check-modal-message');
  const candidatesList = document.getElementById('admin-candidates-list');
  const adminList = document.getElementById('admin-list');
  const candidatesRefreshButton = document.getElementById('admin-candidates-refresh-button');
  const adminListRefreshButton = document.getElementById('admin-list-refresh-button');
  const roleMessage = document.getElementById('admin-users-role-message');

  const showModal = (title, message, state) => {
    if (!modalElement || !modalMessage) {
      alert(message);
      return;
    }

    if (modalTitle) {
      modalTitle.textContent = title;
    }

    modalMessage.textContent = message;
    modalMessage.classList.remove('is-success', 'is-danger');

    if (state) {
      modalMessage.classList.add(state);
    }

    if (window.bootstrap?.Modal) {
      window.bootstrap.Modal.getOrCreateInstance(modalElement).show();
      return;
    }

    alert(message);
  };

  const showRoleMessage = (message, state) => {
    if (!roleMessage) {
      return;
    }

    roleMessage.textContent = message;
    roleMessage.classList.remove('is-success', 'is-danger', 'is-muted');
    roleMessage.classList.add(state || 'is-muted');
  };

  const buildEmail = () => {
    const emailLocal = emailLocalInput?.value.trim() || '';
    const emailDomain = emailDomainSelect?.value.trim() || '';

    if (!emailLocal) {
      return '';
    }

    if (emailLocal.includes('@') || !emailDomain) {
      return emailLocal;
    }

    return `${emailLocal}@${emailDomain}`;
  };

  emailCheckButton?.addEventListener('click', async () => {
    const email = buildEmail();
    const checkUrl = emailCheckButton.dataset.checkUrl;

    if (!email) {
      showModal('이메일 중복확인', '이메일을 입력해 주세요.', 'is-danger');
      emailLocalInput?.focus();
      return;
    }

    try {
      const response = await fetch(`${checkUrl}?email=${encodeURIComponent(email)}`, {
        headers: {
          Accept: 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Email check request failed');
      }

      const result = await response.json();
      showModal('이메일 중복확인', result.message, result.available ? 'is-success' : 'is-danger');
    } catch (error) {
      showModal(
        '이메일 중복확인',
        '중복확인 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
        'is-danger',
      );
    }
  });

  signupForm?.addEventListener('submit', (event) => {
    if (!passwordInput || !passwordConfirmInput) {
      return;
    }

    if (passwordInput.value !== passwordConfirmInput.value) {
      event.preventDefault();
      showModal('비밀번호 확인', '비밀번호가 일치하지 않습니다.', 'is-danger');
      passwordConfirmInput.focus();
    }
  });

  if (!page || !candidatesList || !adminList) {
    return;
  }

  const urls = {
    candidates: page.dataset.adminCandidatesUrl,
    admins: page.dataset.adminsUrl,
    adminRolePrefix: page.dataset.adminRoleUrlPrefix,
  };

  const setListState = (container, message, state) => {
    container.replaceChildren();
    const stateElement = document.createElement('p');
    stateElement.className = `admin-users-list-state ${state || ''}`.trim();
    stateElement.textContent = message;
    container.appendChild(stateElement);
  };

  const formatBirth = (birth) => {
    if (!birth) {
      return '-';
    }

    return birth.replaceAll('-', '.');
  };

  const genderLabel = (gender) => {
    if (gender === 'MALE') {
      return '남성';
    }

    if (gender === 'FEMALE') {
      return '여성';
    }

    return '-';
  };

  const fetchApi = async (url, options) => {
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json',
        ...(options?.headers || {}),
      },
      ...options,
    });

    const body = await response.json();
    if (!response.ok || body.isSuccess === false) {
      throw new Error(body.message || '요청을 처리할 수 없습니다.');
    }

    return body.result;
  };

  const createUserItem = (user, action) => {
    const item = document.createElement('article');
    item.className = `admin-users-list-item is-${action.type}`;

    if (action.type === 'grant') {
      item.tabIndex = 0;
      item.setAttribute('role', 'button');
      item.setAttribute('aria-label', `${user.name} 관리자 권한 부여`);
    }

    const details = document.createElement('div');
    details.className = 'admin-users-list-details';

    const name = document.createElement('strong');
    name.textContent = user.name;

    const email = document.createElement('span');
    email.textContent = user.email;

    const meta = document.createElement('small');
    meta.textContent = `${genderLabel(user.gender)} · ${formatBirth(user.birth)}`;

    details.append(name, email, meta);

    const button = document.createElement('button');
    button.type = 'button';
    button.className = `admin-users-list-action is-${action.type}`;
    button.textContent = action.label;
    button.addEventListener('click', (event) => {
      event.stopPropagation();
      action.handler(user.id, user.name);
    });

    if (action.type === 'grant') {
      item.addEventListener('click', () => action.handler(user.id, user.name));
      item.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          action.handler(user.id, user.name);
        }
      });
    }

    item.append(details, button);
    return item;
  };

  const renderList = (container, users, action) => {
    container.replaceChildren();

    if (!users || users.length === 0) {
      setListState(container, container.dataset.emptyMessage, 'is-empty');
      return;
    }

    users.forEach((user) => {
      container.appendChild(createUserItem(user, action));
    });
  };

  const loadCandidates = async () => {
    setListState(candidatesList, candidatesList.dataset.loadingMessage, 'is-loading');

    try {
      const users = await fetchApi(urls.candidates);
      renderList(candidatesList, users, {
        type: 'grant',
        label: '권한 부여',
        handler: grantAdminRole,
      });
    } catch (error) {
      setListState(candidatesList, error.message, 'is-error');
    }
  };

  const loadAdmins = async () => {
    setListState(adminList, adminList.dataset.loadingMessage, 'is-loading');

    try {
      const users = await fetchApi(urls.admins);
      renderList(adminList, users, {
        type: 'revoke',
        label: '권한 회수',
        handler: revokeAdminRole,
      });
    } catch (error) {
      setListState(adminList, error.message, 'is-error');
    }
  };

  async function grantAdminRole(userId, userName) {
    try {
      await fetchApi(`${urls.adminRolePrefix}/${userId}/admin-role`, {
        method: 'POST',
      });
      showRoleMessage(`${userName}님에게 관리자 권한을 부여했습니다.`, 'is-success');
      await Promise.all([loadCandidates(), loadAdmins()]);
    } catch (error) {
      showRoleMessage(error.message, 'is-danger');
    }
  }

  async function revokeAdminRole(userId, userName) {
    if (!confirm(`${userName}님의 관리자 권한을 회수할까요?`)) {
      return;
    }

    try {
      await fetchApi(`${urls.adminRolePrefix}/${userId}/admin-role`, {
        method: 'DELETE',
      });
      showRoleMessage(`${userName}님의 관리자 권한을 회수했습니다.`, 'is-success');
      await Promise.all([loadCandidates(), loadAdmins()]);
    } catch (error) {
      showRoleMessage(error.message, 'is-danger');
    }
  }

  candidatesRefreshButton?.addEventListener('click', loadCandidates);
  adminListRefreshButton?.addEventListener('click', loadAdmins);

  showRoleMessage('권한 목록을 불러오는 중입니다.', 'is-muted');
  Promise.all([loadCandidates(), loadAdmins()])
    .then(() => showRoleMessage('권한 목록이 최신 상태입니다.', 'is-muted'))
    .catch(() => showRoleMessage('권한 목록을 불러오지 못했습니다.', 'is-danger'));
});
