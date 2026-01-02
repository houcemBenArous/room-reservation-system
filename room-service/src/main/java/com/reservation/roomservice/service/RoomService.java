package com.reservation.roomservice.service;


import com.reservation.roomservice.dto.RoomDTO;
import com.reservation.roomservice.entity.Room;
import com.reservation.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getAvailableRooms() {
        return roomRepository.findByAvailable(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return convertToDTO(room);
    }

    public RoomDTO getRoomByName(String name) {
        Room room = roomRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Room not found with name: " + name));
        return convertToDTO(room);
    }

    public boolean isRoomAvailable(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return room.getAvailable();
    }

    public RoomDTO createRoom(RoomDTO roomDTO) {
        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setCapacity(roomDTO.getCapacity());
        room.setAvailable(true);
        Room savedRoom = roomRepository.save(room);
        return convertToDTO(savedRoom);
    }

    public RoomDTO updateRoomAvailability(Long roomId, Boolean available) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setAvailable(available);
        Room updatedRoom = roomRepository.save(room);
        return convertToDTO(updatedRoom);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    private RoomDTO convertToDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getAvailable()
        );
    }
}