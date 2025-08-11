export interface Order {
    orderId: number;
    accountId: number;
    tenantId: number;
    symbol: string;
    orderType: string; // Enum tanımı yapılabilir
    side: string; // Enum tanımı yapılabilir
    quantity: number;
    price: number;
    filledQuantity: number;
    remainingQuantity: number;
    averagePrice: number;
    status: string; // Enum tanımı yapılabilir
    timeInForce: string; // Enum tanımı yapılabilir
    stopPrice: number;
    expiryDate: string; // ISO date format
    clientOrderId: string;
    createdAt: string; // ISO date format
    updatedAt: string; // ISO date format
    isBot: boolean;
    active: boolean;
    fullyFilled: boolean;
  }
  