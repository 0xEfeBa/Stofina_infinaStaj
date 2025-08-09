import { customerType } from "@/constants/customerType";

export interface CustomerDetail {
  id: number;
  legalAddress: string;
  status: "ACTIVE" | "PASSIVE" | string; // Enum tanımı yapılabilir
}

export interface IndividualCustomer {
  id: number;
  tckn: string;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  customer: CustomerDetail;
}

export interface CorporateCustomer {
  tradeName: string;
  tradeRegistryNumber: string;
  taxNumber: string;
  taxOffice: string;
  representativeName: string;
  representativeTckn: string;
  representativePhone: string;
  representativeEmail: string;
  customer: CustomerDetail;
}

export const mockCustomers: IndividualCustomer[] = [];
