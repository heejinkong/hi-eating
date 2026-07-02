document.addEventListener('DOMContentLoaded', () => {
  const signupForm = document.querySelector('.signup-form');
  const emailLocalInput = document.getElementById('signup-email-local');
  const emailDomainSelect = document.getElementById('signup-email-domain');
  const emailCheckButton = document.getElementById('signup-email-check-button');
  const passwordInput = document.getElementById('signup-password');
  const passwordConfirmInput = document.getElementById('signup-password-confirm');
  const modalElement = document.getElementById('signup-email-check-modal');
  const modalTitle = document.getElementById('signup-email-check-modal-title');
  const modalMessage = document.getElementById('signup-email-check-modal-message');

  if (!signupForm || !modalElement || !modalMessage) {
    return;
  }

  const showModal = (title, message, state) => {
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

  const buildEmail = () => {
    const emailLocal = emailLocalInput.value.trim();
    const emailDomain = emailDomainSelect.value.trim();

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
      emailLocalInput.focus();
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

  signupForm.addEventListener('submit', (event) => {
    if (!passwordInput || !passwordConfirmInput) {
      return;
    }

    if (passwordInput.value !== passwordConfirmInput.value) {
      event.preventDefault();
      showModal('비밀번호 확인', '비밀번호가 일치하지 않습니다.', 'is-danger');
      passwordConfirmInput.focus();
    }
  });
});
