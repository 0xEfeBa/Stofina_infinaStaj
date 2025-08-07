"use client";
import { useState } from "react";
import { Quicksand } from "next/font/google";
import styles from "./LoginPage.module.css";

const quicksand = Quicksand({ subsets: ["latin"], weight: ["400", "600", "700"] });

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div
      className={`${styles.container} ${quicksand.className}`}
      style={{ backgroundImage: "url('/login_bg.png')" }}
    >
      <div className={styles.left}>
        <div className={styles.logoWrapper}>
          <img src="/logo.png" alt="Stofina Refleks" className={styles.logo} />
          <h1 className={styles.title}>
            <span>STOFINA</span>
            <span>REFLEKS</span>
          </h1>
          <p className={styles.subtitle}>financial web application</p>
        </div>
      </div>

      {/* SAĞ TARAF - FORM */}
      <div className={styles.right}>
        <div className={styles.formContainer}>
          <h2 className={styles.welcome}>HOŞ GELDİNİZ</h2>
          <p className={styles.loginTitle}>KULLANICI GİRİŞ EKRANI</p>

          <form className={styles.form}>
            {/* Kullanıcı Adı */}
            <input type="text" placeholder="Kullanıcı Adı" className={styles.input} />

            {/* Şifre */}
            <div className={styles.passwordWrapper}>
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Parolanızı Giriniz"
                className={styles.input}
              />
              <img
                src={showPassword ? "/eye.png" : "/eye-off.png"}
                alt="şifre göster/gizle"
                className={styles.eyeIcon}
                onClick={() => setShowPassword(!showPassword)}
              />
            </div>

            {/* Giriş butonu */}
            <button type="submit" className={styles.button}>
              GİRİŞ YAP
            </button>

            <a href="#" className={styles.forgotPassword}>
              PAROLAMI UNUTTUM
            </a>
          </form>

          <p className={styles.version}>
            Versiyon: 1.0 / 29 Temmuz 2025 12:27 <br />
          </p>
        </div>
      </div>
    </div>
  );
}
