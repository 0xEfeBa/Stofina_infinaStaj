"use client";
import { Quicksand } from "next/font/google";
import { useSearchParams, useRouter } from "next/navigation";
import { useState, FormEvent } from "react";
import axios from "axios";
import styles from "./CreatePasswordPage.module.css";

const quicksand = Quicksand({ subsets: ["latin"], weight: ["400", "600", "700"] });

export default function CreatePasswordPage() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const router = useRouter();

  const [newPassword, setNewPassword] = useState<string>("");
  const [confirmPassword, setConfirmPassword] = useState<string>("");

  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false);

  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+[\]{};':"\\|,.<>/?]).{8,16}$/;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!newPassword || !confirmPassword) {
      setError("Lütfen tüm alanları doldurun.");
      return;
    }

    if (!passwordRegex.test(newPassword)) {
      setError("Şifre belirtilen kurallara uymuyor.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("Şifreler eşleşmiyor.");
      return;
    }

    try {
      await axios.put("http://localhost:9002/api/v1/users/create-password", {
        token,
        newPassword,
      });

      setSuccess("Şifre başarıyla oluşturuldu!");
      setNewPassword("");
      setConfirmPassword("");

      setTimeout(() => {
        router.push("/login");
      }, 1000);
    } catch (err) {
      console.error(err);
      setError("Şifre oluştururken hata oluştu.");
    }
  };

  const validations = [
    { label: "En az 1 büyük harf", valid: /[A-Z]/.test(newPassword) },
    { label: "En az 1 küçük harf", valid: /[a-z]/.test(newPassword) },
    { label: "En az 1 rakam", valid: /\d/.test(newPassword) },
    { label: "En az 1 özel karakter", valid: /[!@#$%^&*()_+[\]{};':"\\|,.<>/?]/.test(newPassword) },
    { label: "En az 8, en fazla 16 karakter", valid: /^.{8,16}$/.test(newPassword) },
  ];

  return (
    <div className={`${quicksand.className} ${styles.container}`}>
      <div className={styles.card}>
        <h1 className={styles.title}>Create New Password</h1>

        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.inputWrapper}>
            <input
              type={showPassword ? "text" : "password"}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Enter your new password"
              className={styles.input}
            />
            <img
              src={showPassword ? "/eye.png" : "/eye-off.png"}
              alt="Toggle visibility"
              className={styles.eyeIcon}
              onClick={() => setShowPassword((prev) => !prev)}
            />
          </div>

          <div className={styles.inputWrapper}>
            <input
              type={showConfirmPassword ? "text" : "password"}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirm your new password"
              className={styles.input}
            />
            <img
              src={showConfirmPassword ? "/eye.png" : "/eye-off.png"}
              alt="Toggle visibility"
              className={styles.eyeIcon}
              onClick={() => setShowConfirmPassword((prev) => !prev)}
            />
          </div>

          {/* Şifre kuralları */}
          <ul className={styles.rules}>
            {validations.map((rule, idx) => (
              <li key={idx} className={rule.valid ? styles.valid : styles.invalid}>
                {rule.label}
              </li>
            ))}
          </ul>

          <button type="submit" className={styles.submitButton}>
            Create
          </button>

          {error && <p className={`${styles.message} ${styles.error}`}>{error}</p>}
          {success && <p className={`${styles.message} ${styles.success}`}>{success}</p>}
        </form>
      </div>
    </div>
  );
}
