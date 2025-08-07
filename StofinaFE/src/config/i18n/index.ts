import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import tr from './locales/tr/common.json';
import en from './locales/en/common.json';

i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        fallbackLng: 'tr',
        debug: process.env.NODE_ENV === 'development',
        resources: {
            tr: { common: tr },
            en: { common: en },
        },
        ns: ['common'],
        defaultNS: 'common',
        interpolation: {
            escapeValue: false, // React zaten escape eder
        },
    });

export default i18n;
