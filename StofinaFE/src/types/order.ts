export interface Order {
    orderNo: number;
    symbol: string;
    orderSide: string;
    orderType: string;
    price: number;
    quantity: number;
    filledQuantity: number;
    filledPrice: number;
    status: string;
    createdAt: Date;

}

export const mockOrders: Order[] = [
    {
        orderNo: 100001,
        symbol: 'AKBNK',
        orderSide: 'ALIM',
        orderType: 'LiMİT',
        price: 67.10,
        quantity: 100,
        filledQuantity: 100,
        filledPrice: 67.10,
        status: 'GERÇEKLEŞTİ',
        createdAt: new Date('2025-08-01T10:15:00Z'),
    },
    {
        orderNo: 230002,
        symbol: 'THYAO',
        orderSide: 'SATIM',
        orderType: 'PİYASA',
        price: 0,
        quantity: 50,
        filledQuantity: 50,
        filledPrice: 168.25,
        status: 'GERÇEKLEŞTİ',
        createdAt: new Date('2025-08-01T10:20:00Z'),
    },
    {
        orderNo: 100003,
        symbol: 'SISE',
        orderSide: 'ALIM',
        orderType: 'STOP',
        price: 48.75,
        quantity: 200,
        filledQuantity: 0,
        filledPrice: 0,
        status: 'BEKLİYOR',
        createdAt: new Date('2025-08-01T10:30:00Z'),
    },
    {
        orderNo: 100004,
        symbol: 'KRDMD',
        orderSide: 'SATIM',
        orderType: 'STOP',
        price: 20.00,
        quantity: 150,
        filledQuantity: 0,
        filledPrice: 0,
        status: 'KISMİ',
        createdAt: new Date('2025-08-01T10:45:00Z'),
    },
    {
        orderNo: 100005,
        symbol: 'BIMAS',
        orderSide: 'ALIM',
        orderType: 'PİYASA',
        price: 0,
        quantity: 80,
        filledQuantity: 80,
        filledPrice: 255.40,
        status: 'İPTAL',
        createdAt: new Date('2025-08-01T11:00:00Z'),
    },
];