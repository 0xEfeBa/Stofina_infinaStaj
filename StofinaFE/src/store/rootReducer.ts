import { combineReducers } from "@reduxjs/toolkit";

import globalLoadingReducer from "@/slice/common/globalLoading";
import SliceGlobalModal from "@/slice/common/sliceGlobalModal";
import SliceUser from "@/slice/UserSlice";
import SliceCustomer from "@/slice/CustomerSlice";

export const rootReducer = combineReducers({
    globalLoading: globalLoadingReducer,
    globalModal: SliceGlobalModal,
    user: SliceUser,
    customer: SliceCustomer,


});