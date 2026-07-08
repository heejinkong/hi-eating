/**
 * 회원가입 페이지 – 나비 마스코트 Lottie 인터랙션
 *
 * lottie-web (bodymovin) 라이브러리를 사용합니다.
 * lottie-player의 XHR responseType 버그 없이 안정적으로 동작합니다.
 */

/* ───────────────────────────────────────────────────────────── *
 *  상태별 Lottie JSON 경로 + 메시지
 * ───────────────────────────────────────────────────────────── */
const MASCOT_STATES = {
  idle: {
    path: '/lotties/nabi-idle.json',
    message: '가입 정보를<br> 차근차근 알려주세요',
  },
  greeting: {
    path: '/lotties/nabi-greeting.json',
    message: '반갑습니다!<br>이름을 알려주세요 😊',
  },
  curious: {
    path: '/lotties/nabi-curious.json',
    message: '이메일 주소를<br> 입력해 주세요 📧',
  },
  covering: {
    path: '/lotties/nabi-covering.json',
    message: '비밀번호는<br> 제가 안 볼게요 🙈',
  },
  peeking: {
    path: '/lotties/nabi-peeking.json',
    message: '한 번 더 맞춰볼게요 👀',
  },
  thinking: {
    path: '/lotties/nabi-thinking.json',
    message: '생일이 언제예요? 🗓️',
  },
};

/* ───────────────────────────────────────────────────────────── *
 *  필드별 설정
 * ───────────────────────────────────────────────────────────── */
const FIELD_CONFIG = [
  { selector: '#signup-name', state: 'greeting' },
  { selector: '#signup-email-local', state: 'curious' },
  {
    selector: '#signup-email-domain',
    state: 'curious',
    message: '이메일 도메인을 선택해 주세요',
  },
  { selector: '#signup-password', state: 'covering' },
  { selector: '#signup-password-confirm', state: 'peeking' },
  { selector: '#signup-birth', state: 'thinking' },
  {
    selector: 'input[name="gender"]',
    state: 'curious',
    message: '어느 쪽이세요? 🤔',
    isRadio: true,
  },
];

/* ───────────────────────────────────────────────────────────── */

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
  const signupMascot = document.querySelector('[data-signup-mascot]');
  const mascotMessageEl = document.querySelector('[data-signup-mascot-message]');
  const lottieContainer = document.getElementById('mascot-lottie');

  // 진단 로그 제거 완료

  if (!signupForm || !modalElement || !modalMessage) {
    return;
  }

  /* ── lottie-web 인스턴스 관리 ── */
  let currentAnim = null;
  let currentState = '';
  let resetTimer = null;

  /* ── JSON 캐시 (같은 파일 재요청 방지) ── */
  const jsonCache = {};

  /**
   * fetch()로 JSON을 직접 로드 후 lottie.loadAnimation(animationData)로 전달.
   * lottie 내부 XHR(responseType 버그)을 완전히 우회합니다.
   * @param {string} path - JSON 파일 경로
   */
  const loadAnimation = async (path) => {
    if (!lottieContainer || typeof lottie === 'undefined') {
      return;
    }

    // 이전 애니메이션 파기 + SVG 누적 방지 즉시 초기화
    if (currentAnim) {
      currentAnim.destroy();
      currentAnim = null;
    }
    lottieContainer.innerHTML = '';

    try {
      if (!jsonCache[path]) {
        const res = await fetch(path);
        if (!res.ok) {
          throw new Error(`Lottie fetch failed: ${path} (${res.status})`);
        }
        jsonCache[path] = await res.json();
      }

      // 다른 호출이 이미 렌더링했으면 중단
      if (lottieContainer.innerHTML !== '') {
        return;
      }

      currentAnim = lottie.loadAnimation({
        container: lottieContainer,
        renderer: 'svg',
        loop: true,
        autoplay: true,
        animationData: jsonCache[path],
      });

      currentAnim.addEventListener('DOMLoaded', () => {
        const svg = lottieContainer.querySelector('svg');
        if (svg) {
          svg.setAttribute('width', '100%');
          svg.setAttribute('height', '100%');
          svg.style.width = '100%';
          svg.style.height = '100%';
        }
      });
    } catch (err) {
      console.warn('[Mascot] Lottie load error:', err);
    }
  };

  /* ── 마스코트 상태 전환 ── */
  const setMascotState = (stateName, customMessage) => {
    const config = MASCOT_STATES[stateName] || MASCOT_STATES.idle;
    const message = customMessage ?? config.message;

    if (mascotMessageEl) {
      mascotMessageEl.innerHTML = message;
    }

    if (stateName === currentState) {
      return;
    }
    currentState = stateName;

    if (signupMascot) {
      signupMascot.setAttribute('data-mascot-state', stateName);
    }
    loadAnimation(config.path);
  };

  /* ── blur 후 idle 복귀 ── */
  const scheduleReset = () => {
    clearTimeout(resetTimer);
    resetTimer = window.setTimeout(() => {
      const active = document.activeElement;
      if (!active || !signupForm.contains(active)) {
        setMascotState('idle');
      }
    }, 120);
  };

  /* ── 초기 idle 상태 ── */
  setMascotState('idle');

  /* ── 필드 이벤트 바인딩 ── */
  FIELD_CONFIG.forEach(({ selector, state, message, isRadio }) => {
    if (isRadio) {
      document.querySelectorAll(selector).forEach((el) => {
        el.addEventListener('focus', () => {
          clearTimeout(resetTimer);
          setMascotState(state, message);
        });
        el.addEventListener('change', () => {
          clearTimeout(resetTimer);
          setMascotState(state, message);
        });
        el.addEventListener('blur', scheduleReset);
      });
    } else {
      const el = document.querySelector(selector);
      if (!el) {
        return;
      }
      el.addEventListener('focus', () => {
        clearTimeout(resetTimer);
        setMascotState(state, message);
      });
      el.addEventListener('blur', scheduleReset);
    }
  });

  /* ── 모달 헬퍼 ── */
  const showModal = (title, msg, state) => {
    if (modalTitle) {
      modalTitle.textContent = title;
    }
    modalMessage.textContent = msg;
    modalMessage.classList.remove('is-success', 'is-danger');
    if (state) {
      modalMessage.classList.add(state);
    }
    if (window.bootstrap?.Modal) {
      window.bootstrap.Modal.getOrCreateInstance(modalElement).show();
    } else {
      alert(msg);
    }
  };

  /* ── 이메일 빌더 ── */
  const buildEmail = () => {
    const local = emailLocalInput.value.trim();
    const domain = emailDomainSelect.value.trim();
    if (!local) {
      return '';
    }
    if (local.includes('@') || !domain) {
      return local;
    }
    return `${local}@${domain}`;
  };

  /* ── 이메일 중복확인 ── */
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
        headers: { Accept: 'application/json' },
      });
      if (!response.ok) {
        throw new Error('Email check request failed');
      }

      const result = await response.json();
      showModal('이메일 중복확인', result.message, result.available ? 'is-success' : 'is-danger');
    } catch {
      showModal(
        '이메일 중복확인',
        '중복확인 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
        'is-danger',
      );
    }
  });

  /* ── 폼 제출 검증 ── */
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
