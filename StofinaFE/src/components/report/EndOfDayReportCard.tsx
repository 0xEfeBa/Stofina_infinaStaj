import React from 'react'
import { useTranslation } from 'react-i18next';

interface EndOfDayReportCardProps {
    date: string;
    time: string;
    type: string;
    statusMessage: string;
    onConfirm: () => void;
    onPrint: () => void;
}

const EndOfDayReportCard = ({ date, time, type, statusMessage, onConfirm, onPrint }: EndOfDayReportCardProps) => {
    const { t } = useTranslation();

    return (
        <div className="bg-white border border-gray-200 rounded-2xl p-8 shadow-md w-3xl max-w-md">
            <h3 className="text-center text-xl font-bold text-gray-800 mb-6 tracking-tight">{t('report.endOfDayCard.title')}</h3>
            <div className="grid grid-cols-2 gap-4 text-sm text-gray-700">
                <div><span className="font-semibold">{t('report.endOfDayCard.date')}:</span> {date}</div>
                <div><span className="font-semibold">{t('report.endOfDayCard.time')}:</span> {time}</div>
                <div className="col-span-2"><span className="font-semibold">{t('report.endOfDayCard.reportType')}:</span> {type}</div>
            </div>
            <div className="mt-4 p-4 bg-gray-50 border border-dashed border-gray-300 rounded text-sm text-gray-700">
                <strong>{t('report.endOfDayCard.status')}:</strong>
                <p className="mt-1 leading-relaxed">{statusMessage}</p>
            </div>
            <p className="text-xs text-right text-gray-400 mt-2">{date} {time}</p>
            <div className="flex gap-4 justify-end mt-6">
                <button onClick={onConfirm} className="px-5 py-2 bg-green-100 text-green-700 rounded-lg font-medium hover:bg-green-200">{t('report.endOfDayCard.ok')}</button>
                <button onClick={onPrint} className="px-5 py-2 bg-purple-100 text-purple-700 rounded-lg font-medium hover:bg-purple-200">{t('report.endOfDayCard.printReport')}</button>
            </div>
        </div>
    )
}

export default EndOfDayReportCard