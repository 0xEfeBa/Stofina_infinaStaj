import React from 'react'
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Button, FormControlLabel, Checkbox, Grid, Box, Typography } from '@mui/material';
import { useState } from 'react';
import { customerType } from '@/constants/customerType';
import { SliceGlobalModal } from '@/slice/common/sliceGlobalModal';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { useTranslation } from 'react-i18next';
import { CorporateCustomer, IndividualCustomer } from '@/types/customer';
import { thunkAccount } from '@/thunks/accountThunk';
import { toast } from 'sonner';

interface Props {
    open: boolean;
    onClose: () => void;
    customer: IndividualCustomer | CorporateCustomer
    onSubmit: () => void
}

export default function NewAccModal({ open, onClose, customer, onSubmit }: Props) {
    const dispatch = useDispatchCustom();
    const { t } = useTranslation();
    const [formData, setFormData] = useState({
        customerId: customer.customer.id,
        initialBalance: 0,
        openingDate: new Date(),
        isApproved: false,
    });

    const handleChange = (field: string, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };



    const handleSubmit = async () => {
        const customerName = 'firstName' in customer
            ? `${customer.firstName} ${customer.lastName}`
            : customer.tradeName;

        dispatch(SliceGlobalModal.actions.openModal({
            modalType: "info",
            message: `${customerName} ${t('customer.modals.newAccount.messages.accountOpeningConfirmation')}`,
            multipleButtons: true,
            modalAction: async () => {
                await handleCreateAccount();
            }
        }))
    };

    const handleCreateAccount = async () => {
        const response = await dispatch(thunkAccount.createAccount(formData.customerId, formData.initialBalance, formData.openingDate));
        if (response) {
            onSubmit();
            toast.success(t('customer.modals.newAccount.messages.accountOpeningSuccess'));
            onClose();
        }
    }

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>{t('customer.modals.newAccount.title')}</DialogTitle>
            <DialogContent dividers>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <TextField
                        size="small"
                        label={t('customer.modals.newAccount.form.customerNumber')}
                        fullWidth
                        value={customer.customer.id || ""}
                        disabled
                    />

                    {'firstName' in customer ? <TextField
                        size="small"
                        label={t('customer.modals.newAccount.form.fullName')}
                        fullWidth
                        value={customer.firstName + " " + customer.lastName}
                        disabled
                    /> :
                        (<>
                            <TextField
                                size="small"
                                label={t('customer.modals.newAccount.form.tradeName')}
                                fullWidth
                                value={customer.tradeName}
                                disabled
                            />
                            <TextField
                                label={t('customer.modals.newAccount.form.vkn')}
                                fullWidth
                                value={customer.taxNumber}
                                disabled
                            />
                        </>)}
                    <TextField
                        size="small"
                        label={t('customer.modals.newAccount.form.initialBalance')}
                        type="number"
                        fullWidth
                        value={formData.initialBalance}
                        onChange={e => handleChange('initialBalance', e.target.value)}
                        onKeyDown={(e) => {
                            if (['-', '+', 'e', '/'].includes(e.key)) {
                                e.preventDefault();
                            }
                        }}
                    />
                    <TextField
                        size="small"
                        label={t('customer.modals.newAccount.form.accountOpeningDate')}
                        type="text"
                        fullWidth
                        value={formData.openingDate.toLocaleDateString('tr-TR', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric'
                        })}
                        disabled
                    />
                    <Box sx={{
                        mt: 3,
                        p: 2,
                        backgroundColor: '#f8f9fa',
                        borderRadius: 2,
                        border: '1px solid #e9ecef'
                    }}>
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={formData.isApproved}
                                    onChange={e => handleChange('isApproved', e.target.checked)}
                                    sx={{ alignSelf: 'flex-start', mt: -0.5 }}
                                />
                            }
                            label={
                                <Box sx={{ ml: 1 }}>
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            fontSize: '0.75rem',
                                            fontWeight: 400,
                                            color: '#6c757d',
                                            lineHeight: 1.4,
                                            textAlign: 'justify'
                                        }}
                                    >
                                        {t('customer.modals.newAccount.approval.title')}
                                    </Typography>
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            fontSize: '0.75rem',
                                            fontWeight: 400,
                                            color: '#6c757d',
                                            lineHeight: 1.4,
                                            textAlign: 'justify'
                                        }}
                                    >
                                        {t('customer.modals.newAccount.approval.description')}
                                    </Typography>
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            fontSize: '0.75rem',
                                            fontWeight: 500,
                                            color: '#495057',
                                            lineHeight: 1.4,
                                            mt: 1,
                                            textAlign: 'justify'
                                        }}
                                    >
                                        {t('customer.modals.newAccount.approval.confirmation')}
                                    </Typography>
                                </Box>
                            }
                            sx={{
                                alignItems: 'flex-start',
                                m: 0,
                                width: '100%'
                            }}
                        />
                    </Box>

                </Box>

            </DialogContent>

            <DialogActions>
                <Button onClick={onClose} color="inherit">
                    {t('customer.modals.newAccount.buttons.cancel')}
                </Button>
                <Button
                    onClick={handleSubmit}
                    disabled={!formData.isApproved}
                    variant="contained"
                    color="success"
                >
                    {t('customer.modals.newAccount.buttons.next')}
                </Button>
            </DialogActions>
        </Dialog>
    );
}