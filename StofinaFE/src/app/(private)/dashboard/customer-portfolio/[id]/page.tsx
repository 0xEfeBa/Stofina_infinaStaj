"use client";
import AutoCompleteCustomerSearch from '@/components/common/AutoCompleteCustomerSearch'
import React, { useEffect, useState } from 'react'
import styles from '@/theme/common.module.css'
import { useRouter } from 'next/navigation'
import { useTranslation } from 'react-i18next'
import CustomerPortfolio from '@/components/customer-portfolio/CustomerPortfolio'
import { useParams } from 'next/navigation'
import { getStocksByAccountId, thunkPortfolio } from '@/thunks/portfolioThunk';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { AccountBalance } from '@/types/balance';
import { Stock } from '@/types/stock';
import { useSelectorCustom } from '@/store';

const Page = () => {
    const { id } = useParams();
    const router = useRouter();
    const { t } = useTranslation();
    const dispatch = useDispatchCustom();
    const [stocks, setStocks] = useState<Stock[]>([]);
    const [balance, setBalance] = useState<AccountBalance | null>(null);
    const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom((state) => state.customer);
    useEffect(() => {
        fetchStocks();
    }, [id]);

    const fetchStocks = async () => {
        const stocks = await dispatch(thunkPortfolio.getStocksByAccountId(Number(id)));
        const balance = await dispatch(thunkPortfolio.getBalanceByAccountId(Number(id)));
        setStocks(stocks);
        setBalance(balance);
    }

    return (
        <div>
            <div className='flex flex-col gap-6 mb-8'>
                {/* Header Section */}
                <div className='flex items-center gap-4'>
                    <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
                        <img src="/menu-icon/back.png" alt={t('report.back')} className={styles.icon} />
                        {t('common.back')}
                    </button>
                </div>

                {/* Customer Information Card */}
                <div className='bg-white rounded-xl shadow-sm border border-gray-100 p-6'>
                    <div className='flex items-center gap-4'>
                        <div className='w-12 h-12 bg-gradient-to-br from-fuchsia-500 to-fuchsia-800 rounded-full flex items-center justify-center'>
                            <span className='text-white font-semibold text-lg'>
                                {selectedIndividualCustomer
                                    ? `${selectedIndividualCustomer.firstName.charAt(0)}${selectedIndividualCustomer.lastName.charAt(0)}`
                                    : selectedCorporateCustomer
                                        ? selectedCorporateCustomer.tradeName.charAt(0)
                                        : 'C'
                                }
                            </span>
                        </div>

                        <div className='flex-1'>
                            {selectedIndividualCustomer ? (
                                // Müşteri bilgileri bireysel müşteri için
                                <div>
                                    <h1 className='text-2xl font-bold text-gray-900 mb-1'>
                                        {selectedIndividualCustomer.firstName} {selectedIndividualCustomer.lastName}
                                    </h1>
                                    <div className='flex items-center gap-4 text-sm text-gray-600'>
                                        <span className='flex items-center gap-1'>
                                            <span className='w-2 h-2 bg-green-500 rounded-full'></span>
                                            {t('customer.customerInfo.customerId')}: {selectedIndividualCustomer.customer.id}
                                        </span>
                                        <span className='text-gray-400'>|</span>
                                        <span>{t('customer.customerInfo.individual')}</span>
                                    </div>
                                </div>
                            ) : selectedCorporateCustomer ? (
                                // Müşteri bilgileri kurumsal müşteri için
                                <div>
                                    <h1 className='text-2xl font-bold text-gray-900 mb-1'>
                                        {selectedCorporateCustomer.tradeName}
                                    </h1>
                                    <div className='flex items-center gap-4 text-sm text-gray-600'>
                                        <span className='flex items-center gap-1'>
                                            <span className='w-2 h-2 bg-blue-500 rounded-full'></span>
                                            {t('customer.customerInfo.customerId')}: {selectedCorporateCustomer.customer.id}
                                        </span>
                                        <span className='text-gray-400'>|</span>
                                        <span>{t('customer.customerInfo.taxNumber')}: {selectedCorporateCustomer.taxNumber}</span>
                                        <span className='text-gray-400'>|</span>
                                        <span>{t('customer.customerInfo.corporate')}</span>
                                    </div>
                                </div>
                            ) : (
                                <div>
                                    <h1 className='text-2xl font-bold text-gray-900 mb-1'>
                                        {t('customer.customerInfo.title')}
                                    </h1>
                                    <div className='text-sm text-gray-500'>
                                        {t('customer.customerInfo.selectAccount')}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
            <div>
                <CustomerPortfolio stocks={stocks} balance={balance} />
            </div>
        </div>
    )
}

export default Page