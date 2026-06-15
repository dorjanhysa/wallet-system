import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login';
import { AccountsComponent } from './features/accounts/accounts';
import { AccountDetailComponent } from './features/accounts/account-detail/account-detail';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'accounts', component: AccountsComponent },
  { path: 'accounts/:id', component: AccountDetailComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
];
