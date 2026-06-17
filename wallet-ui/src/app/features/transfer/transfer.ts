import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { AccountService } from '../../core/services/account';
import { MatToolbarModule } from '@angular/material/toolbar';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { TransferService } from '../../core/services/transfer';
import { Router } from '@angular/router';
import { Account } from '../../models/account';
import { Transfer } from '../../models/transfer';
import { interval, Subscription, switchMap, takeWhile } from 'rxjs';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-transfer',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatToolbarModule,
    DecimalPipe,
  ],
  templateUrl: './transfer.html',
  styleUrl: './transfer.scss',
})
export class TransferComponent implements OnInit, OnDestroy {
  private readonly accountService = inject(AccountService);
  private readonly transferService = inject(TransferService);
  private readonly router = inject(Router);

  accounts = signal<Account[]>([]);
  fromAccountId = signal('');
  toAccountId = signal('');
  amount = signal<number | null>(null);

  transfer = signal<Transfer | null>(null);
  submitting = signal(false);
  errorMessage = signal<string | null>(null);

  private pollSub?: Subscription;

  private readonly finalStates = ['COMPLETED', 'FAILED', 'COMPENSATED'];

  ngOnInit(): void {
    this.accountService.getAccounts().subscribe({
      next: (accs) => this.accounts.set(accs),
      error: () => this.errorMessage.set('Error loading accounts'),
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  submit(): void {
    if (!this.fromAccountId() || !this.toAccountId() || !this.amount()) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }
    if (this.fromAccountId() === this.toAccountId()) {
      this.errorMessage.set('Cannot transfer to the same account.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.transfer.set(null);

    this.transferService
      .createTransfer({
        fromAccountId: this.fromAccountId(),
        toAccountId: this.toAccountId(),
        amount: this.amount()!,
      })
      .subscribe({
        next: (t) => {
          this.transfer.set(t);
          this.submitting.set(false);
          this.startPolling(t.id);
        },
        error: () => {
          this.submitting.set(false);
          this.errorMessage.set('Error creating transfer.');
        },
      });
  }

  private startPolling(transferId: string): void {
    this.pollSub?.unsubscribe();

    this.pollSub = interval(1500)
      .pipe(
        switchMap(() => this.transferService.getTransfer(transferId)),
        takeWhile((t) => !this.finalStates.includes(t.status), true),
      )
      .subscribe({
        next: (t) => this.transfer.set(t),
        error: () => this.errorMessage.set('Error on monitoring the transfer'),
      });
  }

  reset(): void {
    this.transfer.set(null);
    this.pollSub?.unsubscribe();
  }

  goBack(): void {
    this.router.navigate(['/accounts']);
  }

  isFinal(): boolean {
    const t = this.transfer();
    return t ? this.finalStates.includes(t.status) : false;
  }

  isStepReached(step: string): boolean {
    const status = this.transfer()?.status;
    if (!status) return false;
    if (step === 'DEBIT')
      return ['DEBITED', 'COMPLETED', 'COMPENSATING', 'COMPENSATED'].includes(status);
    if (step === 'CREDIT') return ['COMPLETED'].includes(status);
    if (step === 'DONE') return status === 'COMPLETED';
    return false;
  }
}
