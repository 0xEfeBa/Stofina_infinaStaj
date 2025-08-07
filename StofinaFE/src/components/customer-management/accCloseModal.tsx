import { Account } from '@/types/account';
import { Customer } from '@/types/customer';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Button, FormControlLabel, Checkbox, Grid, Box, Typography, Divider, Alert } from '@mui/material';
import { AlertTriangle } from 'lucide-react';
import React from 'react'
import { useTranslation } from 'react-i18next';

interface Props {
    open: boolean;
    onClose: () => void;
    onSubmit: (formData: any) => void;
    customer: Customer
    account: Account
}

const AccCloseModal = ({ open, onClose, onSubmit, customer, account }: Props) => {
    const { t } = useTranslation();

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('tr-TR', {
            style: 'currency',
            currency: 'TRY',
            minimumFractionDigits: 2
        }).format(amount);
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle sx={{ backgroundColor: '#f9fafb', display: 'flex', alignItems: 'center', gap: 1 }}>
                <AlertTriangle className="w-6 h-6 text-red-500" />
                <Typography variant="h6" fontWeight="bold">{t('customer.modals.closeAccount.title')}</Typography>
            </DialogTitle>
            <DialogContent dividers>
                <Typography variant="body2" color="textSecondary" mb={2}>
                    {t('customer.modals.closeAccount.description')}
                </Typography>
                <Grid container spacing={2} >
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            size="small"
                            label={t('customer.modals.closeAccount.form.customerName')}
                            fullWidth
                            value={customer.tradeName || `${customer.firstName} ${customer.lastName}`}
                            disabled
                            variant="outlined"
                        />
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            size="small"
                            label={t('customer.modals.closeAccount.form.customerNumber')}
                            fullWidth
                            value={customer.id}
                            disabled
                            variant="outlined"
                        />
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            size="small"
                            label={t('customer.modals.closeAccount.form.accountNumber')}
                            fullWidth
                            value={account.accountNo}
                            disabled
                            variant="outlined"
                        />
                    </Grid>
                </Grid>

                <Alert
                    severity="warning"
                    sx={{
                        mt: 3,
                        mb: 2,
                        backgroundColor: '#fef3c7',
                        borderColor: '#f59e0b',
                        '& .MuiAlert-icon': {
                            color: '#f59e0b'
                        }
                    }}
                >
                    <Typography variant="body2" fontWeight="600" sx={{ mb: 1, color: '#92400e' }}>
                        {t('customer.modals.closeAccount.warning.title')}
                    </Typography>
                    <Typography variant="body2" sx={{ color: '#92400e' }}>
                        {t('customer.modals.closeAccount.warning.description', {
                            portfolioValue: formatCurrency(account.portfolioValue || 0),
                            balance: formatCurrency(account.balance || 0)
                        })}
                    </Typography>
                </Alert>

                <Box sx={{
                    bgcolor: '#f3f4f6',
                    p: 2.5,
                    borderRadius: 2,
                    border: '1px solid #d1d5db',
                    mt: 2
                }}>
                    <Typography variant="subtitle2" fontWeight="600" sx={{ mb: 1, color: '#374151' }}>
                        {t('customer.modals.closeAccount.regulations.title')}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {t('customer.modals.closeAccount.regulations.subtitle')}
                    </Typography>
                    <Box component="ul" sx={{ pl: 2, m: 0, listStyleType: 'disc' }}>
                        {(t('customer.modals.closeAccount.regulations.items', { returnObjects: true }) as string[]).map((item: string, index: number) => (
                            <Typography key={index} component="li" variant="body2" color="text.secondary" sx={{ mb: 0.8 }}>
                                {item}
                            </Typography>
                        ))}
                    </Box>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Typography variant="body2" color="error" align="center">
                    {t('customer.modals.closeAccount.irreversible')}
                </Typography>
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 2 }}>
                <Button variant="outlined" onClick={onClose} color="primary">
                    {t('customer.modals.closeAccount.buttons.cancel')}
                </Button>
                <Button variant="contained" onClick={onSubmit} color="error">
                    {t('customer.modals.closeAccount.buttons.closeAccount')}
                </Button>
            </DialogActions>
        </Dialog>
    );
};


export default AccCloseModal