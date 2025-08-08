"use client";

import React from 'react';
import { useTranslation } from 'react-i18next';
import { WebSocketStatus } from '../../../../../types/websocket.types';


interface ConnectionStatusProps {
  marketDataStatus: WebSocketStatus;
  orderBookStatus: WebSocketStatus;
  tradeStreamStatus: WebSocketStatus;
  showDetails?: boolean;
}


const ConnectionIndicator: React.FC<{
  label: string;
  status: WebSocketStatus;
  isCompact?: boolean;
}> = ({ label, status, isCompact = false }) => {
  const { t } = useTranslation();
 
  const getStatusColor = (): string => {
    switch (status) {
      case WebSocketStatus.CONNECTED:
        return '#4CAF50';
      case WebSocketStatus.CONNECTING:
      case WebSocketStatus.RECONNECTING:
        return '#FF9800'; 
      case WebSocketStatus.ERROR:
        return '#F44336'; 
      case WebSocketStatus.DISCONNECTED:
      default:
        return '#9E9E9E';
    }
  };

 
  const getStatusText = (): string => {
    switch (status) {
      case WebSocketStatus.CONNECTED:
        return t('trading.connection.connected');
      case WebSocketStatus.CONNECTING:
        return t('trading.connection.connecting');
      case WebSocketStatus.RECONNECTING:
        return t('trading.connection.reconnecting');
      case WebSocketStatus.ERROR:
        return t('trading.connection.error');
      case WebSocketStatus.DISCONNECTED:
      default:
        return t('trading.connection.disconnected');
    }
  };

  const statusColor = getStatusColor();
  const statusText = getStatusText();

  if (isCompact) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
        <div
          style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: statusColor
          }}
        />
        <span style={{ fontSize: '12px', color: '#666' }}>
          {label}
        </span>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '4px 0' }}>
      <div
        style={{
          width: '12px',
          height: '12px',
          borderRadius: '50%',
          backgroundColor: statusColor,
          boxShadow: status === WebSocketStatus.CONNECTED ? '0 0 8px rgba(76, 175, 80, 0.3)' : 'none'
        }}
      />
      <span style={{ fontSize: '14px', fontWeight: '500', color: '#333' }}>
        {label}
      </span>
      <span style={{ fontSize: '12px', color: '#666' }}>
        {statusText}
      </span>
    </div>
  );
};

/**
 * ConnectionStatus Component
 * Single Responsibility: Display WebSocket connection status for all services
 */
export const ConnectionStatus: React.FC<ConnectionStatusProps> = ({
  marketDataStatus,
  orderBookStatus,
  tradeStreamStatus,
  showDetails = false
}) => {
  const { t } = useTranslation();

  const getOverallHealth = (): 'healthy' | 'partial' | 'unhealthy' => {
    const connectedCount = [marketDataStatus, orderBookStatus, tradeStreamStatus]
      .filter(status => status === WebSocketStatus.CONNECTED).length;

    if (connectedCount === 3) return 'healthy';
    if (connectedCount > 0) return 'partial';
    return 'unhealthy';
  };

  
  const getOverallStatusColor = (): string => {
    const health = getOverallHealth();
    switch (health) {
      case 'healthy':
        return '#4CAF50';
      case 'partial':
        return '#FF9800';
      case 'unhealthy':
      default:
        return '#F44336';
    }
  };

 
  const getOverallStatusText = (): string => {
    const health = getOverallHealth();
    switch (health) {
      case 'healthy':
        return t('trading.connection.allActive');
      case 'partial':
        return t('trading.connection.partial');
      case 'unhealthy':
      default:
        return t('trading.connection.problem');
    }
  };

  const overallColor = getOverallStatusColor();
  const overallText = getOverallStatusText();

  
  if (!showDetails) {
    return (
      <div style={{ 
        display: 'flex', 
        alignItems: 'center', 
        gap: '8px',
        padding: '4px 8px',
        borderRadius: '4px',
        backgroundColor: 'rgba(255, 255, 255, 0.1)'
      }}>
        <div
          style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: overallColor
          }}
        />
        <span style={{ fontSize: '12px', color: '#333', fontWeight: '500' }}>
          {overallText}
        </span>
      </div>
    );
  }

  
  return (
    <div style={{
      padding: '12px',
      border: '1px solid #e0e0e0',
      borderRadius: '8px',
      backgroundColor: '#fff'
    }}>
      <h4 style={{ 
        margin: '0 0 12px 0', 
        fontSize: '14px', 
        fontWeight: '600',
        color: '#333'
      }}>
        {t('trading.connection.status')}
      </h4>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        <ConnectionIndicator
          label={t('trading.connection.marketData')}
          status={marketDataStatus}
        />
        <ConnectionIndicator
          label={t('trading.connection.orderBook')}
          status={orderBookStatus}
        />
        <ConnectionIndicator
          label={t('trading.connection.tradeStream')}
          status={tradeStreamStatus}
        />
      </div>

      <div style={{
        marginTop: '12px',
        paddingTop: '8px',
        borderTop: '1px solid #f0f0f0',
        display: 'flex',
        alignItems: 'center',
        gap: '8px'
      }}>
        <div
          style={{
            width: '10px',
            height: '10px',
            borderRadius: '50%',
            backgroundColor: overallColor
          }}
        />
        <span style={{ fontSize: '13px', fontWeight: '600', color: '#333' }}>
          {overallText}
        </span>
      </div>
    </div>
  );
};