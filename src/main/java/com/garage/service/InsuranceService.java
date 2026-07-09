package com.garage.service;

import com.garage.dto.InsuranceRequest;
import com.garage.model.InsurancePolicy;
import com.garage.model.Vehicle;
import com.garage.repository.InsurancePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsurancePolicyRepository insuranceRepository;
    private final VehicleService vehicleService;
    private final ExpenseService expenseService;
    private final DocumentService documentService;

    @Value("${reminder.insurance.days:30}")
    private int insuranceReminderDays;

    private void checkOwnership(Long vehicleId, Long userId) {
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
    }

    public List<InsurancePolicy> getInsurancesByVehicle(Long vehicleId, Long userId) {
        checkOwnership(vehicleId, userId);
        return insuranceRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public InsurancePolicy getInsuranceById(Long id, Long userId) {
        InsurancePolicy policy = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Страховка не найдена (ID: " + id + ")"));
        checkOwnership(policy.getVehicle().getId(), userId);
        return policy;
    }

    @Transactional
    public void deleteInsurance(Long id, Long userId) {
        InsurancePolicy policy = getInsuranceById(id, userId);
        // Удаляем файлы
        if (policy.getFrontDocumentUrl() != null && !policy.getFrontDocumentUrl().isEmpty()) {
            try {
                Path filePath = Paths.get(policy.getFrontDocumentUrl());
                Files.deleteIfExists(filePath);
                log.info("Удалён файл лицевой стороны: {}", policy.getFrontDocumentUrl());
            } catch (IOException e) {
                log.warn("Не удалось удалить файл лицевой стороны: {}", e.getMessage());
            }
        }
        if (policy.getBackDocumentUrl() != null && !policy.getBackDocumentUrl().isEmpty()) {
            try {
                Path filePath = Paths.get(policy.getBackDocumentUrl());
                Files.deleteIfExists(filePath);
                log.info("Удалён файл обратной стороны: {}", policy.getBackDocumentUrl());
            } catch (IOException e) {
                log.warn("Не удалось удалить файл обратной стороны: {}", e.getMessage());
            }
        }
        insuranceRepository.deleteById(id);
        log.info("Страховка удалена (ID: {})", id);
    }

    public List<InsurancePolicy> getExpiringInsurances() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(insuranceReminderDays);
        return insuranceRepository.findByEndDateBetween(today, endDate);
    }

    public List<InsurancePolicy> getExpiringInsurancesForUser(Long userId) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);
        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        if (vehicleIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(insuranceReminderDays);
        return insuranceRepository.findByVehicleIdInAndEndDateBetween(vehicleIds, today, endDate);
    }

    // ===== НОВЫЙ МЕТОД: все страховки пользователя (без ограничения по дате) =====
    public List<InsurancePolicy> getAllInsurancesForUser(Long userId) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);
        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        if (vehicleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return insuranceRepository.findByVehicleIdIn(vehicleIds);
    }

    @Transactional
    public InsurancePolicy addInsurance(InsuranceRequest dto, Long vehicleId, Long userId) {
        return addInsuranceWithFile(dto, vehicleId, userId, null);
    }

    @Transactional
    public InsurancePolicy addInsuranceWithFile(InsuranceRequest dto, Long vehicleId, Long userId, MultipartFile file) {
        checkOwnership(vehicleId, userId);
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);

        InsurancePolicy policy = new InsurancePolicy();
        policy.setVehicle(vehicle);
        policy.setPolicyNumber(dto.getPolicyNumber());
        policy.setCompany(dto.getCompany());
        policy.setType(dto.getType());
        policy.setStartDate(dto.getStartDate());

        LocalDate endDate;
        if (dto.getPeriodMonths() != null && dto.getPeriodMonths() > 0) {
            endDate = dto.getStartDate().plusMonths(dto.getPeriodMonths());
        } else {
            endDate = dto.getEndDate();
            if (endDate == null) {
                throw new IllegalArgumentException("Не указана дата окончания или период страховки");
            }
        }
        policy.setEndDate(endDate);
        policy.setCost(dto.getCost());

        // Сохраняем лицевую сторону
        if (dto.getFrontFile() != null && !dto.getFrontFile().isEmpty()) {
            try {
                com.garage.model.Document doc = documentService.uploadDocument(dto.getFrontFile(), vehicleId, userId, "Лицевая сторона");
                policy.setFrontDocumentUrl(doc.getFilePath());
            } catch (Exception e) {
                log.warn("Не удалось сохранить лицевую сторону: {}", e.getMessage());
            }
        }
        // Сохраняем обратную сторону
        if (dto.getBackFile() != null && !dto.getBackFile().isEmpty()) {
            try {
                com.garage.model.Document doc = documentService.uploadDocument(dto.getBackFile(), vehicleId, userId, "Обратная сторона");
                policy.setBackDocumentUrl(doc.getFilePath());
            } catch (Exception e) {
                log.warn("Не удалось сохранить обратную сторону: {}", e.getMessage());
            }
        }

        InsurancePolicy saved = insuranceRepository.save(policy);

        expenseService.createExpense(
                vehicle,
                "INSURANCE",
                dto.getStartDate(),
                dto.getCost(),
                "Страховка " + dto.getType() + " №" + dto.getPolicyNumber(),null
        );

        log.info("Добавлена страховка для ТС {}", vehicle.getName());
        return saved;
    }

    @Transactional
    public InsurancePolicy updateInsurance(Long id, InsuranceRequest dto, Long userId) {
        InsurancePolicy policy = getInsuranceById(id, userId);
        policy.setPolicyNumber(dto.getPolicyNumber());
        policy.setCompany(dto.getCompany());
        policy.setType(dto.getType());
        policy.setStartDate(dto.getStartDate());

        LocalDate endDate;
        if (dto.getPeriodMonths() != null && dto.getPeriodMonths() > 0) {
            endDate = dto.getStartDate().plusMonths(dto.getPeriodMonths());
        } else {
            endDate = dto.getEndDate();
            if (endDate == null) {
                throw new IllegalArgumentException("Не указана дата окончания или период страховки");
            }
        }
        policy.setEndDate(endDate);
        policy.setCost(dto.getCost());

        // Обновляем файлы
        if (dto.getFrontFile() != null && !dto.getFrontFile().isEmpty()) {
            try {
                com.garage.model.Document doc = documentService.uploadDocument(dto.getFrontFile(), policy.getVehicle().getId(), userId, "Лицевая сторона");
                policy.setFrontDocumentUrl(doc.getFilePath());
            } catch (Exception e) {
                log.warn("Не удалось сохранить лицевую сторону при обновлении: {}", e.getMessage());
            }
        }
        if (dto.getBackFile() != null && !dto.getBackFile().isEmpty()) {
            try {
                com.garage.model.Document doc = documentService.uploadDocument(dto.getBackFile(), policy.getVehicle().getId(), userId, "Обратная сторона");
                policy.setBackDocumentUrl(doc.getFilePath());
            } catch (Exception e) {
                log.warn("Не удалось сохранить обратную сторону при обновлении: {}", e.getMessage());
            }
        }

        return insuranceRepository.save(policy);
    }
}