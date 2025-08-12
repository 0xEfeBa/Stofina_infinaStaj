"use client";
import React, { useEffect, useState } from 'react'
import styles from '@/theme/common.module.css'
import { useRouter } from "next/navigation";
import AutoCompleteCustomerSearch from '@/components/common/AutoCompleteCustomerSearch';
import OrderTrackingTable from '@/components/order-tracking/OrderTrackingTable';
import { useTranslation } from 'react-i18next';
import AccountSelector from '@/components/order-tracking/accountSelector';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { useSelectorCustom } from '@/store';
import { thunkAccount } from '@/thunks/accountThunk';
import { Account } from '@/types/account';
import { Order } from '@/types/order';
import { thunkOrder } from '@/thunks/orderThunk';


const OrderTracking = () => {
    const [openAccountSelector, setOpenAccountSelector] = useState(false);
    const router = useRouter();
    const dispatch = useDispatchCustom();
    const { t } = useTranslation();
    const { selectedIndividualCustomer, selectedCorporateCustomer } = useSelectorCustom(state => state.customer);
    const [accounts, setAccounts] = useState<Account[]>([]);
    const [orders, setOrders] = useState<Order[]>([]);


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

    const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);

    const fetchOrders = async (account: Account) => {
        setSelectedAccount(account);
        const response = await dispatch(thunkOrder.getOrdersByAccountId(account.id));
        if (response) {
            setOrders(response);
        }
    }

    const refreshOrders = async () => {
        if (selectedAccount) {
            const response = await dispatch(thunkOrder.getOrdersByAccountId(selectedAccount.id));
            if (response) {
                setOrders(response);
            }
        }
    }


    return (
        <div>
            <div className=' flex items-start'>
                <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
                    <img src="/menu-icon/back.png" alt={t('report.back')} className={styles.icon} />
                    {t('common.back')}
                </button>

                <AutoCompleteCustomerSearch />
                <AccountSelector open={openAccountSelector} onClose={() => setOpenAccountSelector(false)} accounts={accounts} onSelect={fetchOrders} />
            </div>
            <div>
                {
                    orders && orders.length > 0
                    &&
                    <OrderTrackingTable orders={orders} onRefresh={refreshOrders} />
                }
            </div>

        </div>
    )
}

export default OrderTracking