package com.garage.service;

import com.garage.dto.PasswordChangeDto;
import com.garage.dto.ProfileDto;
import com.garage.model.User;
import com.garage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ProfileDto getProfileDto(User user) {
        ProfileDto dto = new ProfileDto();
        dto.setEmail(user.getEmail());
        dto.setLastName(user.getLastName());
        dto.setFirstName(user.getFirstName());
        dto.setPatronymic(user.getPatronymic());
        dto.setBirthDate(user.getBirthDate());
        dto.setPhone(user.getPhone());
        dto.setProfilePhoto(user.getProfilePhoto());
        return dto;
    }

@Transactional
public User updateProfile(User user, ProfileDto dto) {
    // Проверка даты рождения
    if (dto.getBirthDate() != null) {
        LocalDate minDate = LocalDate.of(1900, 1, 1);
        LocalDate maxDate = LocalDate.now();
        if (dto.getBirthDate().isBefore(minDate) || dto.getBirthDate().isAfter(maxDate)) {
            throw new RuntimeException("Дата рождения должна быть между 1900 годом и сегодня");
        }
    }
    user.setLastName(dto.getLastName());
    user.setFirstName(dto.getFirstName());
    user.setPatronymic(dto.getPatronymic());
    user.setBirthDate(dto.getBirthDate());
    user.setPhone(dto.getPhone());
    return userRepository.save(user);
}

    @Transactional
    public void changePassword(User user, PasswordChangeDto dto) {
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный текущий пароль");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String uploadProfilePhoto(User user, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir, "profiles");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "/uploads/profiles/" + fileName;
        user.setProfilePhoto(photoUrl);
        userRepository.save(user);
        return photoUrl;
    }
    
}