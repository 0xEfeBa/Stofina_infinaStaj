"use client";

import { ReactNode } from "react";
import { Provider } from "react-redux";
import { store } from "@/store";

type ProviderLayoutProps = {
    children: ReactNode;
};

export default function ProviderLayout({ children }: ProviderLayoutProps) {
    return <Provider store={store}>{children}</Provider>;
}