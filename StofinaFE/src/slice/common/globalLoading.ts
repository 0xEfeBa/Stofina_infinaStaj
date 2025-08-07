import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface GlobalLoadingState {
    isLoading: boolean;
}

const initialState: GlobalLoadingState = {
    isLoading: false,
};

export const SliceLoading = createSlice({
    name: 'globalLoading',
    initialState,
    reducers: {
        setLoading: (state, action: PayloadAction<boolean>) => {
            state.isLoading = action.payload;
        },
    },
});

export const { setLoading } = SliceLoading.actions;
export const { actions, reducer } = SliceLoading;
export default SliceLoading.reducer;
