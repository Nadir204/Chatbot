


  const API = 'http://localhost:8080';
//jokon ngrok theke url nibo tokon aita chnage korbo
//const API = 'https://10f5-103-139-164-107.ngrok-free.app';

//  Central headers — works for both localhost and ngrok
// ngrok-skip-browser-warning bypasses ngrok's warning page
const HEADERS = {
  'Content-Type':               'application/json',
  'ngrok-skip-browser-warning': 'true'
};

// ── Tab switching ─────────────────────────────────────────
function showTab(tab) {
    const isLogin = tab === 'login';

    document.getElementById('login-form').classList.toggle('active', isLogin);
    document.getElementById('register-form').classList.toggle('active', !isLogin);

    document.querySelectorAll('.tab-btn').forEach((btn, i) => {
        btn.classList.toggle('active', isLogin ? i === 0 : i === 1);
    });

    // Clear all errors when switching tabs
    clearAllErrors();
}

// ── Toast notification ────────────────────────────────────
let toastTimer = null;

function showToast(msg, type = 'info') {
    const toast   = document.getElementById('toast');
    const msgEl   = document.getElementById('toast-msg');
    const iconEl  = document.getElementById('toast-icon');

    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    iconEl.textContent = icons[type] || 'ℹ️';
    msgEl.textContent  = msg;

    toast.className = `toast ${type} show`;

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        toast.classList.remove('show');
    }, 3500);
}

// ── Show/hide password ────────────────────────────────────
function togglePassword(inputId, btn) {
    const input = document.getElementById(inputId);
    const isHidden = input.type === 'password';
    input.type = isHidden ? 'text' : 'password';
    btn.textContent = isHidden ? '🙈' : '👁';
}

// ── Field error helpers ───────────────────────────────────
function showFieldError(inputId, errId, message) {
    const input = document.getElementById(inputId);
    const err   = document.getElementById(errId);
    input.classList.remove('valid');
    input.classList.add('invalid');
    err.textContent = message;
    err.classList.add('visible');
}

function showFieldSuccess(inputId) {
    const input = document.getElementById(inputId);
    input.classList.remove('invalid');
    input.classList.add('valid');
}

function clearFieldError(inputId, errId) {
    const input = document.getElementById(inputId);
    const err   = document.getElementById(errId);
    input.classList.remove('invalid', 'valid');
    err.classList.remove('visible');
    err.textContent = '';
}

function clearAllErrors() {
    const allInputs = document.querySelectorAll('input');
    allInputs.forEach(i => i.classList.remove('invalid', 'valid'));

    const allErrors = document.querySelectorAll('.field-error');
    allErrors.forEach(e => {
        e.classList.remove('visible');
        e.textContent = '';
    });

    // Hide requirements panel
    document.getElementById('pw-requirements').classList.remove('visible');
    document.getElementById('strength-wrap').classList.remove('visible');
}

// ── Password strength checker ─────────────────────────────
function getStrength(pw) {
    let score = 0;
    if (pw.length >= 6)  score++;
    if (pw.length >= 10) score++;
    if (/[a-zA-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw))    score++;
    if (/[^a-zA-Z0-9]/.test(pw)) score++;
    return score; // 0-5
}

function updateStrengthBar(pw) {
    const score  = getStrength(pw);
    const fill   = document.getElementById('strength-fill');
    const label  = document.getElementById('strength-label');
    const wrap   = document.getElementById('strength-wrap');

    if (!pw) {
        wrap.classList.remove('visible');
        return;
    }

    wrap.classList.add('visible');

    const levels = [
        { pct: '15%',  color: '#f87171', text: 'Very weak' },
        { pct: '30%',  color: '#fb923c', text: 'Weak'      },
        { pct: '55%',  color: '#facc15', text: 'Fair'      },
        { pct: '80%',  color: '#34d399', text: 'Strong'    },
        { pct: '100%', color: '#10b981', text: 'Very strong'},
    ];

    const level = levels[Math.min(score - 1, 4)] || levels[0];
    fill.style.width      = score === 0 ? '0' : level.pct;
    fill.style.background = level.color;
    label.textContent     = score === 0 ? 'Strength: —' : 'Strength: ' + level.text;
}

// ── Live validation — register username ───────────────────
function validateRegUsername() {
    const val = document.getElementById('reg-username').value.trim();
    if (!val) {
        showFieldError('reg-username', 'reg-username-err', 'Username is required.');
        return false;
    }
    if (val.length < 3) {
        showFieldError('reg-username', 'reg-username-err',
            'Username must be at least 3 characters.');
        return false;
    }
    if (/\s/.test(val)) {
        showFieldError('reg-username', 'reg-username-err',
            'Username cannot contain spaces.');
        return false;
    }
    clearFieldError('reg-username', 'reg-username-err');
    showFieldSuccess('reg-username');
    return true;
}

