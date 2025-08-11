import { apiConfig } from "@/config/apiConfig";
import axiosInstance from "@/config/axiosInstance";
import i18n from "@/config/i18n";
import { SliceGlobalModal } from "@/slice/common/sliceGlobalModal";
import { AppThunk } from "@/store";
import { Order } from "@/types/order";

export const getOrdersByAccountId =
  (
    accountId: number
  ): AppThunk<Promise< Order[] | []>> =>
  async (dispatch) => {
    try {
        const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
      const response = await axiosInstance.get(
        `${apiConfig.baseUrlOrder}${apiConfig.order.index}?accountId=${accountId}`,
              {
                  headers: {
                      Authorization: `Bearer ${token}`
                  }
              }
      );
      console.log(response);
      const data = response.data?.content;

      if (response.status === 200) {
        return data;
      } else {
        dispatch(
          SliceGlobalModal.actions.openModal({
            modalType: "error",
            title: i18n.t("order.getOrdersByAccountId.error.title"),
            message:
              data.message ||
              i18n.t("order.getOrdersByAccountId.error.message"),
          })
        );
        return null;
      }
    } catch (error) {
      dispatch(
        SliceGlobalModal.actions.openModal({
          modalType: "error",
          title: i18n.t("order.getOrdersByAccountId.error.title"),
          message: i18n.t("order.getOrdersByAccountId.error.message"),
        })
      );
      return null;
    }
  };
  export const thunkOrder = {
    getOrdersByAccountId
  }