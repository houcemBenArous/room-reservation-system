package com.reservation.reservationservice.tools;


import com.reservation.reservationservice.dto.CreateReservationRequest;
import com.reservation.reservationservice.dto.ReservationDTO;
import com.reservation.reservationservice.dto.RoomDTO;
import com.reservation.reservationservice.service.ReservationService;
import lombok.RequiredArgsConstructor;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationMcpTools {

    private final ReservationService reservationService;

    @McpTool(
            name = "createReservation",
            description = "Cree une nouvelle reservation pour une salle. Verifie automatiquement la disponibilite de la salle et les conflits d'horaires"
    )
    public ReservationDTO createReservation(
            @McpToolParam(description = "Nom d'utilisateur", required = true) String username,
            @McpToolParam(description = "ID de la salle", required = true) Long roomId,
            @McpToolParam(description = "Date de reservation au format YYYY-MM-DD", required = true) String date,
            @McpToolParam(description = "Heure de debut au format HH:mm", required = true) String startTime,
            @McpToolParam(description = "Heure de fin au format HH:mm", required = true) String endTime
    ) {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setUsername(username);
        request.setRoomId(roomId);
        request.setDate(LocalDate.parse(date));
        request.setStartTime(LocalTime.parse(startTime));
        request.setEndTime(LocalTime.parse(endTime));

        return reservationService.createReservation(request);
    }

    @McpTool(
            name = "getUserReservations",
            description = "Recupere toutes les reservations d'un utilisateur specifique"
    )
    public List<ReservationDTO> getUserReservations(
            @McpToolParam(description = "Nom d'utilisateur", required = true) String username
    ) {
        return reservationService.getReservationsByUsername(username);
    }

    @McpTool(
            name = "getRoomReservations",
            description = "Recupere toutes les reservations pour une salle specifique"
    )
    public List<ReservationDTO> getRoomReservations(
            @McpToolParam(description = "ID de la salle", required = true) Long roomId
    ) {
        return reservationService.getReservationsByRoomId(roomId);
    }

    @McpTool(
            name = "cancelReservation",
            description = "Annule une reservation existante"
    )
    public ReservationDTO cancelReservation(
            @McpToolParam(description = "ID de la reservation", required = true) Long reservationId
    ) {
        return reservationService.cancelReservation(reservationId);
    }

    @McpTool(
            name = "listAllReservations",
            description = "Recupere la liste complete de toutes les reservations"
    )
    public List<ReservationDTO> listAllReservations() {
        return reservationService.getAllReservations();
    }

    @McpTool(
            name = "checkAvailability",
            description = "Verifie les salles disponibles pour une date et un creneau horaire specifique"
    )
    public List<RoomDTO> checkAvailability(
            @McpToolParam(description = "Date au format YYYY-MM-DD", required = true) String date,
            @McpToolParam(description = "Heure de debut au format HH:mm", required = true) String startTime,
            @McpToolParam(description = "Heure de fin au format HH:mm", required = true) String endTime
    ) {
        return reservationService.getAvailableRooms(
                LocalDate.parse(date),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime)
        );
    }

    @McpTool(
            name = "getRoomSchedule",
            description = "Recupere le planning des reservations pour une salle donnee (par son nom)"
    )
    public List<ReservationDTO> getRoomSchedule(
            @McpToolParam(description = "Nom de la salle (ex: Salle A)", required = true) String roomName
    ) {
        // 1. Find the room ID by name (we need to inject RoomServiceClient in ReservationService, or just filter all reservations)
        // Ideally we should use a proper service call. For now, let's filter all reservations as a quick win or add a method in service.
        // Actually best practice: Add getReservationsByRoomName to Service.
        // But for now, let's use the stream filter on returning list from Service if easy, OR better:
        return reservationService.getAllReservations().stream()
                .filter(r -> r.getRoomName().equalsIgnoreCase(roomName))
                .toList();
    }
}