'use client';

import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { useSelectorCustom } from '@/store';
import { thunkOrder } from '@/thunks/orderThunk';
import { Order } from '@/types/order';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
// export interface Order {
//     orderId: number;
//     accountId: number;
//     tenantId: number;
//     symbol: string;
//     orderType: string; // Enum tanımı yapılabilir
//     side: string; // Enum tanımı yapılabilir
//     quantity: number;
//     price: number;
//     filledQuantity: number;
//     remainingQuantity: number;
//     averagePrice: number;
//     status: string; // Enum tanımı yapılabilir
//     timeInForce: string; // Enum tanımı yapılabilir
//     stopPrice: number;
//     expiryDate: string; // ISO date format
//     clientOrderId: string;
//     createdAt: string; // ISO date format
//     updatedAt: string; // ISO date format
//     isBot: boolean;
//     active: boolean;
//     fullyFilled: boolean;
//   }
interface OrderTrackingTableProps {
    orders: Order[];
    onRefresh?: () => void;
}

export default function OrderTrackingTable({ orders, onRefresh }: OrderTrackingTableProps) {
    const dispatch = useDispatchCustom();
    const { t } = useTranslation();
    const [search, setSearch] = useState('');
    const [orderTypeFilter, setOrderTypeFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [cancellingOrderId, setCancellingOrderId] = useState<number | null>(null);
    const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom(state => state.customer);

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [isRefreshing, setIsRefreshing] = useState(false);

    const filteredOrders = useMemo(() => {
        return orders.filter((order) =>  // arama yaparken emir filtreleme
            `${order.symbol} ${order.symbol}`
                .toLowerCase()
                .includes(search.toLowerCase())
        )
            .filter((order) => !orderTypeFilter || order.orderType === orderTypeFilter)
            .filter((order) => !statusFilter || order.status === statusFilter)
    }, [search, orderTypeFilter, statusFilter, orders]);

    // Pagination calculations
    const totalPages = Math.ceil(filteredOrders.length / rowsPerPage);
    const startIndex = (currentPage - 1) * rowsPerPage;
    const endIndex = startIndex + rowsPerPage;
    const paginatedOrders = filteredOrders.slice(startIndex, endIndex);

    // Reset to first page when filters change
    useEffect(() => {
        setCurrentPage(1);
    }, [search, orderTypeFilter, statusFilter, rowsPerPage]);

    const handleCancelOrder = async (order: Order) => {
        console.log('Cancel order:', order);

        // Direkt iptal işlemini yap, modal kullanma
        if (window.confirm(`${order.orderId} numaralı emri iptal etmek istediğinizden emin misiniz?`)) {
            setCancellingOrderId(order.orderId);
            try {
                const result = await dispatch(thunkOrder.cancelOrder(order.orderId));

                if (result && onRefresh) {
                    // Emir iptal edildiyse tabloyu yenile
                    setTimeout(() => {
                        onRefresh();
                    }, 1000);
                }
            } catch (error) {
                console.error('Cancel order error:', error);
            } finally {
                setCancellingOrderId(null);
            }
        }
    };

    // Cancel butonunun disabled durumunu kontrol eden fonksiyon
    const isCancelDisabled = (order: Order): boolean => {
        // Emir tamamen dolmuşsa (fullyFilled: true) veya
        // status İPTAL veya GERÇEKLEŞTİ ise buton disabled olacak
        return order.fullyFilled ||
            order.status === 'İPTAL' ||
            order.status === 'GERÇEKLEŞTİ' ||
            order.status === 'FILLED' ||
            order.status === 'CANCELLED';
    };

    if (!selectedIndividualCustomer && !selectedCorporateCustomer) {
        return <></>
    }

    return (
        <div className="p-6 bg-white ">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold">{t('orderTracking.title')}</h1>
                <button
                    onClick={async () => {
                        setIsRefreshing(true);
                        if (onRefresh) {
                            await onRefresh();
                        }
                        setTimeout(() => setIsRefreshing(false), 500);
                    }}
                    disabled={isRefreshing}
                    className={`flex items-center gap-2 px-4 py-2 rounded-md transition-all ${
                        isRefreshing
                            ? 'bg-gray-400 cursor-not-allowed'
                            : 'bg-[#813FB4] hover:bg-[#6b32a3] text-white'
                    }`}
                    title={t('orderTracking.refresh', 'Yenile')}
                >
                    <svg
                        className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`}
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                        />
                    </svg>
                    {isRefreshing ? t('orderTracking.refreshing', 'Yenileniyor...') : t('orderTracking.refresh', 'Yenile')}
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 text-sm">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">{t('orderTracking.search.label')}</label>
                    <input
                        type="text"
                        placeholder={t('orderTracking.search.placeholder')}
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="border border-gray-300 px-4 py-2 rounded-md w-full"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">{t('orderTracking.filters.orderType.label')}</label>
                    <select
                        value={orderTypeFilter}
                        onChange={(e) => setOrderTypeFilter(e.target.value)}
                        className="border border-gray-300 px-4 py-2 rounded-md w-full"
                    >
                        <option value="">{t('orderTracking.filters.orderType.all')}</option>
                        <option value="LİMİT">{t('orderTracking.filters.orderType.limit')}</option>
                        <option value="PİYASA">{t('orderTracking.filters.orderType.market')}</option>
                        <option value="STOP">{t('orderTracking.filters.orderType.stop')}</option>
                    </select>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">{t('orderTracking.filters.status.label')}</label>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="border border-gray-300 px-4 py-2 rounded-md w-full"
                    >
                        <option value="">{t('orderTracking.filters.status.all')}</option>
                        <option value="GERÇEKLEŞTİ">{t('orderTracking.filters.status.completed')}</option>
                        <option value="KISMİ">{t('orderTracking.filters.status.partial')}</option>
                        <option value="BEKLİYOR">{t('orderTracking.filters.status.pending')}</option>
                        <option value="İPTAL">{t('orderTracking.filters.status.cancelled')}</option>
                    </select>
                </div>
            </div>

            <table className="min-w-full border border-gray-300 rounded-md overflow-hidden text-sm">
                <thead className="bg-gray-100">
                <tr className="text-left">
                    <th className="p-2">{t('orderTracking.table.headers.orderNo')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.symbol')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.orderType')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.side')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.orderStatus')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.price')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.quantity')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.filledQuantity')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.remainingQuantity')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.filledPrice')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.stopPrice')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.timeInForce')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.expiryDate')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.clientOrderId')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.isBot')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.active')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.fullyFilled')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.status')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.date')}</th>
                    <th className="p-2">{t('orderTracking.table.headers.action')}</th>
                </tr>
                </thead>
                <tbody>
                {paginatedOrders.map((order, index) => (
                    <tr key={index} className={`hover:bg-gray-50 ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}>
                        <td className="p-2">{order.orderId}</td>
                        <td className="p-2">{order.symbol}</td>
                        <td className="p-2">{order.orderType}</td>
                        <td className="p-2">{order.side}</td>
                        <td className="p-2">{order.orderType}</td>
                        <td className="p-2">{order.price.toFixed(2) + " TL"}</td>
                        <td className="p-2">{order.quantity}</td>
                        <td className="p-2">{order.filledQuantity}</td>
                        <td className="p-2">{order.remainingQuantity}</td>
                        <td className="p-2">{order.averagePrice.toFixed(2) + " TL"}</td>
                        <td className="p-2">{order.stopPrice ? order.stopPrice.toFixed(2) + " TL" : "-"}</td>
                        <td className="p-2">{order.timeInForce}</td>
                        <td className="p-2">{order.expiryDate ? new Date(order.expiryDate).toLocaleDateString() : "-"}</td>
                        <td className="p-2">{order.clientOrderId || "-"}</td>
                        <td className="p-2">
                                <span className={order.isBot ? "text-blue-600 font-bold" : "text-gray-500"}>
                                    {order.isBot ? t('orderTracking.status.bot') : t('orderTracking.status.manual')}
                                </span>
                        </td>
                        <td className="p-2">
                                <span className={order.active ? "text-green-600 font-bold" : "text-red-600 font-bold"}>
                                    {order.active ? t('orderTracking.status.active') : t('orderTracking.status.passive')}
                                </span>
                        </td>
                        <td className="p-2">
                                <span className={order.fullyFilled ? "text-green-600 font-bold" : "text-gray-500"}>
                                    {order.fullyFilled ? t('orderTracking.status.yes') : t('orderTracking.status.no')}
                                </span>
                        </td>
                        <td className="p-2">
                                <span className={
                                    order.status === 'GERÇEKLEŞTİ' ? 'font-bold text-green-600' :
                                        order.status === 'KISMİ' ? 'font-bold text-blue-600' :
                                            order.status === 'BEKLİYOR' ? 'font-bold text-yellow-600' :
                                                order.status === 'İPTAL' ? 'font-bold text-red-600' : ''
                                }>
                                    {order.status}
                                </span>
                        </td>
                        <td className="p-2">{new Date(order.createdAt).toLocaleString()}</td>
                        <td className="p-2">
                            {!isCancelDisabled(order) ? (
                                <button
                                    onClick={() => handleCancelOrder(order)}
                                    disabled={cancellingOrderId === order.orderId}
                                    className={`${
                                        cancellingOrderId === order.orderId
                                            ? 'bg-gray-400 cursor-wait'
                                            : 'bg-red-500 hover:bg-red-600 cursor-pointer'
                                    } text-white px-2 py-1 rounded transition-colors`}
                                >
                                    {cancellingOrderId === order.orderId
                                        ? t('orderTracking.table.actions.cancelling', 'İptal Ediliyor...')
                                        : t('orderTracking.table.actions.cancel')}
                                </button>
                            ) : (
                                <button
                                    className="bg-gray-300 text-gray-500 px-2 py-1 rounded cursor-not-allowed"
                                    disabled
                                    title={order.fullyFilled
                                        ? t('orderTracking.table.actions.fullyFilledTooltip', 'Emir tamamen dolmuş')
                                        : t('orderTracking.table.actions.cancelledTooltip', 'Emir iptal edilmiş veya gerçekleşmiş')}
                                >
                                    {t('orderTracking.table.actions.cancelDisabled')}
                                </button>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {/* Pagination Controls */}
            {filteredOrders.length > 0 && (
                <div className="mt-4 flex flex-col sm:flex-row items-center justify-between gap-4">
                    {/* Rows per page selector */}
                    <div className="flex items-center gap-2">
                        <label className="text-sm text-gray-700">
                            {t('orderTracking.pagination.rowsPerPage', 'Sayfa başına satır:')}
                        </label>
                        <select
                            value={rowsPerPage}
                            onChange={(e) => setRowsPerPage(Number(e.target.value))}
                            className="border border-gray-300 rounded px-2 py-1 text-sm"
                        >
                            <option value={5}>5</option>
                            <option value={10}>10</option>
                            <option value={20}>20</option>
                            <option value={50}>50</option>
                            <option value={100}>100</option>
                        </select>
                    </div>

                    {/* Page info */}
                    <div className="text-sm text-gray-700">
                        {t('orderTracking.pagination.showing', 'Gösterilen:')} {startIndex + 1}-{Math.min(endIndex, filteredOrders.length)} / {t('orderTracking.pagination.of', 'toplam')} {filteredOrders.length}
                    </div>

                    {/* Page navigation */}
                    <div className="flex items-center gap-2">
                        {/* First page */}
                        <button
                            onClick={() => setCurrentPage(1)}
                            disabled={currentPage === 1}
                            className={`px-2 py-1 text-sm rounded ${
                                currentPage === 1
                                    ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                    : 'bg-white border border-gray-300 hover:bg-gray-50 text-gray-700'
                            }`}
                        >
                            {'<<'}
                        </button>

                        {/* Previous page */}
                        <button
                            onClick={() => setCurrentPage(currentPage - 1)}
                            disabled={currentPage === 1}
                            className={`px-3 py-1 text-sm rounded ${
                                currentPage === 1
                                    ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                    : 'bg-white border border-gray-300 hover:bg-gray-50 text-gray-700'
                            }`}
                        >
                            {t('orderTracking.pagination.previous', 'Önceki')}
                        </button>

                        {/* Page numbers */}
                        <div className="flex items-center gap-1">
                            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                let pageNum;
                                if (totalPages <= 5) {
                                    pageNum = i + 1;
                                } else if (currentPage <= 3) {
                                    pageNum = i + 1;
                                } else if (currentPage >= totalPages - 2) {
                                    pageNum = totalPages - 4 + i;
                                } else {
                                    pageNum = currentPage - 2 + i;
                                }

                                return (
                                    <button
                                        key={i}
                                        onClick={() => setCurrentPage(pageNum)}
                                        className={`px-3 py-1 text-sm rounded ${
                                            currentPage === pageNum
                                                ? 'bg-[#813FB4] text-white'
                                                : 'bg-white border border-gray-300 hover:bg-gray-50 text-gray-700'
                                        }`}
                                    >
                                        {pageNum}
                                    </button>
                                );
                            })}
                        </div>

                        {/* Next page */}
                        <button
                            onClick={() => setCurrentPage(currentPage + 1)}
                            disabled={currentPage === totalPages}
                            className={`px-3 py-1 text-sm rounded ${
                                currentPage === totalPages
                                    ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                    : 'bg-white border border-gray-300 hover:bg-gray-50 text-gray-700'
                            }`}
                        >
                            {t('orderTracking.pagination.next', 'Sonraki')}
                        </button>

                        {/* Last page */}
                        <button
                            onClick={() => setCurrentPage(totalPages)}
                            disabled={currentPage === totalPages}
                            className={`px-2 py-1 text-sm rounded ${
                                currentPage === totalPages
                                    ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                    : 'bg-white border border-gray-300 hover:bg-gray-50 text-gray-700'
                            }`}
                        >
                            {'>>'}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}