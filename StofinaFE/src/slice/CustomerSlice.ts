
import { Customer } from '@/types/customer';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CustomerSlice {
    isLoading: boolean;
    customer: Customer | null; // Customer bilgisi null olabilir, çünkü müşteri henüz eklenmemiş olabilir

}

const initialState: CustomerSlice = {
    isLoading: false,
    customer: null, // Başlangıçta müşteri bilgisi yok

};

export const SliceCustomer = createSlice({
    name: 'customer',
    initialState,
    reducers: {
        setLoading: (state, action: PayloadAction<boolean>) => {
            state.isLoading = action.payload;
        },
        setCustomer: (state, action: PayloadAction<Customer | null>) => {
            state.customer = action.payload; // Müşteri bilgisi güncelleniyor
        },

    },
});

export const { setLoading, setCustomer } = SliceCustomer.actions;
export const { actions, reducer } = SliceCustomer;
export default SliceCustomer.reducer;
