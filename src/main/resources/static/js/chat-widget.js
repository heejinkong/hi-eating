(() => {
  const root = document.getElementById('chat-widget-root');
  if (!root) {
    return;
  }

  const launcher = root.querySelector('.chat-widget__launcher');
  const closeButton = root.querySelector('.chat-widget__close');
  const panel = document.getElementById('chat-widget-panel');
  const messagesElement = document.getElementById('chat-widget-messages');
  const statusElement = document.getElementById('chat-widget-status');
  const form = document.getElementById('chat-widget-form');
  const input = document.getElementById('chat-widget-input');
  const badge = document.getElementById('chat-widget-badge');

  let socket = null;
  let currentRoomId = null;
  let unreadCount = 0;

  const isAuthenticated = () => root.dataset.authenticated === 'true';

  const setStatus = (message) => {
    if (statusElement) {
      statusElement.textContent = message;
    }
  };

  const setUnreadCount = (count) => {
    unreadCount = Math.max(0, Number(count || 0));
    if (!badge) {
      return;
    }

    badge.hidden = unreadCount === 0;
    badge.textContent = unreadCount > 99 ? '99+' : String(unreadCount);
  };

  const buildWsUrl = () => {
    const url = new URL(root.dataset.wsUrl || '/ws/chat', window.location.origin);
    url.protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    url.searchParams.set('mode', 'user');
    return url.toString();
  };

  const formatTime = (value) => {
    if (!value) {
      return '';
    }

    return new Date(value).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const scrollToBottom = () => {
    messagesElement.scrollTop = messagesElement.scrollHeight;
  };

  const renderEmpty = () => {
    messagesElement.replaceChildren();
    const empty = document.createElement('p');
    empty.className = 'chat-widget__empty';
    empty.textContent = '궁금한 점을 남겨주시면 관리자가 답변합니다.';
    messagesElement.appendChild(empty);
  };

  const appendMessage = (message) => {
    if (!message) {
      return;
    }

    const empty = messagesElement.querySelector('.chat-widget__empty');
    if (empty) {
      empty.remove();
    }

    const item = document.createElement('article');
    item.className = `chat-widget__message ${
      message.senderType === 'ADMIN' ? 'is-admin' : 'is-user'
    }`;

    const bubble = document.createElement('div');
    bubble.className = 'chat-widget__bubble';
    bubble.textContent = message.content || '';

    const meta = document.createElement('span');
    meta.className = 'chat-widget__meta';
    meta.textContent =
      message.senderType === 'ADMIN'
        ? `관리자 · ${formatTime(message.createdAt)}`
        : formatTime(message.createdAt);

    item.append(bubble, meta);
    messagesElement.appendChild(item);
    scrollToBottom();
  };

  const renderMessages = (messages) => {
    messagesElement.replaceChildren();
    if (!messages || messages.length === 0) {
      renderEmpty();
      return;
    }

    messages.forEach(appendMessage);
    scrollToBottom();
  };

  const loadRoom = async () => {
    setStatus('이전 대화를 불러오는 중');
    const response = await fetch(root.dataset.roomUrl, {
      headers: {
        Accept: 'application/json',
      },
    });

    if (!response.ok || response.redirected) {
      throw new Error('Failed to load chat room.');
    }

    const body = await response.json();
    const result = body.result;
    currentRoomId = result.room.roomId;
    setUnreadCount(result.room.userUnreadCount);
    renderMessages(result.messages);
    setStatus('실시간 연결 중');
  };

  const connect = () => {
    if (
      socket &&
      (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)
    ) {
      return;
    }

    socket = new WebSocket(buildWsUrl());

    socket.addEventListener('open', () => {
      setStatus('상담 가능');
    });

    socket.addEventListener('message', (event) => {
      const payload = JSON.parse(event.data);
      if (payload.type === 'ERROR') {
        setStatus(payload.error || '메시지를 보낼 수 없습니다');
        return;
      }

      if (payload.type !== 'MESSAGE' || !payload.message) {
        return;
      }

      currentRoomId = payload.room?.roomId || currentRoomId;
      appendMessage(payload.message);

      if (payload.message.senderType === 'ADMIN' && panel.hidden) {
        setUnreadCount(unreadCount + 1);
      } else {
        setUnreadCount(0);
      }
    });

    socket.addEventListener('close', () => {
      setStatus('연결이 끊겼습니다');
      setTimeout(connect, 3000);
    });
  };

  const openPanel = async () => {
    if (!isAuthenticated()) {
      window.location.href = root.dataset.loginUrl || '/login';
      return;
    }

    panel.hidden = false;
    launcher.setAttribute('aria-expanded', 'true');
    setUnreadCount(0);

    try {
      await loadRoom();
      connect();
      input.focus();
    } catch (error) {
      console.error(error);
      setStatus('상담을 불러오지 못했습니다');
    }
  };

  const closePanel = () => {
    panel.hidden = true;
    launcher.setAttribute('aria-expanded', 'false');
  };

  launcher?.addEventListener('click', () => {
    if (panel.hidden) {
      openPanel();
      return;
    }

    closePanel();
  });

  closeButton?.addEventListener('click', closePanel);

  input?.addEventListener('input', () => {
    input.style.height = 'auto';
    input.style.height = `${Math.min(input.scrollHeight, 96)}px`;
  });

  input?.addEventListener('keydown', (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      form.requestSubmit();
    }
  });

  form?.addEventListener('submit', (event) => {
    event.preventDefault();
    const content = input.value.trim();
    if (!content) {
      return;
    }

    connect();
    if (!socket || socket.readyState !== WebSocket.OPEN) {
      setStatus('연결 후 다시 보내주세요');
      return;
    }

    socket.send(
      JSON.stringify({
        type: 'SEND_MESSAGE',
        roomId: currentRoomId,
        content,
      }),
    );
    input.value = '';
    input.style.height = 'auto';
  });
})();
