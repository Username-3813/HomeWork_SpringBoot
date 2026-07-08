package com.garage.service;

import com.garage.dto.VehicleRequest;
import com.garage.model.User;
import com.garage.model.Vehicle;
import com.garage.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    private void checkOwnership(Long vehicleId, Long userId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ запрещён: это ТС не принадлежит вам");
        }
    }

    @Transactional
    public void savePhoto(Long vehicleId, MultipartFile file, Long userId) throws IOException {
        Vehicle vehicle = getVehicleByIdAndUser(vehicleId, userId);

        // Удаляем старое фото, если есть
        String oldPhotoUrl = vehicle.getPhotoUrl();
        if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
            try {
                String oldFileName = oldPhotoUrl.substring(oldPhotoUrl.lastIndexOf("/") + 1);
                Path oldFilePath = Paths.get("uploads", "vehicles", oldFileName);
                Files.deleteIfExists(oldFilePath);
                log.info("Удалено старое фото: {}", oldFileName);
            } catch (Exception e) {
                log.warn("Не удалось удалить старое фото: {}", e.getMessage());
            }
        }

        // Сохраняем новое фото
        Path uploadPath = Paths.get("uploads", "vehicles");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = "vehicle_" + vehicleId + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "/uploads/vehicles/" + fileName;
        vehicle.setPhotoUrl(photoUrl);
        vehicleRepository.save(vehicle);
        log.info("Обновлено фото для ТС {}", vehicleId);
    }

    @Transactional
    public void updatePhotoUrl(Long vehicleId, String newPhotoUrl, Long userId) {
        Vehicle vehicle = getVehicleByIdAndUser(vehicleId, userId);
        String oldPhotoUrl = vehicle.getPhotoUrl();
        if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty() && !oldPhotoUrl.equals(newPhotoUrl)) {
            try {
                String oldFileName = oldPhotoUrl.substring(oldPhotoUrl.lastIndexOf("/") + 1);
                Path oldFilePath = Paths.get("uploads", "vehicles", oldFileName);
                boolean deleted = Files.deleteIfExists(oldFilePath);
                if (deleted) {
                    log.info("Удалено старое фото: {}", oldFileName);
                } else {
                    log.warn("Старое фото не найдено: {}", oldFileName);
                }
            } catch (Exception e) {
                log.warn("Не удалось удалить старое фото: {}", e.getMessage());
            }
        }
        vehicle.setPhotoUrl(newPhotoUrl);
        vehicleRepository.save(vehicle);
        log.info("Обновлено фото для ТС {}: {}", vehicleId, newPhotoUrl);
    }

    @Transactional
    public Vehicle addVehicle(VehicleRequest dto, Long userId) {
        User user = userService.findById(userId);
        Vehicle vehicle = new Vehicle();
        vehicle.setUser(user);
        vehicle.setPhotoUrl(dto.getPhotoUrl());
        vehicle.setName(dto.getName());
        vehicle.setType(dto.getType());
        vehicle.setBrand(dto.getBrand());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        // Исправление VIN: если пусто или "-", то записываем null
        vehicle.setVin(normalizeVin(dto.getVin()));
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setInitialOdometer(dto.getInitialOdometer());
        vehicle.setOdometerUnit(dto.getOdometerUnit() != null ? dto.getOdometerUnit() : "km");
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Добавлено ТС {} для пользователя {}", saved.getName(), user.getEmail());
        return saved;
    }

    public List<Vehicle> getUserVehicles(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Транспортное средство не найдено"));
    }

    public Vehicle getVehicleByIdAndUser(Long id, Long userId) {
        Vehicle vehicle = getVehicleById(id);
        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ запрещён: это ТС не принадлежит вам");
        }
        return vehicle;
    }

    @Transactional
    public Vehicle updateVehicle(Long id, VehicleRequest dto, Long userId) {
        checkOwnership(id, userId);
        Vehicle vehicle = getVehicleById(id);
        vehicle.setName(dto.getName());
        vehicle.setType(dto.getType());
        vehicle.setBrand(dto.getBrand());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        // Исправление VIN: если пусто или "-", то записываем null
        vehicle.setVin(normalizeVin(dto.getVin()));
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setInitialOdometer(dto.getInitialOdometer());
        vehicle.setOdometerUnit(dto.getOdometerUnit() != null ? dto.getOdometerUnit() : "km");
        log.info("Обновлено ТС {}", vehicle.getName());
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id, Long userId) {
        Vehicle vehicle = getVehicleByIdAndUser(id, userId);
        String photoUrl = vehicle.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                String fileName = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get("uploads", "vehicles", fileName);
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    log.info("Удалено фото при удалении ТС: {}", fileName);
                }
            } catch (Exception e) {
                log.error("Не удалось удалить фото при удалении ТС: {}", e.getMessage());
            }
        }
        vehicleRepository.deleteById(id);
        log.info("Удалено ТС {}", vehicle.getName());
    }

    // Вспомогательный метод для нормализации VIN
    private String normalizeVin(String vin) {
        if (vin == null || vin.trim().isEmpty() || vin.equals("-")) {
            return null;
        }
        return vin.trim();
    }
}