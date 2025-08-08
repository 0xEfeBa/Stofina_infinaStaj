/**
 * Trading calculation utilities
 * Pure functions for financial calculations in trading interface
 */

import { OrderBookEntry, Trade } from '../types/trading.types';

/**
 * Custom error classes for trading calculations
 */
export class InvalidQuantityError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'InvalidQuantityError';
  }
}

export class InvalidPriceError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'InvalidPriceError';
  }
}

/**
 * Calculates total order value (quantity * price)
 */
export const calculateOrderTotalValue = (quantity: number, price: number): number => {
  if (quantity <= 0) {
    throw new InvalidQuantityError('Quantity must be positive');
  }
  
  if (price <= 0) {
    throw new InvalidPriceError('Price must be positive');
  }
  
  return Number((quantity * price).toFixed(2));
};

/**
 * Calculates spread between best bid and ask
 */
export const calculateSpreadBetweenPrices = (bestBid: number, bestAsk: number): number => {
  if (bestBid <= 0 || bestAsk <= 0) return 0;
  if (bestAsk <= bestBid) return 0;
  
  return Number((bestAsk - bestBid).toFixed(4));
};

/**
 * Calculates spread percentage relative to mid price
 */
export const calculateSpreadPercentage = (bestBid: number, bestAsk: number): number => {
  const spread = calculateSpreadBetweenPrices(bestBid, bestAsk);
  if (spread === 0) return 0;
  
  const midPrice = (bestBid + bestAsk) / 2;
  if (midPrice === 0) return 0;
  
  return Number(((spread / midPrice) * 100).toFixed(4));
};

/**
 * Calculates mid price between best bid and ask
 */
export const calculateMidPrice = (bestBid: number, bestAsk: number): number => {
  if (bestBid <= 0 || bestAsk <= 0) return 0;
  return Number(((bestBid + bestAsk) / 2).toFixed(4));
};

/**
 * Validates order book entry data integrity
 */
export const isValidOrderBookEntry = (entry: any): entry is OrderBookEntry => {
  return (
    entry &&
    typeof entry.price === 'number' &&
    typeof entry.quantity === 'number' &&
    entry.price > 0 &&
    entry.quantity > 0 &&
    Number.isFinite(entry.price) &&
    Number.isFinite(entry.quantity)
  );
};

/**
 * Sorts buy orders by price (highest first)
 */
export const sortBuyOrdersByPrice = (orders: OrderBookEntry[]): OrderBookEntry[] => {
  return [...orders]
    .filter(isValidOrderBookEntry)
    .sort((a, b) => b.price - a.price);
};

/**
 * Sorts sell orders by price (lowest first)
 */
export const sortSellOrdersByPrice = (orders: OrderBookEntry[]): OrderBookEntry[] => {
  return [...orders]
    .filter(isValidOrderBookEntry)
    .sort((a, b) => a.price - b.price);
};

/**
 * Gets best bid price from buy orders
 */
export const getBestBidPrice = (buyOrders: OrderBookEntry[]): number | null => {
  if (buyOrders.length === 0) return null;
  const sortedOrders = sortBuyOrdersByPrice(buyOrders);
  return sortedOrders[0]?.price || null;
};

/**
 * Gets best ask price from sell orders
 */
export const getBestAskPrice = (sellOrders: OrderBookEntry[]): number | null => {
  if (sellOrders.length === 0) return null;
  const sortedOrders = sortSellOrdersByPrice(sellOrders);
  return sortedOrders[0]?.price || null;
};

/**
 * Calculates total quantity at specific price level
 */
export const getQuantityAtPriceLevel = (
  orders: OrderBookEntry[], 
  targetPrice: number, 
  tolerance: number = 0.01
): number => {
  return orders
    .filter(order => Math.abs(order.price - targetPrice) <= tolerance)
    .reduce((total, order) => total + order.quantity, 0);
};

/**
 * Calculates order book depth (total levels)
 */
export const calculateOrderBookDepth = (buyOrders: OrderBookEntry[], sellOrders: OrderBookEntry[]): number => {
  return buyOrders.length + sellOrders.length;
};

/**
 * Validates trade data integrity
 */
export const isValidTrade = (trade: any): trade is Trade => {
  return (
    trade &&
    typeof trade.id === 'string' &&
    typeof trade.symbol === 'string' &&
    typeof trade.price === 'number' &&
    typeof trade.quantity === 'number' &&
    typeof trade.side === 'string' &&
    typeof trade.timestamp === 'string' &&
    trade.price > 0 &&
    trade.quantity > 0 &&
    ['BUY', 'SELL'].includes(trade.side) &&
    trade.id.length > 0 &&
    trade.symbol.length > 0
  );
};

/**
 * Calculates volume-weighted average price (VWAP) from trades
 */
export const calculateVolumeWeightedAveragePrice = (trades: Trade[]): number => {
  const validTrades = trades.filter(isValidTrade);
  if (validTrades.length === 0) return 0;
  
  let totalValue = 0;
  let totalVolume = 0;
  
  validTrades.forEach(trade => {
    const tradeValue = trade.price * trade.quantity;
    totalValue += tradeValue;
    totalVolume += trade.quantity;
  });
  
  return totalVolume > 0 ? Number((totalValue / totalVolume).toFixed(4)) : 0;
};

/**
 * Calculates price change percentage
 */
export const calculatePriceChangePercentage = (currentPrice: number, previousPrice: number): number => {
  if (previousPrice === 0) return 0;
  return Number((((currentPrice - previousPrice) / previousPrice) * 100).toFixed(2));
};

/**
 * Determines if price is trending up based on recent trades
 */
export const isPriceTrendingUpward = (trades: Trade[], lookbackCount: number = 5): boolean | null => {
  const validTrades = trades.filter(isValidTrade);
  if (validTrades.length < lookbackCount) return null;
  
  const recentTrades = validTrades
    .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
    .slice(0, lookbackCount);
  
  const firstPrice = recentTrades[lookbackCount - 1].price;
  const lastPrice = recentTrades[0].price;
  
  return lastPrice > firstPrice;
};

/**
 * Calculates order execution probability based on current order book
 */
export const calculateOrderExecutionProbability = (
  orderPrice: number,
  orderSide: 'BUY' | 'SELL',
  buyOrders: OrderBookEntry[],
  sellOrders: OrderBookEntry[]
): number => {
  const bestBid = getBestBidPrice(buyOrders);
  const bestAsk = getBestAskPrice(sellOrders);
  
  if (bestBid === null || bestAsk === null) return 0;
  
  if (orderSide === 'BUY') {
    // Buy order execution probability
    if (orderPrice >= bestAsk) return 1.0; // Immediate execution
    if (orderPrice < bestBid) return 0.1;   // Low probability
    return 0.5; // Moderate probability between bid and ask
  } else {
    // Sell order execution probability  
    if (orderPrice <= bestBid) return 1.0; // Immediate execution
    if (orderPrice > bestAsk) return 0.1;   // Low probability
    return 0.5; // Moderate probability between bid and ask
  }
};