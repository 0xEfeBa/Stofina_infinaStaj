export interface Account {
    accountNo: string;
    portfolioValue: number;
    balance: number;
    lastTransactionDate: Date;
    status: string;
}

export const mockAccounts: Account[] = [
    {
        accountNo: "TR1000000001",
        portfolioValue: 125000.50,
        balance: 85000.00,
        lastTransactionDate: new Date("2025-07-30T10:15:00"),
        status: "AKTİF",
    },
    {
        accountNo: "TR1000000002",
        portfolioValue: 43250.75,
        balance: 25000.25,
        lastTransactionDate: new Date("2025-07-28T14:30:00"),
        status: "AKTİF",
    },
    {
        accountNo: "TR1000000003",
        portfolioValue: 78000.00,
        balance: 30000.00,
        lastTransactionDate: new Date("2025-07-25T09:45:00"),
        status: "PASİF",
    },
    {
        accountNo: "TR1000000004",
        portfolioValue: 10000.00,
        balance: 8000.00,
        lastTransactionDate: new Date("2025-07-31T16:10:00"),
        status: "PASİF",
    },
    {
        accountNo: "TR1000000005",
        portfolioValue: 0,
        balance: 0,
        lastTransactionDate: new Date("2025-07-27T13:00:00"),
        status: "PASİF",
    },
    {
        accountNo: "TR1000000006",
        portfolioValue: 54320.00,
        balance: 30000.00,
        lastTransactionDate: new Date("2025-07-29T11:25:00"),
        status: "PASİF",
    },
    {
        accountNo: "TR1000000007",
        portfolioValue: 0,
        balance: 0,
        lastTransactionDate: new Date("2025-08-01T15:00:00"),
        status: "AKTİF",
    },

]
