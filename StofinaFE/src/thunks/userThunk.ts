import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { User } from "@/types/user";
import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import { AppThunk } from "@/store";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { SliceUser } from "@/slice/UserSlice";

export const loginUser = (email: string, password: string):
    AppThunk<Promise<User | null>> =>
    async (dispatch) => {
        try {

            const response = await axiosInstance.post(`${apiConfig.baseUrl}${apiConfig.auth.login}`, {
                email,
                password,
            });

            const data = response.data

            if (response.status === 200) {
                dispatch(
                    SliceUser.actions.setUser(data.data)
                )
                return data.data;
            } else {
                dispatch(
                    SliceGlobalModal.actions.openModal({
                        modalType: "error",
                        title: "Giriş Başarısız",
                        message: data.message || "Lütfen bilgilerinizi kontrol ediniz.",
                    })
                );
                return null;
            }
        } catch (error) {
            dispatch(
                SliceGlobalModal.actions.openModal({
                    modalType: "error",
                    title: "Sunucu Hatası",
                    message: "Giriş yapılırken bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.",
                })
            );
            return null;
        }
    };

export const thunkUser = {
    loginUser
}