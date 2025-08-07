"use client";
import AutoCompleteCustomerSearch from '@/components/common/AutoCompleteCustomerSearch';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react'
import { Account, mockAccounts } from '@/types/account';
import { useSelectorCustom } from '@/store';
import styles from '@/theme/common.module.css'
import { PlusCircle } from 'lucide-react';
import NewCustomerModal from '@/components/customer-management/newCustomerModal';
import AccCloseModal from '@/components/customer-management/accCloseModal';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { Tooltip } from '@mui/material';
import { useTranslation } from 'react-i18next';

const CustomerManagement = () => {
    const router = useRouter();
    const { t } = useTranslation();
    const selectedCustomer = useSelectorCustom((state) => state.customer.selectedCustomer);
    const [newAccountModalOpen, setNewAccountModalOpen] = useState<boolean>(false);
    const [accCloseModalOpen, setAccCloseModalOpen] = useState<boolean>(false);
    const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
    const dispatch = useDispatchCustom();
    const onclick = () => {

    }

    const deleteAccount = () => {
        //TODO: Silme servisi çağrılacak
        setAccCloseModalOpen(false);
        setSelectedAccount(null);
    }

    const handleCloseAccount = () => {
        dispatch(SliceGlobalModal.actions.openModal({
            modalType: "warning",
            message: `${selectedCustomer?.firstName} ${t('customer.management.modal.closeAccountConfirmation')} ${selectedAccount?.accountNo}`,
            multipleButtons: true,
            modalAction: () => {
                deleteAccount();
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
                        <NewCustomerModal open={newAccountModalOpen} onClose={() => setNewAccountModalOpen(false)} onSubmit={() => { }} customer={selectedCustomer} />
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
                            <th className="px-4 py-3">{t('customer.management.table.headers.lastTransactionDate')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.status')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.portfolioDetail')}</th>
                            <th className="px-4 py-3">{t('customer.management.table.headers.accountOperations')}</th>

                        </tr>
                    </thead>
                    <tbody>
                        {mockAccounts.map((account, index) => (
                            <tr key={account.accountNo} className={`hover:bg-gray-50 ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}>
                                <td className="px-4 py-1">{account.accountNo}</td>
                                <td className="px-4 py-1">{account.portfolioValue + " TL"}</td>
                                <td className="px-4 py-1">{account.balance + " TL"}</td>
                                <td className="px-4 py-1">{new Date(account.lastTransactionDate).toLocaleDateString('tr-TR', { day: '2-digit', month: '2-digit', year: 'numeric' })}</td>
                                <td className="px-4 py-1">{account.status}</td>
                                <td className="px-4 py-1">
                                    <button
                                        onClick={() => router.push('/dashboard/customer-portfolio')}
                                        className="bg-[#813FB4] hover:bg-[#6B2C91] text-white px-3 py-1.5 rounded-lg text-xs font-medium transition-colors duration-200 flex items-center gap-1.5 min-w-fit"
                                    >
                                        <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                                        </svg>
                                        {t('customer.management.table.actions.portfolio')}
                                    </button>
                                </td>
                                <td className="p-2 px-4">
                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={() => console.log('Hesap düzenleme:', account.accountNo)}
                                            className="group relative cursor-pointer inline-flex justify-center p-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-md hover:bg-gray-50 hover:text-gray-700 hover:border-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 shadow-sm hover:shadow-md"
                                            title={t('customer.management.table.actions.editAccount')}
                                            aria-label={t('customer.management.table.actions.editAccount')}
                                        >
                                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                            </svg>
                                            <span className="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white text-xs rounded py-1 px-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap">
                                                {t('customer.management.table.actions.edit')}
                                            </span>
                                        </button>

                                        <div className="w-px h-6 bg-gray-200 mx-1"></div>

                                        <Tooltip
                                            title={
                                                (account.portfolioValue > 0 || account.balance > 0)
                                                    ? t('customer.management.table.tooltips.accountsWithBalanceCannotBeClosed')
                                                    : ""
                                            }
                                            arrow
                                        >
                                            <span>
                                                <button
                                                    onClick={() => {
                                                        if (account.portfolioValue === 0 && account.balance === 0) {
                                                            setSelectedAccount(account);
                                                            setAccCloseModalOpen(true);
                                                        }
                                                    }}
                                                    disabled={account.portfolioValue > 0 || account.balance > 0}
                                                    className={`group relative inline-flex items-center justify-center p-2 text-sm font-medium rounded-md transition-all duration-200 shadow-sm ${account.portfolioValue > 0 || account.balance > 0
                                                        ? 'cursor-not-allowed text-gray-400 bg-gray-100 border border-gray-200'
                                                        : 'cursor-pointer text-red-600 bg-white border border-red-200 hover:bg-red-50 hover:text-red-700 hover:border-red-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 hover:shadow-md'
                                                        }`}
                                                    title={t('customer.management.table.actions.closeAccount')}
                                                    aria-label={t('customer.management.table.actions.closeAccount')}
                                                >
                                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                    </svg>
                                                    {!(account.portfolioValue > 0 || account.balance > 0) && (
                                                        <span className="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white text-xs rounded py-1 px-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap">
                                                            {t('customer.management.table.actions.close')}
                                                        </span>
                                                    )}
                                                </button>
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
                                    <p className="text-2xl font-bold text-gray-900 mt-1">{mockAccounts.length}</p>
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
                                        {mockAccounts.reduce((sum, account) => sum + account.portfolioValue, 0).toLocaleString('tr-TR', {
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

                        {/* Total Balance */}
                        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-100">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-600">{t('customer.management.summary.totalBalance')}</p>
                                    <p className="text-2xl font-bold text-purple-600 mt-1">
                                        {mockAccounts.reduce((sum, account) => sum + account.balance, 0).toLocaleString('tr-TR', {
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

export default CustomerManagement