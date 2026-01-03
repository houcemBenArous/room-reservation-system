package com.reservation.reservationservice.repository;



import com.reservation.reservationservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUsername(String username);
    List<Reservation> findByRoomId(Long roomId);
    List<Reservation> findByStatus(String status);

    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.date = :date AND r.status <> 'CANCELLED' " +
            "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<Reservation> findConflictingReservations(@Param("roomId") Long roomId, @Param("date") LocalDate date, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    @Query("SELECT DISTINCT r.roomId FROM Reservation r " +
            "WHERE r.date = :date " +
            "AND r.status <> 'CANCELLED' " +
            "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<Long> findBusyRoomIds(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
