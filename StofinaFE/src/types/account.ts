export interface Account {
    id: number;
    customerId: number;
    accountNumber: string;
    status: "ACTIVE" | "PASSIVE" | string; // Enum ile sınırlandırılabilir
    totalBalance: number;
    availableBalance: number;
    reservedBalance: number;
    withdrawableBalance: number;
    stocks: any[]; // Eğer stocks yapısı belli ise ayrı bir interface tanımlanmalı
  }

