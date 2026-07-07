/**
 * 로그인 페이지 – 나비 마스코트 Lottie 인터랙션
 * 비밀번호 입력 시 covering, 아이디 입력 시 curious 상태
 */
document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.querySelector('.auth-form');
  const container = document.getElementById('login-mascot-lottie');
  const passwordInput = document.getElementById('login-password');
  const usernameInput = document.getElementById('login-username');
  const rememberInput = document.querySelector('input[name="rememberId"]');

  const rememberStorageKey = 'hiEatingRememberedLoginId';

  const getRememberedId = () => {
    try {
      return localStorage.getItem(rememberStorageKey);
    } catch (err) {
      console.warn('[LoginRememberId] read error:', err);
      return null;
    }
  };

  const setRememberedId = (value) => {
    try {
      localStorage.setItem(rememberStorageKey, value);
    } catch (err) {
      console.warn('[LoginRememberId] write error:', err);
    }
  };

  const removeRememberedId = () => {
    try {
      localStorage.removeItem(rememberStorageKey);
    } catch (err) {
      console.warn('[LoginRememberId] remove error:', err);
    }
  };

  if (usernameInput && rememberInput) {
    const rememberedId = getRememberedId();
    if (rememberedId) {
      usernameInput.value = rememberedId;
      rememberInput.checked = true;
    }

    rememberInput.addEventListener('change', () => {
      if (!rememberInput.checked) {
        removeRememberedId();
      }
    });
  }

  loginForm?.addEventListener('submit', () => {
    if (!usernameInput || !rememberInput) return;

    const username = usernameInput.value.trim();
    if (rememberInput.checked && username) {
      setRememberedId(username);
    } else {
      removeRememberedId();
    }
  });

  if (!container || typeof lottie === 'undefined') return;

  const jsonCache = {};
  let currentAnim = null;
  let loading = false; // 중복 호출 방지 플래그

  const loadAnimation = async (path) => {
    // 이전 애니메이션 파기 + 컨테이너 즉시 초기화 (SVG 누적 방지)
    if (currentAnim) {
      currentAnim.destroy();
      currentAnim = null;
    }
    container.innerHTML = '';

    loading = true;
    try {
      if (!jsonCache[path]) {
        const res = await fetch(path);
        if (!res.ok) throw new Error(`fetch failed: ${path}`);
        jsonCache[path] = await res.json();
      }

      // innerHTML이 비어있지 않으면(다른 호출이 이미 렌더링) 중단
      if (container.innerHTML !== '') return;

      currentAnim = lottie.loadAnimation({
        container,
        renderer: 'svg',
        loop: true,
        autoplay: true,
        animationData: jsonCache[path],
      });

      currentAnim.addEventListener('DOMLoaded', () => {
        const svg = container.querySelector('svg');
        if (svg) {
          svg.setAttribute('width', '100%');
          svg.setAttribute('height', '100%');
          svg.style.width = '100%';
          svg.style.height = '100%';
        }
      });
    } catch (err) {
      console.warn('[LoginMascot] error:', err);
    } finally {
      loading = false;
    }
  };

  const setState = (state) => {
    container.setAttribute('data-mascot-state', state);
    loadAnimation(`/lotties/nabi-${state}.json`);
  };

  // 초기 idle
  setState('idle');

  // 아이디 필드: curious
  usernameInput?.addEventListener('focus', () => setState('curious'));
  usernameInput?.addEventListener('blur', () => setState('idle'));

  // 비밀번호 필드: covering
  passwordInput?.addEventListener('focus', () => setState('covering'));
  passwordInput?.addEventListener('blur', () => setState('idle'));
});
