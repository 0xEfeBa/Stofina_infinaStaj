import axios from "axios";
import { axiosDefaultConfig } from "./axiosConfig";
import { getAdminToken } from "./cookieUtils";

const axiosInstance = axios.create(axiosDefaultConfig);

// Request Interceptor
axiosInstance.interceptors.request.use(
    (config) => {
        const token = getAdminToken();
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response Interceptor (isteğe bağlı hata yönetimi)
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        // Burada logout işlemleri ya da loglama yapılabilir
        return Promise.reject(error);
    }
);

export default axiosInstance;
