import { createContext, useContext, useEffect, useState } from 'react';
import { api, clearSession, getStoredEmail, getStoredName, getToken, setOnUnauthorized, setSession } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(getToken());
  const [email, setEmail] = useState(getStoredEmail());
  const [name, setName] = useState(getStoredName());

  useEffect(() => {
    setOnUnauthorized(() => {
      setToken(null);
      setEmail(null);
      setName(null);
    });
  }, []);

  async function login(loginEmail, password) {
    const data = await api('/api/auth/login', {
      method: 'POST',
      skipSessionHandling: true,
      body: JSON.stringify({ email: loginEmail, password }),
    });
    setSession(data.token, data.email, data.name);
    setToken(data.token);
    setEmail(data.email);
    setName(data.name);
  }

  async function signup(signupEmail, password, signupName) {
    const data = await api('/api/auth/signup', {
      method: 'POST',
      skipSessionHandling: true,
      body: JSON.stringify({ email: signupEmail, password, name: signupName }),
    });
    setSession(data.token, data.email, data.name);
    setToken(data.token);
    setEmail(data.email);
    setName(data.name);
  }

  function logout() {
    clearSession();
    setToken(null);
    setEmail(null);
    setName(null);
  }

  return (
    <AuthContext.Provider value={{ token, email, name, login, signup, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
