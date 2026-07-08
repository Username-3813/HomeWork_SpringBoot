package com.garage.service;

import com.garage.dto.RegistrationRequest;
import com.garage.model.User;
import com.garage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegistrationRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        // Проверка даты рождения
        if (dto.getBirthDate() != null) {
            LocalDate minDate = LocalDate.of(1900, 1, 1);
            LocalDate maxDate = LocalDate.now();
            if (dto.getBirthDate().isBefore(minDate) || dto.getBirthDate().isAfter(maxDate)) {
                throw new RuntimeException("Дата рождения должна быть между 1900 годом и сегодня");
            }
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setLastName(dto.getLastName());
        user.setFirstName(dto.getFirstName());
        user.setPatronymic(dto.getPatronymic());
        user.setBirthDate(dto.getBirthDate());
        user.setPhone(dto.getPhone());
        user.setRole("USER"); // роль по умолчанию

        User saved = userRepository.save(user);
        log.info("Зарегистрирован новый пользователь: {}", dto.getEmail());
        return saved;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void promoteToAdmin(Long id) {
        User user = findById(id);
        user.setRole("ADMIN");
        userRepository.save(user);
    }

    public void demoteToUser(Long id) {
        User user = findById(id);
        user.setRole("USER");
        userRepository.save(user);
    }

    @Transactional
    public void deleteUserWithAllData(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }
}