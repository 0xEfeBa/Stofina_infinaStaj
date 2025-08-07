"use client";

import React, { useEffect, useState } from 'react';
import { useDispatchCustom } from '@/hooks/useDispatchCustom';
import { SliceCustomer } from '@/slice/CustomerSlice';
import { Customer, mockCustomers } from '@/types/customer';

const SimpleCustomerSearch = () => {
    const dispatch = useDispatchCustom();
    const [query, setQuery] = useState('');
    const [filteredCustomers, setFilteredCustomers] = useState<Customer[]>([]);
    const [showDropdown, setShowDropdown] = useState(false);

    useEffect(() => {
        if (query.length < 2) {
            setFilteredCustomers([]);
            setShowDropdown(false);
            return;
        }

        const filtered = mockCustomers.filter(
    (c) =>
        `${c.firstName} ${c.lastName}`.toLowerCase().includes(query.toLowerCase()) ||
        (c.accountNumber && c.accountNumber.includes(query))
);

        setFilteredCustomers(filtered);
        setShowDropdown(true);
    }, [query]);

    const handleCustomerSelect = (customer: Customer) => {
        dispatch(SliceCustomer.actions.setSelectedCustomer(customer));
        setQuery('Müşteri: ' + `${customer.firstName} ${customer.lastName}`);
        setShowDropdown(false);
        setFilteredCustomers([]);
    };

    const handleClearInput = () => {
        setQuery('');
        dispatch(SliceCustomer.actions.setSelectedCustomer(null));
        setShowDropdown(false);
        setFilteredCustomers([]);
    };

    return (
        <div className="w-full max-w-2xl mx-auto relative">
            <input
                type="text"
                placeholder="Müşteri adı veya hesap numarası girin..."
                value={query}
                onChange={(e) => {
                    setQuery(e.target.value);
                    dispatch(SliceCustomer.actions.setSelectedCustomer(null));
                }}
                className="w-full pl-10 pr-10 py-2 border border-[#989898] rounded-md focus:outline-none focus:ring-1 focus:ring-[#439E54] text-sm"
            />
            {query && (
                <button
                    onClick={handleClearInput}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    type="button"
                >
                    <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            )}
            <svg
                className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>

            {showDropdown && filteredCustomers.length > 0 && (
                <ul className="absolute z-10 w-full mt-2 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto">
                    {filteredCustomers.map((customer, index) => (
                        <li
                            key={customer.id}
                            className={`px-4 py-2 cursor-pointer text-sm hover:bg-blue-100 ${index % 2 === 0 ? 'bg-[#813FB4]/10' : 'bg-white'}`}
                            onClick={() => handleCustomerSelect(customer)}
                        >
                            <div className="flex justify-between">
                                <span>{`${customer.firstName} ${customer.lastName}`}</span>
                                <span className="text-gray-500 text-xs">{customer.accountNumber}</span>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default SimpleCustomerSearch;
