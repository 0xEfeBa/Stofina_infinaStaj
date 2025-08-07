import Cookies from "js-cookie";

// Cookie anahtarını sabit tut
const ADMIN_TOKEN_KEY = "adminToken";

export const getAdminToken = (): string | undefined => {
    if (typeof window === "undefined") return undefined;
    return Cookies.get(ADMIN_TOKEN_KEY);
};