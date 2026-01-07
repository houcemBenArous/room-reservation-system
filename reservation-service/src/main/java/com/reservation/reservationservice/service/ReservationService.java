package com.reservation.reservationservice.service;

import com.reservation.reservationservice.dto.CreateReservationRequest;
import com.reservation.reservationservice.dto.ReservationDTO;
import com.reservation.reservationservice.dto.RoomDTO;
import com.reservation.reservationservice.entity.Reservation;
import com.reservation.reservationservice.feign.RoomServiceClient;
import com.reservation.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomServiceClient roomServiceClient;

    public ReservationDTO createReservation(CreateReservationRequest request) {
        // 1. Check room existence via OpenFeign
        RoomDTO room = roomServiceClient.getRoomById(request.getRoomId());

        if (room == null || !room.getAvailable()) {
            throw new RuntimeException("La salle n'est pas disponible");
        }

        // 2. Check for conflicts
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                request.getRoomId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("La salle est déjà réservée pour cette plage horaire");
        }

        // 3. Create reservation
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

    public List<RoomDTO> getAvailableRooms(LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 1. Get all physically available rooms
        List<RoomDTO> allRooms = roomServiceClient.getAvailableRooms();

        // 2. Get IDs of rooms that are busy during the requested slot
        List<Long> busyRoomIds = reservationRepository.findBusyRoomIds(date, startTime, endTime);

        // 3. Filter out busy rooms
        return allRooms.stream()
                .filter(room -> !busyRoomIds.contains(room.getId()))
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