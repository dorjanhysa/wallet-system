import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../../models/auth';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  private readonly accessToken = signal<string | null>(this.loadToken());

  isAuthenticated = computed(() => this.accessToken() !== null);

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/api/auth/login`, credentials)
      .pipe(
        tap(response => this.setSession(response))
      );
  }

  logout(): void {
    this.accessToken.set(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  getToken(): string | null {
    return this.accessToken();
  }

  private setSession(response: LoginResponse): void {
    this.accessToken.set(response.accessToken);
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
  }

  private loadToken(): string | null {
    return localStorage.getItem('accessToken');
  }
}
