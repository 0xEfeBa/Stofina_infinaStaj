"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { authService } from "@/services/authService";

interface Role {
  id: number;
  roleType: string;
}

interface User {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  title: string;
  email: string;
  phoneNumber: string;
  roles: Role[];
  status: string;
}

interface AuthContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const savedUser = authService.getUser();
    if (savedUser) setUser(savedUser);
  }, []);

  

  const logout = () => {
    authService.clearAuthData();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
