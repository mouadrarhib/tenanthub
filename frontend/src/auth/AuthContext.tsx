import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { getToken, setToken as persistToken } from '../api/client';
import { decodeJwt } from './jwt';
import type { JwtClaims } from '../types';

interface AuthContextValue {
  token: string | null;
  claims: JwtClaims | null;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => getToken());

  const login = (newToken: string) => {
    persistToken(newToken);
    setTokenState(newToken);
  };

  const logout = () => {
    persistToken(null);
    setTokenState(null);
  };

  const claims = useMemo(() => (token ? decodeJwt(token) : null), [token]);

  const value: AuthContextValue = {
    token,
    claims,
    isAuthenticated: token !== null,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
