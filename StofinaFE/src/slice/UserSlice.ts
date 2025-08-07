import { User } from '@/types/user';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UserState {
    isLoading: boolean;
    user: User | null; // User bilgisi null olabilir, çünkü kullanıcı henüz giriş yapmamış olabilir

}

const initialState: UserState = {
    isLoading: false,
    user: null, // Başlangıçta kullanıcı bilgisi yok
};

export const SliceUser = createSlice({
    name: 'user',
    initialState,
    reducers: {
        setLoading: (state, action: PayloadAction<boolean>) => {
            state.isLoading = action.payload;
        },
        setUser: (state, action: PayloadAction<User | null>) => {
            state.user = action.payload; // Kullanıcı bilgisi güncelleniyor
        },
    },
});

export const { setLoading, setUser } = SliceUser.actions;
export const { actions, reducer } = SliceUser;

export default SliceUser.reducer;
