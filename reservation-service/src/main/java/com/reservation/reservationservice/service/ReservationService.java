package com.reservation.reservationservice.service;



import com.reservation.reservationservice.dto.CreateReservationRequest;
import com.reservation.reservationservice.dto.ReservationDTO;
import com.reservation.reservationservice.dto.RoomDTO;
import com.reservation.reservationservice.entity.Reservation;
import com.reservation.reservationservice.feign.RoomServiceClient;
import com.reservation.reservationservice.repository.ReservationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomServiceClient roomServiceClient;

    @CircuitBreaker(name = "roomService", fallbackMethod = "createReservationFallback")
    public ReservationDTO createReservation(CreateReservationRequest request) {
        // 1. Vérifier que la salle existe via OpenFeign
        RoomDTO room = roomServiceClient.getRoomById(request.getRoomId());

        if (room == null || !room.getAvailable()) {
            throw new RuntimeException("La salle n'est pas disponible");
        }

        // 2. Vérifier les conflits d'horaires
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                request.getRoomId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("La salle est déjà réservée pour cette plage horaire");
        }

        // 3. Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setUsername(request.getUsername());
        reservation.setRoomId(request.getRoomId());
        reservation.setRoomName(room.getName());
        reservation.setDate(request.getDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus("CONFIRMED");

        Reservation saved = reservationRepository.save(reservation);
        return convertToDTO(saved);
    }

    // Fallback method pour le Circuit Breaker
    public ReservationDTO createReservationFallback(CreateReservationRequest request, Exception ex) {
        throw new RuntimeException("Le service des salles est temporairement indisponible. Veuillez réessayer plus tard.");
    }

    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getReservationsByUsername(String username) {
        return reservationRepository.findByUsername(username).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReservationDTO cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.setStatus("CANCELLED");
        Reservation updated = reservationRepository.save(reservation);
        return convertToDTO(updated);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    private ReservationDTO convertToDTO(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                reservation.getUsername(),
                reservation.getRoomId(),
                reservation.getRoomName(),
                reservation.getDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus()
        );
    }
}