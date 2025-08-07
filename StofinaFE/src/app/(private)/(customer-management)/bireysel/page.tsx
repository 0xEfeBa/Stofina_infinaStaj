"use client";

import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import styles from "./BireyselMüşteri.module.css";
import { useTranslation } from 'react-i18next';

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

export default function Page() {
  const router = useRouter();
  const { t } = useTranslation();

  const schema = yup.object({
    tckn: yup
      .string()
      .required(t('customer.validation.tcknRequired'))
      .matches(/^\d{11}$/, t('customer.validation.tcknFormat')),
    ad: yup
      .string()
      .required(t('customer.validation.nameRequired'))
      .max(30, t('customer.validation.nameMaxLength'))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]+$/, t('customer.validation.nameFormat')),
    soyad: yup
      .string()
      .required(t('customer.validation.surnameRequired'))
      .max(30, t('customer.validation.surnameMaxLength'))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]+$/, t('customer.validation.surnameFormat')),
    email: yup
      .string()
      .required(t('customer.validation.email'))
      .email(t('customer.validation.email')),
    telefon: yup
      .string()
      .required(t('customer.validation.phoneRequired'))
      .matches(/^[1-9]\d{9}$/, t('customer.validation.phoneFormat')),
    adres: yup
      .string()
      .required(t('customer.validation.addressRequired'))
      .max(400, t('customer.validation.addressMaxLength')),
    uygunluk: yup.boolean().oneOf([true], t('customer.validation.suitabilityRequired')),
    kvkk: yup.boolean().oneOf([true], t('customer.validation.kvkkRequired')),
    mkk: yup.boolean().oneOf([true], t('customer.validation.mkkRequired')),
  }).required();

  const [popup, setPopup] = useState<{ message: string; type: "success" | "error" } | null>(null);
  const popupTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const [showUsersTable, setShowUsersTable] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const users = [
    { id: 1, ad: "Ali", soyad: "Veli", telefon: "1234567890", email: "ali@example.com", adres: "Abdurrahman Nafiz Gürman mah. Gülbayır sok. no 14" },
    { id: 2, ad: "Ayşe", soyad: "Demir", telefon: "0987654321", email: "ayse@example.com", adres: "Ankara" },
    { id: 3, ad: "Mehmet", soyad: "Yılmaz", telefon: "05443332211", email: "mehmet@example.com", adres: "İzmir" }
  ];
  const filteredUsers = users.filter(user =>
    Object.values(user).some(value =>
      String(value).toLowerCase().includes(searchTerm.toLowerCase())
    )
  );

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    resolver: yupResolver(schema),
  });

  const showPopup = (message: string, type: "success" | "error") => {
    setPopup({ message, type });
    if (popupTimeoutRef.current) clearTimeout(popupTimeoutRef.current);
    popupTimeoutRef.current = setTimeout(() => setPopup(null), 3000);
  };

  const onSubmit = (data: any) => {
    showPopup(t('customer.individual.messages.success'), "success");
    reset();
  };

  const onError = (errors: any) => {
    const firstErrorField = Object.keys(errors)[0];
    if (firstErrorField) {
      const message = errors[firstErrorField]?.message;
      if (message) {
        showPopup(message, "error");
      }
    }
  };

  const handleClear = () => {
    reset();
  };

  const scrollToUserList = () => {
    const el = document.getElementById("userListSection");
    if (el) el.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <div className={styles.container}>
      <div className={styles.tabContainer}>
        <button
          className={`${styles.tabButton} ${styles.activeTab}`}
          onClick={() => router.push("/dashboard/bireysel")}
          disabled
        >
          {t('customer.individual.tabs.individual')}
        </button>
        <button
          className={styles.tabButton}
          onClick={() => router.push("/dashboard/kurumsal")}
        >
          {t('customer.individual.tabs.corporate')}
        </button>
      </div>

      <div className={styles.topButtons}>
        <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
          <img src="/menu-icon/back.png" alt={t('customer.individual.buttons.back')} className={styles.icon} />
          {t('customer.individual.buttons.back')}
        </button>

        <button type="button" className={styles.secondaryButton} onClick={handleClear}>
          <img src="/menu-icon/clear.png" alt={t('customer.individual.buttons.clear')} className={styles.icon} />
          {t('customer.individual.buttons.clear')}
        </button>

        <button
          type="button"
          className={styles.secondaryButton}
          onClick={() => {
            setShowUsersTable(!showUsersTable);
            setTimeout(() => scrollToUserList(), 100);
          }}
        >
          <img src="/menu-icon/persons.png" alt={t('customer.individual.buttons.listUsers')} className={styles.icon} />
          {t('customer.individual.buttons.listUsers')}
        </button>
      </div>

      <form
        className={styles.form}
        noValidate
        onSubmit={handleSubmit(onSubmit, onError)}
      >
        <h2 className={styles.formTitle}>{t('customer.individual.title')}</h2>

        {/* TCKN */}
        <div className={styles.formGroup}>
          <label className={styles.label}>
            {t('customer.individual.form.tckn')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
          </label>
          <input
            type="text"
            maxLength={11}
            {...register("tckn")}
            className={styles.input}
          />
        </div>

        {/* Ad Soyad */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t('customer.individual.form.firstName')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </label>
            <input
              type="text"
              maxLength={30}
              {...register("ad")}
              className={styles.input}
            />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t('customer.individual.form.lastName')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </label>
            <input
              type="text"
              maxLength={30}
              {...register("soyad")}
              className={styles.input}
            />
          </div>
        </div>

        {/* E-posta Telefon */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t('customer.individual.form.email')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </label>
            <input
              type="email"
              {...register("email")}
              className={styles.input}
            />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t('customer.individual.form.phone')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </label>
            <input
              type="text"
              maxLength={10}
              {...register("telefon")}
              className={styles.input}
            />
          </div>
        </div>

        {/* Adres */}
        <div className={styles.formGroup}>
          <label className={styles.label}>
            {t('customer.individual.form.address')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
          </label>
          <textarea
            rows={3}
            maxLength={400}
            {...register("adres")}
            className={styles.textarea}
          />
        </div>

        {/* Checkboxlar */}
        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="uygunluk" {...register("uygunluk")} />
          <label htmlFor="uygunluk" className={styles.uygunlukLabel}>
            {t('customer.individual.form.suitabilityTest')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
          </label>
        </div>

        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="kvkk" {...register("kvkk")} />
          <label htmlFor="kvkk">
            <a href="/kvkk-aydinlatma.pdf" target="_blank" rel="noopener noreferrer">
              {t('customer.individual.form.kvkkText')}
              <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </a>
          </label>
        </div>

        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="mkk" {...register("mkk")} />
          <label htmlFor="mkk">
            <a href="/mkk-onay-metni.pdf" target="_blank" rel="noopener noreferrer">
              {t('customer.individual.form.mkkText')}
              <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </a>
          </label>
        </div>

        <button type="submit" className={styles.submitButton}>
          {t('customer.individual.buttons.save')}
        </button>
      </form>

      {popup && (
        <div
          className={`${styles.popup} ${popup.type === "success" ? styles.popupSuccess : styles.popupError
            }`}
          role="alert"
          aria-live="assertive"
        >
          {popup.message}
        </div>
      )}

      {showUsersTable && (
        <div className={styles.userListContainer} id="userListSection">
          <div className={styles.userListHeader}>
            <h3 className={styles.userListTitle}>{t('customer.individual.userList.title')}</h3>
            <input
              type="text"
              value={searchTerm}
              placeholder={t('customer.individual.userList.searchPlaceholder')}
              onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
          </div>

          <table className={styles.userTable}>
            <thead>
              <tr>
                <th>{t('customer.individual.userList.headers.id')}</th>
                <th>{t('customer.individual.userList.headers.firstName')}</th>
                <th>{t('customer.individual.userList.headers.lastName')}</th>
                <th>{t('customer.individual.userList.headers.phone')}</th>
                <th>{t('customer.individual.userList.headers.email')}</th>
                <th>{t('customer.individual.userList.headers.address')}</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length > 0 ? (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.ad}</td>
                    <td>{user.soyad}</td>
                    <td>{user.telefon}</td>
                    <td>{user.email}</td>
                    <td>{user.adres || "-"}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className={styles.noData}>
                    {t('customer.individual.userList.noData')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
