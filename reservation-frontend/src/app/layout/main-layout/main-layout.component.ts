import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-main-layout',
    standalone: true,
    imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
    template: `
    <div class="flex h-screen bg-gray-100 font-sans">
      
      <!-- Sidebar -->
      <div class="w-64 bg-gray-900 text-white flex flex-col shadow-xl">
        <div class="p-6 border-b border-gray-800">
            <h1 class="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-indigo-500">
                ResaBot AI
            </h1>
            <p class="text-xs text-gray-500 mt-1">Système Intelligent</p>
        </div>

        <div class="p-6 flex-1 overflow-y-auto">
            <div class="mb-8">
                <h3 class="text-xs uppercase text-gray-500 font-semibold tracking-wider mb-4">
                    Votre Espace ({{ authService.currentUser()?.role }})
                </h3>

                <nav class="space-y-2">
                    <!-- SHARED LINKS -->
                    <a routerLink="/chat" routerLinkActive="bg-gray-800"
                       class="flex items-center space-x-3 px-4 py-3 rounded-lg text-white hover:bg-gray-800 transition-colors cursor-pointer text-gray-400">
                        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                        </svg>
                        <span>Chat</span>
                    </a>

                    <!-- ADMIN LINKS -->
                    <ng-container *ngIf="isAdmin">
                        <a routerLink="/admin/reservations" routerLinkActive="bg-gray-800"
                            class="flex items-center space-x-3 px-4 py-3 rounded-lg text-white hover:bg-gray-800 transition-colors cursor-pointer text-gray-400">
                            <svg class="h-5 w-5 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                            </svg>
                            <span>Gestion Réservations</span>
                        </a>
                    </ng-container>

                    <!-- USER LINKS -->
                    <ng-container *ngIf="!isAdmin">
                        <a routerLink="/reservations" routerLinkActive="bg-gray-800"
                            class="flex items-center space-x-3 px-4 py-3 rounded-lg text-white hover:bg-gray-800 transition-colors cursor-pointer text-gray-400">
                            <svg class="h-5 w-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            <span>Mes Réservations</span>
                        </a>
                    </ng-container>
                </nav>
            </div>
        </div>

        <!-- User Profile & Logout -->
        <div class="p-4 border-t border-gray-800">
            <div class="flex items-center p-2 rounded-lg bg-gray-800 mb-3">
                <div class="h-8 w-8 rounded-full bg-indigo-500 flex items-center justify-center text-sm font-bold">
                    {{ authService.currentUser()?.username?.charAt(0)?.toUpperCase() }}
                </div>
                <div class="ml-3">
                    <p class="text-sm font-medium text-white">{{ authService.currentUser()?.username }}</p>
                    <p class="text-xs text-gray-400">{{ authService.currentUser()?.role }}</p>
                </div>
            </div>
            <button (click)="logout()"
                class="w-full flex items-center justify-center space-x-2 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg transition-colors text-sm font-medium">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                <span>Déconnexion</span>
            </button>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="flex-1 overflow-auto bg-gray-50">
        <router-outlet></router-outlet>
      </div>

    </div>
  `
})
export class MainLayoutComponent {
    authService = inject(AuthService);

    get isAdmin() {
        return this.authService.currentUser()?.role === 'ADMIN';
    }

    logout() {
        this.authService.logout();
    }
}
