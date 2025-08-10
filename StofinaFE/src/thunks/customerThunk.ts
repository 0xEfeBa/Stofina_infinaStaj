import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { User } from "@/types/user";
import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import { AppThunk } from "@/store";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { SliceUser } from "@/slice/UserSlice";
import i18n from "@/config/i18n";
import {
  IndividualCustomer,
  CorporateCustomer,
  ReqCorporateCustomerCreate,
  ReqIndividualCustomerCreate,
} from "@/types/customer";
import { SliceCustomer } from "@/slice/CustomerSlice";

export const getIndividuals =
  (): AppThunk<Promise<IndividualCustomer | null>> => async (dispatch) => {
    try {
      const token = localStorage.getItem("accessToken");
      const response = await axiosInstance.get(
        `${apiConfig.baseUrlCustomer}${apiConfig.customer.individual}`,
        {
          headers: {
            accept: "*/*",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = response.data;

      if (response.status === 200) {
        dispatch(SliceCustomer.actions.setIndividualCustomers(data));
        return data;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("customer.getIndividuals.error.title"),
            message: i18n.t("customer.getIndividuals.error.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("customer.getIndividuals.error.title"),
          message: i18n.t("customer.getIndividuals.error.message"),
        })
      );
      return null;
    }
  };

export const getCorporateCustomers =
  (): AppThunk<Promise<IndividualCustomer | null>> => async (dispatch) => {
    try {
      const token = localStorage.getItem("accessToken");
      const response = await axiosInstance.get(
        `${apiConfig.baseUrlCustomer}${apiConfig.customer.corporate}`,
        {
          headers: {
            accept: "*/*",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = response.data;

      if (response.status === 200) {
        dispatch(SliceCustomer.actions.setCorporateCustomers(data));
        return data;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("customer.getCorporateCustomers.error.title"),
            message: i18n.t("customer.getCorporateCustomers.error.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("customer.getCorporateCustomers.error.title"),
          message: i18n.t("customer.getCorporateCustomers.error.message"),
        })
      );
      return null;
    }
  };

export const createIndividualCustomer =
  (req: ReqIndividualCustomerCreate): AppThunk<Promise<boolean>> => 
  async (dispatch) => {
    try {
      const token = localStorage.getItem("accessToken");
      const response = await axiosInstance.post(
        `${apiConfig.baseUrlCustomer}${apiConfig.customer.individual}`,
        req,
        {
          headers: {
            accept: "*/*",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = response.data;

      if (response.status === 200 || response.status === 201) {
        return true;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("customer.createIndividualCustomer.error.title"),
            message: i18n.t("customer.createIndividualCustomer.error.message"),
          })
        );
        return false;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("customer.createIndividualCustomer.error.title"),
          message: i18n.t("customer.createIndividualCustomer.error.message"),
        })
      );
      return false;
    }
  };

export const createCorporateCustomer =
  (
    req: ReqCorporateCustomerCreate): AppThunk<Promise<boolean >> =>
  async (dispatch) => {
    try {
      const token = localStorage.getItem("accessToken");
      const response = await axiosInstance.post(
        `${apiConfig.baseUrlCustomer}${apiConfig.customer.corporate}`,
        req,
        {
          headers: {
            accept: "*/*",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = response.data;

      if (response.status === 200 || response.status === 201) {
        return true;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("customer.createCorporateCustomer.error.title"),
            message:
              data.message ||
              i18n.t("customer.createCorporateCustomer.error.message"),
          })
        );
        return false;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("customer.createCorporateCustomer.error.title"),
          message: i18n.t("customer.createCorporateCustomer.error.message"),
        })
      );
      return false;
    }
  };

export const thunkCustomer = {
  getIndividuals,
  getCorporateCustomers,
  createIndividualCustomer,
  createCorporateCustomer,
};
