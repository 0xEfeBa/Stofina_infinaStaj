import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { useSelectorCustom } from '@/store';
import { AccountBalance } from '@/types/balance';
import { mockPortfolios, Portfolio } from '@/types/portfolio';
import { Stock } from '@/types/stock';
import { useTranslation } from 'next-i18next';
import { useRouter } from 'next/navigation';
import React, { useMemo, useState } from 'react'

interface CustomerPortfolioProps {
    stocks: Stock[];
    balance: AccountBalance | null;
}

export default function CustomerPortfolio({ stocks, balance }: CustomerPortfolioProps) {
    const { t } = useTranslation("common");
    const dispatch = useDispatchCustom();
    const [search, setSearch] = useState('');
    const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom((state) => state.customer);
    const router = useRouter();

    const filteredOrders = useMemo(() => {
        // Arama boş ise tüm hisseleri döndür
        if (!search || search.trim() === '') {
            return stocks;
        }

        return stocks
            .filter((order) =>  // arama yaparken emir filtreleme
                `${order.symbol} `
                    .toLowerCase()
                    .includes(search.toLowerCase())
            )

    }, [search, stocks]);
    if (!selectedIndividualCustomer && !selectedCorporateCustomer) {
        return <></>
    }

    const handleBuyOrder = () => {
        router.push(`/dashboard/trading`);
    }

    const handleSellOrder = () => {
        router.push(`/dashboard/trading`);
    }

    return (

        <div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 text-sm">
                {/* Search Bar */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">{t('customerPortfolio.search.label')}</label>
                    <div className="relative flex   items-center">
                        <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                            <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                            </svg>
                        </div>
                        {/* Search Input */}
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
                    <th className="px-4 py-3">{t('customerPortfolio.table.headers.id')}</th>
                    <th className="px-4 py-3">{t('customerPortfolio.table.headers.symbol')}</th>
                    <th className="px-4 py-3">{t('customerPortfolio.table.headers.quantity')}</th>
                    <th className="px-4 py-3">{t('customerPortfolio.table.headers.averageCost')}</th>
                    <th className="px-4 py-3">{t('customerPortfolio.table.headers.actions')}</th>
                </tr>
                </thead>
                <tbody>
                {filteredOrders && filteredOrders.map((stock, index) => (
                    <tr key={stock.symbol} className={`hover:bg-gray-50 ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}>
                        <td className="px-4 py-1">{stock.id}</td>
                        <td className="px-4 py-1">{stock.symbol}</td>
                        <td className="px-4 py-1">{stock.quantity}</td>
                        <td className="px-4 py-1">{stock.averageCost}</td>
                        <td className="px-4 py-1">
                            <div className="flex gap-2 justify items-center">
                                <button
                                    onClick={() => handleBuyOrder()}
                                    className="bg-gradient-to-r cursor-pointer from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 border border-green-400"
                                    title={t('customerPortfolio.table.actions.buyTitle')}
                                >
                                    <div className="flex items-center gap-1">
                                        {t('customerPortfolio.table.actions.buy')}
                                    </div>
                                </button>
                                <button
                                    onClick={() => handleSellOrder()}
                                    className="bg-gradient-to-r cursor-pointer from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 border border-red-400"
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
            <div className="mt-8">
                <h3 className="text-lg font-semibold text-gray-800 mb-6 flex items-center">
                    <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                    {t('customerPortfolio.summary.title')}
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
                    {/* Status Card */}
                    <div className="relative overflow-hidden bg-gradient-to-br from-green-50 to-emerald-100 p-4 rounded-xl shadow-sm border border-green-200 hover:shadow-md transition-all duration-300 group">
                        <div className="absolute top-0 right-0 w-20 h-20 bg-green-200 rounded-full -mr-10 -mt-10 opacity-20 group-hover:opacity-30 transition-opacity"></div>
                        <div className="relative z-10">
                            <div className="flex items-center justify-between mb-1">
                                <div className="text-sm font-medium text-green-700">{t('customerPortfolio.summary.status.label')}</div>
                                <div className="w-3 h-3 rounded-full bg-green-500 animate-pulse"></div>
                            </div>
                            <div className="flex items-center">
                                <svg className="w-6 h-6 text-green-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <span className="font-bold text-green-800 text-lg">{t('customerPortfolio.summary.status.active')}</span>
                            </div>
                        </div>
                    </div>

                    {/* Total Balance */}
                    <div className="relative overflow-hidden bg-gradient-to-br from-blue-50 to-indigo-100 p-4 rounded-xl shadow-sm border border-blue-200 hover:shadow-md transition-all duration-300 group">
                        <div className="absolute top-0 right-0 w-20 h-20 bg-blue-200 rounded-full -mr-10 -mt-10 opacity-20 group-hover:opacity-30 transition-opacity"></div>
                        <div className="relative z-10">
                            <div className="flex items-center justify-between mb-1">
                                <div className="text-sm font-medium text-blue-700">{t('customerPortfolio.summary.totalAssets.label')}</div>
                                <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                                </svg>
                            </div>
                            <div className="text-xl font-bold text-blue-800">
                                {new Intl.NumberFormat('tr-TR', {
                                    style: 'currency',
                                    currency: 'TRY',
                                    minimumFractionDigits: 2
                                }).format(balance?.totalBalance || 0)}
                            </div>
                        </div>
                    </div>

                    {/* Available Balance */}
                    <div className="relative overflow-hidden bg-gradient-to-br from-emerald-50 to-teal-100 p-4 rounded-xl shadow-sm border border-emerald-200 hover:shadow-md transition-all duration-300 group">
                        <div className="absolute top-0 right-0 w-20 h-20 bg-emerald-200 rounded-full -mr-10 -mt-10 opacity-20 group-hover:opacity-30 transition-opacity"></div>
                        <div className="relative z-10">
                            <div className="flex items-center justify-between mb-1">
                                <div className="text-sm font-medium text-emerald-700">{t('customerPortfolio.summary.availableCash.label')}</div>
                                <svg className="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                                </svg>
                            </div>
                            <div className="text-xl font-bold text-emerald-800">
                                {new Intl.NumberFormat('tr-TR', {
                                    style: 'currency',
                                    currency: 'TRY',
                                    minimumFractionDigits: 2
                                }).format(balance?.availableBalance || 0)}
                            </div>
                        </div>
                    </div>

                    {/* Reserved Balance */}
                    <div className="relative overflow-hidden bg-gradient-to-br from-amber-50 to-orange-100 p-4 rounded-xl shadow-sm border border-amber-200 hover:shadow-md transition-all duration-300 group">
                        <div className="absolute top-0 right-0 w-20 h-20 bg-amber-200 rounded-full -mr-10 -mt-10 opacity-20 group-hover:opacity-30 transition-opacity"></div>
                        <div className="relative z-10">
                            <div className="flex items-center justify-between mb-1">
                                <div className="text-sm font-medium text-amber-700">{t('customerPortfolio.summary.t2PendingBalance.label')}</div>
                                <svg className="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <div className="text-xl font-bold text-amber-800">
                                {new Intl.NumberFormat('tr-TR', {
                                    style: 'currency',
                                    currency: 'TRY',
                                    minimumFractionDigits: 2
                                }).format(balance?.restrictedBalance || 0)}
                            </div>
                        </div>
                    </div>

                    {/* Withdrawable Balance */}
                    <div className="relative overflow-hidden bg-gradient-to-br from-red-50 to-pink-100 p-4 rounded-xl shadow-sm border border-red-200 hover:shadow-md transition-all duration-300 group">
                        <div className="absolute top-0 right-0 w-20 h-20 bg-red-200 rounded-full -mr-10 -mt-10 opacity-20 group-hover:opacity-30 transition-opacity"></div>
                        <div className="relative z-10">
                            <div className="flex items-center justify-between mb-1">
                                <div className="text-sm font-medium text-red-700">{t('customerPortfolio.summary.blockedAmount.label')}</div>
                                <svg className="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                </svg>
                            </div>
                            <div className="text-xl font-bold text-red-800">
                                {new Intl.NumberFormat('tr-TR', {
                                    style: 'currency',
                                    currency: 'TRY',
                                    minimumFractionDigits: 2
                                }).format(balance?.reservedBalance || 0)}
                            </div>
                        </div>
                    </div>
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