"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Quicksand } from "next/font/google";
import styles from "./LoginPage.module.css";
import { authService } from "@/services/authService";
import { useAuth } from "@/contexts/AuthContext";

const quicksand = Quicksand({ subsets: ["latin"], weight: ["400", "600", "700"] });

export default function LoginPage() {
  const router = useRouter();
  const { setUser } = useAuth();

  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState("");

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setSuccessMsg("");

    if (!email.trim()) {
      setError("Email boş bırakılamaz");
      return;
    }
    if (!password) {
      setError("Parola boş bırakılamaz");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch("http://localhost:9002/api/v1/auth/login", {
        method: "POST",
        headers: {
          accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        throw new Error("Email veya şifre yanlış");
      }

      const data = await res.json();

      authService.saveTokens(data.accessToken, data.refreshToken);
      authService.saveUser(data.userDto);
      setUser(data.userDto);

      setSuccessMsg("Giriş başarılı, yönlendiriliyorsunuz...");
      setTimeout(() => router.push("/dashboard"), 2000);
    } catch (err: any) {
      setError(err.message || "Giriş sırasında hata oluştu");
    } finally {
      setLoading(false);
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
              placeholder="Email"
              className={styles.input}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
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

            {error && <p style={{ color: "red" }}>{error}</p>}
            {successMsg && <p style={{ color: "green" }}>{successMsg}</p>}

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
