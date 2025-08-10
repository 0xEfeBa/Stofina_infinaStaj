"use client";

import { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import styles from "./BireyselMüşteri.module.css";
import { useTranslation } from 'react-i18next';

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import { Lasso } from "lucide-react";
import { useDispatchCustom } from "@/hooks/useDispatchCustom";
import { useSelectorCustom } from "@/store";
import { thunkCustomer } from "@/thunks/customerThunk";
import { toast } from "sonner";
import { IndividualCustomer } from "@/types/customer";

export default function Page() {
  const router = useRouter();
  const { t } = useTranslation();
  const { individualCustomers } = useSelectorCustom(state => state.customer);

  const dispatch = useDispatchCustom();


  const schema = yup.object({
    tckn: yup
      .string()
      .required(t('customer.validation.tcknRequired'))
      .matches(/^\d{11}$/, t('customer.validation.tcknFormat')),
    firstName: yup
      .string()
      .required(t('customer.validation.nameRequired'))
      .max(30, t('customer.validation.nameMaxLength'))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]+$/, t('customer.validation.nameFormat')),
    lastName: yup
      .string()
      .required(t('customer.validation.surnameRequired'))
      .max(30, t('customer.validation.surnameMaxLength'))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]+$/, t('customer.validation.surnameFormat')),
    email: yup
      .string()
      .required(t('customer.validation.email'))
      .email(t('customer.validation.email')),
    phone: yup
      .string()
      .required(t('customer.validation.phoneRequired'))
      .matches(/^[1-9]\d{9}$/, t('customer.validation.phoneFormat')),
    legalAddress: yup
      .string()
      .required(t('customer.validation.addressRequired'))
      .max(400, t('customer.validation.addressMaxLength')),
    uygunluk: yup.boolean().oneOf([true], t('customer.validation.suitabilityRequired')),
    kvkk: yup.boolean().oneOf([true], t('customer.validation.kvkkRequired')),
    mkk: yup.boolean().oneOf([true], t('customer.validation.mkkRequired')),
  }).required();

  const [popup, setPopup] = useState<{ message: string; type: "success" | "error" } | null>(null);
  const popupTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const fetchIndividualCustomers = async () => {
    await dispatch(thunkCustomer.getIndividuals());
  }

  const [showUsersTable, setShowUsersTable] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");


  const filteredUsers = individualCustomers?.filter(individualCustomers =>
    Object.values(individualCustomers)?.some(value =>
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

  const onSubmit = async (data: any) => {
    var result = await dispatch(thunkCustomer.createIndividualCustomer({
      tckn: data.tckn,
      firstName: data.firstName,
      lastName: data.lastName,
      phone: data.phone,
      email: data.email,
      customer: {
        legalAddress: data.legalAddress,
      },
    }));
    if (result) {
      toast.success(t('customer.individual.messages.success'));
      reset();
      fetchIndividualCustomers();
    } else {
      toast.error(t('customer.individual.messages.error'));
    }

  };

  const onError = (errors: any) => {
    const firstErrorField = Object.keys(errors)[0];
    if (firstErrorField) {
      const message = errors[firstErrorField]?.message;
      if (message) {
        toast.error(message);
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

  useEffect(() => {
    fetchIndividualCustomers();
  }, []);

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
              {...register("firstName")}
              className={styles.input}
            />
          </div>
          {/* Soyad */}
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t('customer.individual.form.lastName')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </label>
            <input
              type="text"
              maxLength={30}
              {...register("lastName")}
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
          {/* Telefon */}
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t('customer.individual.form.phone')} <span className={styles.required}>{t('customer.individual.form.required')}</span>
            </label>
            <input
              type="text"
              maxLength={10}
              {...register("phone")}
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
            {...register("legalAddress")}
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
        {/* KVKK */}
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
              {filteredUsers && filteredUsers.length > 0 ? (
                filteredUsers.map((user: IndividualCustomer) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.firstName}</td>
                    <td>{user.lastName}</td>
                    <td>{user.phone}</td>
                    <td>{user.email}</td>
                    <td>{user.customer.legalAddress || "-"}</td>
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
