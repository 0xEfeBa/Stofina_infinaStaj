"use client";

import React, { createContext, useContext, ReactNode } from 'react';
import useDashboard from '../hooks/useDashboard';

interface DashboardData {
  totalBalance?: number;
  totalUsers?: number;
  totalCustomers?: number;
  totalOrders?: number;
  dailyPnL?: number;
  weeklyPnL?: number;
  monthlyPnL?: number;
  recentTransactions?: any[];
  topStocks?: any[];
  userInfo?: {
    name: string;
    role: string;
    tenantId: string;
  };
  [key: string]: any;
}

interface DashboardContextType {
  dashboardData: DashboardData | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

const DashboardContext = createContext<DashboardContextType | undefined>(undefined);

export const DashboardProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const dashboardHook = useDashboard();

  return (
    <DashboardContext.Provider value={dashboardHook}>
      {children}
    </DashboardContext.Provider>
  );
};

export const useDashboardContext = (): DashboardContextType => {
  const context = useContext(DashboardContext);
  if (context === undefined) {
    throw new Error('useDashboardContext must be used within a DashboardProvider');
  }
  return context;
};

export default DashboardContext;