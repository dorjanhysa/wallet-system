import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { CreateTransferRequest, Transfer } from '../../models/transfer';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/transfers`;

  createTransfer(request: CreateTransferRequest): Observable<Transfer> {
    return this.http.post<Transfer>(this.baseUrl, request);
   }

   getTransfer(id: string): Observable<Transfer> {
    return this.http.get<Transfer>(`${this.baseUrl}/${id}`);
   }
}
