import { apiConfig } from "./apiConfig";

export const axiosDefaultConfig = {
    baseURL: apiConfig.baseUrl,
    headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
    },
    timeout: 10000, // performans için istek süresi sınırı
};