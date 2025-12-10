package com.hotel.service;

import com.hotel.dto.UserRegistrationDTO;
import com.hotel.dto.UserUpdateDTO;
import com.hotel.model.User;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("Имя пользователя уже существует");
        }
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email уже существует");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setPhone(registrationDTO.getPhone());
        user.getRoles().add(User.Role.GUEST);

        return userRepository.save(user);
    }

    public User updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Убеждаемся, что roles инициализированы
        if (user.getRoles() == null) {
            user.setRoles(new java.util.HashSet<>());
            // Если roles пусты, добавляем роль GUEST по умолчанию
            if (user.getRoles().isEmpty()) {
                user.getRoles().add(User.Role.GUEST);
            }
        }

        // Обрабатываем email - проверяем на пустую строку и валидность
        String email = updateDTO.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(trimmedEmail)) {
                    throw new RuntimeException("Email уже существует");
                }
                user.setEmail(trimmedEmail);
            }
        }
        // Если email не передан или пустой, оставляем текущий

        // Обрабатываем firstName - не может быть пустым из-за @NotBlank
        String firstName = updateDTO.getFirstName();
        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName.trim());
        }
        // Если firstName не передан, оставляем текущий

        // Обрабатываем lastName - не может быть пустым из-за @NotBlank
        String lastName = updateDTO.getLastName();
        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }
        // Если lastName не передан, оставляем текущий

        // Обрабатываем phone - может быть пустым
        String phone = updateDTO.getPhone();
        if (phone != null) {
            String trimmedPhone = phone.trim();
            user.setPhone(trimmedPhone.isEmpty() ? null : trimmedPhone);
        }
        // Если phone не передан, оставляем текущий

        return userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}

