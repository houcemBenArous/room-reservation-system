import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Reservation, CreateReservationRequest } from '../models/reservation.model';

@Injectable({
    providedIn: 'root'
})
export class ReservationService {
    private apiUrl = 'http://localhost:8888/api/reservations';

    constructor(private http: HttpClient) { }

    createReservation(request: CreateReservationRequest): Observable<Reservation> {
        return this.http.post<Reservation>(this.apiUrl, request).pipe(
            catchError(this.handleError)
        );
    }

    // Admin: Get all reservations
    getAllReservations(): Observable<Reservation[]> {
        return this.http.get<Reservation[]>(this.apiUrl);
    }

    getUserReservations(username: string): Observable<Reservation[]> {
        return this.http.get<Reservation[]>(`${this.apiUrl}/user/${username}`);
    }

    getRoomReservations(roomId: number): Observable<Reservation[]> {
        return this.http.get<Reservation[]>(`${this.apiUrl}/room/${roomId}`);
    }

    cancelReservation(id: number): Observable<Reservation> {
        return this.http.put<Reservation>(`${this.apiUrl}/${id}/cancel`, {});
    }

    deleteReservation(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    private handleError(error: HttpErrorResponse) {
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred.
            console.error('An error occurred:', error.error.message);
        } else {
            // The backend returned an unsuccessful response code.
            console.error(
                `Backend returned code ${error.status}, ` +
                `body was: ${JSON.stringify(error.error)}`);
        }
        // Return an observable with a user-facing error message.
        const msg = error.error?.message || 'Une erreur est survenue lors de la réservation.';
        return throwError(() => new Error(msg));
    }
}
