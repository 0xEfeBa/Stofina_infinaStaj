"use client";
import React from "react";
import { useTranslation } from "next-i18next";

export interface TransferTipSelectorProps {
  selectedTip: string;
  onChange: (tip: string) => void;
  availableTips?: string[]; // Sadece izin verilen tipler
}

const TransferTipSelector: React.FC<TransferTipSelectorProps> = ({
  selectedTip,
  onChange,
  availableTips = ["HESAPLAR_ARASI", "MÜŞTERİLER_ARASI", "KURUMLAR_ARASI", "HARİCİ"], // default tüm tipler
}) => {
  const { t } = useTranslation("common");

  const allTips = [
    { key: "HESAPLAR_ARASI", label: t("transfer.transferSection.options.accountsBetween") },
    { key: "MÜŞTERİLER_ARASI", label: t("transfer.transferSection.options.customersBetween") },
    { key: "KURUMLAR_ARASI", label: t("transfer.transferSection.options.institutionsBetween") },
    { key: "HARİCİ", label: t("transfer.transferSection.options.external") },
  ];

  return (
    <div className="mb-6">
      <div className="flex flex-wrap justify-center gap-4">
        {allTips.map((tip) => {
          const isDisabled = !availableTips.includes(tip.key);
          const isSelected = selectedTip === tip.key;

          return (
            <label
              key={tip.key}
              className={`flex items-center gap-2 px-4 py-2 rounded-full border text-sm transition-all
                ${isDisabled ? "opacity-50 cursor-not-allowed bg-gray-100" : "hover:border-[#439E54]"}
                ${isSelected ? "border-[#439E54] bg-[#e8ffec]" : "border-gray-300 bg-white"}`}
            >
              <div
                className={`w-4 h-4 rounded-full border-2 flex items-center justify-center
                  ${isSelected ? "border-[#439E54]" : "border-gray-400"}
                  ${isDisabled ? "bg-gray-200" : "bg-white"}`}
              >
                {isSelected && !isDisabled && (
                  <div className="w-2 h-2 bg-[#439E54] rounded-full"></div>
                )}
              </div>
              <span className="text-gray-800">{tip.label}</span>
              <input
                type="radio"
                name="transferTip"
                className="hidden"
                disabled={isDisabled}
                checked={isSelected}
                onChange={() => !isDisabled && onChange(tip.key)}
              />
            </label>
          );
        })}
      </div>
    </div>
  );
};

export default TransferTipSelector;
