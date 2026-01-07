import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './login.component.html'
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private router = inject(Router);

    loginForm = this.fb.group({
        username: ['', Validators.required],
        password: ['', Validators.required]
    });

    error: string | null = null;
    isLoading = false;

    onSubmit() {
        if (this.loginForm.valid) {
            this.isLoading = true;
            this.error = null;
            const { username, password } = this.loginForm.value;

            this.authService.login({ username: username!, password: password! }).subscribe({
                next: () => {
                    this.router.navigate(['/chat']);
                },
                error: () => {
                    this.error = 'Identifiants invalides';
                    this.isLoading = false;
                }
            });
        }
    }
}
