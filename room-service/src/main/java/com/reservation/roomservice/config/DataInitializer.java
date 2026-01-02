package com.reservation.roomservice.config;

import com.reservation.roomservice.entity.Room;
import com.reservation.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {

            Room room1 = new Room(null, "Salle A", 10, true);
            Room room2 = new Room(null, "Salle B", 20, true);
            Room room3 = new Room(null, "Salle C", 15, true);

            roomRepository.save(room1);
            roomRepository.save(room2);
            roomRepository.save(room3);

            System.out.println("✅ 3 rooms created: Salle A, Salle B, Salle C");
        }
    }
}
