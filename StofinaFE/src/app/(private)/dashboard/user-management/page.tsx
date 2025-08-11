"use client";
import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { useTranslation } from "next-i18next";
import styles from "./UserForm.module.css";

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

export default function Page() {
  const { t } = useTranslation("common");
  const router = useRouter();

  const [showUsersTable, setShowUsersTable] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [users, setUsers] = useState<any[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  const tableRef = useRef<HTMLDivElement | null>(null);

  const [popup, setPopup] = useState<{ message: string; type: "success" | "error" } | null>(null);
  const popupTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const filteredUsers = users.filter(user =>
    Object.values(user).some(value =>
      String(value).toLowerCase().includes(searchTerm.toLowerCase())
    )
  );

  const schema = yup.object({
    ad: yup
      .string()
      .required(t("userManagement.messages.validationErrors.firstNameRequired"))
      .max(30, t("userManagement.messages.validationErrors.firstNameMaxLength"))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]+$/, t("userManagement.messages.validationErrors.firstNameFormat")),
    soyad: yup
      .string()
      .required(t("userManagement.messages.validationErrors.lastNameRequired"))
      .max(30, t("userManagement.messages.validationErrors.lastNameMaxLength"))
      .matches(/^[a-zA-ZçğıöşüÇĞİÖŞÜ\s]+$/, t("userManagement.messages.validationErrors.lastNameFormat")),
    telefon: yup
      .string()
      .required(t("userManagement.messages.validationErrors.phoneRequired"))
      .matches(/^[1-9]\d{9}$/, t("userManagement.messages.validationErrors.phoneFormat")),
    email: yup
      .string()
      .required(t("userManagement.messages.validationErrors.emailRequired"))
      .email(t("userManagement.messages.validationErrors.emailFormat")),
    unvan: yup.string().required(t("userManagement.messages.validationErrors.titleRequired")),
    yetki: yup.string().required(t("userManagement.messages.validationErrors.authorityRequired")),
  }).required();

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

  const fetchUsers = async () => {
    try {
      setLoadingUsers(true);
      const token = localStorage.getItem("accessToken");
      if (!token) {
        showPopup("Token bulunamadı", "error");
        return;
      }

      const response = await fetch("http://localhost:9002/api/v1/users", {
        method: "GET",
        headers: {
          "accept": "application/json",
          "Authorization": `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          showPopup("Token süresi dolmuş", "error");
          router.push("/login");
          return;
        }
        throw new Error(`Kullanıcılar getirilemedi: ${response.status}`);
      }

      const userData = await response.json();
      console.log("Fetched users:", userData);
      
      const formattedUsers = userData.map((user: any) => ({
        id: user.id,
        ad: user.firstName,
        soyad: user.lastName,
        telefon: user.phoneNumber,
        email: user.email,
        unvan: user.title,
        yetki: user.roleTypes?.[0] || "USER"
      }));
      
      setUsers(formattedUsers);
    } catch (error) {
      console.error("Fetch users error:", error);
      showPopup("Kullanıcılar yüklenirken hata oluştu", "error");
    } finally {
      setLoadingUsers(false);
    }
  };

  const onSubmit = async (data: any) => {
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        showPopup(t("userManagement.messages.authRequired"), "error");
        return;
      }

      let mappedRole = "CUSTOMER_TRADER";
      if (data.yetki === "admin") {
        mappedRole = "CUSTOMER_SUPER_ADMIN";
      } else if (data.yetki === "trader") {
        mappedRole = "CUSTOMER_TRADER";
      }

      const response = await fetch("http://localhost:9002/api/v1/users/create-user", {
        method: "POST",
        headers: {
          "accept": "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          firstName: data.ad,
          lastName: data.soyad,
          title: data.unvan,
          phoneNumber: data.telefon,
          email: data.email,
          roleTypes: [mappedRole],
        }),
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        console.error("API Error:", errorData);
        
        if (response.status === 401) {
          showPopup(t("userManagement.messages.tokenExpired"), "error");
          router.push("/login");
          return;
        }
        
        throw new Error(`Kullanıcı oluşturma başarısız: ${response.status}`);
      }
      
      showPopup(t("userManagement.messages.formSubmitted"), "success");
      reset();
      if (showUsersTable) {
        fetchUsers();
      }
    } catch (error) {
      console.error("Create user error:", error);
      showPopup(t("userManagement.messages.createUserError"), "error");
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

  return (
    <div className={styles.container}>

      <div className={styles.topButtons}>
        <button type="button" className={styles.secondaryButton} onClick={() => router.back()}>
          <img src="/menu-icon/back.png" alt={t("common.back")} className={styles.icon} />
          {t("common.back")}
        </button>

        <button type="button" className={styles.secondaryButton} onClick={handleClear}>
          <img src="/menu-icon/clear.png" alt={t("common.buttons.clear")} className={styles.icon} />
          {t("common.buttons.clear")}
        </button>

        <button
          type="button"
          className={styles.secondaryButton}
          onClick={() => {
            setShowUsersTable(true);
            fetchUsers();
            setTimeout(() => {
              tableRef.current?.scrollIntoView({ behavior: "smooth" });
            }, 100);
          }}
        >
          <img src="/menu-icon/persons.png" alt={t("userManagement.buttons.listUsers")} className={styles.icon} />
          {t("userManagement.buttons.listUsers")}
        </button>
      </div>

      <form
        className={styles.form}
        onSubmit={handleSubmit(onSubmit, onError)}
        noValidate
      >
        <h2 className={styles.formTitle}>{t("userManagement.title")}</h2>

        {/* Ad Soyad */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t("userManagement.form.firstName")} <span className={styles.required}>{t("userManagement.form.required")}</span>
            </label>
            <input
              type="text"
              {...register("ad")}
              className={styles.input}
            />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t("userManagement.form.lastName")} <span className={styles.required}>{t("userManagement.form.required")}</span>
            </label>
            <input
              type="text"
              {...register("soyad")}
              className={styles.input}
            />
          </div>
        </div>
        {/* E-posta ve Telefon */}
        <div className={styles.row}>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t("userManagement.form.email")} <span className={styles.required}>{t("userManagement.form.required")}</span>
            </label>
            <input
              type="email"
              {...register("email")}
              className={styles.input}
            />
          </div>
          <div className={styles.formGroupRow}>
            <label className={styles.label}>
              {t("userManagement.form.phone")} <span className={styles.required}>{t("userManagement.form.required")}</span>
            </label>
            <input
              type="tel"
              {...register("telefon")}
              className={styles.input}
            />
          </div>
        </div>

        {/* Ünvan */}
        <div className={styles.formGroup}>
          <label className={styles.label}>
            {t("userManagement.form.title")} <span className={styles.required}>{t("userManagement.form.required")}</span>
          </label>
          <select {...register("unvan")} className={styles.select} defaultValue="">
            <option value="" disabled>{t("userManagement.form.titleSelect")}</option>
            <option value="muhendis">{t("userManagement.form.titleOptions.engineer")}</option>
            <option value="uzman">{t("userManagement.form.titleOptions.expert")}</option>
            <option value="yonetici">{t("userManagement.form.titleOptions.manager")}</option>
          </select>
        </div>

        {/* Yetki */}
        <div className={styles.formGroup}>
          <label className={styles.label}>
            {t("userManagement.form.authority")} <span className={styles.required}>{t("userManagement.form.required")}</span>
          </label>
          <select {...register("yetki")} className={styles.select} defaultValue="">
            <option value="" disabled>{t("userManagement.form.authoritySelect")}</option>
            <option value="admin">{t("userManagement.form.authorityOptions.admin")}</option>
            <option value="trader">{t("userManagement.form.authorityOptions.trader")}</option>
          </select>
        </div>

        <button type="submit" className={styles.submitButton}>{t("userManagement.buttons.save")}</button>
      </form>

      {popup && (
        <div
          className={`${styles.popup} ${popup.type === "success" ? styles.popupSuccess : styles.popupError}`}
          role="alert"
          aria-live="assertive"
        >
          {popup.message}
        </div>
      )}

      {showUsersTable && (
        <div ref={tableRef} className={styles.userListContainer}>
          <div className={styles.userListHeader}>
            <h3 className={styles.userListTitle}>{t("userManagement.userList.title")}</h3>
            <div className={styles.userListActions}>
              <input
                type="text"
                placeholder={t("userManagement.userList.searchPlaceholder")}
                className={styles.searchInput}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <button
                type="button"
                className={styles.refreshButton}
                onClick={fetchUsers}
                disabled={loadingUsers}
              >
                {loadingUsers ? "Yükleniyor..." : "Yenile"}
              </button>
            </div>
          </div>

          {loadingUsers ? (
            <div className={styles.loadingContainer}>
              <p>Kullanıcılar yükleniyor...</p>
            </div>
          ) : (
            <table className={styles.userTable}>
              <thead>
                <tr>
                  <th>{t("userManagement.userList.headers.id")}</th>
                  <th>{t("userManagement.userList.headers.firstName")}</th>
                  <th>{t("userManagement.userList.headers.lastName")}</th>
                  <th>{t("userManagement.userList.headers.phone")}</th>
                  <th>{t("userManagement.userList.headers.email")}</th>
                  <th>{t("userManagement.userList.headers.title")}</th>
                  <th>{t("userManagement.userList.headers.authority")}</th>
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
                      <td>{user.unvan}</td>
                      <td>{user.yetki}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={7} className={styles.noData}>
                      {users.length === 0 ? "Kullanıcı bulunamadı" : t("userManagement.userList.noData")}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}