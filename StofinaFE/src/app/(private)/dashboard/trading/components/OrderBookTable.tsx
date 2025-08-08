"use client";

import React, { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { OrderBookEntry } from '../../../../../types/trading.types';
import { 
  formatPriceToTurkishLira, 
  formatQuantity 
} from '../../../../../utils/priceFormatters';
import { 
  sortBuyOrdersByPrice, 
  sortSellOrdersByPrice,
  calculateSpreadBetweenPrices,
  getBestBidPrice,
  getBestAskPrice
} from '../../../../../utils/tradingCalculations';
import styles from '../Trading.module.css';


interface OrderBookTableProps {
  buyOrders: OrderBookEntry[];
  sellOrders: OrderBookEntry[];
  symbol: string;
  maxLevels?: number;
  showSpread?: boolean;
}


const OrderRow: React.FC<{ 
  order: OrderBookEntry; 
  side: 'buy' | 'sell';
  onClick?: (price: number) => void;
}> = memo(({ order, side, onClick }) => {
  const handleRowClick = () => {
    if (onClick) {
      onClick(order.price);
    }
  };

  return (
    <div 
      className={styles.orderRow} 
      onClick={handleRowClick}
      style={{ cursor: onClick ? 'pointer' : 'default' }}
    >
      <span className={styles.orderPrice}>
        {formatPriceToTurkishLira(order.price)}
      </span>
      <span className={styles.orderQuantity}>
        {formatQuantity(order.quantity)}
      </span>
      <span className={styles.orderTotal}>
        {formatQuantity(order.total)}
      </span>
    </div>
  );
});

OrderRow.displayName = 'OrderRow';


const SpreadIndicator: React.FC<{
  buyOrders: OrderBookEntry[];
  sellOrders: OrderBookEntry[];
}> = memo(({ buyOrders, sellOrders }) => {
  const { t } = useTranslation();
  const bestBid = getBestBidPrice(buyOrders);
  const bestAsk = getBestAskPrice(sellOrders);
  const spread = bestBid && bestAsk ? calculateSpreadBetweenPrices(bestBid, bestAsk) : 0;

  return (
    <div className={styles.spreadIndicator}>
      <span className={styles.spreadArrows}>⬆⬇</span>
      <span className={styles.spreadLabel}>
        {t('trading.orderBook.spread', { value: formatPriceToTurkishLira(spread) })}
      </span>
    </div>
  );
});

SpreadIndicator.displayName = 'SpreadIndicator';


export const OrderBookTable: React.FC<OrderBookTableProps> = memo(({
  buyOrders,
  sellOrders,
  symbol,
  maxLevels = 20,
  showSpread = true
}) => {
  const { t } = useTranslation();
  
  const getDisplayBuyOrders = (): OrderBookEntry[] => {
    return sortBuyOrdersByPrice(buyOrders).slice(0, maxLevels);
  };

  
  const getDisplaySellOrders = (): OrderBookEntry[] => {
    return sortSellOrdersByPrice(sellOrders).slice(0, maxLevels);
  };

  
  const handleOrderPriceClick = (price: number) => {
   
    console.log(`Selected price: ${price} for ${symbol}`);
  };

  
  if (!symbol) {
    return (
      <div className={styles.orderBook}>
        <div className={styles.orderBookHeader}>
          <div>{t('trading.messages.invalidSymbol')}</div>
        </div>
      </div>
    );
  }

  const displayBuyOrders = getDisplayBuyOrders();
  const displaySellOrders = getDisplaySellOrders();

  
  if (displayBuyOrders.length === 0 && displaySellOrders.length === 0) {
    return (
      <div className={styles.orderBook}>
        <div className={styles.orderBookHeader}>
          <div className={styles.buyHeader}>{t('trading.orderBook.buyOrders')}</div>
          <div className={styles.sellHeader}>{t('trading.orderBook.sellOrders')}</div>
        </div>
        <div className={styles.orderBookContent}>
          <div className={styles.emptyOrderBook}>
            <span>{t('trading.orderBook.loading')}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.orderBook}>
      
      <div className={styles.orderBookHeader}>
        <div className={styles.buyHeader}>
          <span>{t('trading.orderBook.buyOrders')}</span>
        </div>
        <div className={styles.sellHeader}>
          <span>{t('trading.orderBook.sellOrders')}</span>
        </div>
      </div>


      {showSpread && (
        <SpreadIndicator buyOrders={buyOrders} sellOrders={sellOrders} />
      )}

     
      <div className={styles.orderBookContent}>
       
        <div className={styles.buyOrders}>
          {displayBuyOrders.map((order, index) => (
            <OrderRow
              key={`buy-${order.price}-${index}`}
              order={order}
              side="buy"
              onClick={handleOrderPriceClick}
            />
          ))}
        </div>

       
        <div className={styles.sellOrders}>
          {displaySellOrders.map((order, index) => (
            <OrderRow
              key={`sell-${order.price}-${index}`}
              order={order}
              side="sell"
              onClick={handleOrderPriceClick}
            />
          ))}
        </div>
      </div>

 
      <div className={styles.orderBookFooter}>
        <div className={styles.footerLabels}>
          <span>{t('trading.orderBook.price')}</span>
          <span>{t('trading.orderBook.quantity')}</span>
          <span>{t('trading.orderBook.total')}</span>
        </div>
        <div className={styles.footerLabels}>
          <span>{t('trading.orderBook.price')}</span>
          <span>{t('trading.orderBook.quantity')}</span>
          <span>{t('trading.orderBook.total')}</span>
        </div>
      </div>
    </div>
  );
});

OrderBookTable.displayName = 'OrderBookTable';