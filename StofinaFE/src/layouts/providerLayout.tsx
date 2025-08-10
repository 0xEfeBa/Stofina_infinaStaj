"use client";

import { ReactNode } from "react";
import { Provider } from "react-redux";
import { store } from "@/store";
import GlobalModal from "@/components/common/GlobalModal";
import { I18nextProvider } from 'react-i18next';
import i18n from '@/config/i18n';
import { Toaster } from "sonner";
import { AuthProvider } from "@/contexts/AuthContext";

type ProviderLayoutProps = {
    children: ReactNode;
};

export default function ProviderLayout({ children }: ProviderLayoutProps) {
    return (
        <I18nextProvider i18n={i18n}>
            <Provider store={store}>
                <AuthProvider>
                    {children}
                    <GlobalModal />
                    <Toaster />
                </AuthProvider>
            </Provider>
        </I18nextProvider>
    );
}
