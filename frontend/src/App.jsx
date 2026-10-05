import { useEffect, useState } from 'react';
import { AuthProvider, useAuth } from './AuthContext.jsx';
import { initTheme } from './theme.js';
import AuthView from './components/AuthView.jsx';
import TopBar from './components/TopBar.jsx';
import Dashboard from './components/Dashboard.jsx';
import StatsView from './components/StatsView.jsx';

function AppShell() {
  const { isAuthenticated } = useAuth();
  const [tab, setTab] = useState('dashboard');

  if (!isAuthenticated) {
    return (
      <main className="auth-page">
        <AuthView />
      </main>
    );
  }

  return (
    <>
      <TopBar tab={tab} onTabChange={setTab} />
      <main>
        {tab === 'dashboard' ? <Dashboard /> : <StatsView />}
      </main>
    </>
  );
}

export default function App() {
  useEffect(() => {
    initTheme();
  }, []);

  return (
    <AuthProvider>
      <AppShell />
    </AuthProvider>
  );
}
