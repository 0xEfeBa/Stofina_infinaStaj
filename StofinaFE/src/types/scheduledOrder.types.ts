// CLEAN CODE: Type-first approach for scheduled orders
// Single source of truth for all scheduled order related types

export type OrderType = 'MARKET_BUY' | 'MARKET_SELL' | 'LIMIT_BUY' | 'LIMIT_SELL' | 'STOP_LOSS_SELL';
export type OrderSide = 'BUY' | 'SELL';
export type TimeInForce = 'DAY' | 'GTC' | 'IOC' | 'FOK';
export type ScheduledStatus = 'IMMEDIATE' | 'SCHEDULED' | 'ACTIVATED' | 'EXPIRED';

// Core scheduled order form interface
export interface ScheduledOrderForm {
  readonly accountId: number;
  readonly tenantId: number;
  readonly symbol: string;
  readonly orderType: OrderType;
  readonly side: OrderSide;
  readonly quantity: number;
  readonly price?: number;
  readonly stopPrice?: number;
  readonly timeInForce: TimeInForce;
  readonly clientOrderId: string;
  readonly isBot: boolean;
  readonly isScheduled: boolean;
  readonly scheduledTime?: Date;
}

// Validation result interface
export interface ValidationError {
  readonly field: string;
  readonly message: string;
}

export interface ScheduledOrderValidation {
  readonly isValid: boolean;
  readonly errors: ReadonlyArray<ValidationError>;
}

// API request interface
export interface CreateScheduledOrderRequest {
  readonly accountId: number;
  readonly tenantId: number;
  readonly symbol: string;
  readonly orderType: string;
  readonly side: string;
  readonly quantity: number;
  readonly price?: number;
  readonly stopPrice?: number;
  readonly timeInForce: string;
  readonly clientOrderId: string;
  readonly isBot: boolean;
  readonly isScheduled: boolean;
  readonly scheduledTime?: string; // ISO format string for API
}

// API response interface
export interface ScheduledOrderResponse {
  readonly orderId: number;
  readonly accountId: number;
  readonly tenantId: number;
  readonly symbol: string;
  readonly orderType: string;
  readonly side: string;
  readonly quantity: number;
  readonly price?: number;
  readonly stopPrice?: number;
  readonly filledQuantity: number;
  readonly remainingQuantity: number;
  readonly averagePrice: number;
  readonly status: string;
  readonly timeInForce: string;
  readonly expiryDate: string;
  readonly clientOrderId: string;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly isBot: boolean;
  readonly isScheduled: boolean;
  readonly scheduledTime?: string;
  readonly expireTime: string;
  readonly scheduledStatus: ScheduledStatus;
  readonly scheduledOrder: boolean;
  readonly expired: boolean;
  readonly fullyFilled: boolean;
  readonly awaitingActivation: boolean;
  readonly active: boolean;
}

// Market hours configuration
export interface MarketHours {
  readonly open: string; // "09:30"
  readonly close: string; // "18:00"
  readonly timezone: string; // "Europe/Istanbul"
}

// Quick time selection options
export interface QuickTimeOption {
  readonly label: string;
  readonly minutes: number;
}

// Component props interfaces
export interface ScheduledOrderSectionProps {
  readonly isScheduled: boolean;
  readonly scheduledTime?: Date;
  readonly onScheduledChange: (isScheduled: boolean) => void;
  readonly onTimeChange: (time: Date) => void;
  readonly validation?: ScheduledOrderValidation;
  readonly disabled?: boolean;
}

export interface DateTimeSelectorProps {
  readonly value?: Date;
  readonly onChange: (date: Date) => void;
  readonly minDate?: Date;
  readonly maxDate?: Date;
  readonly marketHours: MarketHours;
  readonly disabled?: boolean;
  readonly error?: string;
}

export interface ScheduledToggleProps {
  readonly checked: boolean;
  readonly onChange: (checked: boolean) => void;
  readonly disabled?: boolean;
  readonly label?: string;
}