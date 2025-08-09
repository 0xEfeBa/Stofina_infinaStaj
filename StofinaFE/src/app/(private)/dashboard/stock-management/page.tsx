"use client";
import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import styles from "./StockForm.module.css";

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

const schema = yup.object({
  kodu: yup
    .string()
    .required("Lütfen hisse kodunu giriniz.")
    .matches(/^[A-Z]{1,5}$/, "Hisse kodu yalnızca büyük harflerden oluşmalı ve 1-5 karakter olmalı."),
  adi: yup
    .string()
    .required("Lütfen hisse adını giriniz.")
    .max(50, "Hisse adı 50 karakterden uzun olamaz."),
  sirketUnvani: yup
    .string()
    .required("Lütfen şirket unvanını giriniz.")
    .max(100, "Şirket unvanı 100 karakterden uzun olamaz."),
  payPazari: yup.string().required("Lütfen pay piyasa pazarı seçiniz."),
  borsa: yup.string().required("Lütfen borsa seçiniz."),
  paraBirimi: yup.string().required("Lütfen para birimi seçiniz."),
  isinKodu: yup
    .string()
    .required("Lütfen ISIN kodunu giriniz.")
    .matches(/^TR\d{10}$/, "ISIN kodu TR ile başlamalı ve ardından 10 hane olmalı.")
}).required();

export default function Page() {
  const router = useRouter();

  const [popup, setPopup] = useState<{ message: string; type: "success" | "error" } | null>(null);
  const popupTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      kodu: "",
      adi: "",
      sirketUnvani: "",
      payPazari: "",
      borsa: "",
      paraBirimi: "",
      isinKodu: ""
    }
  });

  const showPopup = (message: string, type: "success" | "error") => {
    setPopup({ message, type });
    if (popupTimeoutRef.current) clearTimeout(popupTimeoutRef.current);
    popupTimeoutRef.current = setTimeout(() => setPopup(null), 3000);
  };

  const onSubmit = (data: any) => {
    const token = localStorage.getItem("accessToken");
    
    const requestBody = {
      symbol: data.kodu,
      stockName: data.adi,
      companyName: data.sirketUnvani,
      equityMarket: data.payPazari,
      exchange: data.borsa,
      currency: data.paraBirimi,
      isinCode: data.isinKodu,
      status: "INACTIVE",
      defaultPrice: 0,
      currentPrice: 0,
    };

    fetch("http://localhost:9005/api/v1/market/stocks", {
      method: "POST",
      headers: {
        "accept": "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(requestBody),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Hata oluştu");
        return res.json();
      })
      .then((result) => {
        showPopup(`Hisse "${data.adi}" başarıyla kaydedildi.`, "success");
        reset();
      })
      .catch((error) => {
        showPopup("Kayıt sırasında hata oluştu", "error");
      });
  };

  const onError = (errors: any) => {
    const firstErrorField = Object.keys(errors)[0];
    if (firstErrorField) {
      showPopup(errors[firstErrorField]?.message, "error");
    }
  };

  return (
    <div className={styles.container}>
      {/* ÜST BUTONLAR */}
      <div className={styles.topButtons}>
        <button
          type="button"
          className={styles.secondaryButton}
          onClick={() => router.back()}
        >
          <img src="/menu-icon/back.png" alt="Geri" className={styles.icon} />
          Geri
        </button>

        <button
          type="button"
          className={styles.secondaryButton}
          onClick={() => reset()}
        >
          <img src="/menu-icon/clear.png" alt="Temizle" className={styles.icon} />
          Temizle
        </button>
      </div>

      <form className={styles.form} onSubmit={handleSubmit(onSubmit, onError)}>
        <h2 className={styles.formTitle}>HİSSE SENEDİ TANIMLAMA</h2>

        {/* KODU + ADI */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>Hisse Kodu*</label>
            <input
              type="text"
              {...register("kodu")}
              className={styles.input}
              onChange={(e) => setValue("kodu", e.target.value.toUpperCase())}
            />
          </div>

          <div className={styles.formGroupRow}>
            <label className={styles.label}>Hisse Adı*</label>
            <input
              type="text"
              {...register("adi")}
              className={styles.input}
            />
          </div>
        </div>

        {/* ŞİRKET UNVANI */}
        <div className={styles.formGroup}>
          <label className={styles.label}>Şirket Unvanı*</label>
          <input
            type="text"
            {...register("sirketUnvani")}
            className={styles.input}
          />
        </div>

        {/* PAY PAZARI + BORSA */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>Pay Piyasa Pazarı*</label>
            <select {...register("payPazari")} className={styles.select}>
              <option value="">Seçiniz</option>
              <option value="Ana Piyasa">Ana Piyasa</option>
              <option value="Yıldız Pazarı">Yıldız Pazarı</option>
              <option value="Gelişen İşletmeler Pazarı">Gelişen İşletmeler Pazarı</option>
            </select>
          </div>

          <div className={styles.formGroupRow}>
            <label className={styles.label}>Borsa*</label>
            <select {...register("borsa")} className={styles.select}>
              <option value="">Seçiniz</option>
              <option value="BIST100">BIST100</option>
              <option value="BIST50">BIST50</option>
              <option value="BIST30">BIST30</option>
            </select>
          </div>
        </div>

        {/* PARA BİRİMİ + ISIN KODU */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>Para Birimi*</label>
            <select {...register("paraBirimi")} className={styles.select}>
              <option value="">Seçiniz</option>
              <option value="TRY">TRY</option>
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
              <option value="GBP">GBP</option>
            </select>
          </div>

          <div className={styles.formGroupRow}>
            <label className={styles.label}>ISIN Kodu*</label>
            <input
              type="text"
              {...register("isinKodu")}
              className={styles.input}
            />
          </div>
        </div>
        <button type="submit" className={styles.submitButton}>Kaydet</button>
      </form>

      {popup && (
        <div
          className={`${styles.popup} ${popup.type === "success" ? styles.popupSuccess : styles.popupError
            }`}
        >
          {popup.message}
        </div>
      )}
    </div>
  );
}