"use client";

import { useState, useEffect, useCallback } from 'react';

interface StockInfo {
  symbol: string;
  companyName: string;
  currentPrice: number;
  change: number;
  lastUpdated: string;
}

interface UseMarketDataAPIReturn {
  symbols: StockInfo[];
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export const useMarketDataAPI = (): UseMarketDataAPIReturn => {
  const [symbols, setSymbols] = useState<StockInfo[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSymbols = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);

      const token = localStorage.getItem("accessToken");
      const response = await fetch('http://localhost:9005/api/v1/market/symbols', {
        headers: {
          Authorization: `Bearer ${token}`,
          accept: "application/json",
        },
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: StockInfo[] = await response.json();
      setSymbols(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
      console.error('Failed to fetch market symbols:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSymbols();
  }, [fetchSymbols]);

  return {
    symbols,
    isLoading,
    error,
    refetch: fetchSymbols
  };
};