"use client";
import SystemDateCard from '@/components/report/SystemDateCard';
import React, { useState } from 'react'
import styles from '@/theme/common.module.css'
import { useRouter } from 'next/navigation';
import ReportStatusCard from '@/components/report/ReportStatusCard';
import EndOfDayReportCard from '@/components/report/EndOfDayReportCard';
import { useTranslation } from 'react-i18next';

const Page = () => {
    const { t } = useTranslation();
    const router = useRouter();
    const [reportReady, setReportReady] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [systemDate, setSystemDate] = useState(new Date());

    const handleEndOfDayClick = () => {
        setIsLoading(true);
        // 3 saniye delay ekle
        setTimeout(() => {
            setReportReady(true);
            setIsLoading(false);
        }, 3000);
    };

    const handleConfirmReport = () => {
        // Sistem tarihini sonraki güne ilerlet
        const nextDay = new Date(systemDate);
        nextDay.setDate(nextDay.getDate() + 1);
        setSystemDate(nextDay);

        // Rapor durumunu sıfırla
        setReportReady(false);

    };

    const handlePrintReport = () => {
        // Open the PDF in a new tab
        window.open('/assets/pdf/gun_sonu_rapor.pdf', '_blank');
    };

    return (
        <div>
            <div className=' flex items-start mb-6'> {/* Geri butonu */}
                <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
                    <img src="/menu-icon/back.png" alt={t('report.back')} className={styles.icon} />
                    {t('common.back')}
                </button>
            </div>
            <div className='flex gap-6 items-center'>
                <div> {/* Sistem tarihi ve rapor durumu */}
                    <div className='mb-6'>
                        <SystemDateCard
                            systemDate={systemDate}
                            currentDate={new Date()}
                            onEndOfDayClick={handleEndOfDayClick}
                        />
                    </div>

                    {isLoading ? (
                        <ReportStatusCard status={t('report.status.pending')} />
                    ) : !reportReady ? (
                        <ReportStatusCard status={t('report.status.notReady')} />
                    ) : (
                        <ReportStatusCard
                            status={t('report.status.ready')}
                            onCloseReport={() => setReportReady(false)}
                        />
                    )}
                </div>
                <div className='w-full'>
                    {reportReady && (
                        <div className="flex justify-center w-full">
                            <EndOfDayReportCard
                                date={systemDate.toLocaleDateString()}
                                time={new Date().toLocaleTimeString()}
                                type={t('report.type.endOfDay')}
                                statusMessage={t('report.statusMessage')}
                                onConfirm={handleConfirmReport}
                                onPrint={handlePrintReport}
                            />
                        </div>
                    )}
                </div>
            </div>
            <div className='absolute bottom-5  right-5'>
                <button className="
                    cursor-pointer 
                    relative overflow-hidden
                    bg-gradient-to-r from-[#813FB4] to-[#9B5BC7] 
                    hover:from-[#9B5BC7] hover:to-[#B47CD9]
                    active:from-[#6B2E9A] active:to-[#813FB4]
                    text-white px-4 py-2  rounded-2xl 
                    font-bold text-lg
                    transition-all duration-300 ease-in-out
                    shadow-2xl hover:shadow-3xl
                    hover:scale-105 active:scale-95
                    border-2 border-[#813FB4] hover:border-[#9B5BC7]
                    flex items-center gap-3
                ">



                    {/* Button Text */}
                    <span className="relative z-10 whitespace-nowrap">
                        {t('report.sendResultDocument.button')}
                    </span>


                </button>
            </div>
        </div>
    )
}

export default Page