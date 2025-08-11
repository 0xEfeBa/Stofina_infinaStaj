import { Account } from '@/types/account'
import React, { useState } from 'react'
import styles from './AccountSelector.module.css'


interface Props {
    open: boolean
    onClose: () => void
    accounts: Account[]
    onSelect: (account: Account) => void
}

const AccountSelector = ({ open, onClose, accounts, onSelect }: Props) => {
    const [selectedAccount, setSelectedAccount] = useState<Account | null>(null)

    const handleAccountSelect = (account: Account) => {
        setSelectedAccount(account)
    }

    const handleConfirm = () => {
        if (selectedAccount) {
            onSelect(selectedAccount)
            onClose()
            setSelectedAccount(null)
        }
    }

    const handleCancel = () => {
        onClose()
        setSelectedAccount(null)
    }
    console.log(accounts)
    if (!open) return null

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modal}>
                <div className={styles.modalHeader}>
                    <h2>Hesap Seçin</h2>
                    <button
                        className={styles.closeButton}
                        onClick={handleCancel}
                    >
                        ×
                    </button>
                </div>

                <div className={styles.modalBody}>
                    {accounts.length === 0 ? (
                        <div className={styles.noAccounts}>
                            <p>Kullanılabilir hesap bulunamadı.</p>
                        </div>
                    ) : (
                        <div className={styles.accountList}>
                            {accounts.map((account) => (
                                <div
                                    key={account.id}
                                    className={`${styles.accountItem} ${selectedAccount?.id === account.id ? styles.selected : ''
                                        }`}
                                    onClick={() => handleAccountSelect(account)}
                                >
                                    <div className={styles.accountInfo}>
                                        <div className={styles.accountNumber}>
                                            {account.accountNumber}
                                        </div>
                                        <div className={styles.accountStatus}>
                                            <span className={`${styles.status} ${styles[account.status.toLowerCase()]}`}>
                                                {account.status}
                                            </span>
                                        </div>
                                    </div>
                                    <div className={styles.balanceInfo}>
                                        <div className={styles.balanceItem}>
                                            <span className={styles.label}>Toplam Bakiye:</span>
                                            <span className={styles.value}>
                                                {account.totalBalance?.toLocaleString('tr-TR', {
                                                    minimumFractionDigits: 2,
                                                    maximumFractionDigits: 2
                                                })} ₺
                                            </span>
                                        </div>
                                        <div className={styles.balanceItem}>
                                            <span className={styles.label}>Kullanılabilir:</span>
                                            <span className={styles.value}>
                                                {account.availableBalance?.toLocaleString('tr-TR', {
                                                    minimumFractionDigits: 2,
                                                    maximumFractionDigits: 2
                                                })} ₺
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className={styles.modalFooter}>
                    <button
                        className={styles.cancelButton}
                        onClick={handleCancel}
                    >
                        İptal
                    </button>
                    <button
                        className={`${styles.confirmButton} ${!selectedAccount ? styles.disabled : ''}`}
                        onClick={handleConfirm}
                        disabled={!selectedAccount}
                    >
                        Seç
                    </button>
                </div>
            </div>
        </div>
    )
}

export default AccountSelector