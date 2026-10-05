import { createContext, useContext, useEffect, useState } from 'react';
import { api, clearSession, getStoredEmail, getToken, setOnUnauthorized, setSession } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(getToken());
  const [email, setEmail] = useState(getStoredEmail());

  useEffect(() => {
    setOnUnauthorized(() => {
      setToken(null);
      setEmail(null);
    });
  }, []);

  async function login(loginEmail, password) {
    const data = await api('/api/auth/login', {
      method: 'POST',
      skipSessionHandling: true,
      body: JSON.stringify({ email: loginEmail, password }),
    });
    setSession(data.token, data.email);
    setToken(data.token);
    setEmail(data.email);
  }

  async function signup(signupEmail, password) {
    const data = await api('/api/auth/signup', {
      method: 'POST',
      skipSessionHandling: true,
      body: JSON.stringify({ email: signupEmail, password }),
    });
    setSession(data.token, data.email);
    setToken(data.token);
    setEmail(data.email);
  }

  function logout() {
    clearSession();
    setToken(null);
    setEmail(null);
  }

  return (
    <AuthContext.Provider value={{ token, email, login, signup, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
