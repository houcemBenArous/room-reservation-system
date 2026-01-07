import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

interface LoginResponse {
    token: string;
    username: string;
    role: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = 'http://localhost:8888/api/auth';
    private tokenKey = 'auth_token';

    // Signals for reactive state
    currentUser = signal<{ username: string, role: string } | null>(null);

    constructor(private http: HttpClient, private router: Router) {
        this.loadUserFromToken();
    }

    login(credentials: { username: string, password: string }): Observable<LoginResponse> {
        console.log('[AuthService] Attempting login for:', credentials.username);
        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
            tap(response => {
                console.log('[AuthService] Login response received:', response);
                console.log('[AuthService] Token:', response.token ? response.token.substring(0, 50) + '...' : 'NULL');
                localStorage.setItem(this.tokenKey, response.token);
                console.log('[AuthService] Token stored in localStorage');
                this.loadUserFromToken();
            })
        );
    }

    logout() {
        localStorage.removeItem(this.tokenKey);
        this.currentUser.set(null);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        const token = localStorage.getItem(this.tokenKey);
        console.log('[AuthService] getToken() called, token exists:', !!token);
        return token;
    }

    isLoggedIn(): boolean {
        const token = this.getToken();
        if (!token) return false;

        try {
            const decoded: any = jwtDecode(token);
            const isValid = decoded.exp * 1000 > Date.now();
            console.log('[AuthService] Token valid:', isValid, 'Expires:', new Date(decoded.exp * 1000));
            return isValid;
        } catch (e) {
            console.log('[AuthService] Token decode error:', e);
            return false;
        }
    }

    private loadUserFromToken() {
        const token = this.getToken();
        if (token && this.isLoggedIn()) {
            const decoded: any = jwtDecode(token);
            this.currentUser.set({
                username: decoded.sub,
                role: decoded.role
            });
            console.log('[AuthService] User loaded from token:', this.currentUser());
        } else {
            this.currentUser.set(null);
        }
    }
}
