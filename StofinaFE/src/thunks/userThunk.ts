import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { User } from "@/types/user";
import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import { AppThunk } from "@/store";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { SliceUser } from "@/slice/UserSlice";
import i18n from "@/config/i18n";

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
                        title: i18n.t('auth.login.failed.title'),
                        message: data.message || i18n.t('auth.login.failed.message'),
                    })
                );
                return null;
            }
        } catch (error) {
            dispatch(
                SliceGlobalModal.actions.openModal({
                    modalType: "error",
                    title: i18n.t('auth.login.serverError.title'),
                    message: i18n.t('auth.login.serverError.message'),
                })
            );
            return null;
        }
    };

export const thunkUser = {
    loginUser
}