import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CreateAccountRequest } from '../../../models/account';

@Component({
  selector: 'app-create-account-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule],
  templateUrl: './create-account-dialog.html',
  styleUrl: './create-account-dialog.scss',
})
export class CreateAccountDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<CreateAccountDialogComponent>);

  currency = signal('EUR');
  readonly currencies = ['EUR', 'USD', 'GBP'];

  confirm(): void {
    const request: CreateAccountRequest = { currency: this.currency() };
    this.dialogRef.close(request);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
