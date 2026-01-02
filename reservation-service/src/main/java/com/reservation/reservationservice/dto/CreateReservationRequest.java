package com.reservation.reservationservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {
    private String username;
    private Long roomId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
