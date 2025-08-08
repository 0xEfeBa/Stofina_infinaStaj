"use client";

import DashboardLayout from "@/components/dashboard/dashboardLayout";
import { DashboardProvider } from "@/contexts/DashboardContext";

export default function DashboardLayoutWrapper({ children }: { children: React.ReactNode }) {
    return (
        <DashboardProvider>
            <DashboardLayout>{children}</DashboardLayout>
        </DashboardProvider>
    );
}
