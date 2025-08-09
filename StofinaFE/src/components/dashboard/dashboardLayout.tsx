"use client";
import { Quicksand } from "next/font/google";
import styles from "./DashboardLayout.module.css";
import { useState, useEffect, useRef } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useTranslation } from 'react-i18next';

const quicksand = Quicksand({ subsets: ["latin"], weight: ["400", "600", "700"] });

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [currentTime, setCurrentTime] = useState<Date | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isUserOpen, setIsUserOpen] = useState(false);
  const settingsRef = useRef<HTMLDivElement>(null);
  const userRef = useRef<HTMLDivElement>(null);
  const { t, i18n } = useTranslation();

  const path = usePathname();

  const menuItems = [
    { label: t('dashboard.menu.dashboard'), href: "/dashboard", icon: "/menu-icon/kontrol.png" },
    { label: t('dashboard.menu.trading'), href: "/dashboard/trading", icon: "/menu-icon/trade.png" },
    { label: t('dashboard.menu.stocks'), href: "/dashboard/stock", icon: "/menu-icon/stock.png" },
    { label: t('dashboard.menu.stockDefinition'), href: "/dashboard/stock-management", icon: "/menu-icon/add_stock.png" },
    { label: t('dashboard.menu.customerDefinition'), href: "/dashboard/bireysel", icon: "/menu-icon/add_customer.png" },
    { label: t('dashboard.menu.customerPortfolio'), href: "/dashboard/customer-portfolio", icon: "/menu-icon/basket.png" },
    { label: t('dashboard.menu.customerAccountManagement'), href: "/dashboard/customer-management", icon: "/menu-icon/wallet.png" },
    { label: t('dashboard.menu.orderTracking'), href: "/dashboard/order-tracking", icon: "/menu-icon/order.png" },
    { label: t('dashboard.menu.reporting'), href: "/dashboard/report", icon: "/menu-icon/report.png" },
    { label: t('dashboard.menu.userManagement'), href: "/dashboard/user-management", icon: "/menu-icon/portfolio.png" },
    { label: t('dashboard.menu.transfer'), href: "/dashboard/transfer", icon: "/menu-icon/virman.png" }
  ];

  useEffect(() => {
    setCurrentTime(new Date());
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (settingsRef.current && !settingsRef.current.contains(event.target as Node)) {
        setIsSettingsOpen(false);
      }
      if (userRef.current && !userRef.current.contains(event.target as Node)) {
        setIsUserOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleLanguageChange = (language: string) => {
    i18n.changeLanguage(language);
    setIsSettingsOpen(false);
  };

  const handleLogout = async () => {
    try {
      const accessToken = localStorage.getItem("accessToken");
      const user = JSON.parse(localStorage.getItem("user") || "{}");

      if (!accessToken || !user?.id) {
        console.warn("Token veya kullanıcı bilgisi bulunamadı");
      } else {
        const res = await fetch(`http://localhost:9002/api/v1/auth/logout/${user.id}`, {
          method: "POST",
          headers: {
            accept: "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (!res.ok) {
          console.error("Logout başarısız:", await res.text());
        }
      }

      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");

      setTimeout(() => {
        window.location.href = "/login";
      }, 2000);

    } catch (err) {
      console.error("Logout sırasında hata:", err);
    } finally {
      setIsUserOpen(false);
    }
  };


  const filteredItems = menuItems.filter((item) =>
    item.label.toLowerCase().startsWith(searchTerm.toLowerCase())
  );

  return (
    <div className={`${styles.container} ${quicksand.className}`}>
      {/* ÜST HEADER */}
      <header className={styles.header} style={{ fontFamily: quicksand.style.fontFamily }}>
        <img src="/logo.png" alt="Logo" className={styles.logo} />
        <h1 className="text-xl font-semibold mr-20 mt-1">{t('dashboard.header.title')}</h1>

        {/* Kullanıcı Bilgisi */}
        <div style={{ display: 'flex', alignItems: 'center', marginLeft: 'auto', gap: 12 }}>
          <div className={styles.userDropdown} ref={userRef}>
            <button
              className={`${styles.userButton} ${isUserOpen ? styles.active : ''}`}
              onClick={() => setIsUserOpen(!isUserOpen)}
              aria-label="User"
            >
              <img src="/account.png" alt="Account" className={styles.userIcon} />
              <span className="font-medium">{t('dashboard.header.userName')}</span>
            </button>
            {isUserOpen && (
              <div className={styles.dropdownMenu}>
                <button
                  className={styles.dropdownItem}
                  onClick={handleLogout}
                >
                  <img src="/logout.png" alt={t('dashboard.sidebar.logout')} className={styles.dropdownIcon} />
                  <span>{t('dashboard.sidebar.logout')}</span>
                </button>
              </div>
            )}
          </div>

          <div className={styles.settingsDropdown} ref={settingsRef}>
            <button
              className={`${styles.settingsButton} ${isSettingsOpen ? styles.active : ''}`}
              onClick={() => setIsSettingsOpen(!isSettingsOpen)}
              aria-label={t('dashboard.header.settings')}
            >
              <img src="/assets/icons/settings.png" alt={t('dashboard.header.settings')} className={styles.userIcon} />
              <span className="font-medium">{t('dashboard.header.settings')}</span>
            </button>
            {isSettingsOpen && (
              <div className={styles.dropdownMenu}>
                <div className={styles.dropdownHeader}>
                  <span>{t('dashboard.settings.languageSelection')}</span>
                </div>
                <button
                  className={`${styles.languageOption} ${i18n.language === 'tr' ? styles.active : ''}`}
                  onClick={() => handleLanguageChange('tr')}
                >
                  <div className={styles.languageBadge}>
                    <span className={styles.languageCode}>TR</span>
                  </div>
                  <span className={styles.languageName}>{t('dashboard.settings.turkish')}</span>
                  {i18n.language === 'tr' && (
                    <div className={styles.selectionIndicator}>
                      <div className={styles.selectionDot}></div>
                    </div>
                  )}
                </button>
                <button
                  className={`${styles.languageOption} ${i18n.language === 'en' ? styles.active : ''}`}
                  onClick={() => handleLanguageChange('en')}
                >
                  <div className={styles.languageBadge}>
                    <span className={styles.languageCode}>EN</span>
                  </div>
                  <span className={styles.languageName}>{t('dashboard.settings.english')}</span>
                  {i18n.language === 'en' && (
                    <div className={styles.selectionIndicator}>
                      <div className={styles.selectionDot}></div>
                    </div>
                  )}
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Tarih & Saat */}
        <div className={styles.dateTimeBox}>
          <p className={styles.date}>
            {currentTime
              ? currentTime.toLocaleDateString("tr-TR", {
                weekday: "long",
                day: "numeric",
                month: "long",
                year: "numeric",
              })
              : t('dashboard.header.loading')}
          </p>
          <p className={styles.time}>
            {currentTime ? currentTime.toLocaleTimeString("tr-TR") : "--:--:--"}
          </p>
        </div>
      </header>

      <div className="flex flex-1 pt-20">
        <aside className={styles.sidebar}>
          <div className={styles.searchContainer}>
            <input
              type="text"
              placeholder={t('dashboard.sidebar.search.placeholder')}
              className={styles.searchInput}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              autoComplete="off"
            />
            {searchTerm && (
              <nav className={styles.nav} style={{ marginTop: "12px" }}>
                {filteredItems.length > 0 ? (
                  filteredItems.map((item) => {
                    const isActive = path.endsWith(item.href)
                    return (
                      <a key={item.label} href={item.href} className={`${path.endsWith(item.href) ? 'bg-[#813FB4]/10' : ''}`}>
                        <img src={item.icon} alt={item.label} className="w-6 h-6" />
                        {item.label}
                      </a>
                    )
                  })
                ) : (
                  <p className="text-center text-gray-500 p-2">{t('dashboard.sidebar.search.noResults')}</p>
                )}
              </nav>
            )}
          </div>

          {!searchTerm && (
            <nav className={styles.nav}>
              {menuItems.map((item) => {
                return (
                  <a key={item.label} href={item.href} className={`${path.endsWith(item.href) ? 'bg-[#813FB4]/30 ' : ''}`}>
                    <img src={item.icon} alt={item.label} className="w-6 h-6" />
                    {item.label}
                  </a>
                )
              })}
            </nav>
          )}
        </aside>
        <main className={styles.main}>{children}</main>
      </div>
    </div>
  );
}
