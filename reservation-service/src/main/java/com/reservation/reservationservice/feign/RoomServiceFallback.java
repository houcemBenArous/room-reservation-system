package com.reservation.reservationservice.feign;



import com.reservation.reservationservice.dto.RoomDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RoomServiceFallback implements RoomServiceClient {

    @Override
    public RoomDTO getRoomById(Long id) {
        // Fallback : retourne une salle par défaut
        return new RoomDTO(id, "Salle Indisponible (Fallback)", 0, false);
    }

    @Override
    public Boolean checkAvailability(Long id) {
        // Fallback : considère la salle comme indisponible
        return false;
    }

    @Override
    public List<RoomDTO> getAvailableRooms() {
        // Fallback : retourne une liste vide
        return Collections.emptyList();
    }
}