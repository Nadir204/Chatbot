//ngrok http 8080 ---ngrok cmd
//jokon ngrok theke url nibo tokon aita chnage korbo
//const API = 'https://10f5-103-139-164-107.ngrok-free.app';
// local host takle aita use
const API = 'http://localhost:8080';

//  Central headers — works for both localhost and ngrok
// ngrok-skip-browser-warning bypasses ngrok's warning page
const HEADERS = {
  'Content-Type':               'application/json',
  'ngrok-skip-browser-warning': 'true'
};

const userId   = sessionStorage.getItem('userId');
const username = sessionStorage.getItem('username') || 'User';

if (!userId) window.location.href = 'login.html';

// ── Set up user display ────────────────────────
document.getElementById('username-display').textContent = username;
document.getElementById('user-avatar').textContent = username.charAt(0).toUpperCase();

// ── Session / history store ────────────────────
let currentSessionId = null;
let sessions = JSON.parse(localStorage.getItem(`cb_sessions_${userId}`) || '[]');

function saveSessions() {
  localStorage.setItem(`cb_sessions_${userId}`, JSON.stringify(sessions));
}

function getCurrentSession() {
  return sessions.find(s => s.id === currentSessionId);
}

// ── Sidebar toggle (mobile only) ──────────────
function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
  document.getElementById('sidebar-overlay').classList.toggle('show');
}

function closeSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebar-overlay').classList.remove('show');
}

// ── Configure marked ──────────────────────────
//  FIXED: marked.setOptions() removed — conflicts with marked.use() in v9
// Everything (breaks, gfm, renderer) combined into ONE marked.use() call

// Custom renderer for code blocks with copy button
const renderer = new marked.Renderer();
renderer.code = function(code, lang) {
  const language = (lang || 'plaintext').toLowerCase();
  const highlighted = hljs.getLanguage(language)
    ? hljs.highlight(code, { language }).value
    : hljs.highlightAuto(code).value;

  const id = 'cb-' + Math.random().toString(36).slice(2, 9);
  return `
    <div class="code-block-wrapper">
      <div class="code-block-header">
        <span class="code-lang">${language}</span>
        <button class="copy-btn" onclick="copyCode('${id}', this)">⎘ Copy</button>
      </div>
      <pre><code id="${id}" class="hljs language-${language}">${highlighted}</code></pre>
    </div>`;
};

//  breaks + gfm + renderer all in ONE marked.use() — no conflict
marked.use({ breaks: true, gfm: true, renderer });

// ── Copy code button ───────────────────────────
function copyCode(id, btn) {
  const code = document.getElementById(id).innerText;
  navigator.clipboard.writeText(code).then(() => {
    btn.textContent = '✓ Copied';
    btn.classList.add('copied');
    setTimeout(() => {
      btn.textContent = '⎘ Copy';
      btn.classList.remove('copied');
    }, 2000);
  });
}

// ── Render sidebar history ─────────────────────
function renderHistory() {
  const list = document.getElementById('history-list');
  list.innerHTML = '';
  if (!sessions.length) {
    list.innerHTML = '<div style="padding:10px 16px;font-size:12px;color:var(--text-muted)">No chats yet</div>';
    return;
  }
  // Newest first
  [...sessions].reverse().forEach(s => {
    const div = document.createElement('div');
    div.className = 'history-item' + (s.id === currentSessionId ? ' active' : '');
    div.innerHTML = `
      <span class="h-icon">💬</span>
      <span class="h-text">${escapeHtml(s.title)}</span>
      <span class="h-del" onclick="deleteSession(event, '${s.id}')">✕</span>`;
    div.onclick = (e) => {
      if (e.target.classList.contains('h-del')) return;
      loadSession(s.id);
    };
    list.appendChild(div);
  });
}

function deleteSession(e, id) {
  e.stopPropagation();
  sessions = sessions.filter(s => s.id !== id);
  saveSessions();
  if (currentSessionId === id) newChat();
  else renderHistory();
}

// ── Session management ─────────────────────────
function newChat() {
  currentSessionId = 'sess_' + Date.now();
  const session = { id: currentSessionId, title: 'New Chat', messages: [] };
  sessions.push(session);
  saveSessions();
  clearMessages();
  document.getElementById('chat-title').textContent = 'New Conversation';
  renderHistory();
  closeSidebar(); // close on mobile after new chat
}

function loadSession(id) {
  currentSessionId = id;
  const session = getCurrentSession();
  if (!session) return;

  clearMessages();
  document.getElementById('chat-title').textContent = session.title;
  session.messages.forEach(m => renderMessage(m.content, m.sender, m.time, false));
  renderHistory();
  scrollToBottom();
  closeSidebar(); // close on mobile after selecting chat
}

