"use client";

import { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import styles from "./KurumsalMüşteri.module.css";
import { useTranslation } from 'react-i18next';

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import { thunkCustomer } from "@/thunks/customerThunk";
import { useDispatchCustom } from "@/hooks/useDispatchCustom";
import { useSelectorCustom } from "@/store";
import { CorporateCustomer } from "@/types/customer";

export default function Page() {
  const router = useRouter();
  const { t } = useTranslation();

  const dispatch = useDispatchCustom();

  const schema = yup.object({
    tradeName: yup
      .string()
      .required(t('customer.corporate.validation.tradeNameRequired'))
      .min(3, t('customer.corporate.validation.tradeNameMin'))
      .max(100, t('customer.corporate.validation.tradeNameMax')),

    tradeRegistryNumber: yup
      .string()
      .required(t('customer.corporate.validation.tradeRegistryRequired'))
      .matches(/^[1-9]\d{4,9}$/, t('customer.corporate.validation.tradeRegistryFormat')),

    taxNumber: yup
      .string()
      .required(t('customer.corporate.validation.taxNumberRequired'))
      .matches(/^\d{10}$/, t('customer.corporate.validation.taxNumberFormat')),

    taxOffice: yup
      .string()
      .required(t('customer.corporate.validation.taxOfficeRequired'))
      .min(5, t('customer.corporate.validation.taxOfficeMin'))
      .max(100, t('customer.corporate.validation.taxOfficeMax')),

    legalAddress: yup
      .string()
      .required(t('customer.corporate.validation.legalAddressRequired'))
      .min(10, t('customer.corporate.validation.legalAddressMin'))
      .max(300, t('customer.corporate.validation.legalAddressMax')),

    representativeName: yup
      .string()
      .required(t('customer.corporate.validation.authorizedRequired'))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]{3,50}$/, t('customer.corporate.validation.authorizedFormat')),

    representativeTckn: yup
      .string()
      .required(t('customer.corporate.validation.authorizedTcknRequired'))
      .matches(/^[1-9]\d{10}$/, t('customer.corporate.validation.authorizedTcknFormat')),

    representativePhone: yup
      .string()
      .required(t('customer.corporate.validation.authorizedPhoneRequired'))
      .matches(/^[5]\d{9}$/, t('customer.corporate.validation.authorizedPhoneFormat')),

    representativeEmail: yup
      .string()
      .required(t('customer.corporate.validation.authorizedEmailRequired'))
      .matches(/^[^\sçÇğĞıİöÖşŞüÜ]+@[^\sçÇğĞıİöÖşŞüÜ]+\.[^\sçÇğĞıİöÖşŞüÜ]+$/, t('customer.corporate.validation.authorizedEmailFormat')),

    uygunluk: yup.boolean().oneOf([true], t('customer.corporate.validation.suitabilityRequired')),
    mkk: yup.boolean().oneOf([true], t('customer.corporate.validation.mkkRequired')),
    kvkk: yup.boolean().oneOf([true], t('customer.corporate.validation.kvkkRequired')),

    representativeNameOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]{3,50}$/, t('customer.corporate.validation.authorizedFormat'))
      .notRequired(),

    representativeTcknOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[1-9]\d{10}$/, t('customer.corporate.validation.authorizedTcknFormat'))
      .notRequired(),

    representativePhoneOps: yup
      .string()
      .nullable()
      .transform((value) => (value === "" ? null : value))
      .matches(/^[5]\d{9}$/, t('customer.corporate.validation.authorizedPhoneFormat'))
      .notRequired(),

    representativeEmailOps: yup
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
  const { corporateCustomers } = useSelectorCustom(state => state.customer);

  const filteredUsers = corporateCustomers?.filter(corporateCustomers =>
    Object.values(corporateCustomers)?.some(value =>
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

  const onSubmit = async (data: any) => {
    //Yeni kurumsal müşteri oluşturulduğunda hesap açılıyor.
    var result = await dispatch(thunkCustomer.createCorporateCustomer({
      tradeName: data.tradeName,
      tradeRegistryNumber: data.tradeRegistryNumber,
      taxNumber: data.taxNumber,
      taxOffice: data.taxOffice,
      representativeName: data.representativeName,
      representativeTckn: data.representativeTckn,
      representativePhone: data.representativePhone,
      representativeEmail: data.representativeEmail,
      customer: {
        legalAddress: data.legalAddress,
      },
    }));
    if (result) {
      fetchCorporateCustomers();
      showPopup(t('customer.corporate.messages.success'), "success");
      reset();
    } else {
      showPopup(t('customer.corporate.messages.error'), "error");
    }
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

  const fetchCorporateCustomers = async () => {
    await dispatch(thunkCustomer.getCorporateCustomers());
  }

  useEffect(() => {
    fetchCorporateCustomers();
  }, []);

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

        {/* Ticaret Unvanı */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.tradeName')} {t('customer.corporate.form.required')}</label>
            <input {...register("tradeName")} className={styles.input} />
          </div>
          {/* Ticaret Sicil No */}
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.tradeRegistryNo')} {t('customer.corporate.form.required')}</label>
            <input {...register("tradeRegistryNumber")} className={styles.input} />
          </div>
        </div>

        {/* Vergi No */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.taxNumber')} {t('customer.corporate.form.required')}</label>
            <input {...register("taxNumber")} className={styles.input} />
          </div>
          {/* Vergi Dairesi */}
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.taxOffice')} {t('customer.corporate.form.required')}</label>
            <input {...register("taxOffice")} className={styles.input} />
          </div>
        </div>

        {/* Yetkili */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPerson')} {t('customer.corporate.form.required')}</label>
            <input {...register("representativeName")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPersonOptional')}</label>
            <input {...register("representativeNameOps")} className={styles.input} />
          </div>
        </div>

        {/* Yetkili TCKN */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedTckn')} {t('customer.corporate.form.required')}</label>
            <input maxLength={11} {...register("representativeTckn")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedTcknOptional')}</label>
            <input maxLength={11} {...register("representativeTcknOps")} className={styles.input} />
          </div>
        </div>

        {/* Yetkili Telefon */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPhone')} {t('customer.corporate.form.required')}</label>
            <input maxLength={10} {...register("representativePhone")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedPhoneOptional')}</label>
            <input maxLength={10} {...register("representativePhoneOps")} className={styles.input} />
          </div>
        </div>

        {/* Yetkili Email */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedEmail')} {t('customer.corporate.form.required')}</label>
            <input type="email" {...register("representativeEmail")} className={styles.input} />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>{t('customer.corporate.form.authorizedEmailOptional')}</label>
            <input type="email" {...register("representativeEmailOps")} className={styles.input} />
          </div>
        </div>

        {/* Yasal Adres */}
        <div className={styles.formGroup}>
          <label className={styles.label}>{t('customer.corporate.form.legalAddress')} {t('customer.corporate.form.required')}</label>
          <textarea rows={3} {...register("legalAddress")} className={styles.textarea} />
        </div>

        {/* Uygunluk Testi */}
        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="uygunluk" {...register("uygunluk")} />
          <label htmlFor="uygunluk">{t('customer.corporate.form.suitabilityTest')} <span className={styles.required}>{t('customer.corporate.form.required')}</span></label>
        </div>

        {/* MKK */}
        <div className={styles.checkboxGroup}>
          <input type="checkbox" id="mkk" {...register("mkk")} />
          <label htmlFor="mkk">{t('customer.corporate.form.mkkText')} <span className={styles.required}>{t('customer.corporate.form.required')}</span></label>
        </div>

        {/* KVKK */}
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
              {filteredUsers && filteredUsers.length > 0 ? (
                filteredUsers.map((user: CorporateCustomer) => (
                  <tr key={user.customer.id}>
                    <td>{user.customer.id}</td>
                    <td>{user.tradeName}</td>
                    <td>{user.taxNumber}</td>
                    <td>{user.representativeName}</td>
                    <td>{user.representativePhone}</td>
                    <td>{user.representativeEmail}</td>
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
