/**
 * Stock price data from Market Data Service
 */
export interface StockPrice {
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  open?: number;
  high?: number;
  low?: number;
  timestamp?: string;
  lastUpdated?: Date | string;
  marketCap?: number;
  companyName?: string; // Added for company name display
}

/**
 * Market data update message
 */
export interface MarketDataUpdate {
  type: 'PRICE_UPDATE' | 'MARKET_STATUS' | 'SYMBOL_LIST';
  symbol?: string;
  data: StockPrice | StockPrice[] | MarketStatus;
  timestamp: string;
}

/**
 * Market status information
 */
export interface MarketStatus {
  isOpen: boolean;
  openTime: string;
  closeTime: string;
  timezone: string;
  lastUpdate: string;
}

/**
 * Market data subscription
 */
export interface MarketDataSubscription {
  symbols: string[];
  updateFrequency?: number;
  includeVolume?: boolean;
  includeExtendedHours?: boolean;
}