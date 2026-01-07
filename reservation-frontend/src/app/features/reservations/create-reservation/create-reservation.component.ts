import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReservationService } from '../../../core/services/reservation.service';
import { RoomService } from '../../../core/services/room.service';
import { AuthService } from '../../../core/services/auth.service';
import { CreateReservationRequest } from '../../../core/models/reservation.model';
import { Room } from '../../../core/models/room.model';

@Component({
  selector: 'app-create-reservation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container mx-auto p-6 max-w-lg">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">Réserver une salle</h2>

      <div *ngIf="errorMessage()" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6" role="alert">
        <p class="font-bold">Erreur</p>
        <p>{{ errorMessage() }}</p>
      </div>

      <form (ngSubmit)="onSubmit()" #reservationForm="ngForm" class="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
        
        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="room">
            Salle
          </label>
          <select 
            id="room" 
            name="roomId" 
            [(ngModel)]="request.roomId" 
            required
            class="shadow border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
            <option [ngValue]="null" disabled selected>-- Choisir une salle --</option>
            <option *ngFor="let room of rooms()" [value]="room.id">
              {{ room.name }} (Capacité: {{ room.capacity }}) 
              <span *ngIf="!room.available" class="text-red-500">[Indisponible]</span>
            </option>
          </select>
        </div>

        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="date">
            Date
          </label>
          <input 
            type="date" 
            id="date" 
            name="date" 
            [(ngModel)]="request.date" 
            required
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
        </div>

        <div class="flex space-x-4 mb-6">
          <!-- Start Time -->
          <div class="w-1/2">
            <label class="block text-gray-700 text-sm font-bold mb-2">
              Heure de début
            </label>
            <div class="flex space-x-2">
                <select [(ngModel)]="startHour" name="startHour" class="shadow border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
                    <option *ngFor="let h of hours" [value]="h">{{ h }}</option>
                </select>
                <span class="self-center">:</span>
                <select [(ngModel)]="startMinute" name="startMinute" class="shadow border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
                    <option value="00">00</option>
                    <option value="30">30</option>
                </select>
            </div>
          </div>

          <!-- End Time -->
          <div class="w-1/2">
            <label class="block text-gray-700 text-sm font-bold mb-2">
              Heure de fin
            </label>
             <div class="flex space-x-2">
                <select [(ngModel)]="endHour" name="endHour" class="shadow border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
                    <option *ngFor="let h of hours" [value]="h">{{ h }}</option>
                </select>
                <span class="self-center">:</span>
                <select [(ngModel)]="endMinute" name="endMinute" class="shadow border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
                    <option value="00">00</option>
                    <option value="30">30</option>
                </select>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <button 
            type="submit" 
            [disabled]="loading()"
            class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline w-full transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed">
            <span *ngIf="loading()">Réservation en cours...</span>
            <span *ngIf="!loading()">Confirmer la réservation</span>
          </button>
        </div>
        <div class="text-center mt-4">
             <a href="#" (click)="cancel($event)" class="text-blue-500 hover:underline">Annuler</a>
        </div>
      </form>
    </div>
  `
})
export class CreateReservationComponent implements OnInit {
  reservationService = inject(ReservationService);
  roomService = inject(RoomService);
  authService = inject(AuthService);
  router = inject(Router);

  rooms = signal<Room[]>([]);
  errorMessage = signal<string>('');
  loading = signal<boolean>(false);

  // Time selection helpers
  hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'));

  startHour = '09';
  startMinute = '00';
  endHour = '10';
  endMinute = '00';

  request: Partial<CreateReservationRequest> = {
    roomId: undefined,
    date: '',
  };

  ngOnInit() {
    this.loadRooms();
  }

  loadRooms() {
    this.roomService.getAllRooms().subscribe({
      next: (data) => {
        this.rooms.set(data.filter(r => r.available));
      },
      error: (err) => {
        console.error('Failed to load rooms', err);
        this.errorMessage.set('Impossible de charger la liste des salles.');
      }
    });
  }

  onSubmit() {
    this.errorMessage.set('');

    if (!this.request.roomId || !this.request.date) {
      this.errorMessage.set('Veuillez remplir tous les champs.');
      return;
    }

    const startTime = `${this.startHour}:${this.startMinute}:00`;
    const endTime = `${this.endHour}:${this.endMinute}:00`;

    if (startTime >= endTime) {
      this.errorMessage.set('L\'heure de fin doit être après l\'heure de début.');
      return;
    }

    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.errorMessage.set('Vous devez être connecté.');
      return;
    }

    this.loading.set(true);

    const fullRequest: CreateReservationRequest = {
      username: currentUser.username,
      roomId: this.request.roomId,
      date: this.request.date!,
      startTime: startTime,
      endTime: endTime,
    };

    console.log('Sending request:', fullRequest);

    this.reservationService.createReservation(fullRequest).subscribe({
      next: (res) => {
        alert('Réservation confirmée avec succès !');
        this.router.navigate(['/reservations']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Échec de la réservation.');
      }
    });
  }

  cancel(event: Event) {
    event.preventDefault();
    this.router.navigate(['/reservations']);
  }
}
