/**
 * Order book level for display
 */
export interface OrderBookLevel {
  price: number;
  quantity: number;
  total: number;
  percentage?: number;
}