"use client"
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceCustomer } from '@/slice/CustomerSlice';
import { CorporateCustomer, IndividualCustomer } from '@/types/customer';
import React from 'react'
import { useState, useEffect } from "react";
import Image from 'next/image';
import { customerType } from '@/constants/customerType';
import { mockCustomers } from '@/types/customer';
import { useTranslation } from 'react-i18next';
import { thunkCustomer } from '@/thunks/customerThunk';
import { useSelectorCustom } from '@/store';
//TODO: Type klasöründe tanımlanacak


const AutoCompleteCustomerSearch = () => {
    const dispatch = useDispatchCustom(); // redux dispatch hook
    const { t } = useTranslation();
    const { individualCustomers, corporateCustomers } = useSelectorCustom((state) => state.customer);


    // Customer type selection state
    const [selectedType, setSelectedType] = useState<string>(customerType.BIREYSEL);
    const [query, setQuery] = useState('');
    const [filteredCustomers, setFilteredCustomers] = useState<IndividualCustomer[] | CorporateCustomer[]>([]);

    const [showDropdown, setShowDropdown] = useState(false); // dropdown menü gösterimi



    useEffect(() => { // arama yaparken 

        if (query.length < 2) {
            setFilteredCustomers([]);
            setShowDropdown(false);
            return;
        }

        if (selectedType === customerType.BIREYSEL) {
            // Bireysel müşteriler için filtreleme
            const filtered = (individualCustomers || [])?.filter((c: IndividualCustomer) =>
                c?.firstName?.toLowerCase().includes(query.toLowerCase()) ||
                c?.lastName?.toLowerCase().includes(query.toLowerCase()) ||
                c?.tckn?.includes(query)
            );
            setFilteredCustomers(filtered);
        } else if (selectedType === customerType.KURUMSAL) {
            // Kurumsal müşteriler için filtreleme
            const filtered = (corporateCustomers || [])?.filter((c: CorporateCustomer) =>
                c?.tradeName?.toLowerCase().includes(query.toLowerCase()) ||
                c?.taxNumber?.includes(query)
            );
            setFilteredCustomers(filtered);
        }

        setShowDropdown(true);
    }, [query, selectedType, individualCustomers, corporateCustomers]); // query değiştiğinde veya selectedType değiştiğinde veya individualCustomers veya corporateCustomers değiştiğinde çalışır


    // render edildiğinde müşteri listesini getir 
    useEffect(() => {
        dispatch(thunkCustomer.getIndividuals());
        dispatch(thunkCustomer.getCorporateCustomers());
    }, []);



    const handleCustomerSelect = (customer: IndividualCustomer | CorporateCustomer) => { // müşteri seçildiğinde
        if (selectedType === customerType.BIREYSEL) {
            dispatch(SliceCustomer.actions.setSelectedIndividualCustomer(customer as IndividualCustomer));
            setQuery(t('customer.search.dropdown.selectedCustomer') + (customer as IndividualCustomer).firstName);
        } else if (selectedType === customerType.KURUMSAL) {
            dispatch(SliceCustomer.actions.setSelectedCorporateCustomer(customer as CorporateCustomer));
            setQuery(t('customer.search.dropdown.selectedCustomer') + (customer as CorporateCustomer).tradeName);
        }
        setShowDropdown(false);
        setFilteredCustomers([]);
    };

    const handleClearInput = () => { // arama kutusunu temizlediğinde
        setQuery('');
        if (selectedType === customerType.BIREYSEL) {
            dispatch(SliceCustomer.actions.setSelectedIndividualCustomer(null));
        } else if (selectedType === customerType.KURUMSAL) {
            dispatch(SliceCustomer.actions.setSelectedCorporateCustomer(null));
        }
        setShowDropdown(false);
        setFilteredCustomers([]);
    };

    return (
        <div className=" min-w-4xl ">
            <div className="max-w-3xl mx-auto">
                {/* Customer Type Toggle */}
                <div className="mb-6">
                    <div className="relative inline-flex bg-gray-100 rounded-lg p-1 w-full max-w-md ">
                        <button
                            type="button"
                            onClick={() => {
                                setSelectedType(customerType.BIREYSEL)
                                dispatch(SliceCustomer.actions.setSelectedIndividualCustomer(null));
                                dispatch(SliceCustomer.actions.setSelectedCorporateCustomer(null));
                            }}
                            className={`flex-1 cursor-pointer px-4 py-2 text-sm font-semibold rounded-md transition-all duration-200 flex items-center justify-center gap-2 ${selectedType === customerType.BIREYSEL
                                ? 'bg-white text-[#813FB4] shadow-sm border border-gray-200'
                                : 'text-gray-600 hover:text-gray-800'
                                }`}
                        >
                            <Image
                                src="/assets/icons/user.png"
                                alt="User"
                                width={16}
                                height={16}
                                className="w-4 h-4"
                            />  {t('customer.search.customerType.individual')}</button>

                        <button
                            type="button"
                            onClick={() => {
                                setSelectedType(customerType.KURUMSAL)
                                dispatch(SliceCustomer.actions.setSelectedIndividualCustomer(null));
                                dispatch(SliceCustomer.actions.setSelectedCorporateCustomer(null));
                            }}
                            className={`flex-1 cursor-pointer px-4 py-2 text-sm font-semibold rounded-md transition-all duration-200 flex items-center justify-center gap-2 ${selectedType === customerType.KURUMSAL
                                ? 'bg-white font-bold text-[#813FB4] shadow-sm border border-gray-200'
                                : 'text-gray-600 hover:text-gray-800'
                                }`}
                        >
                            <Image
                                src="/assets/icons/corporation.png"
                                alt="Corporation"
                                width={16}
                                height={16}
                                className="w-4 h-4"
                            />{t('customer.search.customerType.corporate')}</button>
                    </div>
                </div>

                <div className="relative">
                    <div className="relative">
                        <svg className={`absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 ${customerType ? 'text-gray-400' : 'text-gray-300'
                            }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                        </svg>
                        <input // arama kutusu
                            type="text"
                            placeholder={customerType ? t('customer.search.placeholder.enabled') : t('customer.search.placeholder.disabled')}
                            value={query}
                            disabled={!customerType}
                            onChange={(e) => {
                                setQuery(e.target.value);
                                dispatch(SliceCustomer.actions.setSelectedIndividualCustomer(null));
                                dispatch(SliceCustomer.actions.setSelectedCorporateCustomer(null));
                            }}
                            className={`w-full pl-10 pr-10 py-1.5 rounded-md border transition-all duration-200 text-sm ${customerType
                                ? 'border-gray-300 focus:outline-none focus:ring-1 focus:ring-[#813FB4] bg-white text-gray-900'
                                : 'border-gray-200 bg-gray-50 text-gray-400 cursor-not-allowed'
                                }`}
                        />
                        {query && ( // arama kutusunu temizle butonu
                            <button
                                onClick={handleClearInput}
                                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none"
                                type="button"
                            >
                                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        )}
                    </div>

                    {showDropdown && filteredCustomers.length > 0 && ( // dropdown menü
                        <ul className="absolute z-10 w-full mt-2 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto">
                            {filteredCustomers.map((customer, index) => (
                                <li
                                    key={index}
                                    className={`px-4 py-2 hover:bg-blue-100 cursor-pointer text-sm ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}
                                    onClick={() => handleCustomerSelect(customer)}
                                >
                                    <div className="grid grid-cols-4 ">
                                        <div className="flex col-span-2 gap-2">
                                            <span className="font-semibold text-gray-900 text-sm">{t('customer.search.dropdown.customerName')}</span>
                                            <span className="text-gray-700 truncate">
                                                {selectedType === customerType.BIREYSEL
                                                    ? `${(customer as IndividualCustomer).firstName} ${(customer as IndividualCustomer).lastName} `
                                                    : (customer as CorporateCustomer).tradeName
                                                }
                                            </span>
                                        </div>

                                        <div className="flex gap-2 col-span-2">
                                            <span className="font-semibold text-gray-900 text-sm">
                                                {t('customer.search.dropdown.customerId')}
                                            </span>
                                            <span className="text-gray-700">
                                                {selectedType === customerType.BIREYSEL
                                                    ? (customer as IndividualCustomer).tckn
                                                    : (customer as CorporateCustomer).taxNumber
                                                }
                                            </span>
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
}

export default AutoCompleteCustomerSearch