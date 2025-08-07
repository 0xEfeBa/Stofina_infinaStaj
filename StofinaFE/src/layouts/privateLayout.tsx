// app/(protected)/layout.tsx
import { ReactNode } from "react";
import { redirect } from "next/navigation";
import { cookies } from "next/headers";
// import Sidebar from "@/components/Sidebar"; // varsayılan bir bileşen
//import Navbar from "@/components/Navbar";   // varsayılan bir bileşen
//import { verifyAccessToken } from "@/lib/auth"; // JWT doğrulama yardımcı fonksiyonu

export default async function PrivateLayout({ children }: { children: ReactNode }) {
    const cookieStore = cookies();
    const accessToken = (await cookieStore).get("accessToken")?.value;
    const refreshToken = (await cookieStore).get("refreshToken")?.value;

    // Token kontrolü
    // const isValid = await verifyAccessToken(accessToken);

    // if (!isValid) {
    //     if (refreshToken) {
    //         redirect("/refresh"); // Refresh sayfasına yönlendir
    //     } else {
    //         redirect("/login");
    //     }
    // }

    return (
        <div className="min-h-screen flex">
            {/* <Sidebar /> */}
            <div className="flex-1">
                {/* <Navbar /> */}
                <main className="p-4">{children}</main>
            </div>
        </div>
    );
}
