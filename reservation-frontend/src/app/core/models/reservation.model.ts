import { Time } from "@angular/common";

export interface Reservation {
    id?: number;
    username: string;
    roomId: number;
    roomName?: string;
    date: string; // ISO Date YYYY-MM-DD
    startTime: string; // HH:mm:ss or HH:mm
    endTime: string; // HH:mm:ss or HH:mm
    status?: string;
}

export interface CreateReservationRequest {
    username: string;
    roomId: number;
    date: string;
    startTime: string;
    endTime: string;
}
