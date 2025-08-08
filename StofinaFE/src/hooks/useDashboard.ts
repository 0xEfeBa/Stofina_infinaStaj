"use client";

import { useState, useEffect } from 'react';

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
  [key: string]: any; // Dashboard'da başka veriler de olabilir
}

interface UseDashboardReturn {
  dashboardData: DashboardData | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

const useDashboard = (): UseDashboardReturn => {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // TODO: Backend hazır olunca bu kısmı açın
      // const response = await axiosInstance.get('/dashboard');
      // setDashboardData(response.data);
      
      // Geçici mock data
      await new Promise(resolve => setTimeout(resolve, 500)); // Loading simulation
      const mockData: DashboardData = {
        totalBalance: 1250000,
        totalUsers: 45,
        totalCustomers: 1250,
        totalOrders: 28,
        dailyPnL: 15400,
        weeklyPnL: 87200,
        monthlyPnL: 245600,
        userInfo: {
          name: "İrem Türen",
          role: "B2B_ADMIN",
          tenantId: "tenant_001"
        },
        recentTransactions: [
          { id: 1, symbol: "THYAO", type: "BUY", amount: 10000 },
          { id: 2, symbol: "GARAN", type: "SELL", amount: 5000 }
        ],
        topStocks: [
          { symbol: "THYAO", price: 48.50, change: 2.5 },
          { symbol: "GARAN", price: 28.75, change: -1.2 }
        ]
      };
      
      setDashboardData(mockData);
    } catch (err: any) {
      console.error('Dashboard fetch error:', err);
      setError(err.response?.data?.message || err.message || 'Dashboard verileri alınamadı');
    } finally {
      setLoading(false);
    }
  };

  // Component mount olduğunda dashboard verilerini al
  useEffect(() => {
    fetchDashboard();
  }, []);

  return {
    dashboardData,
    loading,
    error,
    refetch: fetchDashboard
  };
};

export default useDashboard;