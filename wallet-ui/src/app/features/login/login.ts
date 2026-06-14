import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  username = signal('');
  password = signal('');
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  onSubmit(): void {
    if (this.username().trim() === '' || this.password().trim() === '') {
      this.errorMessage.set('Please enter both username and password.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login({
      username: this.username(),
      password: this.password()
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/accounts']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.status === 401 ? 'Invalid credentials.' : 'An error occurred. Please try again.'
        );
      }
    });
  }
}
