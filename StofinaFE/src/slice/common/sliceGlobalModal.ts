import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface SliceGlobalModal {
    isOpen: boolean; // Modal'un açık mı kapalı mı olduğunu tutar
    modalType: 'warning' | 'info' | 'error' | 'success'; // Modal türü
    title?: string; // Modal başlığı
    message: string; // Modal mesajı
    multipleButtons?: boolean; // Birden fazla buton olup olmadığını belirtir
    modalAction?: () => void;


}

const initialState: SliceGlobalModal = {
    isOpen: false, // Başlangıçta modal kapalı
    modalType: 'info', // Varsayılan modal türü
    title: '', // Başlangıçta başlık boş
    message: '', // Başlangıçta mesaj boş
    multipleButtons: false, // Başlangıçta birden fazla buton yok

};

const reducers = {
    openModal: (
        state: SliceGlobalModal,
        action: PayloadAction<{
            modalType: 'warning' | 'error' | 'success' | 'info';
            title?: string;
            message: string;
            multipleButtons?: boolean;
            modalAction?: () => void;
        }>
    ) => {
        state.isOpen = true;
        state.modalType = action.payload.modalType;
        state.message = action.payload.message;
        state.title = action.payload?.title || '';
        state.multipleButtons = action.payload?.multipleButtons;
        state.modalAction = action.payload?.modalAction;
    },
    closeModal: (state: SliceGlobalModal) => {
        state.isOpen = false;
        state.message = '';
    },

};

export const SliceGlobalModal = createSlice({
    name: 'sliceGlobalModal',
    initialState,
    reducers,
});
export const { openModal, closeModal } = SliceGlobalModal.actions;
export const { actions, reducer } = SliceGlobalModal;


export default SliceGlobalModal.reducer;
