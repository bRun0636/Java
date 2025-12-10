package com.hotel.repository;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room WHERE b.user = :user")
    List<Booking> findByUser(@Param("user") User user);
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room WHERE b.room = :room")
    List<Booking> findByRoom(@Param("room") Room room);
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room WHERE b.room = :room AND b.status = 'ACTIVE' " +
           "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<Booking> findConflictingBookings(@Param("room") Room room,
                                          @Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);
    
    @Query("SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room")
    @Override
    List<Booking> findAll();
}
