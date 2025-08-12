import axios from "axios";
import { axiosDefaultConfig } from "./axiosConfig";
import { getAdminToken } from "./cookieUtils";
import { authService } from "@/services/authService";
import { checkTokenValidity } from "@/utils/authUtils";
import Router from "next/router";

const axiosInstance = axios.create(axiosDefaultConfig);

axiosInstance.interceptors.request.use(
    async (config) => {
        const token = getAdminToken();
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        const valid = await checkTokenValidity();
        if (!valid) {
            authService.clearAuthData();
            if (typeof window !== "undefined") {
                window.location.href = "/login";
            }
            return Promise.reject(new Error("Token expired"));
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {


        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
