"use client";

import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import styles from "./KurumsalMüşteri.module.css";
import { useTranslation } from 'react-i18next';

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

export default function Page() {
  const router = useRouter();
  const { t } = useTranslation();

  const schema = yup.object({
    ticaretUnvani: yup
      .string()
      .required(t('customer.corporate.validation.tradeNameRequired'))
      .min(3, t('customer.corporate.validation.tradeNameMin'))
      .max(100, t('customer.corporate.validation.tradeNameMax')),

    ticaretSicilNo: yup
      .string()
      .required(t('customer.corporate.validation.tradeRegistryRequired'))
      .matches(/^[1-9]\d{4,9}$/, t('customer.corporate.validation.tradeRegistryFormat')),

    vergiNo: yup
      .string()
      .required(t('customer.corporate.validation.taxNumberRequired'))
      .matches(/^\d{10}$/, t('customer.corporate.validation.taxNumberFormat')),

    vergiDairesi: yup
      .string()
      .required(t('customer.corporate.validation.taxOfficeRequired'))
      .min(5, t('customer.corporate.validation.taxOfficeMin'))
      .max(100, t('customer.corporate.validation.taxOfficeMax')),

    yasalAdres: yup
      .string()
      .required(t('customer.corporate.validation.legalAddressRequired'))
      .min(10, t('customer.corporate.validation.legalAddressMin'))
      .max(300, t('customer.corporate.validation.legalAddressMax')),

    yetkili: yup
      .string()
      .required(t('customer.corporate.validation.authorizedRequired'))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]{3,50}$/, t('customer.corporate.validation.authorizedFormat')),

    yetkiliTckn: yup
      .string()
      .required(t('customer.corporate.validation.authorizedTcknRequired'))
      .matches(/^[1-9]\d{10}$/, t('customer.corporate.validation.authorizedTcknFormat')),

    yetkiliTelefon: yup
      .string()
      .required(t('customer.corporate.validation.authorizedPhoneRequired'))
      .matches(/^[5]\d{9}$/, t('customer.corporate.validation.authorizedPhoneFormat')),

    yetkiliEmail: yup
      .string()
      .required(t('customer.corporate.validation.authorizedEmailRequired'))
      .matches(/^[^\sçÇğĞıİöÖşŞüÜ]+@[^\sçÇğĞıİöÖşŞüÜ]+\.[^\sçÇğĞıİöÖşŞüÜ]+$/, t('customer.corporate.validation.authorizedEmailFormat')),

    uygunluk: yup.boolean().oneOf([true], t('customer.corporate.validation.suitabilityRequired')),
    mkk: yup.boolean().oneOf([true], t('customer.corporate.validation.mkkRequired')),
    kvkk: yup.boolean().oneOf([true], t('customer.corporate.validation.kvkkRequired')),

    yetkiliOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]{3,50}$/, t('customer.corporate.validation.authorizedFormat'))
      .notRequired(),

    yetkiliTcknOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[1-9]\d{10}$/, t('customer.corporate.validation.authorizedTcknFormat'))
      .notRequired(),

    yetkiliTelefonOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[5]\d{9}$/, t('customer.corporate.validation.authorizedPhoneFormat'))
      .notRequired(),

    yetkiliEmailOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[^\sçÇğĞıİöÖşŞüÜ]+@[^\sçÇğĞıİöÖşŞüÜ]+\.[^\sçÇğĞıİöÖşŞüÜ]+$/, t('customer.corporate.validation.authorizedEmailFormat'))
      .notRequired(),
  }).required();

  const [popup, setPopup] = useState<{ message: string; type: "success" | "error" } | null>(null);
  const popupTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const [showUsersTable, setShowUsersTable] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const corporateUsers = [
    {
      id: 1,
      ticaretUnvani: "OpenAI Yazılım A.Ş.",
      vkn: "1234567890",
      yetkili: "Ali Veli",
      yetkiliTelefon: "5321112233",
      yetkiliEmail: "ali@openai.com"
    },
    {
      id: 2,
      ticaretUnvani: "Biblioteca Teknoloji Ltd.",
      vkn: "9876543210",
      yetkili: "Ayşe Demir",
      yetkiliTelefon: "5332223344",
      yetkiliEmail: "ayse@biblioteca.com"
    }
  ];

  const filteredUsers = corporateUsers.filter(user =>
    Object.values(user).some(value =>
      String(value).toLowerCase().includes(searchTerm.toLowerCase())
    )
  );

  const { register, handleSubmit, reset } = useForm({
    resolver: yupResolver(schema),
  });

  const showPopup = (message: string, type: "success" | "error") => {
    setPopup({ message, type });
    if (popupTimeoutRef.current) clearTimeout(popupTimeoutRef.current);
    popupTimeoutRef.current = setTimeout(() => setPopup(null), 3000);
  };

  const onSubmit = (data: any) => {
    showPopup(t('customer.corporate.messages.success'), "success");
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
    const el = document.getElementById("corporateUserList");
    if (el) el.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <div className={styles.container}>
      <div className={styles.tabContainer}>
        <button className={styles.tabButton} onClick={() => router.push("/dashboard/bireysel")}>{t('customer.corporate.tabs.individual')}</button>
        <button className={`${styles.tabButton} ${styles.activeTab}`} onClick={() => router.push("/dashboard/kurumsal")}>{t('customer.corporate.tabs.corporate')}</button>
      </div>

      <div className={styles.topButtons}>
        <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
          <img src="/menu-icon/back.png" alt={t('customer.corporate.buttons.back')} className={styles.icon} /> {t('customer.corporate.buttons.back')}
        </button>

        <button type="button" className={styles.secondaryButton} onClick={handleClear}>
          <img src="/menu-icon/clear.png" alt={t('customer.corporate.buttons.clear')} className={styles.icon} /> {t('customer.corporate.buttons.clear')}
        </button>

        <button
          type="button"
          className={styles.secondaryButton}
          onClick={() => {
            setShowUsersTable(!showUsersTable);
            setTimeout(() => scrollToUserList(), 100);
          }}
        >
          <img src="/menu-icon/persons.png" alt={t('customer.corporate.buttons.listUsers')} className={styles.icon} /> {t('customer.corporate.buttons.listUsers')}
        </button>
      </div>

      <form className={styles.form} noValidate onSubmit={handleSubmit(onSubmit, onError)}>
        <h2 className={styles.formTitle}>{t('customer.corporate.title')}</h2>

        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.tradeName')} {t('customer.corporate.form.required')}</label>
            <input {...register("ticaretUnvani")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.tradeRegistryNo')} {t('customer.corporate.form.required')}</label>
            <input {...register("ticaretSicilNo")} className={styles.input} />
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.taxNumber')} {t('customer.corporate.form.required')}</label>
            <input {...register("vergiNo")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.taxOffice')} {t('customer.corporate.form.required')}</label>
            <input {...register("vergiDairesi")} className={styles.input} />
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPerson')} {t('customer.corporate.form.required')}</label>
            <input {...register("yetkili")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPersonOptional')}</label>
            <input {...register("yetkiliOps")} className={styles.input} />
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedTckn')} {t('customer.corporate.form.required')}</label>
            <input maxLength={11} {...register("yetkiliTckn")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedTcknOptional')}</label>
            <input maxLength={11} {...register("yetkiliTcknOps")} className={styles.input} />
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPhone')} {t('customer.corporate.form.required')}</label>
            <input maxLength={10} {...register("yetkiliTelefon")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPhoneOptional')}</label>
            <input maxLength={10} {...register("yetkiliTelefonOps")} className={styles.input} />
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedEmail')} {t('customer.corporate.form.required')}</label>
            <input type="email" {...register("yetkiliEmail")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedEmailOptional')}</label>
            <input type="email" {...register("yetkiliEmailOps")} className={styles.input} />
          </div>
        </div>

        <div className={styles.formGroup}>
          <label className={styles.label}>{t('customer.corporate.form.legalAddress')} {t('customer.corporate.form.required')}</label>
          <textarea rows={3} {...register("yasalAdres")} className={styles.textarea} />
        </div>

        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="uygunluk" {...register("uygunluk")} />
          <label htmlFor="uygunluk">{t('customer.corporate.form.suitabilityTest')} <span className={styles.required}>{t('customer.corporate.form.required')}</span></label>
        </div>

        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="mkk" {...register("mkk")} />
          <label htmlFor="mkk">{t('customer.corporate.form.mkkText')} <span className={styles.required}>{t('customer.corporate.form.required')}</span></label>
        </div>

        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="kvkk" {...register("kvkk")} />
          <label htmlFor="kvkk">{t('customer.corporate.form.kvkkText')} <span className={styles.required}>{t('customer.corporate.form.required')}</span></label>
        </div>

        <button type="submit" className={styles.submitButton}>{t('customer.corporate.buttons.save')}</button>
      </form>

      {popup && (
        <div className={`${styles.popup} ${popup.type === "success" ? styles.popupSuccess : styles.popupError}`}>
          {popup.message}
        </div>
      )}

      {showUsersTable && (
        <div className={styles.userListContainer} id="corporateUserList">
          <div className={styles.userListHeader}>
            <h3 className={styles.userListTitle}>{t('customer.corporate.userList.title')}</h3>
            <input
              type="text"
              value={searchTerm}
              placeholder={t('customer.corporate.userList.searchPlaceholder')}
              onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
          </div>

          <table className={styles.userTable}>
            <thead>
              <tr>
                <th>{t('customer.corporate.userList.headers.id')}</th>
                <th>{t('customer.corporate.userList.headers.tradeName')}</th>
                <th>{t('customer.corporate.userList.headers.vkn')}</th>
                <th>{t('customer.corporate.userList.headers.authorizedPerson')}</th>
                <th>{t('customer.corporate.userList.headers.authorizedPhone')}</th>
                <th>{t('customer.corporate.userList.headers.authorizedEmail')}</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length > 0 ? (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.ticaretUnvani}</td>
                    <td>{user.vkn}</td>
                    <td>{user.yetkili}</td>
                    <td>{user.yetkiliTelefon}</td>
                    <td>{user.yetkiliEmail}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className={styles.noData}>
                    {t('customer.corporate.userList.noData')}
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