// ── Live validation — register password ───────────────────
function validateRegPassword() {
    const pw = document.getElementById('reg-password').value;

    // Show requirements panel
    document.getElementById('pw-requirements').classList.add('visible');

    // Update each requirement item
    setReq('req-length',  pw.length >= 6);
    setReq('req-letter',  /[a-zA-Z]/.test(pw));
    setReq('req-number',  /[0-9]/.test(pw));
    setReq('req-special', /[^a-zA-Z0-9]/.test(pw));

    // Update strength bar
    updateStrengthBar(pw);

    if (!pw) {
        showFieldError('reg-password', 'reg-password-err', 'Password is required.');
        return false;
    }
    if (pw.length < 6) {
        showFieldError('reg-password', 'reg-password-err',
            'Password must be at least 6 characters.');
        return false;
    }
    clearFieldError('reg-password', 'reg-password-err');
    showFieldSuccess('reg-password');

    // Re-validate confirm if it has a value
    const confirm = document.getElementById('reg-confirm').value;
    if (confirm) validateConfirm();

    return true;
}

function setReq(id, met) {
    const el = document.getElementById(id);
    el.classList.toggle('met', met);
}

// ── Live validation — confirm password ────────────────────
function validateConfirm() {
    const pw      = document.getElementById('reg-password').value;
    const confirm = document.getElementById('reg-confirm').value;

    if (!confirm) {
        showFieldError('reg-confirm', 'reg-confirm-err',
            'Please confirm your password.');
        return false;
    }
    if (pw !== confirm) {
        showFieldError('reg-confirm', 'reg-confirm-err',
            'Passwords do not match.');
        return false;
    }
    clearFieldError('reg-confirm', 'reg-confirm-err');
    showFieldSuccess('reg-confirm');
    return true;
}

// ── Button loading state ──────────────────────────────────
function setLoading(btnId, loading) {
    const btn = document.getElementById(btnId);
    btn.classList.toggle('loading', loading);
    btn.disabled = loading;
}

// ── LOGIN ─────────────────────────────────────────────────
async function doLogin() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    let valid = true;

    // Validate username
    if (!username) {
        showFieldError('login-username', 'login-username-err',
            'Please enter your username.');
        valid = false;
    }

    // Validate password
    if (!password) {
        showFieldError('login-password', 'login-password-err',
            'Please enter your password.');
        valid = false;
    }

    if (!valid) return;

    setLoading('login-btn', true);

    try {
        //  HEADERS includes ngrok-skip-browser-warning
        const res = await fetch(`${API}/auth/login`, {
            method:  'POST',
            headers: HEADERS,
            body:    JSON.stringify({ username, password })
        });

        const data = await res.json();

        if (res.ok) {
            sessionStorage.setItem('userId',   data.userId);
            sessionStorage.setItem('username', data.username);
            showToast('Welcome back, ' + data.username + '! 🎉', 'success');
            setTimeout(() => { window.location.href = 'chat.html'; }, 900);
        } else {
            // Show error on both fields so user knows which is wrong
            showFieldError('login-username', 'login-username-err', ' ');
            showFieldError('login-password', 'login-password-err',
                'Incorrect username or password. Please try again.');
            showToast(data.message || 'Login failed. Check your credentials.', 'error');
        }

    } catch (err) {
        showToast('Cannot reach server. Is Spring Boot running on port 8080?', 'error');
    } finally {
        setLoading('login-btn', false);
    }
}

// ── REGISTER ──────────────────────────────────────────────
async function doRegister() {
    // Run all validations first
    const usernameOk = validateRegUsername();
    const passwordOk = validateRegPassword();
    const confirmOk  = validateConfirm();

    if (!usernameOk || !passwordOk || !confirmOk) {
        showToast('Please fix the errors above before continuing.', 'error');
        return;
    }

    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value;

    setLoading('register-btn', true);

    try {
        //  HEADERS includes ngrok-skip-browser-warning
        const res = await fetch(`${API}/auth/register`, {
            method:  'POST',
            headers: HEADERS,
            body:    JSON.stringify({ username, password })
        });

        const data = await res.json();

        if (res.ok || res.status === 201) {
            showToast('Account created! Please sign in. 🎉', 'success');
            // Clear fields and switch to login tab
            document.getElementById('reg-username').value = '';
            document.getElementById('reg-password').value = '';
            document.getElementById('reg-confirm').value  = '';
            clearAllErrors();
            setTimeout(() => { showTab('login'); }, 1200);
        } else {
            // Username already taken
            showFieldError('reg-username', 'reg-username-err',
                data.message || 'Username already taken. Try another.');
            showToast(data.message || 'Registration failed.', 'error');
        }

    } catch (err) {
        showToast('Cannot reach server. Is Spring Boot running on port 8080?', 'error');
    } finally {
        setLoading('register-btn', false);
    }
}