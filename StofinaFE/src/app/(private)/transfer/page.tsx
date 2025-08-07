"use client";

import { useState } from "react";
import { useTranslation } from 'next-i18next';
import styles from "./KıymetTransferi.module.css";
import SimpleCustomerSearch from "@/components/common/SimpleCustomerSearch";
import TransferSection from '@/components/common/TransferSection';

const mockAccounts = ["Hesap 1", "Hesap 2", "Hesap 3"];

const todayISODate = () => {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
};

export default function TransferPage() {
  const { t } = useTranslation("common");
  const [transferType, setTransferType] = useState<"NAKIT" | "HISSE">("NAKIT");
  const [transferCategory, setTransferCategory] = useState("HESAPLAR_ARASI");
  const [senderAccount, setSenderAccount] = useState("");
  const [receiverAccounts, setReceiverAccounts] = useState<string[]>([]);
  const [receiverAccountSelected, setReceiverAccountSelected] = useState<string>("");

  const [amount, setAmount] = useState<number | "">("");
  const [quantity, setQuantity] = useState<number | "">("");
  const [stockCode, setStockCode] = useState("");
  const [transferDate, setTransferDate] = useState<string>(todayISODate());

  const handleSenderChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = e.target.value;
    setSenderAccount(selected);
    setReceiverAccounts(mockAccounts.filter((acc) => acc !== selected));
    // seçili alıcıyı temizle (gönderici değişince)
    setReceiverAccountSelected("");
  };

  const handleSubmit = () => {
    // örnek: göndereceğiniz payload burada hazırlanır
    const payload: any = {
      transferType,
      transferCategory,
      transferDate,
      senderAccount,
      receiverAccount: receiverAccountSelected || undefined,
    };

    if (transferType === "NAKIT") {
      payload.amount = amount;
    } else {
      payload.stockCode = stockCode;
      payload.quantity = quantity;
      payload.amount = amount;
    }

    console.log("Transfer payload:", payload);
    // burada API çağrısı/dispatch yapılır
  };

  return (

    <div className={styles.container}>
      {/* Sayfa Başlığı */}
      <h2 className={styles.pageTitle}>{t('transfer.title')}</h2>

      {/* Müşteri Arama Alanı */}
      <div className={styles.customerSearchWrapper}>
        <SimpleCustomerSearch />
      </div>

      <div className={styles.content}>
        <div className={styles.formSection}>
          {/* Transfer Türü */}
          <TransferSection />

          {/* Transfer Tipi */}
          <div className={styles.formGroup}>
            <label>{t('transfer.form.transferType.label')}</label>
            <select
              value={transferType}
              onChange={(e) => setTransferType(e.target.value as "NAKIT" | "HISSE")}
            >
              <option value="NAKIT">{t('transfer.form.transferType.cash')}</option>
              <option value="HISSE">{t('transfer.form.transferType.stock')}</option>
            </select>
          </div>

          {/* Dinamik Alanlar */}
          {transferType === "NAKIT" ? (
            <div className={styles.formGroup}>
              <label>{t('transfer.form.amount.label')}</label>
              <input
                type="number"
                placeholder={t('transfer.form.amount.placeholder')}
                value={amount === "" ? "" : amount}
                onChange={(e) => setAmount(e.target.value === "" ? "" : Number(e.target.value))}
              />
            </div>
          ) : (
            <>
              <div className={styles.formGroup}>
                <label>{t('transfer.form.stockCode.label')}</label>
                <input
                  type="text"
                  placeholder={t('transfer.form.stockCode.placeholder')}
                  value={stockCode}
                  onChange={(e) => setStockCode(e.target.value)}
                />
              </div>
              <div className={styles.formGroup}>
                <label>{t('transfer.form.quantity.label')}</label>
                <input
                  type="number"
                  placeholder={t('transfer.form.quantity.placeholder')}
                  value={quantity === "" ? "" : quantity}
                  onChange={(e) => setQuantity(e.target.value === "" ? "" : Number(e.target.value))}
                />
              </div>
              <div className={styles.formGroup}>
                <label>{t('transfer.form.amount.label')}</label>
                <input
                  type="number"
                  placeholder={t('transfer.form.amount.placeholder')}
                  value={amount === "" ? "" : amount}
                  onChange={(e) => setAmount(e.target.value === "" ? "" : Number(e.target.value))}
                />
              </div>
            </>
          )}
        </div>

        <div className={styles.accountSection}>
          {/* Gönderi Tarihi */}
          <div className={styles.formGroup}>
            <label>{t('transfer.form.transferDate.label')}</label>
            <input
              type="date"
              value={transferDate}
              onChange={(e) => setTransferDate(e.target.value)}
            />
          </div>

          {/* Gönderici Hesap */}
          <div className={styles.formGroup}>
            <label>{t('transfer.form.senderAccount.label')}</label>
            <select value={senderAccount} onChange={handleSenderChange}>
              <option value="">{t('transfer.form.senderAccount.placeholder')}</option>
              {mockAccounts.map((acc, idx) => (
                <option key={idx} value={acc}>
                  {acc}
                </option>
              ))}
            </select>
          </div>

          {/* Alıcı Hesap*/}
          <div className={styles.formGroup}>
            <label>{t('transfer.form.receiverAccount.label')}</label>
            <select
              value={receiverAccountSelected}
              onChange={(e) => setReceiverAccountSelected(e.target.value)}
              disabled={receiverAccounts.length === 0}
            >
              <option value="">{t('transfer.form.receiverAccount.placeholder')}</option>
              {receiverAccounts.map((acc, idx) => (
                <option key={idx} value={acc}>
                  {acc}
                </option>
              ))}
            </select>
          </div>

          <button className={styles.submitButton} onClick={handleSubmit}>{t('transfer.buttons.startTransfer')}</button>
        </div>
      </div>
    </div>
  );
}
