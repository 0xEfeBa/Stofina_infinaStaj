"use client";
import AutoCompleteCustomerSearch from '@/components/common/AutoCompleteCustomerSearch'
import React, { useEffect, useState } from 'react'
import styles from '@/theme/common.module.css'
import { useRouter } from 'next/navigation'
import { useTranslation } from 'react-i18next'
import CustomerPortfolio from '@/components/customer-portfolio/CustomerPortfolio'
import AccountSelector from '@/components/order-tracking/accountSelector';
import { Account } from '@/types/account';
import { useSelectorCustom } from '@/store';
import { thunkAccount } from '@/thunks/accountThunk';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { Stock } from '@/types/stock';
import { AccountBalance } from '@/types/balance';
import { thunkPortfolio } from '@/thunks/portfolioThunk';
const Page = () => {
    const router = useRouter();
    const [openAccountSelector, setOpenAccountSelector] = useState(false);
    const dispatch = useDispatchCustom();
    const { t } = useTranslation();
    const [stocks, setStocks] = useState<Stock[]>([]);
    const [balance, setBalance] = useState<AccountBalance | null>(null);
    const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom(state => state.customer);
    const [accounts, setAccounts] = useState<Account[]>([]);

    const fetchStocks = async (account: Account) => {
        const stocks = await dispatch(thunkPortfolio.getStocksByAccountId(account.id));
        const balance = await dispatch(thunkPortfolio.getBalanceByAccountId(account.id));
        setStocks(stocks);
        setBalance(balance);
    }

    useEffect(() => {
        fetchAccounts();
    }, [selectedIndividualCustomer, selectedCorporateCustomer]);

    const fetchAccounts = async () => {
        if (selectedIndividualCustomer) {
            const response = await dispatch(thunkAccount.getAccountsByCustomerId(selectedIndividualCustomer?.customer.id));
            if (response) {
                setAccounts(response);
                setOpenAccountSelector(true);
            }
        }
        else if (selectedCorporateCustomer) {
            const response = await dispatch(thunkAccount.getAccountsByCustomerId(selectedCorporateCustomer?.customer.id));
            if (response) {
                setAccounts(response);
                setOpenAccountSelector(true);
            }
        }
        else {
            setAccounts([]);
            setOpenAccountSelector(false);
        }
    }

    return (
        <div>
            <div className=' flex items-start mb-14'>
                <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
                    <img src="/menu-icon/back.png" alt={t('report.back')} className={styles.icon} />
                    {t('common.back')}
                </button>
                <AccountSelector open={openAccountSelector} onClose={() => setOpenAccountSelector(false)} accounts={accounts} onSelect={fetchStocks} />
                <AutoCompleteCustomerSearch />
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
            <div>
                <CustomerPortfolio stocks={stocks} balance={balance} />
            </div>
        </div>
    )
}

export default Page