//  INDUSTRY STANDARD FIX — reads from <template> tag in HTML
// Never hardcodes HTML in JS — edit empty state in chat.html only
function clearMessages() {
  const container = document.getElementById('messages');
  const template  = document.getElementById('empty-state-template');

  // Clone the template content from HTML — JS never owns this HTML
  const clone = template.content.cloneNode(true);
  container.innerHTML = '';
  container.appendChild(clone);
}

function removeEmptyState() {
  const es = document.getElementById('empty-state');
  if (es) es.remove();
}

// ── Render a single message ────────────────────
function renderMessage(content, sender, time, save = true) {
  removeEmptyState();
  const container = document.getElementById('messages');

  const row = document.createElement('div');
  row.className = `msg-row ${sender}`;

  const meta = document.createElement('div');
  meta.className = 'msg-meta';
  meta.innerHTML = `<span class="sender">${sender === 'user' ? username : 'ChatBot'}</span><span>${time}</span>`;

  const bubble = document.createElement('div');
  bubble.className = 'bubble';

  if (sender === 'bot') {
    bubble.innerHTML = marked.parse(content);
  } else {
    // User message: plain text, escape HTML
    bubble.textContent = content;
  }

  row.appendChild(meta);
  row.appendChild(bubble);
  container.appendChild(row);
  scrollToBottom();

  // Save to session
  if (save && currentSessionId) {
    const session = getCurrentSession();
    if (session) {
      session.messages.push({ sender, content, time });
      // Set title from first user message
      if (sender === 'user' && session.title === 'New Chat') {
        session.title = content.slice(0, 38) + (content.length > 38 ? '…' : '');
        document.getElementById('chat-title').textContent = session.title;
        renderHistory();
      }
      saveSessions();
    }
  }
}

function scrollToBottom() {
  const c = document.getElementById('messages');
  c.scrollTop = c.scrollHeight;
}

function setTyping(v) {
  const t = document.getElementById('typing');
  t.style.display = v ? 'flex' : 'none';
  if (v) scrollToBottom();
}

function setLoading(v) {
  const btn = document.getElementById('send-btn');
  btn.disabled = v;
}

function nowTime() {
  return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(t) {
  return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ── Send message ───────────────────────────────
async function sendMessage() {
  const input = document.getElementById('msg-input');
  const text = input.value.trim();
  if (!text) return;

  input.value = '';
  input.style.height = 'auto';

  renderMessage(text, 'user', nowTime());
  setTyping(true);
  setLoading(true);

  try {
    //  HEADERS includes ngrok-skip-browser-warning
    const res = await fetch(
      `${API}/chat?userId=${encodeURIComponent(userId)}&message=${encodeURIComponent(text)}`,
      {
        method: 'POST',
        headers: HEADERS
      }
    );
    const data = await res.json();
    setTyping(false);
    setLoading(false);

    if (res.ok) {
      renderMessage(data.reply, 'bot', nowTime());
    } else {
      renderMessage('⚠️ Something went wrong. Please try again.', 'bot', nowTime());
    }
  } catch (err) {
    setTyping(false);
    setLoading(false);
    renderMessage('⚠️ Cannot reach server. Is Spring Boot running on port 8080?', 'bot', nowTime());
  }
}

function usePrompt(text) {
  const input = document.getElementById('msg-input');
  input.value = text;
  input.focus();
  autoResize(input);
}

// ── Load server history on first load ─────────
async function loadServerHistory() {
  try {
    //  HEADERS includes ngrok-skip-browser-warning
    const res = await fetch(
      `${API}/chat/history?userId=${encodeURIComponent(userId)}`,
      { headers: HEADERS }
    );
    if (!res.ok) return;
    const messages = await res.json();
    if (!messages.length) return;

    // Create a session for server history
    const histSession = {
      id: 'sess_server_' + Date.now(),
      title: 'Previous Session',
      messages: []
    };
    currentSessionId = histSession.id;
    sessions.unshift(histSession);

    removeEmptyState();
    messages.forEach(msg => {
      const sender = msg.sender === 'USER' ? 'user' : 'bot';
      const time = msg.timestamp
        ? new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        : '';
      renderMessage(msg.content, sender, time);
    });

    document.getElementById('chat-title').textContent = 'Previous Session';
    saveSessions();
    renderHistory();
  } catch (err) {
    // Silently skip
  }
}

function logout() {
  sessionStorage.removeItem('userId');
  sessionStorage.removeItem('username');
  window.location.href = 'login.html';
}

// ── Auto-resize textarea ───────────────────────
function autoResize(el) {
  el.style.height = 'auto';
  el.style.height = Math.min(el.scrollHeight, 160) + 'px';
}

document.getElementById('msg-input').addEventListener('input', function() {
  autoResize(this);
});

document.getElementById('msg-input').addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
});

// ── Init ───────────────────────────────────────
loadServerHistory().then(() => {
  if (!currentSessionId) newChat();
  renderHistory();
});