
import { Customer } from '@/types/customer';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CustomerSlice {
    isLoading: boolean;
    customer: Customer | null; // Customer bilgisi null olabilir, çünkü müşteri henüz eklenmemiş olabilir
    selectedCustomer: Customer | null; // Seçilen müşteri bilgisi null olabilir, çünkü müşteri henüz seçilmemiş olabilir
}

const initialState: CustomerSlice = {
    isLoading: false,
    customer: null, // Başlangıçta müşteri bilgisi yok
    selectedCustomer: null, // Başlangıçta seçilen müşteri bilgisi yok
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
        setSelectedCustomer: (state, action: PayloadAction<Customer | null>) => {
            state.selectedCustomer = action.payload; // Seçilen müşteri bilgisi güncelleniyor
        },
    },
});

export const { setLoading, setCustomer, setSelectedCustomer } = SliceCustomer.actions;
export const { actions, reducer } = SliceCustomer;
export default SliceCustomer.reducer;
