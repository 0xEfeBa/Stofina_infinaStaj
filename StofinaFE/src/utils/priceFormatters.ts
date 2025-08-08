/**
 * Price formatting utilities for trading interface
 * Pure functions for consistent price display across the application
 */

/**
 * Formats price to Turkish Lira currency format
 * Handles null, undefined, and invalid price values gracefully
 */
export const formatPriceToTurkishLira = (price?: number | null): string => {
  // Early return for invalid values
  if (price === null || price === undefined || isNaN(price)) {
    return "₺--";
  }
  
  if (price === 0) return "₺0.00";
  if (price < 0) return `₺-${Math.abs(price).toFixed(2)}`;
  return `₺${price.toFixed(2)}`;
};

/**
 * Formats price change with appropriate sign and color indicator
 * Handles null, undefined, and invalid change values gracefully
 */
export const formatPriceChange = (change?: number | null): { text: string; isPositive: boolean } => {
  // Handle invalid values
  if (change === null || change === undefined || isNaN(change)) {
    return {
      text: '--',
      isPositive: true // Neutral color for unknown values
    };
  }
  
  const isPositive = change >= 0;
  const sign = isPositive ? '+' : '';
  const formattedChange = `${sign}${change.toFixed(2)}`;
  
  return {
    text: formattedChange,
    isPositive
  };
};

/**
 * Formats percentage change with sign
 * Handles null, undefined, and invalid percentage values gracefully
 */
export const formatPercentageChange = (percentage?: number | null): string => {
  // Handle invalid values
  if (percentage === null || percentage === undefined || isNaN(percentage)) {
    return '--%';
  }
  
  const sign = percentage >= 0 ? '+' : '';
  return `${sign}${percentage.toFixed(2)}%`;
};

/**
 * Formats quantity with Turkish number formatting
 * Handles null, undefined, and invalid quantity values gracefully
 */
export const formatQuantity = (quantity?: number | null): string => {
  if (quantity === null || quantity === undefined || isNaN(quantity)) {
    return '--';
  }
  return quantity.toLocaleString('tr-TR');
};

/**
 * Formats volume in abbreviated format (K, M, B)
 * Handles null, undefined, and invalid volume values gracefully
 */
export const formatVolume = (volume?: number | null): string => {
  if (volume === null || volume === undefined || isNaN(volume) || volume < 0) {
    return '--';
  }
  
  if (volume >= 1_000_000_000) {
    return `${(volume / 1_000_000_000).toFixed(1)}B`;
  }
  if (volume >= 1_000_000) {
    return `${(volume / 1_000_000).toFixed(1)}M`;
  }
  if (volume >= 1_000) {
    return `${(volume / 1_000).toFixed(1)}K`;
  }
  return volume.toString();
};

/**
 * Formats timestamp to Turkish locale time
 */
export const formatTradeTime = (timestamp: string): string => {
  try {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('tr-TR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch {
    return '--:--:--';
  }
};

/**
 * Formats market cap in abbreviated format
 * Handles null, undefined, and invalid market cap values gracefully
 */
export const formatMarketCap = (marketCap?: number | null): string => {
  if (marketCap === null || marketCap === undefined || isNaN(marketCap) || marketCap < 0) {
    return '₺--';
  }
  
  if (marketCap >= 1_000_000_000) {
    return `₺${(marketCap / 1_000_000_000).toFixed(1)}B`;
  }
  if (marketCap >= 1_000_000) {
    return `₺${(marketCap / 1_000_000).toFixed(1)}M`;
  }
  return `₺${marketCap.toLocaleString('tr-TR')}`;
};

/**
 * Gets display text for price when no data is available
 */
export const getEmptyPriceDisplay = (): string => "-- TL";

/**
 * Gets display text for market closed state
 */
export const getMarketClosedDisplay = (): string => "Market Kapalı";

/**
 * Validates if price is displayable (not null, undefined, NaN, or negative)
 */
export const isValidDisplayPrice = (price?: number | null): boolean => {
  return price !== undefined && price !== null && !isNaN(price) && price >= 0;
};