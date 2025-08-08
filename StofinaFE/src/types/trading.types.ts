/**
 * Order book entry
 */
export interface OrderBookEntry {
  price: number;
  quantity: number;
  total: number;
  orderId?: string;
  timestamp?: string;
}

/**
 * Complete order book data
 */
export interface OrderBook {
  symbol: string;
  buyOrders: OrderBookEntry[];
  sellOrders: OrderBookEntry[];
  spread: number;
  lastUpdate: string;
}

/**
 * Trade execution data
 */
export interface Trade {
  id: string;
  symbol: string;
  price: number;
  quantity: number;
  side: 'BUY' | 'SELL';
  timestamp: string;
  buyOrderId?: string;
  sellOrderId?: string;
}

/**
 * Order book update message
 */
export interface OrderBookUpdate {
  type: 'FULL_UPDATE' | 'INCREMENTAL_UPDATE' | 'TRADE_EXECUTED';
  symbol: string;
  data: OrderBook | OrderBookEntry[] | Trade;
  timestamp: string;
}

/**
 * Trade event message
 */
export interface TradeEvent {
  type: 'TRADE_EXECUTED' | 'ORDER_MATCHED' | 'ORDER_CANCELLED';
  trade?: Trade;
  symbol: string;
  timestamp: string;
}