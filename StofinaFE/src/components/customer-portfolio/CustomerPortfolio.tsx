import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { useSelectorCustom } from '@/store';
import { mockPortfolios, Portfolio } from '@/types/portfolio';
import { useTranslation } from 'next-i18next';
import React, { useMemo, useState } from 'react'

export default function CustomerPortfolio() {
    const { t } = useTranslation("common");
    const dispatch = useDispatchCustom();
    const [search, setSearch] = useState('');
    const selectedCustomer = useSelectorCustom((state) => state.customer.selectedCustomer);

    const filteredOrders = useMemo(() => {
        return mockPortfolios
            .filter((order) =>  // arama yaparken emir filtreleme
                `${order.symbol} `
                    .toLowerCase()
                    .includes(search.toLowerCase())
            )

    }, [search]);
    if (!selectedCustomer) {
        return <></>
    }

    const handleBuyOrder = (order: Portfolio) => {
        // Add buy order functionality here
    }

    const handleSellOrder = (order: Portfolio) => {
        // Add sell order functionality here
    }

    return (

        <div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 text-sm">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">{t('customerPortfolio.search.label')}</label>
                    <div className="relative flex   items-center">
                        <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                            <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                            </svg>
                        </div>

                        <input
                            type="text"
                            placeholder={t('customerPortfolio.search.placeholder')}
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="border border-gray-300 px-4 py-2 pr-10 rounded-md w-full"
                        />
                    </div>
                </div>
            </div>
            <table className="min-w-full border border-gray-300 rounded-md overflow-hidden text-sm">
                <thead className="bg-gray-100">
                    <tr className="text-left">
                        <th className="p-2">{t('customerPortfolio.table.headers.symbol')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.quantity')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.availableQuantity')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.t2Quantity')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.price')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.currentValue')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.averageCost')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.profitLoss')}</th>
                        <th className="p-2">{t('customerPortfolio.table.headers.actions')}</th>
                    </tr>
                </thead>
                <tbody>
                    {filteredOrders.map((order, index) => (
                        <tr key={order.symbol} className={`hover:bg-gray-50 ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}>
                            <td className="p-2">{order.symbol}</td>
                            <td className="p-2">{order.quantity}</td>
                            <td className="p-2">{order.availableQuantity}</td>
                            <td className="p-2">{order.t2Quantity}</td>
                            <td className="p-2">{order.price.toFixed(2) + " TL"}</td>
                            <td className="p-2">{order.currentValue}</td>
                            <td className="p-2">{order.averageCost.toFixed(2) + " TL"}</td>
                            <td className="p-2"> </td>
                            <td className="p-2">
                                <div className="flex gap-2 justify-center items-center">
                                    <button
                                        onClick={() => handleBuyOrder(order)}
                                        className="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 border border-green-400"
                                        title={t('customerPortfolio.table.actions.buyTitle')}
                                    >
                                        <div className="flex items-center gap-1">
                                            {t('customerPortfolio.table.actions.buy')}
                                        </div>
                                    </button>
                                    <button
                                        onClick={() => handleSellOrder(order)}
                                        className="bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 border border-red-400"
                                        title={t('customerPortfolio.table.actions.sellTitle')}
                                    >
                                        <div className="flex items-center gap-1">
                                            {t('customerPortfolio.table.actions.sell')}
                                        </div>
                                    </button>
                                </div>
                            </td>

                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Portfolio Summary Section */}
            <div className="mt-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {/* Status Card */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.status.label')}</div>
                    <div className="flex items-center">
                        <div className="w-2 h-2 rounded-full bg-green-500 mr-2"></div>
                        <span className="font-semibold">{t('customerPortfolio.summary.status.active')}</span>
                    </div>
                </div>

                {/* Total Assets */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.totalAssets.label')}</div>
                    <div className="text-xl font-bold text-gray-800">1,250,450.75 TL</div>
                </div>

                {/* Total Cash */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.totalCash.label')}</div>
                    <div className="text-xl font-bold text-gray-800">450,250.50 TL</div>
                </div>

                {/* Total Stock Value */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.totalStockValue.label')}</div>
                    <div className="text-xl font-bold text-gray-800">800,200.25 TL</div>
                </div>

                {/* Total P/L */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.totalProfitLoss.label')}</div>
                    <div className="text-xl font-bold text-green-600">+ 45,250.75 TL</div>
                    <div className="text-xs text-gray-500 mt-1">+5.6%</div>
                </div>

                {/* Available Cash */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.availableCash.label')}</div>
                    <div className="text-lg font-semibold text-gray-800">320,150.00 TL</div>
                </div>

                {/* T+2 Pending Balance */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.t2PendingBalance.label')}</div>
                    <div className="text-lg font-semibold text-amber-600">130,100.50 TL</div>
                </div>

                {/* Blocked Amount */}
                <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                    <div className="text-sm font-medium text-gray-500 mb-1">{t('customerPortfolio.summary.blockedAmount.label')}</div>
                    <div className="text-lg font-semibold text-red-600">25,000.00 TL</div>
                </div>
            </div>

            {/* Summary Notes */}
            <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-700">
                <div className="font-medium mb-1">{t('customerPortfolio.notes.title')}</div>
                <p>{t('customerPortfolio.notes.t2Balance')}</p>
                <p>{t('customerPortfolio.notes.blockedAmount')}</p>
            </div>
        </div>
    )
}