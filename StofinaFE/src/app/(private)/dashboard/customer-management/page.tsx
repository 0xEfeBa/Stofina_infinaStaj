"use client";
import AutoCompleteCustomerSearch from '@/components/common/AutoCompleteCustomerSearch';
import { useRouter } from 'next/navigation';
import React, { useEffect, useState } from 'react'
import { Account } from '@/types/account';
import { useSelectorCustom } from '@/store';
import styles from '@/theme/common.module.css'
import { PlusCircle } from 'lucide-react';
import NewAccModal from '@/components/customer-management/newAccModal';
import AccCloseModal from '@/components/customer-management/accCloseModal';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { Tooltip } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { thunkAccount } from '@/thunks/accountThunk';
import { AccountStatus } from '@/constants/EnumAccountStatus';
import { toast } from 'sonner';
import { CorporateCustomer, IndividualCustomer } from '@/types/customer';

const CustomerManagement = () => {
    const router = useRouter();
    const { t } = useTranslation();
    const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom((state) => state.customer);
    const [newAccountModalOpen, setNewAccountModalOpen] = useState<boolean>(false);
    const [accCloseModalOpen, setAccCloseModalOpen] = useState<boolean>(false);
    const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
    const dispatch = useDispatchCustom();
    const [selectedCustomer, setSelectedCustomer] = useState<IndividualCustomer | CorporateCustomer | null>(null);
    const [accounts, setAccounts] = useState<Account[]>([]);


    console.log(selectedIndividualCustomer)
    console.log(selectedCorporateCustomer)
    console.log(selectedCustomer)
    useEffect(() => {
        setAccounts([]); // accounts arrayini temizle
        if (selectedIndividualCustomer) {
            setSelectedCustomer(selectedIndividualCustomer);
            fetchAccounts();
        }
        else if (selectedCorporateCustomer) {
            setSelectedCustomer(selectedCorporateCustomer);
            fetchAccounts();
        }
        else {
            setSelectedCustomer(null);
        }

    }, [selectedIndividualCustomer, selectedCorporateCustomer]);

    useEffect(() => {
        fetchAccounts();
    }, [selectedCustomer]);

    const fetchAccounts = async () => {
        if (selectedCustomer) {
            const response = await dispatch(thunkAccount.getAccountsByCustomerId(selectedCustomer?.customer.id));
            if (response) {
                setAccounts(response);
            }
        }
    }

    const deleteAccount = async () => {
        if (!selectedAccount) {
            return;
        }
        const response = await dispatch(thunkAccount.changeAccountStatus(selectedAccount?.id, AccountStatus.PASSIVE));
        if (response) {
            fetchAccounts();
            toast.success(t('customer.management.modal.closeAccountSuccess'));
            setAccCloseModalOpen(false);
            setSelectedAccount(null);
        }
        else {
            toast.error(t('customer.management.modal.closeAccountError'));

        }
    }
    const openAccount = async (acc: Account) => {
        if (!acc) {
            return;
        }
        const response = await dispatch(thunkAccount.changeAccountStatus(acc?.id, AccountStatus.ACTIVE));
        if (response) {
            fetchAccounts();
            toast.success(t('customer.management.modal.openAccountSuccess'));
            setAccCloseModalOpen(false);
            setSelectedAccount(null);
        }
        else {
            toast.error(t('customer.management.modal.closeAccountError'));

        }
    }


    const handleCloseAccount = () => {
        dispatch(SliceGlobalModal.actions.openModal({
            modalType: "warning",
            message: `${selectedIndividualCustomer?.firstName || selectedCorporateCustomer?.tradeName} ${t('customer.management.modal.closeAccountConfirmation')} Hesap No: ${selectedAccount?.accountNumber}`,
            multipleButtons: true,
            modalAction: () => {
                deleteAccount();
            }
        }))
    }
    const handleOpenAccount = (acc: Account) => {
        dispatch(SliceGlobalModal.actions.openModal({
            modalType: "warning",
            message: `${selectedIndividualCustomer?.firstName || selectedCorporateCustomer?.tradeName} ${t('customer.management.modal.openAccountConfirmation')} Hesap No: ${acc?.accountNumber}`,
            multipleButtons: true,
            modalAction: () => {
                openAccount(acc);
            }
        }))
    }



    return (
        <div>
            <div className=' flex items-start mb-14'> {/* Geri butonu */}
                <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
                    <img src="/menu-icon/back.png" alt={t('customer.management.buttons.back')} className={styles.icon} />
                    {t('customer.management.buttons.back')}
                </button>

                <AutoCompleteCustomerSearch /> {/* Müşteri arama */}
            </div>
            <div> {/* Hesap filtreleme ve bilgileri */}
                <div className="flex items-center mb-1 py-2 justify-between">
                    <div className='flex items-center gap-2'>

                        <svg className="w-4 h-4 ml-2 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                        </svg>
                        <label className="text-sm font-medium text-gray-700">{t('customer.management.description')}</label>
                    </div>

                    <button
                        onClick={() => {
                            if (
                                selectedCustomer)
                                setNewAccountModalOpen(true)
                        }
                        }
                        className=" w-60 cursor-pointer justify-center flex items-center gap-2 px-6 py-2 rounded-xl text-green-600 bg-green-500/10 hover:bg-green-500/20 font-semibold transition duration-300 shadow-sm hover:shadow-md focus:outline-none focus:ring-2 focus:ring-green-400 focus:ring-offset-2"
                    >
                        <PlusCircle className="w-5 h-5" />
                        <span>{t('customer.management.buttons.addNewAccount')}</span>
                    </button>
                    {selectedCustomer &&
                        <NewAccModal open={newAccountModalOpen} onClose={() => setNewAccountModalOpen(false)} onSubmit={fetchAccounts} customer={selectedCustomer} />
                    }
                    {selectedAccount && selectedCustomer &&
                        <AccCloseModal open={accCloseModalOpen} onClose={() => {
                            setSelectedAccount(null);
                            setAccCloseModalOpen(false)
                        }} onSubmit={handleCloseAccount} customer={selectedCustomer} account={selectedAccount} />
                    }
                </div>
                <table className="min-w-full border border-gray-300 rounded-md overflow-hidden text-sm">
                    <thead className="bg-gray-100">
                        <tr className="text-left">
                            <th className="px-4 py-3">{t('customer.management.table.headers.accountNumber')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.portfolioValue')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.balance')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.reservedBalance')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.status')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.portfolioDetail')}</th>
                            <th className="px-4 py-3 text-center">{t('customer.management.table.headers.accountOperations')}</th>

                        </tr>
                    </thead>
                    <tbody>
                        {accounts.map((account, index) => (
                            <tr key={index} className={`hover:bg-gray-50 ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}>
                                <td className="px-4 py-1">{account.accountNumber}</td>
                                <td className="px-4 py-1">{account.totalBalance + " TL"}</td>
                                <td className="px-4 py-1">{account.availableBalance + " TL"}</td>
                                <td className="px-4 py-1">{account.reservedBalance + " TL"}</td>
                                <td className="px-4 py-1">{account.status}</td>
                                <td className="px-4 py-1">
                                    <button
                                        onClick={() => router.push('/dashboard/customer-portfolio/' + account.id)}
                                        disabled={account.status === AccountStatus.PASSIVE}
                                        className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors duration-200 flex items-center gap-1.5 min-w-fit ${account.status === AccountStatus.PASSIVE
                                            ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                            : 'bg-[#813FB4] hover:bg-[#6B2C91] text-white cursor-pointer'
                                            }`}
                                    >
                                        <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                                        </svg>
                                        {t('customer.management.table.actions.portfolio')}
                                    </button>
                                </td>
                                <td className="p-2 px-4">
                                    <div className="flex justify-center w-full">

                                        <Tooltip
                                            title={
                                                (account.availableBalance > 0 || account.reservedBalance > 0)
                                                    ? t('customer.management.table.tooltips.accountsWithBalanceCannotBeClosed')
                                                    : ""
                                            }
                                            arrow
                                        >
                                            <span>
                                                {
                                                    account.status === AccountStatus.ACTIVE ?
                                                        <button
                                                            onClick={() => {
                                                                if (account.availableBalance === 0 && account.reservedBalance === 0) {
                                                                    setSelectedAccount(account);
                                                                    setAccCloseModalOpen(true);
                                                                }
                                                            }}
                                                            disabled={account.availableBalance > 0 || account.reservedBalance > 0}
                                                            className={`group relative inline-flex items-center justify-center p-2 text-sm font-medium rounded-md transition-all duration-200 shadow-sm ${account.availableBalance > 0 || account.reservedBalance > 0
                                                                ? 'cursor-not-allowed text-gray-400 bg-gray-100 border border-gray-200'
                                                                : 'cursor-pointer text-red-600 bg-white border border-red-200 hover:bg-red-50 hover:text-red-700 hover:border-red-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 hover:shadow-md'
                                                                }`}
                                                            title={t('customer.management.table.actions.closeAccount')}
                                                            aria-label={t('customer.management.table.actions.closeAccount')}
                                                        >
                                                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                            </svg>
                                                            {!(account.availableBalance > 0 || account.reservedBalance > 0) && (
                                                                <span className="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white text-xs rounded py-1 px-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap">
                                                                    {t('customer.management.table.actions.close')}
                                                                </span>
                                                            )}
                                                        </button>
                                                        :
                                                        <button
                                                            onClick={() => {
                                                                setSelectedAccount(account);
                                                                handleOpenAccount(account);
                                                            }}
                                                            className="group relative inline-flex items-center justify-center p-2 text-sm font-medium rounded-md transition-all shadow-sm-pointer text-green-600 bg-white border border-green-200 hover:bg-green-50 hover:text-green-700 hover:border-green-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 hover:shadow-md duration-200 shadow-sm"
                                                        >
                                                            <img src="/assets/icons/active-user.png" alt={t('customer.management.table.actions.openAccount')} className={styles.icon} />
                                                        </button>
                                                }
                                            </span>
                                        </Tooltip>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                {/* Summary Section */}
                <div className="mt-6 bg-gradient-to-r from-gray-50 to-gray-100 rounded-lg p-3 border border-gray-200 shadow-sm">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* Total Accounts */}
                        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-100">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-600">{t('customer.management.summary.totalAccounts')}</p>
                                    <p className="text-2xl font-bold text-gray-900 mt-1">{accounts?.length || 0}</p>
                                </div>
                                <div className="p-3 bg-blue-100 rounded-full">
                                    <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 8h1m-1-4h1m4 4h1m-1-4h1" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        {/* Total Portfolio Value */}
                        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-100">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-600">{t('customer.management.summary.totalPortfolioValue')}</p>
                                    <p className="text-2xl font-bold text-green-600 mt-1">
                                        {accounts.reduce((sum, account) => sum + account.totalBalance, 0).toLocaleString('tr-TR', {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2
                                        })} TL
                                    </p>
                                </div>
                                <div className="p-3 bg-green-100 rounded-full">
                                    <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        {/* Available Balance */}
                        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-100">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-600">{t('customer.management.summary.totalBalance')}</p>
                                    <p className="text-2xl font-bold text-purple-600 mt-1">
                                        {accounts.reduce((sum, account) => sum + account.availableBalance, 0).toLocaleString('tr-TR', {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2
                                        })} TL
                                    </p>
                                </div>
                                <div className="p-3 bg-purple-100 rounded-full">
                                    <img src="/assets/icons/turkish-lira.png" alt={t('customer.management.buttons.back')} className={styles.icon} />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    )
}


export default CustomerManagement;