// CLEAN CODE: Pure functions for scheduled order validation and formatting
// No side effects, predictable outputs, easy to test

import { ValidationError, ScheduledOrderValidation, MarketHours } from '../types/scheduledOrder.types';

// Market hours constants
export const DEFAULT_MARKET_HOURS: MarketHours = {
  open: '09:30',
  close: '18:00',
  timezone: 'Europe/Istanbul'
} as const;

// SMART QUICK SELECTION OPTIONS
export const SMART_QUICK_OPTIONS = [
  { 
    id: '15-min',
    label: '15 dakika sonra',
    calculateTime: () => addMinutesToNow(15)
  },
  { 
    id: '30-min',
    label: '30 dakika sonra',
    calculateTime: () => addMinutesToNow(30)
  },
  { 
    id: '1-hour',
    label: '1 saat sonra',
    calculateTime: () => addMinutesToNow(60)
  },
  { 
    id: 'market-close-today',
    label: 'Bugün kapanışta',
    calculateTime: () => {
      const today = new Date();
      today.setHours(18, 0, 0, 0);
      return today;
    }
  },
  { 
    id: 'market-open-tomorrow',
    label: 'Yarın açılışta',
    calculateTime: () => {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(9, 30, 0, 0);
      return tomorrow;
    }
  }
] as const;

// PURE FUNCTION: Validate scheduled time
export const validateScheduledTime = (scheduledTime: Date): { readonly isValid: boolean; readonly error?: string } => {
  const now = new Date();
  
  // Must be in future
  if (scheduledTime <= now) {
    return { isValid: false, error: 'Zamansal emir gelecek bir zamana ayarlanmalıdır' };
  }
  
  // Max 7 days in future
  const maxFutureDate = new Date();
  maxFutureDate.setDate(maxFutureDate.getDate() + 7);
  
  if (scheduledTime > maxFutureDate) {
    return { isValid: false, error: 'Zamansal emir en fazla 7 gün ilerisine ayarlanabilir' };
  }
  
  // Must be weekday (Monday-Friday)
  const dayOfWeek = scheduledTime.getDay();
  if (dayOfWeek === 0 || dayOfWeek === 6) { // Sunday = 0, Saturday = 6
    return { isValid: false, error: 'Zamansal emir sadece hafta içi günlerde ayarlanabilir' };
  }
  
  // Must be within market hours
  if (!isWithinMarketHours(scheduledTime)) {
    return { isValid: false, error: 'Zamansal emir piyasa saatleri içinde olmalıdır (09:30-18:00)' };
  }
  
  return { isValid: true };
};

// PURE FUNCTION: Check if time is within market hours
export const isWithinMarketHours = (date: Date, marketHours: MarketHours = DEFAULT_MARKET_HOURS): boolean => {
  const timeString = date.toTimeString().substring(0, 5); // "HH:MM"
  return timeString >= marketHours.open && timeString <= marketHours.close;
};

// PURE FUNCTION: Check if date is weekday
export const isWeekday = (date: Date): boolean => {
  const dayOfWeek = date.getDay();
  return dayOfWeek >= 1 && dayOfWeek <= 5; // Monday = 1, Friday = 5
};

// PURE FUNCTION: Format date for API (backend expects "YYYY-MM-DD HH:mm:ss")
export const formatScheduledTimeForAPI = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// PURE FUNCTION: Format date for display
export const formatScheduledTimeForDisplay = (date: Date): string => {
  return new Intl.DateTimeFormat('tr-TR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Europe/Istanbul'
  }).format(date);
};

// PURE FUNCTION: Add minutes to current time
export const addMinutesToNow = (minutes: number): Date => {
  const now = new Date();
  return new Date(now.getTime() + minutes * 60 * 1000);
};

// PURE FUNCTION: Get next valid market time
export const getNextValidMarketTime = (fromDate: Date = new Date()): Date => {
  let nextTime = new Date(fromDate);
  
  // Add 5 minutes minimum
  nextTime = addMinutesToNow(5);
  
  // If outside market hours, set to next market open
  if (!isWithinMarketHours(nextTime)) {
    // Set to next day 09:30
    nextTime.setDate(nextTime.getDate() + 1);
    nextTime.setHours(9, 30, 0, 0);
  }
  
  // If weekend, move to Monday
  while (!isWeekday(nextTime)) {
    nextTime.setDate(nextTime.getDate() + 1);
    nextTime.setHours(9, 30, 0, 0);
  }
  
  return nextTime;
};

// PURE FUNCTION: Validate entire scheduled order form
export const validateScheduledOrderForm = (
  isScheduled: boolean,
  scheduledTime?: Date,
  symbol?: string,
  quantity?: number
): ScheduledOrderValidation => {
  const errors: ValidationError[] = [];
  
  // Basic validations
  if (!symbol || symbol.trim() === '') {
    errors.push({ field: 'symbol', message: 'Sembol seçilmelidir' });
  }
  
  if (!quantity || quantity <= 0) {
    errors.push({ field: 'quantity', message: 'Miktar 0\'dan büyük olmalıdır' });
  }
  
  // Scheduled specific validations
  if (isScheduled) {
    if (!scheduledTime) {
      errors.push({ field: 'scheduledTime', message: 'Zamansal emir için tarih ve saat seçilmelidir' });
    } else {
      const timeValidation = validateScheduledTime(scheduledTime);
      if (!timeValidation.isValid) {
        errors.push({ field: 'scheduledTime', message: timeValidation.error! });
      }
    }
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

// PURE FUNCTION: Generate unique client order ID
export const generateClientOrderId = (prefix: string = 'ORD'): string => {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 1000);
  return `${prefix}-${timestamp}-${random}`;
};

// PURE FUNCTION: Get available custom date options (next 7 weekdays)
export const getCustomDateOptions = (): Array<{ value: string; label: string; date: Date }> => {
  const options: Array<{ value: string; label: string; date: Date }> = [];
  const today = new Date();
  
  for (let i = 0; i < 14; i++) { // Check up to 14 days to get 7 weekdays
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    
    if (isWeekday(date) && options.length < 7) {
      const value = date.toISOString().split('T')[0]; // YYYY-MM-DD
      let label: string;
      
      if (i === 0) {
        label = 'Bugün';
      } else if (i === 1) {
        label = 'Yarın';
      } else {
        const dayNames = ['Pazar', 'Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi'];
        label = dayNames[date.getDay()];
      }
      
      options.push({ value, label, date });
    }
  }
  
  return options;
};

// PURE FUNCTION: Format quick option display time
export const formatQuickOptionTime = (calculateTime: () => Date): string => {
  try {
    const time = calculateTime();
    return new Intl.DateTimeFormat('tr-TR', {
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'Europe/Istanbul'
    }).format(time);
  } catch {
    return '--:--';
  }
};

// PURE FUNCTION: Check if quick option is available
export const isQuickOptionValid = (calculateTime: () => Date): boolean => {
  try {
    const time = calculateTime();
    const validation = validateScheduledTime(time);
    return validation.isValid;
  } catch {
    return false;
  }
};