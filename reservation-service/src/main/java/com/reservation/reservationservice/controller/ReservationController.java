package com.reservation.reservationservice.controller;

import com.reservation.reservationservice.dto.CreateReservationRequest;
import com.reservation.reservationservice.dto.ReservationDTO;
import com.reservation.reservationservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest request) {
        try {
            ReservationDTO reservation = reservationService.createReservation(request);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            // Retourner le message d'erreur du fallback
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Reservation failed",
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAuthority('ADMIN') or #username == authentication.name")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUser(@PathVariable String username) {
        return ResponseEntity.ok(reservationService.getReservationsByUsername(username));
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(reservationService.getReservationsByRoomId(roomId));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
