import { Component, inject, OnInit, signal } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountService } from '../../../core/services/account';
import { MatDialog } from '@angular/material/dialog';
import { Account } from '../../../models/account';
import { AmountRequest, Transaction } from '../../../models/transaction';
import { DatePipe, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-account-detail',
  imports: [MatCardModule, MatButtonModule, MatToolbarModule, MatListModule, DecimalPipe, DatePipe],
  templateUrl: './account-detail.html',
  styleUrl: './account-detail.scss',
})
export class AccountDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly accountService = inject(AccountService);
  private readonly dialog = inject(MatDialog);

  account = signal<Account | null>(null);
  transactions = signal<Transaction[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  private accountId = '';

  ngOnInit(): void {
    this.accountId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.accountService.getAccount(this.accountId).subscribe({
      next: (acc) => {
        this.account.set(acc);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Account not found');
        this.loading.set(false);
      },
    });
    this.accountService.getTransactions(this.accountId).subscribe({
      next: (page) => this.transactions.set(page.content),
      error: () => this.errorMessage.set('Error loading transactions'),
    });
  }

  deposit(): void {
    const amount = prompt('Amount to deposit:');
    if (!amount) return;
    const request: AmountRequest = { amount: Number(amount) };
    this.accountService.deposit(this.accountId, request).subscribe({
      next: () => this.loadData(),
      error: () => this.errorMessage.set('Error depositing funds'),
    });
  }

  withdraw(): void {
    const amount = prompt('Amount to withdraw:');
    if (!amount) return;
    const request: AmountRequest = { amount: Number(amount) };
    this.accountService.withdraw(this.accountId, request).subscribe({
      next: () => this.loadData(),
      error: (err) =>
        this.errorMessage.set(
          err.status === 422 ? 'Insufficient funds' : 'Error withdrawing funds',
        ),
    });
  }

  goBack(): void {
    this.router.navigate(['/accounts']);
  }
}
