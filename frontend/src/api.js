const TOKEN_KEY = 'habit_tracker_token';
const EMAIL_KEY = 'habit_tracker_email';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredEmail() {
  return localStorage.getItem(EMAIL_KEY);
}

export function setSession(token, email) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(EMAIL_KEY, email);
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(EMAIL_KEY);
}

// Set by AuthContext so a 401 can clear app state without this module touching the DOM directly.
export let onUnauthorized = () => {};
export function setOnUnauthorized(callback) {
  onUnauthorized = callback;
}

export async function api(path, options = {}) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  const token = getToken();
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const response = await fetch(path, Object.assign({}, options, { headers }));

  if (response.status === 401 && !options.skipSessionHandling) {
    clearSession();
    onUnauthorized();
    throw new Error('Session expired, please log in again');
  }

  if (!response.ok) {
    let message = 'Something went wrong';
    try {
      const body = await response.json();
      message = body.error || message;
    } catch (_) {
      // response had no JSON body
    }
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}
