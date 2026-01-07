import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservationService } from '../../../core/services/reservation.service';
import { Reservation } from '../../../core/models/reservation.model';

@Component({
  selector: 'app-admin-reservations',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container mx-auto p-6">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">All Reservations (Admin)</h2>

      <div *ngIf="errorMessage()" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
        <p>{{ errorMessage() }}</p>
      </div>

      <div *ngIf="loading()" class="text-center py-4">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p class="mt-2 text-gray-600">Loading all reservations...</p>
      </div>

      <div *ngIf="!loading() && reservations().length > 0" class="bg-white shadow-md rounded-lg overflow-hidden">
        <table class="min-w-full leading-normal">
          <thead>
            <tr>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">User</th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Room</th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Date</th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Time</th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
              <th class="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let res of reservations()" class="hover:bg-gray-50 transition duration-150">
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">{{ res.id }}</td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm font-semibold">{{ res.username }}</td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">{{ res.roomName || res.roomId }}</td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">{{ res.date }}</td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">{{ res.startTime.slice(0, 5) }} - {{ res.endTime.slice(0, 5) }}</td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                  [ngClass]="{
                    'bg-green-100 text-green-800': res.status === 'CONFIRMED',
                    'bg-red-100 text-red-800': res.status === 'CANCELLED'
                  }">
                  {{ res.status }}
                </span>
              </td>
              <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                <div class="flex space-x-2">
                    <button *ngIf="res.status !== 'CANCELLED'"
                      (click)="cancelReservation(res.id!)"
                      class="text-orange-600 hover:text-orange-900 text-xs font-bold">
                      CANCEL
                    </button>
                    <button 
                      (click)="deleteReservation(res.id!)"
                      class="text-red-600 hover:text-red-900 text-xs font-bold">
                      DELETE
                    </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AdminReservationsComponent implements OnInit {
  reservationService = inject(ReservationService);

  reservations = signal<Reservation[]>([]);
  loading = signal<boolean>(true);
  errorMessage = signal<string>('');

  ngOnInit() {
    this.loadAllReservations();
  }

  loadAllReservations() {
    this.loading.set(true);
    this.reservationService.getAllReservations().subscribe({
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
  }

  cancelReservation(id: number) {
    if (confirm('Are you sure you want to cancel this reservation?')) {
      this.reservationService.cancelReservation(id).subscribe({
        next: (updatedRes) => {
          this.reservations.update(list => list.map(r => r.id === id ? updatedRes : r));
        },
        error: (err) => alert(err.message)
      });
    }
  }

  deleteReservation(id: number) {
    if (confirm('Permanently delete this reservation?')) {
      this.reservationService.deleteReservation(id).subscribe({
        next: () => {
          this.reservations.update(list => list.filter(r => r.id !== id));
        },
        error: (err) => alert(err.message)
      });
    }
  }
}
