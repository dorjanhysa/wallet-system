export interface Transaction {
  id: string;
  type: string;
  amount: number;
  balanceAfter: number;
  createdAt: string;
}

export interface AmountRequest {
  amount: number;
}
