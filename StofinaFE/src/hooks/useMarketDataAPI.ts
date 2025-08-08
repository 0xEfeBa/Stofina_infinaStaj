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

/**
 * Hook for fetching market data from REST API
 * Used for initial data loading and fallback when WebSocket is not available
 */
export const useMarketDataAPI = (): UseMarketDataAPIReturn => {
  const [symbols, setSymbols] = useState<StockInfo[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSymbols = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);

      const response = await fetch('http://localhost:8081/api/v1/market/symbols');
      
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

  // Fetch symbols on mount
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