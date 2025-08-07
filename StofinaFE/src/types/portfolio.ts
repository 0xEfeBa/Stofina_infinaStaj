export interface Portfolio {
    symbol: string;
    quantity: number;
    availableQuantity: number;
    t2Quantity: number;
    price: number;
    currentValue: number;
    averageCost: number;
    profitLoss: number;
    profitLossPercentage: number;

}

export const mockPortfolios: Portfolio[] = [
    {
        symbol: "AKBNK",
        quantity: 120,
        availableQuantity: 100,
        t2Quantity: 20,
        price: 15.5,
        currentValue: 1860,
        averageCost: 14.0,
        profitLoss: 180,
        profitLossPercentage: 10.71,
    },
    {
        symbol: "THYAO",
        quantity: 50,
        availableQuantity: 50,
        t2Quantity: 0,
        price: 95.2,
        currentValue: 4760,
        averageCost: 90.0,
        profitLoss: 260,
        profitLossPercentage: 5.78,
    },
    {
        symbol: "ASELS",
        quantity: 200,
        availableQuantity: 150,
        t2Quantity: 50,
        price: 38.75,
        currentValue: 7750,
        averageCost: 40.0,
        profitLoss: -250,
        profitLossPercentage: -3.12,
    },
    {
        symbol: "BIMAS",
        quantity: 80,
        availableQuantity: 80,
        t2Quantity: 0,
        price: 182.3,
        currentValue: 14584,
        averageCost: 170.0,
        profitLoss: 984,
        profitLossPercentage: 7.23,
    },
    {
        symbol: "EREGL",
        quantity: 150,
        availableQuantity: 100,
        t2Quantity: 50,
        price: 31.5,
        currentValue: 4725,
        averageCost: 29.0,
        profitLoss: 375,
        profitLossPercentage: 8.62,
    },
    {
        symbol: "SISE",
        quantity: 300,
        availableQuantity: 300,
        t2Quantity: 0,
        price: 26.4,
        currentValue: 7920,
        averageCost: 27.0,
        profitLoss: -180,
        profitLossPercentage: -2.22,
    },
    {
        symbol: "GARAN",
        quantity: 250,
        availableQuantity: 200,
        t2Quantity: 50,
        price: 29.8,
        currentValue: 7450,
        averageCost: 28.0,
        profitLoss: 450,
        profitLossPercentage: 6.43,
    },
    {
        symbol: "KOZAL",
        quantity: 40,
        availableQuantity: 40,
        t2Quantity: 0,
        price: 173.5,
        currentValue: 6940,
        averageCost: 180.0,
        profitLoss: -260,
        profitLossPercentage: -3.61,
    },
    {
        symbol: "TOASO",
        quantity: 60,
        availableQuantity: 60,
        t2Quantity: 0,
        price: 100.0,
        currentValue: 6000,
        averageCost: 95.0,
        profitLoss: 300,
        profitLossPercentage: 5.26,
    },
    {
        symbol: "FROTO",
        quantity: 30,
        availableQuantity: 25,
        t2Quantity: 5,
        price: 500.0,
        currentValue: 15000,
        averageCost: 520.0,
        profitLoss: -600,
        profitLossPercentage: -3.85,
    },
];
