import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { User } from "@/types/user";
import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import { AppThunk } from "@/store";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { SliceUser } from "@/slice/UserSlice";
import i18n from "@/config/i18n";
import { IndividualCustomer } from "@/types/customer";
import { SliceCustomer } from "@/slice/CustomerSlice";

export const getIndividuals =
  (): AppThunk<Promise<IndividualCustomer | null>> => async (dispatch) => {
    try {
      const response = await axiosInstance.get(
        `${apiConfig.baseUrl}${apiConfig.customer.individual}`
      );
      const data = response.data;

      if (response.status === 200) {
        dispatch(SliceCustomer.actions.setIndividualCustomers(data));
        return data;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("auth.login.failed.title"),
            message: data.message || i18n.t("auth.login.failed.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("auth.login.serverError.title"),
          message: i18n.t("auth.login.serverError.message"),
        })
      );
      return null;
    }
  };

export const getCorporateCustomers =
  (): AppThunk<Promise<IndividualCustomer | null>> => async (dispatch) => {
    try {
      const response = await axiosInstance.get(
        `${apiConfig.baseUrl}${apiConfig.customer.corporate}`
      );
      const data = response.data;

      if (response.status === 200) {
        dispatch(SliceCustomer.actions.setCorporateCustomers(data));
        return data;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("auth.login.failed.title"),
            message: data.message || i18n.t("auth.login.failed.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("auth.login.serverError.title"),
          message: i18n.t("auth.login.serverError.message"),
        })
      );
      return null;
    }
  };

export const thunkCustomer = {
  getIndividuals,
  getCorporateCustomers,
};
