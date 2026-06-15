import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Account, CreateAccountRequest } from '../../models/account';
import { AmountRequest, Transaction } from '../../models/transaction';
import { Page } from '../../models/page';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/accounts`;

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(this.baseUrl);
  }

  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, request);
  }

  getAccount(id: string): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${id}`);
  }

  deposit(id: string, request: AmountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/${id}/deposit`, request);
  }

  withdraw(id: string, request: AmountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/${id}/withdraw`, request);
  }

  getTransactions(id: string): Observable<Page<Transaction>> {
    return this.http.get<Page<Transaction>>(`${this.baseUrl}/${id}/transactions`);
  }
}
