import { useState } from 'react';
import { useAuth } from '../AuthContext.jsx';

export default function AuthView() {
  const [tabName, setTabName] = useState('login');
  const [error, setError] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const auth = useAuth();

  function switchTab(name) {
    setTabName(name);
    setError('');
    setEmail('');
    setPassword('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    try {
      if (tabName === 'login') {
        await auth.login(email.trim(), password);
      } else {
        await auth.signup(email.trim(), password);
      }
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="auth-card">
      <div className="brand auth-brand">
        <span className="brand-mark">🔥</span>
        <span className="brand-name">Habit Tracker</span>
      </div>
      <div className="auth-tabs">
        <button
          type="button"
          className={`auth-tab ${tabName === 'login' ? 'active' : ''}`}
          onClick={() => switchTab('login')}
        >
          Log in
        </button>
        <button
          type="button"
          className={`auth-tab ${tabName === 'signup' ? 'active' : ''}`}
          onClick={() => switchTab('signup')}
        >
          Sign up
        </button>
      </div>

      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          Email
          <input
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label>
          Password
          <input
            type="password"
            required
            minLength={tabName === 'signup' ? 8 : undefined}
            autoComplete={tabName === 'login' ? 'current-password' : 'new-password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {tabName === 'signup' && <span className="hint">At least 8 characters</span>}
        </label>
        <p className="form-error">{error}</p>
        <button type="submit" className="btn btn-primary">
          {tabName === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>
    </div>
  );
}
