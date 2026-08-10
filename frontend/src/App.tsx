import { useState } from 'react';
import { AuthProvider, useAuth } from './auth/AuthContext';
import { LoginForm } from './components/LoginForm';
import { RegisterForm } from './components/RegisterForm';
import { Dashboard } from './components/Dashboard';

function Screen() {
  const { isAuthenticated } = useAuth();
  const [showRegister, setShowRegister] = useState(false);

  if (isAuthenticated) {
    return <Dashboard />;
  }

  return showRegister ? (
    <RegisterForm onSwitchToLogin={() => setShowRegister(false)} />
  ) : (
    <LoginForm onSwitchToRegister={() => setShowRegister(true)} />
  );
}

function App() {
  return (
    <AuthProvider>
      <Screen />
    </AuthProvider>
  );
}

export default App;
