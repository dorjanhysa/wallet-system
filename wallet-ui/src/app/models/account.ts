export interface Account {
  id: string;
  ownerUsername: string;
  balance: number;
  currency: string;
}

export interface CreateAccountRequest {
  currency: string;
}
