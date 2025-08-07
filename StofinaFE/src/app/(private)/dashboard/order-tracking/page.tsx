"use client";
import React from 'react'
import styles from '@/theme/common.module.css'
import { useRouter } from "next/navigation";
import AutoCompleteCustomerSearch from '@/components/common/AutoCompleteCustomerSearch';
import OrderTrackingTable from '@/components/order-tracking/OrderTrackingTable';
import { useTranslation } from 'react-i18next';


const OrderTracking = () => {
    const router = useRouter();
    const { t } = useTranslation();
    return (
        <div>
            <div className=' flex items-start'>
                <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
                    <img src="/menu-icon/back.png" alt={t('report.back')} className={styles.icon} />
                    {t('common.back')}
                </button>

                <AutoCompleteCustomerSearch />
            </div>
            <div>
                <OrderTrackingTable />
            </div>

        </div>
    )
}

export default OrderTracking