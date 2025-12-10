package com.hotel.controller.web;

import com.hotel.dto.BookingDTO;
import com.hotel.dto.RoomDTO;
import com.hotel.dto.UserRegistrationDTO;
import com.hotel.dto.UserUpdateDTO;
import com.hotel.model.User;
import com.hotel.service.BookingService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class WebController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(registrationDTO);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/guest/dashboard")
    public String guestDashboard(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<RoomDTO> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("user", user);
        return "guest/dashboard";
    }

    @GetMapping("/guest/bookings")
    public String guestBookings(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long userId = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId();

        List<BookingDTO> bookings = bookingService.getUserBookings(userId);
        model.addAttribute("bookings", bookings);
        return "guest/bookings";
    }

    @GetMapping("/guest/profile")
    public String guestProfile(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("updateDTO", new UserUpdateDTO());
        return "guest/profile";
    }

    @PostMapping("/guest/profile")
    public String updateProfile(@Valid @ModelAttribute("updateDTO") UserUpdateDTO updateDTO, 
                               BindingResult result, Authentication authentication, Model model) {
        if (result.hasErrors()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            model.addAttribute("user", user);
            return "guest/profile";
        }
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            userService.updateUser(user.getId(), updateDTO);
            return "redirect:/guest/profile?updated";
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.user", e.getMessage());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            model.addAttribute("user", user);
            return "guest/profile";
        }
    }

    @PostMapping("/guest/bookings/create")
    public String createBooking(@ModelAttribute BookingDTO bookingDTO, Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();

            bookingService.createBooking(bookingDTO, userId);
            return "redirect:/guest/bookings?created";
        } catch (RuntimeException e) {
            return "redirect:/guest/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/guest/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();

            bookingService.cancelBooking(id, userId);
            return "redirect:/guest/bookings?cancelled";
        } catch (RuntimeException e) {
            return "redirect:/guest/bookings?error=" + e.getMessage();
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        List<RoomDTO> rooms = roomService.getAllRooms();
        List<BookingDTO> bookings = bookingService.getAllBookings();
        model.addAttribute("rooms", rooms);
        model.addAttribute("bookings", bookings);
        return "admin/dashboard";
    }

    @GetMapping("/admin/rooms")
    public String adminRooms(Model model) {
        List<RoomDTO> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomDTO", new RoomDTO());
        return "admin/rooms";
    }

    @PostMapping("/admin/rooms")
    public String createRoom(@Valid @ModelAttribute("roomDTO") RoomDTO roomDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/rooms";
        }
        try {
            roomService.createRoom(roomDTO);
            return "redirect:/admin/rooms?created";
        } catch (RuntimeException e) {
            result.rejectValue("number", "error.room", e.getMessage());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/rooms";
        }
    }

    @PostMapping("/admin/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return "redirect:/admin/rooms?deleted";
        } catch (RuntimeException e) {
            return "redirect:/admin/rooms?error=" + e.getMessage();
        }
    }

    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/admin/bookings/{id}/cancel")
    public String cancelBookingByAdmin(@PathVariable Long id) {
        try {
            bookingService.cancelBookingByAdmin(id);
            return "redirect:/admin/dashboard?cancelled";
        } catch (RuntimeException e) {
            return "redirect:/admin/dashboard?error=" + e.getMessage();
        }
    }
}

