document.addEventListener('DOMContentLoaded', () => {
  const page = document.querySelector('.admin-chat-main');
  if (!page) {
    return;
  }

  const roomsElement = document.getElementById('admin-chat-rooms');
  const roomCountElement = document.getElementById('admin-chat-room-count');
  const refreshButton = document.getElementById('admin-chat-refresh');
  const messagesElement = document.getElementById('admin-chat-messages');
  const userNameElement = document.getElementById('admin-chat-user-name');
  const userEmailElement = document.getElementById('admin-chat-user-email');
  const statusElement = document.getElementById('admin-chat-status');
  const statusSelect = document.getElementById('admin-chat-status-select');
  const form = document.getElementById('admin-chat-form');
  const input = document.getElementById('admin-chat-input');

  let rooms = [];
  let selectedRoomId = null;
  let socket = null;

  const setStatus = (message) => {
    statusElement.textContent = message;
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
      throw new Error(body.message || '요청을 처리하지 못했습니다.');
    }

    return body.result;
  };

  const buildWsUrl = () => {
    const url = new URL(page.dataset.wsUrl || '/ws/chat', window.location.origin);
    url.protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    url.searchParams.set('mode', 'admin');
    return url.toString();
  };

  const formatTime = (value) => {
    if (!value) {
      return '';
    }

    return new Date(value).toLocaleString('ko-KR', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const renderRoomState = (message) => {
    roomsElement.replaceChildren();
    const state = document.createElement('p');
    state.className = 'admin-chat-state';
    state.textContent = message;
    roomsElement.appendChild(state);
  };

  const renderRooms = () => {
    roomsElement.replaceChildren();
    roomCountElement.textContent = String(rooms.length);

    if (rooms.length === 0) {
      renderRoomState('아직 들어온 상담이 없습니다.');
      return;
    }

    rooms.forEach((room) => {
      const item = document.createElement('button');
      item.type = 'button';
      item.className = `admin-chat-room ${room.roomId === selectedRoomId ? 'is-active' : ''}`;

      const head = document.createElement('div');
      head.className = 'admin-chat-room-head';

      const name = document.createElement('span');
      name.className = 'admin-chat-room-name';
      name.textContent = room.userName || '사용자';
      head.appendChild(name);

      if (room.adminUnreadCount > 0) {
        const unread = document.createElement('span');
        unread.className = 'admin-chat-room-unread';
        unread.textContent = room.adminUnreadCount > 99 ? '99+' : String(room.adminUnreadCount);
        head.appendChild(unread);
      }

      const email = document.createElement('span');
      email.className = 'admin-chat-room-email';
      email.textContent = room.userEmail || '';

      const preview = document.createElement('span');
      preview.className = 'admin-chat-room-preview';
      preview.textContent =
        room.lastMessageContent || `상담방 생성 · ${formatTime(room.createdAt)}`;

      item.append(head, email, preview);
      item.addEventListener('click', () => selectRoom(room.roomId));
      roomsElement.appendChild(item);
    });
  };

  const scrollToBottom = () => {
    messagesElement.scrollTop = messagesElement.scrollHeight;
  };

  const appendMessage = (message) => {
    const item = document.createElement('article');
    item.className = `admin-chat-message ${
      message.senderType === 'ADMIN' ? 'is-admin' : 'is-user'
    }`;

    const bubble = document.createElement('div');
    bubble.className = 'admin-chat-bubble';
    bubble.textContent = message.content || '';

    const meta = document.createElement('span');
    meta.className = 'admin-chat-meta';
    meta.textContent = `${message.senderName || ''} · ${formatTime(message.createdAt)}`;

    item.append(bubble, meta);
    messagesElement.appendChild(item);
  };

  const renderMessages = (messages) => {
    messagesElement.replaceChildren();
    if (!messages || messages.length === 0) {
      const empty = document.createElement('p');
      empty.className = 'admin-chat-empty';
      empty.textContent = '아직 메시지가 없습니다.';
      messagesElement.appendChild(empty);
      return;
    }

    messages.forEach(appendMessage);
    scrollToBottom();
  };

  const syncSelectedRoomHeader = (room) => {
    userNameElement.textContent = room?.userName || '상담방을 선택하세요';
    userEmailElement.textContent = room?.userEmail || '사용자 문의가 이곳에 표시됩니다.';
  };

  const loadRooms = async () => {
    renderRoomState('상담방을 불러오는 중입니다.');
    rooms = await fetchApi(page.dataset.roomsUrl);
    renderRooms();

    if (!selectedRoomId && rooms.length > 0) {
      await selectRoom(rooms[0].roomId);
      return;
    }

    if (selectedRoomId) {
      const selectedRoom = rooms.find((room) => room.roomId === selectedRoomId);
      syncSelectedRoomHeader(selectedRoom);
    }
  };

  const loadAdminStatus = async () => {
    if (!statusSelect) {
      return;
    }

    const result = await fetchApi(page.dataset.statusUrl);
    statusSelect.value = result.status === 'AWAY' ? 'AWAY' : 'ONLINE';
  };

  const updateAdminStatus = async (status, showStatusMessage = true) => {
    if (!statusSelect) {
      return;
    }

    statusSelect.disabled = true;
    try {
      const result = await fetchApi(page.dataset.statusUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status }),
      });
      statusSelect.value = result.status;
      if (showStatusMessage) {
        setStatus(result.status === 'AWAY' ? '자리비움 상태' : '상담 가능');
      }
    } catch (error) {
      console.error(error);
      setStatus('상태를 변경하지 못했습니다');
      await loadAdminStatus();
    } finally {
      statusSelect.disabled = false;
    }
  };

  async function selectRoom(roomId) {
    selectedRoomId = roomId;
    const selectedRoom = rooms.find((room) => room.roomId === selectedRoomId);
    syncSelectedRoomHeader(selectedRoom);
    renderRooms();
    setStatus('대화를 불러오는 중');

    try {
      const result = await fetchApi(`${page.dataset.roomUrlPrefix}/${roomId}/messages`);
      const updatedRoom = result.room;
      rooms = rooms.map((room) => (room.roomId === updatedRoom.roomId ? updatedRoom : room));
      syncSelectedRoomHeader(updatedRoom);
      renderRooms();
      renderMessages(result.messages);
      setStatus('상담 가능');
      input.focus();
    } catch (error) {
      console.error(error);
      setStatus('대화를 불러오지 못했습니다');
    }
  }

  const upsertRoom = (room) => {
    if (!room) {
      return;
    }

    rooms = rooms.filter((item) => item.roomId !== room.roomId);
    rooms.unshift(room);
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
      setStatus('실시간 연결됨');
      if (statusSelect?.value === 'AWAY') {
        updateAdminStatus('AWAY', false);
      } else if (statusSelect) {
        statusSelect.value = 'ONLINE';
      }
    });
    socket.addEventListener('close', () => {
      setStatus('실시간 연결 끊김');
      setTimeout(connect, 3000);
    });
    socket.addEventListener('message', (event) => {
      const payload = JSON.parse(event.data);
      if (payload.type === 'ERROR') {
        setStatus(payload.error || '메시지를 처리하지 못했습니다');
        return;
      }

      if (payload.type !== 'MESSAGE') {
        return;
      }

      upsertRoom(payload.room);
      renderRooms();

      if (payload.room?.roomId === selectedRoomId && payload.message) {
        appendMessage(payload.message);
        scrollToBottom();
      }
    });
  };

  refreshButton?.addEventListener('click', () => {
    connect();
    loadRooms().catch((error) => {
      console.error(error);
      renderRoomState('상담방을 불러오지 못했습니다.');
    });
  });

  statusSelect?.addEventListener('change', () => {
    updateAdminStatus(statusSelect.value);
  });

  input?.addEventListener('input', () => {
    input.style.height = 'auto';
    input.style.height = `${Math.min(input.scrollHeight, 120)}px`;
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
    if (!content || !selectedRoomId) {
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
        roomId: selectedRoomId,
        content,
      }),
    );
    input.value = '';
    input.style.height = 'auto';
  });

  connect();
  loadAdminStatus().catch((error) => {
    console.error(error);
    setStatus('상태를 불러오지 못했습니다');
  });
  loadRooms().catch((error) => {
    console.error(error);
    renderRoomState('상담방을 불러오지 못했습니다.');
    setStatus('초기화 실패');
  });
});
