export interface AccountBalance {
    totalBalance: number;
    availableBalance: number;
    reservedBalance: number;
    withdrawableBalance: number;
    positionsMarketValue: number | null;
  }