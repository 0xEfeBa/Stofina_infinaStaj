"use client";
import React from 'react';
import { useTranslation } from 'react-i18next';

interface SystemDateCardProps {
    systemDate: Date;
    currentDate: Date;
    onEndOfDayClick: () => void;
}

const SystemDateCard = ({ systemDate, currentDate, onEndOfDayClick }: SystemDateCardProps) => {
    const { t } = useTranslation();

    const formatDate = (date: Date) => {
        return date.toLocaleDateString('tr-TR', {
            day: '2-digit',
            month: 'long',
            year: 'numeric'
        });
    };

    const formatShortDate = (date: Date) => {
        return date.toLocaleDateString('tr-TR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    return (
        <div className="group relative bg-white border border-gray-200 rounded-3xl p-4 shadow-lg hover:shadow-xl transition-colors duration-300 w-96 max-w-md backdrop-blur-sm">
            <div className="absolute top-0 left-0 right-0 bottom-0 bg-gradient-to-br from-blue-50/50 via-transparent to-green-50/50 rounded-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

            {/* Sistem Tarihi Header */}
            <div className="relative z-10 flex items-center justify-between mb-4">
                <div className="flex items-center gap-3">
                    <div className="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl shadow-lg">
                        <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                    <div>
                        <h3 className="text-lg font-bold text-gray-900">{t('report.systemDate.title')}</h3>
                        <p className="text-sm text-gray-500">{t('report.systemDate.subtitle')}</p>
                    </div>
                </div>
                <div className="flex items-center justify-center w-3 h-3 bg-green-500 rounded-full animate-pulse shadow-lg">
                    <div className="w-1.5 h-1.5 bg-green-300 rounded-full" />
                </div>
            </div>

            {/* Ana Tarih Gösterimi */}
            <div className="relative z-10 mb-2">
                <div className="bg-gradient-to-r from-gray-50 to-blue-50/50 rounded-2xl p-2 border border-gray-100">
                    <div className="text-center">
                        <p className="text-2xl font-bold text-gray-900 mb-2">
                            {formatDate(systemDate)}
                        </p>
                        <div className="flex items-center justify-center gap-2 text-sm text-gray-600">
                            <div className="w-2 h-2 bg-blue-400 rounded-full" />
                            <span>{t('report.systemDate.systemDateLabel')}</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Mevcut tarih bilgisi aktif dönem */}
            <div className="relative z-10 mb-2">
                <div className="flex items-center justify-between p-2 bg-gray-50/50 rounded-xl border border-gray-100">
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                            <svg className="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-700">{t('report.systemDate.currentPeriod')}</p>
                            <p className="text-xs text-gray-500">{t('report.systemDate.currentPeriodSubtitle')}</p>
                        </div>
                    </div>
                    <div className="text-right">
                        <p className="font-bold  text-gray-900">{formatShortDate(currentDate)}</p>
                    </div>
                </div>
            </div>

            {/* Gün Sonu Al Butonu */}
            <div className="relative z-10">
                <button
                    onClick={onEndOfDayClick}
                    className="group/btn w-full cursor-pointer relative overflow-hidden bg-gradient-to-r from-green-500 via-green-600 to-emerald-600 text-white rounded-2xl py-2 px-4 font-bold text-lg shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] focus:outline-none focus:ring-4 focus:ring-green-200"
                >
                    <div className="absolute inset-0 bg-gradient-to-r from-green-400 to-emerald-500 opacity-0 group-hover/btn:opacity-100 transition-opacity duration-300" />
                    <div className="relative flex items-center justify-center gap-3">
                        <svg className="w-5 h-5 transition-transform group-hover/btn:rotate-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <span>{t('report.systemDate.endOfDayButton')}</span>
                        <svg className="w-4 h-4 transition-transform group-hover/btn:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                        </svg>
                    </div>
                </button>
            </div>
        </div>
    );
};

export default SystemDateCard;
