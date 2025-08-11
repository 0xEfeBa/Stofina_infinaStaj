import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import i18n from "@/config/i18n";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { AppThunk } from "@/store";
import { AccountBalance } from "@/types/balance";
import { Stock } from "@/types/stock";

export const getStocksByAccountId =
  (
    accountId: number
  ): AppThunk<Promise< Stock[] | []>> =>
  async (dispatch) => {
    try {
      const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
      const response = await axiosInstance.get(
        `${apiConfig.baseUrlPortfolio}${apiConfig.stock.index}/${accountId}`,
        {
          headers: {
            Authorization: token ? `Bearer ${token}` : ''
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
            title: i18n.t("portfolio.getStocksByAccountId.error.title"),
            message:
              data.message ||
              i18n.t("portfolio.getStocksByAccountId.error.message"),
          })
        );
        return [];
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("portfolio.getStocksByAccountId.error.title"),
          message: i18n.t("portfolio.getStocksByAccountId.error.message"),
        })
      );
      return [];
    }
  };

  export const getBalanceByAccountId =
  (
    accountId: number
  ): AppThunk<Promise< AccountBalance | null>> =>
  async (dispatch) => {
    try {
      const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
      const response = await axiosInstance.get(
        `${apiConfig.baseUrlPortfolio}${apiConfig.balance.index}/${accountId}`,
        {
          headers: {
            Authorization: token ? `Bearer ${token}` : ''
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
            title: i18n.t("portfolio.getBalanceByAccountId.error.title"),
            message:
              data.message ||
              i18n.t("portfolio.getBalanceByAccountId.error.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("portfolio.getBalanceByAccountId.error.title"),
          message: i18n.t("portfolio.getBalanceByAccountId.error.message"),
        })
      );
      return null;
    }
  };
  export const thunkPortfolio = {
    getStocksByAccountId,
    getBalanceByAccountId
  };
