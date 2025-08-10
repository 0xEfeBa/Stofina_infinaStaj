"use client";

import { useState, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import { Quicksand } from "next/font/google";
import styles from './main.module.css';

const quicksand = Quicksand({ subsets: ["latin"], weight: ["400", "600", "700"] });

export default function Main() {
  const { t } = useTranslation();
  const [currentTime, setCurrentTime] = useState(new Date());
  const [greeting, setGreeting] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Simulate loading
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 1000);

    const timeTimer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => {
      clearTimeout(timer);
      clearInterval(timeTimer);
    };
  }, []);

  useEffect(() => {
    const hour = currentTime.getHours();
    if (hour < 12) {
      setGreeting(t('main.greeting.goodMorning'));
    } else if (hour < 18) {
      setGreeting(t('main.greeting.goodAfternoon'));
    } else {
      setGreeting(t('main.greeting.goodEvening'));
    }
  }, [currentTime, t]);

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('tr-TR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('tr-TR', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const quickActions = [
    {
      title: t('main.quickActions.actions.trading.title'),
      description: t('main.quickActions.actions.trading.description'),
      icon: "📈",
      color: "from-blue-500 to-cyan-500",
      href: "/dashboard/trading",
      badge: t('main.quickActions.actions.trading.badge')
    },
    {
      title: t('main.quickActions.actions.orderTracking.title'),
      description: t('main.quickActions.actions.orderTracking.description'),
      icon: "💼",
      color: "from-green-500 to-emerald-500",
      href: "/dashboard/order-tracking"
    },
    {
      title: t('main.quickActions.actions.customerManagement.title'),
      description: t('main.quickActions.actions.customerManagement.description'),
      icon: "👥",
      color: "from-purple-500 to-pink-500",
      href: "/dashboard/customer-management"
    },
    {
      title: t('main.quickActions.actions.reports.title'),
      description: t('main.quickActions.actions.reports.description'),
      icon: "📊",
      color: "from-orange-500 to-red-500",
      href: "/dashboard/report"
    }
  ];

  const stats = [
    {
      label: t('main.stats.activeCustomers.label'),
      value: t('main.stats.activeCustomers.value'),
      change: t('main.stats.activeCustomers.change'),
      changeType: "positive",
      color: "blue"
    },
    {
      label: t('main.stats.dailyTransactions.label'),
      value: t('main.stats.dailyTransactions.value'),
      change: t('main.stats.dailyTransactions.change'),
      changeType: "positive",
      color: "green"
    },
    {
      label: t('main.stats.activePortfolios.label'),
      value: t('main.stats.activePortfolios.value'),
      change: t('main.stats.activePortfolios.change'),
      changeType: "positive",
      color: "purple"
    }
  ];
  //loading animation
  if (isLoading) {
    return (
      <div className={`${quicksand.className} ${styles.loadingContainer}`}>
        <div className="text-center">
          <div className={styles.loadingSpinner}></div>
          <p className={styles.loadingText}>{t('main.loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`${quicksand.className} ${styles.mainContainer}`}>
      {/* Hero Section */}
      <div className={styles.heroSection}>
        <div className={styles.heroBackground}></div>

        <div className={styles.heroContent}>
          <div className={styles.heroTextContainer}>
            <div className={styles.logoContainer}>
              <div className={styles.logoWrapper}>
                <img
                  src="/assets/icons/mainpage-logo.png"
                  alt="Stofina Logo"
                  className={styles.logo}
                />
                <div className={styles.logoGlow}></div>
              </div>
            </div>
            {/* greeting */}
            <h1 className={`${styles.greeting} ${styles.animateFadeIn}`}>
              {greeting}!
            </h1>
            <p className={styles.welcomeText}>
              {t('main.welcome')}
            </p>

            {/* Scroll to Quick Actions Button */}
            <div className={styles.scrollButtonContainer}>
              <button
                onClick={() => {
                  const quickActionsSection = document.querySelector('[data-section="quick-actions"]');
                  if (quickActionsSection) {
                    quickActionsSection.scrollIntoView({ behavior: 'smooth' });
                  }
                }}
                className={styles.scrollButton}
                aria-label={t('main.quickActions.scrollToQuickActions')}
              >
                <svg
                  className={styles.scrollIcon}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
              </button>
            </div>

          </div>
        </div>
      </div>

      {/* Quick Actions Section */}
      <div className={styles.quickActionsSection} data-section="quick-actions">
        <div className={styles.quickActionsHeader}>
          <h2 className={styles.quickActionsTitle}>
            {t('main.quickActions.title')}
          </h2>
          <p className={styles.quickActionsDescription}>
            {t('main.quickActions.description')}
          </p>
        </div>

        <div className={styles.quickActionsGrid}>
          {quickActions.map((action, index) => (
            <div
              key={index}
              className={styles.actionCard}
              onClick={() => window.location.href = action.href}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  window.location.href = action.href;
                }
              }}
              tabIndex={0}
              role="button"
              aria-label={`${action.title} sayfasına git`}
            >
              <div className={`${styles.actionCardBackground} bg-gradient-to-r ${action.color}`}></div>

              {action.badge && (
                <div className={styles.actionBadge}>
                  <span className={styles.badge}>
                    {action.badge}
                  </span>
                </div>
              )}

              <div className={styles.actionContent}>
                <div className={styles.actionIcon}>{action.icon}</div>
                <h3 className={styles.actionTitle}>
                  {action.title}
                </h3>
                <p className={styles.actionDescription}>
                  {action.description}
                </p>
              </div>

              <div className={styles.actionArrow}>
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Stats Section */}
      <div className={styles.statsSection}>
        <div className={styles.statsContainer}>
          <h2 className={styles.statsTitle}>
            {t('main.stats.title')}
          </h2>

          <div className={styles.statsGrid}>
            {stats.map((stat, index) => (
              <div key={index} className={`${styles.statCard} ${stat.color === 'blue' ? styles.statCardBlue : stat.color === 'green' ? styles.statCardGreen : styles.statCardPurple}`}>
                <div className={`${styles.statValue} ${stat.color === 'blue' ? styles.statValueBlue : stat.color === 'green' ? styles.statValueGreen : styles.statValuePurple}`}>
                  {stat.value}
                </div>
                <div className={styles.statLabel}>
                  {stat.label}
                </div>
                <div className={`${styles.statChange} ${stat.changeType === 'positive' ? styles.statChangePositive : styles.statChangeNegative}`}>
                  <svg className={styles.statChangeIcon} fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z" clipRule="evenodd" />
                  </svg>
                  {stat.change}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

    </div>
  );
}
