import { Box, Button, Modal, Typography } from '@mui/material';
import React from 'react';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { useSelectorCustom } from '@/store';

const iconStyle = { width: 100, height: 100 };

const getIcon = (type: string) => {
    const iconProps = { style: iconStyle };
    switch (type) {
        case 'error':
            return <ErrorOutlineIcon {...iconProps} color="error" />;
        case 'success':
            return <CheckCircleOutlineIcon {...iconProps} color="success" />;
        case 'warning':
            return <InfoOutlinedIcon {...iconProps} color="warning" />;
        default:
            return <InfoOutlinedIcon {...iconProps} color="info" />;
    }
};

const getModalTitle = (type: string) => {
    switch (type) {
        case 'error':
        case 'warning':
            return 'Uyarı';
        case 'success':
            return 'Başarılı';
        default:
            return 'Bilgi';
    }
};

const modalStyle = {
    position: 'absolute' as const,
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 350,
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 4,
    borderRadius: 2,
    display: 'flex',
    flexDirection: 'column',
    gap: 2,
    alignItems: 'center',
    justifyContent: 'center',
};

const GlobalModal = () => {
    const dispatch = useDispatchCustom();
    const { isOpen, modalType, message, title, multipleButtons, modalAction } = useSelectorCustom((state) => state.globalModal);

    const handleConfirm = async () => {
        if (modalAction) {
            modalAction();
        }
        dispatch(SliceGlobalModal.actions.closeModal());
    };

    const handleClose = () => {
        dispatch(SliceGlobalModal.actions.closeModal());
    };

    return (
        <Modal
            open={isOpen}
            onClose={handleClose}
            aria-labelledby="modal-title"
            aria-describedby="modal-description"
        >
            <Box sx={modalStyle}>
                {getIcon(modalType)}
                <Typography variant="h4" component="h2">
                    {getModalTitle(modalType)}
                </Typography>

                <Box sx={{ textAlign: 'center' }}>
                    {title && <Typography sx={{ fontWeight: 600 }}>{title}</Typography>}
                    <Typography>{message}</Typography>
                </Box>

                {multipleButtons ? (
                    <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                        <Button onClick={handleConfirm} variant="contained" color="primary">
                            Evet
                        </Button>
                        <Button onClick={handleClose} variant="contained" color="error">
                            Hayır
                        </Button>
                    </Box>
                ) : (
                    <Button onClick={handleClose} variant="contained" color="primary">
                        Tamam
                    </Button>
                )}
            </Box>
        </Modal>
    );
};

export default GlobalModal;
