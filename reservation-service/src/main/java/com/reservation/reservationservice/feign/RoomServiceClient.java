package com.reservation.reservationservice.feign;



import com.reservation.reservationservice.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "room-service",
        fallback = RoomServiceFallback.class
)
public interface RoomServiceClient {

    @GetMapping("/api/rooms/{id}")
    RoomDTO getRoomById(@PathVariable Long id);

    @GetMapping("/api/rooms/check/{id}")
    Boolean checkAvailability(@PathVariable Long id);

    @GetMapping("/api/rooms/available")
    List<RoomDTO> getAvailableRooms();
}