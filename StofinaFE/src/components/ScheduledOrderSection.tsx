
import React, { useCallback, useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { ScheduledOrderSectionProps } from '../types/scheduledOrder.types';
import { 
  SMART_QUICK_OPTIONS, 
  formatQuickOptionTime, 
  isQuickOptionValid,
  getCustomDateOptions 
} from '../utils/scheduledOrderUtils';
import styles from './ScheduledOrderSection.module.css';

export const ScheduledOrderSection: React.FC<ScheduledOrderSectionProps> = ({
  isScheduled,
  scheduledTime,
  onScheduledChange,
  onTimeChange,
  validation,
  disabled = false
}) => {
  const { t } = useTranslation();
  
  // Map utility option IDs to translation keys
  const getTranslationKey = (optionId: string): string => {
    const keyMap: Record<string, string> = {
      '15-min': t('scheduledOrder.quickOptions.in15Minutes'),
      '30-min': t('scheduledOrder.quickOptions.in30Minutes'),
      '1-hour': t('scheduledOrder.quickOptions.in1Hour'),
      'market-close-today': t('scheduledOrder.quickOptions.marketClose'),
      'market-open-tomorrow': t('scheduledOrder.quickOptions.marketOpen')
    };
    return keyMap[optionId] || optionId;
  };
  
  // LOCAL STATE: Selection mode (quick vs custom)
  const [selectionMode, setSelectionMode] = useState<'quick' | 'custom'>('quick');
  const [selectedQuickOption, setSelectedQuickOption] = useState<string>('');
  const [customDate, setCustomDate] = useState<string>('');
  const [customTime, setCustomTime] = useState<string>('');
  
  // MEMOIZED: Available custom date options
  const customDateOptions = useMemo(() => getCustomDateOptions(), []);
  
  // CLEAN CODE: Handle quick option selection
  const handleQuickOptionChange = useCallback((optionId: string): void => {
    const option = SMART_QUICK_OPTIONS.find(opt => opt.id === optionId);
    if (option && isQuickOptionValid(option.calculateTime)) {
      setSelectedQuickOption(optionId);
      setSelectionMode('quick');
      const calculatedTime = option.calculateTime();
      onTimeChange(calculatedTime);
    }
  }, [onTimeChange]);
  
  // CLEAN CODE: Handle custom date change
  const handleCustomDateChange = useCallback((event: React.ChangeEvent<HTMLSelectElement>): void => {
    const dateValue = event.target.value;
    setCustomDate(dateValue);
    setSelectionMode('custom');
    setSelectedQuickOption('');
    
    if (dateValue && customTime) {
      const [year, month, day] = dateValue.split('-').map(Number);
      const [hours, minutes] = customTime.split(':').map(Number);
      
      const newDate = new Date(year, month - 1, day, hours, minutes, 0);
      onTimeChange(newDate);
    }
  }, [customTime, onTimeChange]);

  // CLEAN CODE: Handle custom time change
  const handleCustomTimeChange = useCallback((event: React.ChangeEvent<HTMLInputElement>): void => {
    const timeValue = event.target.value;
    setCustomTime(timeValue);
    setSelectionMode('custom');
    setSelectedQuickOption('');
    
    if (customDate && timeValue) {
      const [year, month, day] = customDate.split('-').map(Number);
      const [hours, minutes] = timeValue.split(':').map(Number);
      
      const newDate = new Date(year, month - 1, day, hours, minutes, 0);
      onTimeChange(newDate);
    }
  }, [customDate, onTimeChange]);
  
  
  // Get validation error for scheduled time field
  const scheduledTimeError = validation?.errors.find(error => error.field === 'scheduledTime');
  
  return (
    <>
      {/* Simple Checkbox */}
      <div className={styles.checkboxContainer}>
        <label className={styles.checkboxLabel}>
          <input
            type="checkbox"
            checked={isScheduled}
            onChange={(e) => onScheduledChange(e.target.checked)}
            disabled={disabled}
            className={styles.checkbox}
          />
          <span className={styles.checkboxText}>{t('scheduledOrder.title')}</span>
        </label>
      </div>
      
      {/* Smart Quick Selection - Only show when scheduled */}
      {isScheduled && (
        <div className={styles.smartSelectionContainer}>
          
          {/* Quick Options */}
          <div className={styles.quickOptionsContainer}>
            {SMART_QUICK_OPTIONS.map((option) => {
              const isValid = isQuickOptionValid(option.calculateTime);
              const displayTime = isValid ? formatQuickOptionTime(option.calculateTime) : t('common.messages.error');
              
              return (
                <label key={option.id} className={`${styles.quickOptionLabel} ${!isValid ? styles.disabled : ''}`}>
                  <input
                    type="radio"
                    name="scheduledTimeOption"
                    value={option.id}
                    checked={selectedQuickOption === option.id}
                    onChange={() => handleQuickOptionChange(option.id)}
                    disabled={disabled || !isValid}
                    className={styles.quickOptionRadio}
                  />
                  <span className={styles.quickOptionText}>
                    {getTranslationKey(option.id)} <span className={styles.quickOptionTime}>({displayTime})</span>
                  </span>
                </label>
              );
            })}
            
            {/* Custom Option */}
            <label className={styles.quickOptionLabel}>
              <input
                type="radio"
                name="scheduledTimeOption"
                value="custom"
                checked={selectionMode === 'custom'}
                onChange={() => {
                  setSelectionMode('custom');
                  setSelectedQuickOption('');
                }}
                disabled={disabled}
                className={styles.quickOptionRadio}
              />
              <span className={styles.quickOptionText}>{t('scheduledOrder.quickOptions.custom')}:</span>
              
              {/* Custom Date and Time Inputs */}
              {selectionMode === 'custom' && (
                <div className={styles.customInputsContainer}>
                  <select
                    value={customDate}
                    onChange={handleCustomDateChange}
                    disabled={disabled}
                    className={styles.customDateSelect}
                  >
                    <option value="">{t('scheduledOrder.customTime.selectDay')}</option>
                    {customDateOptions.map((dateOption) => (
                      <option key={dateOption.value} value={dateOption.value}>
                        {dateOption.label}
                      </option>
                    ))}
                  </select>
                  
                  <input
                    type="time"
                    value={customTime}
                    onChange={handleCustomTimeChange}
                    disabled={disabled}
                    className={styles.customTimeInput}
                    min="09:30"
                    max="18:00"
                    placeholder="--:--"
                  />
                </div>
              )}
            </label>
          </div>
          
          {/* Error Message */}
          {scheduledTimeError && (
            <div className={styles.errorMessage}>
              {scheduledTimeError.message}
            </div>
          )}
        </div>
      )}
    </>
  );
};