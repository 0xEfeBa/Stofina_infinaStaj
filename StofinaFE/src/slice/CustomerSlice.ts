import { CorporateCustomer, IndividualCustomer } from "@/types/customer";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface CustomerState {
  isLoading: boolean;
  individualCustomers: IndividualCustomer[] | null; // Customer bilgisi null olabilir, çünkü müşteri henüz eklenmemiş olabilir
  selectedIndividualCustomer: IndividualCustomer | null; // Seçilen müşteri bilgisi null olabilir, çünkü müşteri henüz seçilmemiş olabilir
  corporateCustomers: CorporateCustomer[] | null; // Başlangıçta müşteri bilgisi yok
  selectedCorporateCustomer: CorporateCustomer | null; // Başlangıçta seçilen müşteri bilgisi yok
}

const initialState: CustomerState = {
  isLoading: false,
  individualCustomers: null, // Başlangıçta müşteri bilgisi yok
  selectedIndividualCustomer: null, // Başlangıçta seçilen müşteri bilgisi yok
  corporateCustomers: null, // Başlangıçta müşteri bilgisi yok
  selectedCorporateCustomer: null, // Başlangıçta seçilen müşteri bilgisi yok
};

export const SliceCustomer = createSlice({
  name: "customer",
  initialState,
  reducers: {
    // reducerlar state'i günceller
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setIndividualCustomers: (
      state,
      action: PayloadAction<IndividualCustomer[] | null>
    ) => {
      state.individualCustomers = action.payload; // Müşteri bilgisi güncelleniyor
    },
    setSelectedIndividualCustomer: (
      state,
      action: PayloadAction<IndividualCustomer | null>
    ) => {
      state.selectedIndividualCustomer = action.payload; // Seçilen müşteri bilgisi güncelleniyor
    },
    setCorporateCustomers: (
      state,
      action: PayloadAction<CorporateCustomer[] | null>
    ) => {
      state.corporateCustomers = action.payload; // Müşteri bilgisi güncelleniyor
    },
    setSelectedCorporateCustomer: (
      state,
      action: PayloadAction<CorporateCustomer | null>
    ) => {
      state.selectedCorporateCustomer = action.payload; // Seçilen müşteri bilgisi güncelleniyor
    },
  },
});

export const {
  setLoading,
  setIndividualCustomers,
  setSelectedIndividualCustomer,
  setCorporateCustomers,
  setSelectedCorporateCustomer,
} = SliceCustomer.actions;
export const { actions, reducer } = SliceCustomer;
export default SliceCustomer.reducer;
