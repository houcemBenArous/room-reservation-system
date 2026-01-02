package com.reservation.roomservice.tools;


import com.reservation.roomservice.dto.RoomDTO;
import com.reservation.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomMcpTools {

    private final RoomService roomService;

    @McpTool(
            name = "listAllRooms",
            description = "Recupere la liste complete de toutes les salles avec leurs details (nom, capacite, disponibilite)"
    )
    public List<RoomDTO> listAllRooms() {
        return roomService.getAllRooms();
    }

    @McpTool(
            name = "listAvailableRooms",
            description = "Recupere uniquement les salles disponibles pour une reservation"
    )
    public List<RoomDTO> listAvailableRooms() {
        return roomService.getAvailableRooms();
    }

    @McpTool(
            name = "getRoomDetails",
            description = "Recupere les details complets d'une salle specifique par son ID"
    )
    public RoomDTO getRoomDetails(
            @McpToolParam(description = "ID de la salle", required = true) Long roomId
    ) {
        return roomService.getRoomById(roomId);
    }

    @McpTool(
            name = "checkRoomAvailability",
            description = "Verifie si une salle specifique est disponible pour reservation"
    )
    public boolean checkRoomAvailability(
            @McpToolParam(description = "ID de la salle a verifier", required = true) Long roomId
    ) {
        return roomService.isRoomAvailable(roomId);
    }

    @McpTool(
            name = "getRoomByName",
            description = "Recherche une salle par son nom exact"
    )
    public RoomDTO getRoomByName(
            @McpToolParam(description = "Nom exact de la salle", required = true) String roomName
    ) {
        return roomService.getRoomByName(roomName);
    }
}
