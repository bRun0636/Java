package com.hotel.controller.api;

import com.hotel.dto.BookingDTO;
import com.hotel.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private com.hotel.service.UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingDTO bookingDTO, Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId();

            bookingService.createBooking(bookingDTO, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Бронирование успешно создано");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<List<BookingDTO>> getMyBookings(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long userId = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();

        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId();

            bookingService.cancelBooking(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Бронирование успешно отменено");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/available-dates")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @RequestParam Long roomId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(bookingService.getAvailableDates(roomId, startDate, endDate));
    }
}

