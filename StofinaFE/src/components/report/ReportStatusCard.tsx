import React from 'react'
import { CheckCircle, Clock, AlertCircle, Download } from 'lucide-react'
import { useTranslation } from 'react-i18next';

// type Status = 'Hazır' | 'Bekleniyor' | 'Henüz Alınmadı';

interface ReportStatusCardProps {
    lastReportDate?: string;
    status: string;
    onCloseReport?: () => void;
}

const ReportStatusCard = ({ lastReportDate, status, onCloseReport }: ReportStatusCardProps) => {

    const { t } = useTranslation();

    const getStatusConfig = () => {
        switch (status) {
            case 'Hazır':
                return {
                    icon: CheckCircle,
                    color: 'text-emerald-600',
                    bgColor: 'bg-emerald-50',
                    borderColor: 'border-emerald-200',
                    iconColor: 'text-emerald-500',
                    gradient: 'from-emerald-500 to-emerald-600'
                };
            case 'Bekleniyor':
                return {
                    icon: Clock,
                    color: 'text-amber-600',
                    bgColor: 'bg-amber-50',
                    borderColor: 'border-amber-200',
                    iconColor: 'text-amber-500',
                    gradient: 'from-amber-500 to-amber-600'
                };
            default:
                return {
                    icon: AlertCircle,
                    color: 'text-gray-500',
                    bgColor: 'bg-gray-50',
                    borderColor: 'border-gray-200',
                    iconColor: 'text-gray-400',
                    gradient: 'from-gray-500 to-gray-600'
                };
        }
    };

    const getStatusText = () => {
        switch (status) {
            case 'Hazır':
                return t('report.statusCard.ready');
            case 'Bekleniyor':
                return t('report.statusCard.pending');
            default:
                return t('report.statusCard.notReady');
        }
    };

    const statusConfig = getStatusConfig();
    const StatusIcon = statusConfig.icon;

    return (
        <div className={`relative overflow-hidden bg-white border ${statusConfig.borderColor} rounded-2xl p-4 shadow-sm hover:shadow-md transition-all duration-300 w-full max-w-sm group`}>
            {/* Background Pattern */}
            <div className="absolute inset-0 bg-gradient-to-br from-transparent via-transparent to-gray-50/30 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

            {/* Header */}
            <div className="relative z-10">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-gray-800 font-bold text-lg">{t('report.statusCard.title')}</h2>
                    <div className={`p-2 rounded-full ${statusConfig.bgColor}`}>
                        <StatusIcon className={`w-5 h-5 ${statusConfig.iconColor}`} />
                    </div>
                </div>

                {/* Status Badge */}
                <div className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full ${statusConfig.bgColor} border ${statusConfig.borderColor} mb-4`}>
                    <StatusIcon className={`w-4 h-4 ${statusConfig.iconColor}`} />
                    <span className={`text-sm font-semibold ${statusConfig.color}`}>
                        {getStatusText()}
                    </span>
                </div>

                {/* Last Report Date */}
                <div className="space-y-2">
                    <div className="flex items-center gap-2 text-gray-600">
                        <span className="text-sm font-medium">{t('report.statusCard.lastReport')}:</span>
                        <span className="text-sm font-semibold text-gray-800">
                            {lastReportDate || t('report.statusCard.notReady')}
                        </span>
                    </div>

                    {lastReportDate && (
                        <div className="text-xs text-gray-500">
                            {t('report.statusCard.lastUpdate')}: {new Date().toLocaleDateString('tr-TR')}
                        </div>
                    )}
                </div>

                {/* Action Button */}
                {status === 'Hazır' && onCloseReport && (
                    <div className="mt-6">
                        <button
                            onClick={() => {
                                // Create a temporary anchor element
                                const link = document.createElement('a');
                                link.href = '/assets/pdf/gun_sonu_rapor.pdf';
                                link.download = 'gun_sonu_rapor.pdf';
                                document.body.appendChild(link);
                                link.click();
                                document.body.removeChild(link);

                                // Call the original onCloseReport if it exists
                                if (onCloseReport) {
                                    onCloseReport();
                                }
                            }}
                            className={`w-full cursor-pointer group/btn relative overflow-hidden bg-gradient-to-r ${statusConfig.gradient} text-white rounded-xl py-3 px-4 font-semibold transition-all duration-300 hover:shadow-lg hover:scale-[1.02] active:scale-95 flex items-center justify-center gap-2`}
                        >
                            <Download className="w-4 h-4 group-hover/btn:animate-bounce" />
                            <span>{t('report.statusCard.download')}</span>
                            <div className="absolute inset-0 bg-white/20 transform -skew-x-12 -translate-x-full group-hover/btn:translate-x-full transition-transform duration-700" />
                        </button>
                    </div>
                )}

                {/* Progress Indicator for "Bekleniyor" status */}
                {status === 'Bekleniyor' && (
                    <div className="mt-6">
                        <div className="flex items-center gap-2 mb-2">
                            <div className="w-2 h-2 bg-amber-500 rounded-full animate-pulse" />
                            <span className="text-xs text-amber-600 font-medium">{t('report.statusCard.reportPreparing')}</span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-1.5">
                            <div className="bg-gradient-to-r from-amber-400 to-amber-500 h-1.5 rounded-full animate-pulse" style={{ width: '60%' }} />
                        </div>
                    </div>
                )}
            </div>

            {/* Decorative Corner */}
            <div className={`absolute top-0 right-0 w-16 h-16 ${statusConfig.bgColor} rounded-bl-full opacity-20`} />
        </div>
    )
}

export default ReportStatusCard