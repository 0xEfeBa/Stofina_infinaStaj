"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Quicksand } from "next/font/google";
import styles from "./LoginPage.module.css";

const quicksand = Quicksand({ subsets: ["latin"], weight: ["400", "600", "700"] });

export default function LoginPage() {
  const router = useRouter();
  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState("");

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setSuccessMsg("");

    if (!username.trim()) {
      setError("Kullanıcı adı boş bırakılamaz");
      return;
    }

    if (!password) {
      setError("Parola boş bırakılamaz");
      return;
    }

    if (username === "admin" && password === "1234") {
      setLoading(true);
      setSuccessMsg("Giriş başarılı, yönlendiriliyorsunuz...");

      setTimeout(() => {
        router.push("/dashboard");
      }, 2000);
    } else {
      setError("Kullanıcı adı veya şifre yanlış");
    }
  };

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

      <div className={styles.right}>
        <div className={styles.formContainer}>
          <h2 className={styles.welcome}>HOŞ GELDİNİZ</h2>
          <p className={styles.loginTitle}>KULLANICI GİRİŞ EKRANI</p>

          <form className={styles.form} onSubmit={handleSubmit}>
            <input
              type="text"
              placeholder="Kullanıcı Adı"
              className={styles.input}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />

            <div className={styles.passwordWrapper}>
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Parolanızı Giriniz"
                className={styles.input}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
              />
              <img
                src={showPassword ? "/eye.png" : "/eye-off.png"}
                alt="şifre göster/gizle"
                className={styles.eyeIcon}
                onClick={() => setShowPassword(!showPassword)}
                style={{ cursor: "pointer" }}
              />
            </div>

            <button type="submit" className={styles.button} disabled={loading}>
              {loading ? "Giriş Yapılıyor..." : "GİRİŞ YAP"}
            </button>

            {error && <p style={{ color: "red"}}>{error}</p>}
            {successMsg && <p style={{ color: "green"}}>{successMsg}</p>}

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
