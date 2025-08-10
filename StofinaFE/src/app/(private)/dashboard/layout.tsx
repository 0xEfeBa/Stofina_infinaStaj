"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import DashboardLayout from "@/components/dashboard/dashboardLayout";
import { DashboardProvider } from "@/contexts/DashboardContext";
import { checkTokenValidity } from "@/utils/authUtils";
import { authService } from "@/services/authService";

export default function PrivateLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [isVerified, setIsVerified] = useState(false);

  useEffect(() => {
    const verify = async () => {
      const valid = await checkTokenValidity();
      if (!valid) {
  authService.clearAuthData();
        router.push("/login");
      } else {
        setIsVerified(true);
      }
    };
    verify();
  }, [router]);

  if (!isVerified) return null;

  return (
    <DashboardProvider>
      <DashboardLayout>{children}</DashboardLayout>
    </DashboardProvider>
  );
}
