"use client";

import { useState, useEffect, useRef } from "react";
import { useTranslation } from "next-i18next";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import styles from "./KıymetTransferi.module.css";
import SimpleCustomerSearch from "@/components/common/SimpleCustomerSearch";
import TransferTipSelector from "@/components/common/TransferTipSelector";
import { UnifiedCustomer } from "@/types/customer";

export default function TransferPage() {
	const { t } = useTranslation("common");

	const [transferCategory, setTransferCategory] = useState("HESAPLAR_ARASI");
	const [selectedCustomer, setSelectedCustomer] = useState<UnifiedCustomer | null>(null);
	const [receiverCustomer, setReceiverCustomer] = useState<UnifiedCustomer | null>(null);

	const [transferType, setTransferType] = useState<"NAKIT" | "HISSE">("NAKIT");
	const [accounts, setAccounts] = useState<any[]>([]);
	const [receiverAccounts, setReceiverAccounts] = useState<any[]>([]);

	const popupTimeoutRef = useRef<NodeJS.Timeout | null>(null);
	const [popup, setPopup] = useState<{ message: string; type: "success" | "error" } | null>(null);

	const schema = yup.object({
		senderAccount: yup.string().required("Lütfen gönderici hesabını seçiniz."),
		receiverAccount: yup.string().required("Lütfen alıcı hesabını seçiniz."),
		amount: yup
			.number()
			.transform((value, originalValue) => {
				return originalValue === "" || originalValue == null ? undefined : value;
			})
			.when("transferType", {
				is: "NAKIT",
				then: (schema) =>
					schema
						.required("Lütfen tutarı giriniz.")
						.positive("Tutar pozitif bir değer olmalıdır.")
						.min(0.01, "Tutar en az 0.01 TL olmalıdır.")
						.test("balance-check", "Yetersiz bakiye! Lütfen bakiyenizi kontrol edin.", function (value) {
							const { senderAccount } = this.parent;
							if (!senderAccount || !Array.isArray(accounts) || accounts.length === 0) return true;
							const selectedAccount = accounts.find((acc) => acc.accountNumber === senderAccount);
							if (!selectedAccount) return true;
							return value <= selectedAccount.availableBalance;
						}),
				otherwise: (schema) => schema.nullable(),
			}),
		quantity: yup
			.number()
			.transform((value, originalValue) => {
				return originalValue === "" || originalValue == null ? undefined : value;
			})
			.when("transferType", {
				is: "HISSE",
				then: (schema) =>
					schema
						.required("Lütfen miktarı giriniz.")
						.positive("Miktar pozitif bir değer olmalıdır.")
						.integer("Miktar tam sayı olmalıdır.")
						.min(1, "Miktar en az 1 olmalıdır."),
				otherwise: (schema) => schema.nullable(),
			}),
		stockCode: yup
			.string()
			.when("transferType", {
				is: "HISSE",
				then: (schema) =>
					schema
						.required("Lütfen hisse kodunu giriniz.")
						.min(3, "Hisse kodu en az 3 karakter olmalıdır.")
						.max(10, "Hisse kodu en fazla 10 karakter olmalıdır."),
				otherwise: (schema) => schema.nullable(),
			}),
		description: yup.string().nullable(),
		transferType: yup.string().required(),
	});

	const {
		register,
		handleSubmit,
		formState: { errors },
		reset,
		watch,
		setValue,
		trigger,
	} = useForm({
		resolver: yupResolver(schema),
		defaultValues: {
			senderAccount: "",
			receiverAccount: "",
			amount: undefined,
			quantity: undefined,
			stockCode: "",
			description: "",
			transferType: "NAKIT",
		},
	});

	const watchedSenderAccount = watch("senderAccount");

	const showPopup = (message: string, type: "success" | "error") => {
		setPopup({ message, type });
		if (popupTimeoutRef.current) clearTimeout(popupTimeoutRef.current);

		const timeout = type === "success" ? 4000 : 3000;
		popupTimeoutRef.current = setTimeout(() => setPopup(null), timeout);
	};

	useEffect(() => {
		if (!selectedCustomer) {
			setAccounts([]);
			if (transferCategory === "HESAPLAR_ARASI") setReceiverAccounts([]);
			reset();
			return;
		}

		fetch(`http://localhost:9001/api/v1/accounts/customer/${selectedCustomer.customer.id}`, {
			headers: {
				Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
				accept: "application/json",
			},
		})
			.then((res) => res.json())
			.then((data) => {
				const accountsArray = Array.isArray(data) ? data : [];
				setAccounts(accountsArray);

				if (transferCategory === "HESAPLAR_ARASI") {
					const filtered = accountsArray.filter((acc: any) => acc.accountNumber !== watchedSenderAccount);
					setReceiverAccounts(filtered);
				}
				reset({
					senderAccount:
						watchedSenderAccount && accountsArray.find((acc: any) => acc.accountNumber === watchedSenderAccount)
							? watchedSenderAccount
							: accountsArray.length > 0
								? accountsArray[0].accountNumber
								: "",
					receiverAccount: "",
					amount: undefined,
					quantity: undefined,
					stockCode: "",
					description: "",
					transferType: transferType,
				});
			})
			.catch((error) => {
				console.error("Accounts fetch error:", error);
				setAccounts([]); 
				showPopup("Gönderici hesaplar yüklenirken hata oluştu.", "error");
			});
	}, [selectedCustomer, reset, transferCategory, watchedSenderAccount, transferType]);

	useEffect(() => {
		if (transferCategory === "MÜŞTERİLER_ARASI" && receiverCustomer) {
			fetch(`http://localhost:9001/api/v1/accounts/customer/${receiverCustomer.customer.id}`, {
				headers: {
					Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
					accept: "application/json",
				},
			})
				.then((res) => res.json())
				.then((data) => {
					const accountsArray = Array.isArray(data) ? data : [];
					setReceiverAccounts(accountsArray);
				})
				.catch((error) => {
					console.error("Receiver accounts fetch error:", error);
					setReceiverAccounts([]); 
					showPopup("Alıcı hesaplar yüklenirken hata oluştu.", "error");
				});
		}
	}, [receiverCustomer, transferCategory]);

	useEffect(() => {
		setValue("transferType", transferType);
		trigger();
	}, [transferType, setValue, trigger]);


	const onSubmit = async (data: any) => {
		try {
			const token = localStorage.getItem("accessToken");

			if (data.transferType === "NAKIT") {
				const queryParams = new URLSearchParams({
					fromAccountNumber: data.senderAccount,
					toAccountNumber: data.receiverAccount,
					amount: data.amount.toString(),
					description: data.description || "",
				}).toString();

				const response = await fetch(`http://localhost:9001/api/v1/accounts/transfer-money?${queryParams}`, {
					method: "POST",
					headers: {
						accept: "*/*",
						Authorization: `Bearer ${token}`,  // Bearer token ekle
					},
					body: "",
				});

				if (!response.ok) {
					const text = await response.text();
					throw new Error(`Transfer işlemi başarısız: ${response.status} - ${text}`);
				}

				await response.text();
				showPopup("Transfer işlemi başarıyla tamamlandı!", "success");

				setTimeout(() => {
					window.location.reload();
				}, 2000);

			} else {
				showPopup("Hisse transferi henüz desteklenmiyor.", "error");
			}
		} catch (error: any) {
			console.error("Transfer error:", error);
			showPopup(`Transfer başarısız: ${error.message}`, "error");
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

	return (
		<div className={styles.container}>
			<h2 className={styles.pageTitle}>{t("transfer.title")}</h2>

			{/* Transfer tipi seçici */}
			<TransferTipSelector
				selectedTip={transferCategory}
				onChange={(tip) => {
					setTransferCategory(tip);
					setSelectedCustomer(null);
					setReceiverCustomer(null);
					setAccounts([]);
					setReceiverAccounts([]);
					reset();
				}}
				availableTips={["HESAPLAR_ARASI", "MÜŞTERİLER_ARASI"]}
			/>

			{/* Müşteri seçim alanları */}
			{transferCategory === "MÜŞTERİLER_ARASI" ? (
				<div className="flex gap-6 mb-6">
					<div className="flex-1">
						<h4 className="mb-2 font-semibold text-gray-500 ">{t("transfer.form.senderCustomer.label")}</h4>
						<SimpleCustomerSearch onSelect={(c) => setSelectedCustomer(c)} />
					</div>
					<div className="flex-1">
						<h4 className="mb-2 font-semibold  text-gray-500">{t("transfer.form.receiverCustomer.label")}</h4>
						<SimpleCustomerSearch onSelect={(c) => setReceiverCustomer(c)} />
					</div>
				</div>
			) : (
				<div className={styles.customerSearchWrapper}>
					<SimpleCustomerSearch onSelect={(customer) => setSelectedCustomer(customer)} />
				</div>
			)}

			<div className={styles.content}>
				<form className={styles.formSection} onSubmit={handleSubmit(onSubmit, onError)} noValidate>
					{/* Transfer Tipi */}
					<div className={styles.formGroup}>
						<label>{t("transfer.form.transferType.label")}</label>
						<select
							{...register("transferType")}
							value={transferType}
							onChange={(e) => {
								setTransferType(e.target.value as "NAKIT" | "HISSE");
								setValue("transferType", e.target.value);
							}}
						>
							<option value="NAKIT">{t("transfer.form.transferType.cash")}</option>
							<option value="HISSE" disabled>
								{t("transfer.form.transferType.stock")}
							</option>
						</select>
					</div>

					{/* Hesap seçimleri */}
					<div className="flex gap-6">
						<div className={styles.formGroup}>
							<label>
								{t("transfer.form.senderAccount.label")} <span className={styles.required}>*</span>
							</label>
							<select {...register("senderAccount")} className={styles.customSelect}>
								<option value="">{t("transfer.form.senderAccount.placeholder")}</option>
								{Array.isArray(accounts) && accounts.map((acc) => (
									<option key={acc.id} value={acc.accountNumber}>
										{`Hesap: ${acc.accountNumber} → Bakiye: ${acc.availableBalance} ₺`}
									</option>
								))}
							</select>
						</div>

						<div className={styles.formGroup}>
							<label>
								{t("transfer.form.receiverAccount.label")} <span className={styles.required}>*</span>
							</label>
							<select {...register("receiverAccount")} className={styles.customSelect}>
								<option value="">{t("transfer.form.receiverAccount.placeholder")}</option>
								{Array.isArray(receiverAccounts) && receiverAccounts.map((acc) => (
									<option key={acc.id} value={acc.accountNumber}>
										{`Hesap: ${acc.accountNumber} → Bakiye: ${acc.availableBalance} ₺`}
									</option>
								))}
							</select>
						</div>
					</div>

					{/* Dinamik Alanlar */}
					{transferType === "NAKIT" ? (
						<div className={styles.formGroup}>
							<label>
								{t("transfer.form.amount.label")} <span className={styles.required}>*</span>
							</label>
							<input
								{...register("amount")}
								type="number"
								placeholder={t("transfer.form.amount.placeholder")}
								step="0.01"
								min="0"
							/>
						</div>
					) : (
						<>
							<div className={styles.formGroup}>
								<label>
									{t("transfer.form.stockCode.label")} <span className={styles.required}>*</span>
								</label>
								<input
									{...register("stockCode")}
									type="text"
									placeholder={t("transfer.form.stockCode.placeholder")}
								/>
							</div>
							<div className={styles.formGroup}>
								<label>
									{t("transfer.form.quantity.label")} <span className={styles.required}>*</span>
								</label>
								<input
									{...register("quantity")}
									type="number"
									placeholder={t("transfer.form.quantity.placeholder")}
									min="1"
								/>
							</div>
							<div className={styles.formGroup}>
								<label>
									{t("transfer.form.amount.label")} <span className={styles.required}>*</span>
								</label>
								<input
									{...register("amount")}
									type="number"
									placeholder={t("transfer.form.amount.placeholder")}
									step="0.01"
									min="0"
								/>
							</div>
						</>
					)}

					{/* Açıklama */}
					<div className={styles.formGroup}>
						<label>{t("transfer.form.description.label")}</label>
						<textarea
							{...register("description")}
							placeholder={t("transfer.form.description.placeholder")}
							rows={3}
							className={styles.textArea}
						/>
					</div>

					<button type="submit" className={styles.submitButton}>
						{t("transfer.buttons.startTransfer")}
					</button>
				</form>
			</div>

			{/* Popup sistem - Error için sağ üstte, Success için ortada */}
			{popup && popup.type === "error" && (
				<div className={styles.popupError} role="alert" aria-live="assertive">
					{popup.message}
				</div>
			)}

			{popup && popup.type === "success" && (
				<div className={styles.popupSuccessOverlay}>
					<div className={styles.popupSuccess} role="alert" aria-live="polite">
						<div className={styles.successIcon}>✅</div>
						<h3 className={styles.successTitle}>Başarılı!</h3>
						<p className={styles.successMessage}>{popup.message}</p>
						<button
							className={styles.successButton}
							onClick={() => setPopup(null)}
						>
							Tamam
						</button>
					</div>
				</div>
			)}
		</div>
	);
}