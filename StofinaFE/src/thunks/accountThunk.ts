import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import i18n from "@/config/i18n";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { AppThunk } from "@/store";
import { Account } from "@/types/account";
import { AccountStatus } from "@/constants/EnumAccountStatus";


export const getAccountsByCustomerId =
  (
    customerId: number
  ): AppThunk<Promise< Account[] | []>> =>
  async (dispatch) => {
    try {
        const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
        const response = await axiosInstance.get(
          `${apiConfig.baseUrlPortfolio}${apiConfig.account.customer}${customerId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );
      console.log(response);
      const data = response.data;

      if (response.status === 200) {
        return data;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("account.getAccountsByCustomerId.error.title"),
            message:
              data.message ||
              i18n.t("account.getAccountsByCustomerId.error.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("customer.createCorporateCustomer.error.title"),
          message: i18n.t("customer.createCorporateCustomer.error.message"),
        })
      );
      return null;
    }
  };

  // create account thunk
  export const createAccount =
  (
    customerId: number,
    initialBalance: number,
    openingDate: Date
  ): AppThunk<Promise< boolean>> =>
  async (dispatch) => {
    try {
        const token = localStorage.getItem('accessToken');
        const response = await axiosInstance.post(
          `${apiConfig.baseUrlPortfolio}${apiConfig.account.index}`,
          {
            customerId: customerId,
            initialBalance: initialBalance,
            openingDate: openingDate
          },
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );
      console.log(response);
      const data = response.data;

      if (response.status === 200 || response.status === 201) {
        dispatch(thunkAccount.getAccountsByCustomerId(customerId));
        return true;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("account.createAccount.error.title"),
            message:
              data.message ||
              i18n.t("account.createAccount.error.message"),
          })
        );
        return false;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("account.createAccount.error.title"),
          message: i18n.t("account.createAccount.error.message"),
        })
      );
      return false;
    }
  };

  export const changeAccountStatus =
  (
    accountId: number,
    newStatus: typeof AccountStatus.ACTIVE | typeof AccountStatus.PASSIVE
  ): AppThunk<Promise< boolean>> =>
  async (dispatch) => {
    try {
        const token = localStorage.getItem('accessToken');
        const response = await axiosInstance.patch(
          `${apiConfig.baseUrlPortfolio}${apiConfig.account.index}/${accountId}/status`,
          {
            newStatus: newStatus
          },
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );
      console.log(response);
      const data = response.data;

      if (response.status === 200 || response.status === 201) {
        return true;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("account.changeAccountStatus.error.title"),
            message:
              data.message ||
              i18n.t("account.changeAccountStatus.error.message"),
          })
        );
        return false;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("account.changeAccountStatus.error.title"),
          message: i18n.t("account.changeAccountStatus.error.message"),
        })
      );
      return false;
    }
  };


export const thunkAccount = {
  getAccountsByCustomerId,
  createAccount,
  changeAccountStatus
};
