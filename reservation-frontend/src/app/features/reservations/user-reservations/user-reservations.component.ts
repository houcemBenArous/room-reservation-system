import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservationService } from '../../../core/services/reservation.service';
import { AuthService } from '../../../core/services/auth.service';
import { Reservation } from '../../../core/models/reservation.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-reservations',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container mx-auto p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-800">My Reservations</h2>
        <a routerLink="/reservations/new" class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition duration-200">
          + New Reservation
        </a>
      </div>

      <div *ngIf="errorMessage()" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
        <p>{{ errorMessage() }}</p>
      </div>

      <div *ngIf="loading()" class="text-center py-4">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p class="mt-2 text-gray-600">Loading reservations...</p>
      </div>

      <div *ngIf="!loading() && reservations().length === 0" class="bg-blue-50 border-l-4 border-blue-500 text-blue-700 p-4" role="alert">
        <p>You have no reservations yet.</p>
      </div>

      <div *ngIf="!loading() && reservations().length > 0" class="bg-white shadow-md rounded-lg overflow-hidden">
        <table class="min-w-full leading-normal">
          <thead>
            <tr>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Room
              </th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Date
              </th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Time
              </th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Status
              </th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let res of reservations()" class="hover:bg-gray-50 transition duration-150">
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <div class="flex items-center">
                  <div class="ml-3">
                    <p class="text-gray-900 whitespace-no-wrap font-medium">
                      {{ res.roomName || 'Room #' + res.roomId }}
                    </p>
                  </div>
                </div>
              </td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <p class="text-gray-900 whitespace-no-wrap">{{ res.date }}</p>
              </td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <p class="text-gray-900 whitespace-no-wrap">
                  {{ res.startTime.slice(0, 5) }} - {{ res.endTime.slice(0, 5) }}
                </p>
              </td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <span class="relative inline-block px-3 py-1 font-semibold leading-tight"
                  [ngClass]="{
                    'text-green-900': res.status === 'CONFIRMED',
                    'text-red-900': res.status === 'CANCELLED'
                  }">
                  <span aria-hidden="true" class="absolute inset-0 opacity-50 rounded-full"
                    [ngClass]="{
                      'bg-green-200': res.status === 'CONFIRMED',
                      'bg-red-200': res.status === 'CANCELLED'
                    }">
                  </span>
                  <span class="relative">{{ res.status }}</span>
                </span>
              </td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <button *ngIf="res.status !== 'CANCELLED'"
                  (click)="cancelReservation(res.id!)"
                  class="text-red-600 hover:text-red-900 font-medium focus:outline-none underline">
                  Cancel
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class UserReservationsComponent implements OnInit {
  reservationService = inject(ReservationService);
  authService = inject(AuthService);

  reservations = signal<Reservation[]>([]);
  loading = signal<boolean>(true);
  errorMessage = signal<string>('');

  ngOnInit() {
    this.loadReservations();
  }

  loadReservations() {
    this.loading.set(true);
    const currentUser = this.authService.currentUser();

    if (currentUser) {
      this.reservationService.getUserReservations(currentUser.username).subscribe({
        next: (data) => {
          this.reservations.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.errorMessage.set('Failed to load reservations.');
          this.loading.set(false);
          console.error(err);
        }
      });
    } else {
      this.errorMessage.set('User not logged in.');
      this.loading.set(false);
    }
  }

  cancelReservation(id: number) {
    if (confirm('Are you sure you want to cancel this reservation?')) {
      this.reservationService.cancelReservation(id).subscribe({
        next: (updatedRes) => {
          // Update the list immediately
          this.reservations.update(reservations =>
            reservations.map(r => r.id === id ? updatedRes : r)
          );
        },
        error: (err) => {
          alert('Failed to cancel reservation: ' + err.message);
        }
      });
    }
  }
}
