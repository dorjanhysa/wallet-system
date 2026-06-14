import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login';
import { AccountsComponent } from './features/accounts/accounts';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'accounts', component: AccountsComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
];
