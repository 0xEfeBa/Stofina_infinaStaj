export const customerType = {
    BIREYSEL: 'individual',
    KURUMSAL: 'corporate',
};

// Çeviri anahtarları için tip tanımı
export type CustomerTypeKey = 'individual' | 'corporate';

// Müşteri tipi değerlerini çeviri anahtarlarına dönüştüren yardımcı fonksiyon
export const getCustomerTypeTranslationKey = (type: string): CustomerTypeKey => {
    switch (type) {
        case 'Bireysel':
        case 'individual':
            return 'individual';
        case 'Kurumsal':
        case 'corporate':
            return 'corporate';
        default:
            return 'individual';
    }
};