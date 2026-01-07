import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Room } from '../models/room.model';

@Injectable({
    providedIn: 'root'
})
export class RoomService {
    private apiUrl = 'http://localhost:8888/api/rooms';

    constructor(private http: HttpClient) { }

    getAllRooms(): Observable<Room[]> {
        return this.http.get<Room[]>(this.apiUrl);
    }

    getAvailableRooms(): Observable<Room[]> {
        return this.http.get<Room[]>(`${this.apiUrl}/available`);
    }

    getRoomById(id: number): Observable<Room> {
        return this.http.get<Room>(`${this.apiUrl}/${id}`);
    }

    checkAvailability(id: number): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/check/${id}`);
    }
}
