
import { useState, useCallback, useMemo } from 'react';
import { 
  ScheduledOrderForm, 
  ScheduledOrderValidation, 
  CreateScheduledOrderRequest,
  ScheduledOrderResponse,
  OrderType,
  OrderSide 
} from '../types/scheduledOrder.types';
import { 
  validateScheduledOrderForm, 
  formatScheduledTimeForAPI, 
  generateClientOrderId,
  getNextValidMarketTime 
} from '../utils/scheduledOrderUtils';

const createInitialFormState = (): ScheduledOrderForm => ({
  accountId: 1, // TODO: Get from user context
  tenantId: 1,  // TODO: Get from user context
  symbol: '',
  orderType: 'LIMIT_BUY',
  side: 'BUY',
  quantity: 0,
  price: undefined,
  stopPrice: undefined,
  timeInForce: 'DAY',
  clientOrderId: generateClientOrderId('WEB'),
  isBot: false,
  isScheduled: false,
  scheduledTime: undefined
});

interface UseScheduledOrderReturn {
  readonly formData: ScheduledOrderForm;
  readonly validation: ScheduledOrderValidation;
  readonly isSubmitting: boolean;
  readonly error: string | null;
  readonly updateFormData: <K extends keyof ScheduledOrderForm>(
    field: K, 
    value: ScheduledOrderForm[K]
  ) => void;
  readonly updateMultipleFields: (updates: Partial<ScheduledOrderForm>) => void;
  readonly setScheduledTime: (date: Date | undefined) => void;
  readonly toggleScheduled: () => void;
  readonly resetForm: () => void;
  readonly submitOrder: () => Promise<ScheduledOrderResponse | null>;
  readonly canSubmit: boolean;
}

export const useScheduledOrder = (): UseScheduledOrderReturn => {
  const [formData, setFormData] = useState<ScheduledOrderForm>(createInitialFormState);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const validation = useMemo((): ScheduledOrderValidation => {
    return validateScheduledOrderForm(
      formData.isScheduled,
      formData.scheduledTime,
      formData.symbol,
      formData.quantity
    );
  }, [formData.isScheduled, formData.scheduledTime, formData.symbol, formData.quantity]);
  
  const canSubmit = useMemo((): boolean => {
    return validation.isValid && !isSubmitting && formData.symbol !== '' && formData.quantity > 0;
  }, [validation.isValid, isSubmitting, formData.symbol, formData.quantity]);
  
  const updateFormData = useCallback(<K extends keyof ScheduledOrderForm>(
    field: K, 
    value: ScheduledOrderForm[K]
  ): void => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    setError(null); 
  }, []);
  
  const updateMultipleFields = useCallback((updates: Partial<ScheduledOrderForm>): void => {
    setFormData(prev => ({
      ...prev,
      ...updates
    }));
    setError(null);
  }, []);
  
  const setScheduledTime = useCallback((date: Date | undefined): void => {
    setFormData(prev => ({
      ...prev,
      scheduledTime: date
    }));
    setError(null);
  }, []);
  
  const toggleScheduled = useCallback((): void => {
    setFormData(prev => ({
      ...prev,
      isScheduled: !prev.isScheduled,
      scheduledTime: !prev.isScheduled && !prev.scheduledTime 
        ? getNextValidMarketTime() 
        : prev.scheduledTime
    }));
    setError(null);
  }, []);
  
  const resetForm = useCallback((): void => {
    setFormData(createInitialFormState());
    setError(null);
  }, []);
  
  const submitOrder = useCallback(async (): Promise<ScheduledOrderResponse | null> => {
    if (!canSubmit) {
      setError('Form geçerli değil, lütfen kontrol edin');
      return null;
    }
    
    setIsSubmitting(true);
    setError(null);
    
    try {
      const apiRequest: CreateScheduledOrderRequest = {
        accountId: formData.accountId,
        tenantId: formData.tenantId,
        symbol: formData.symbol,
        orderType: formData.orderType,
        side: formData.side,
        quantity: formData.quantity,
        price: formData.price,
        stopPrice: formData.stopPrice,
        timeInForce: formData.timeInForce,
        clientOrderId: formData.clientOrderId,
        isBot: formData.isBot,
        isScheduled: formData.isScheduled,
        scheduledTime: formData.scheduledTime 
          ? formatScheduledTimeForAPI(formData.scheduledTime) 
          : undefined
      };
      
      const token = localStorage.getItem("accessToken");
      const response = await fetch('http://localhost:9006/api/v1/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(apiRequest)
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Emir oluşturulamadı: ${errorText}`);
      }
      
      const result: ScheduledOrderResponse = await response.json();
      
      // Reset form on success
      resetForm();
      
      return result;
      
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Bilinmeyen hata oluştu';
      setError(errorMessage);
      return null;
    } finally {
      setIsSubmitting(false);
    }
  }, [canSubmit, formData, resetForm]);
  
  return {
    formData,
    validation,
    isSubmitting,
    error,
    updateFormData,
    updateMultipleFields,
    setScheduledTime,
    toggleScheduled,
    resetForm,
    submitOrder,
    canSubmit
  };
};