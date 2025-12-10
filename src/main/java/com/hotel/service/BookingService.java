package com.hotel.service;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    public Booking createBooking(BookingDTO bookingDTO, Long userId) {
        if (bookingDTO.getCheckInDate() == null || bookingDTO.getCheckOutDate() == null) {
            throw new RuntimeException("Необходимо указать даты заезда и выезда");
        }
        
        if (bookingDTO.getCheckInDate().isAfter(bookingDTO.getCheckOutDate()) ||
            bookingDTO.getCheckInDate().isEqual(bookingDTO.getCheckOutDate()) ||
            bookingDTO.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Неверные даты");
        }

        Room room = roomService.getRoomEntityById(bookingDTO.getRoomId());
        
        if (bookingDTO.getNumberOfGuests() == null || bookingDTO.getNumberOfGuests() <= 0) {
            throw new RuntimeException("Количество гостей должно быть больше 0");
        }
        
        if (bookingDTO.getNumberOfGuests() > room.getCapacity()) {
            throw new RuntimeException("Количество гостей превышает вместимость номера");
        }

        // Проверяем конфликты: интервалы пересекаются, если начало одного <= конец другого И конец одного >= начало другого
        // Логика: существующая бронь [checkIn, checkOut] конфликтует с новой [newCheckIn, newCheckOut], если:
        // checkIn <= newCheckOut AND checkOut >= newCheckIn
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                room, bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        if (!conflictingBookings.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Номер уже забронирован на эти даты. ");
            for (Booking conflict : conflictingBookings) {
                errorMessage.append(String.format("Существующее бронирование: с %s по %s. ", 
                    conflict.getCheckInDate(), conflict.getCheckOutDate()));
            }
            throw new RuntimeException(errorMessage.toString());
        }

        User user = userService.getUserById(userId);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setNumberOfGuests(bookingDTO.getNumberOfGuests());
        booking.setStatus(Booking.BookingStatus.ACTIVE);

        return bookingRepository.save(booking);
    }

    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        // Инициализируем user для проверки
        Long bookingUserId = booking.getUser().getId();
        if (!bookingUserId.equals(userId)) {
            throw new RuntimeException("Вы можете отменять только свои бронирования");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public void cancelBookingByAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getUserBookings(Long userId) {
        User user = userService.getUserById(userId);
        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocalDate> getAvailableDates(Long roomId, LocalDate startDate, LocalDate endDate) {
        Room room = roomService.getRoomEntityById(roomId);
        List<Booking> bookings = bookingRepository.findByRoom(room);
        
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> bookings.stream()
                        .filter(b -> b.getStatus() == Booking.BookingStatus.ACTIVE)
                        .noneMatch(b -> !date.isBefore(b.getCheckInDate()) && date.isBefore(b.getCheckOutDate())))
                .collect(Collectors.toList());
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomNumber(booking.getRoom().getNumber());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setStatus(booking.getStatus().name());
        return dto;
    }
}

