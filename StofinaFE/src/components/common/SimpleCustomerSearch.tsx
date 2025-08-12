"use client";

import React, { useEffect, useState } from "react";
import {
  IndividualCustomer,
  CorporateCustomer,
  CustomerDetail,
} from "@/types/customer";

export type UnifiedCustomer = IndividualCustomer | CorporateCustomer;

interface SimpleCustomerSearchProps {
  onSelect?: (customer: UnifiedCustomer | null) => void;
}

const SimpleCustomerSearch: React.FC<SimpleCustomerSearchProps> = ({
  onSelect,
}) => {
  const [query, setQuery] = useState("");
  const [allCustomers, setAllCustomers] = useState<UnifiedCustomer[]>([]);
  const [filteredCustomers, setFilteredCustomers] = useState<UnifiedCustomer[]>(
    []
  );
  const [showDropdown, setShowDropdown] = useState(false);

  useEffect(() => {
    async function fetchCustomers() {
      try {
        const token = localStorage.getItem("accessToken");
        const [indRes, corpRes] = await Promise.all([
          fetch("http://localhost:9003/api/v1/individual", {
            headers: {
              accept: "*/*",
              Authorization: `Bearer ${token}`,
            },
          }),
          fetch("http://localhost:9003/api/v1/corporate", {
            headers: {
              accept: "*/*",
              Authorization: `Bearer ${token}`,
            },
          }),
        ]);

        const individualCustomers: IndividualCustomer[] = await indRes.json();
        const corporateCustomers: CorporateCustomer[] = await corpRes.json();

        setAllCustomers([...individualCustomers, ...corporateCustomers]);
      } catch (error) {
        console.error("Müşteriler çekilemedi:", error);
      }
    }
    fetchCustomers();
  }, []);

  useEffect(() => {
    if (query.length < 2) {
      setFilteredCustomers([]);
      setShowDropdown(false);
      return;
    }

    const q = query.toLowerCase();
    const filtered = allCustomers.filter((c) => {
      if ("firstName" in c) {
        return (
          c.firstName.toLowerCase().includes(q) ||
          c.lastName.toLowerCase().includes(q) ||
          c.email.toLowerCase().includes(q) ||
          c.phone.includes(q)
        );
      } else {
        return (
          c.tradeName.toLowerCase().includes(q) ||
          c.representativeName.toLowerCase().includes(q) ||
          c.representativeEmail.toLowerCase().includes(q) ||
          c.representativePhone.includes(q)
        );
      }
    });

    setFilteredCustomers(filtered);
    setShowDropdown(true);
  }, [query, allCustomers]);

  const handleCustomerSelect = (customer: UnifiedCustomer) => {
    const displayName =
      "firstName" in customer
        ? `${customer.firstName} ${customer.lastName}`
        : customer.tradeName;

    setQuery(displayName);
    setShowDropdown(false);
    setFilteredCustomers([]);
    if (onSelect) onSelect(customer);
  };

  const handleClearInput = () => {
    setQuery("");
    if (onSelect) onSelect(null);
    setShowDropdown(false);
    setFilteredCustomers([]);
  };

  return (
    <div className="w-full max-w-2xl mx-auto relative">
      <input
        type="text"
        placeholder="Müşteri adı, e-posta veya telefon girin..."
        value={query}
        onChange={(e) => {
          setQuery(e.target.value);
          if (onSelect) onSelect(null);
        }}
        className="w-full pl-10 pr-10 py-2 border border-gray-400 rounded-md text-sm font-semibold
           hover:border-green-600 focus:border-green-600 focus:ring-1 focus:ring-green-600 focus:outline-none"
      />

      {query && (
        <button
          onClick={handleClearInput}
          className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
          type="button"
          aria-label="Clear input"
        >
          <svg
            className="h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      )}
      <svg
        className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
        />
      </svg>

      {showDropdown && filteredCustomers.length > 0 && (
        <ul className="absolute z-10 w-full mt-2 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto">
          {filteredCustomers.map((customer, index) => {
            const displayName =
              "firstName" in customer
                ? `${customer.firstName} ${customer.lastName}`
                : customer.tradeName;
            const email =
              "firstName" in customer
                ? customer.email
                : customer.representativeEmail;
            const type = "firstName" in customer ? "Bireysel" : "Kurumsal";

            return (
              <li
                key={customer.id}
                className={`px-4 py-2 cursor-pointer text-sm hover:bg-blue-100 ${index % 2 === 0 ? "bg-[#813FB4]/10" : "bg-white"
                  }`}
                onClick={() => handleCustomerSelect(customer)}
              >
                <div className="flex justify-between">
                  <span>
                    {displayName} ({type})
                  </span>
                  <span className="text-gray-500 text-xs">{email}</span>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default SimpleCustomerSearch;
