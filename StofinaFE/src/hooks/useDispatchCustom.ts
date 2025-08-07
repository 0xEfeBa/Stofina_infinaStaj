import { useCallback } from "react";
import { useDispatch } from "react-redux";
import type { AppDispatch } from "@/store"; // yolunu değiştir
import { Action } from "@reduxjs/toolkit";
import { setLoading } from "@/slice/common/globalLoading";

export const useDispatchCustom = () => {
   const dispatch = useDispatch<AppDispatch>();

   const dispatchWithLoading = useCallback(async (action: any | Action) => {
      dispatch(setLoading(true));
      try {
         return await dispatch(action);
      } catch (error) {
         console.error(error);
      } finally {
         dispatch(setLoading(false));
      }
   }, [dispatch]);

   return dispatchWithLoading;
};