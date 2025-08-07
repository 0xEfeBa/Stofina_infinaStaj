
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface SliceGlobalModal {
    isOpen: boolean; // Modal'un açık mı kapalı mı olduğunu tutar
    modalType: 'warning' | 'info' | 'error' | 'success'; // Modal türü
    title?: string; // Modal başlığı
    message: string; // Modal mesajı
    multipleButtons?: boolean; // Birden fazla buton olup olmadığını belirtir
    modalResult: boolean | null; // Modalın sonucu (kullanıcı onayladı mı?)

}

const initialState: SliceGlobalModal = {
    isOpen: false, // Başlangıçta modal kapalı
    modalType: 'info', // Varsayılan modal türü
    title: '', // Başlangıçta başlık boş
    message: '', // Başlangıçta mesaj boş
    multipleButtons: false, // Başlangıçta birden fazla buton yok
    modalResult: null, // Başlangıçta modal sonucu false (kullanıcı onaylamadı)

};

const reducers = {
    openModal: (
        state: SliceGlobalModal,
        action: PayloadAction<{
            modalType: 'warning' | 'error' | 'success' | 'info';
            title?: string;
            message: string;
            multipleButton?: boolean;
        }>
    ) => {
        state.isOpen = true;
        state.modalType = action.payload.modalType;
        state.message = action.payload.message;
        state.title = action.payload?.title || '';
        state.multipleButtons = action.payload?.multipleButton;
    },
    closeModal: (state: SliceGlobalModal) => {
        state.isOpen = false;
        state.message = '';
        state.modalResult = false;
    },
    changeResultTrue: (state: SliceGlobalModal) => {
        state.modalResult = true;
    },
    changeResultFalse: (state: SliceGlobalModal) => {
        state.modalResult = false;
    },
};

export const SliceGlobalModal = createSlice({
    name: 'sliceGlobalModal',
    initialState,
    reducers,
});
export const { openModal, closeModal, changeResultFalse, changeResultTrue } = SliceGlobalModal.actions;
export const { actions, reducer } = SliceGlobalModal;


export default SliceGlobalModal.reducer;
