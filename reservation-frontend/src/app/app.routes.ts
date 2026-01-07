import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { ChatComponent } from './features/chat/chat.component';
import { authGuard } from './core/guards/auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },

    // Authenticated Routes (Wrapped in MainLayout)
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: 'chat', component: ChatComponent },
            {
                path: 'reservations',
                loadComponent: () => import('./features/reservations/user-reservations/user-reservations.component').then(m => m.UserReservationsComponent)
            },
            {
                path: 'reservations/new',
                loadComponent: () => import('./features/reservations/create-reservation/create-reservation.component').then(m => m.CreateReservationComponent)
            },
            {
                path: 'admin/reservations',
                loadComponent: () => import('./features/admin/admin-reservations/admin-reservations.component').then(m => m.AdminReservationsComponent)
            },
            { path: '', redirectTo: 'chat', pathMatch: 'full' }
        ]
    },

    { path: '**', redirectTo: 'chat' }
];
