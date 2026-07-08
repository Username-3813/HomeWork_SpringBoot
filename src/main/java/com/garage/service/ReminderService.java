package com.garage.service;

import com.garage.dto.ReminderRequest;
import com.garage.model.MaintenanceReminder;
import com.garage.model.Vehicle;
import com.garage.repository.MaintenanceReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final MaintenanceReminderRepository reminderRepository;
    private final VehicleService vehicleService;

    @Value("${reminder.maintenance.days:30}")
    private int maintenanceReminderDays;

    private void checkOwnership(Long vehicleId, Long userId) {
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
    }

    public List<MaintenanceReminder> getRemindersByVehicle(Long vehicleId, Long userId) {
        checkOwnership(vehicleId, userId);
        return reminderRepository.findByVehicleId(vehicleId);
    }

    public MaintenanceReminder getReminderById(Long id, Long userId) {
        MaintenanceReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Напоминание не найдено (ID: " + id + ")"));
        checkOwnership(reminder.getVehicle().getId(), userId);
        return reminder;
    }

    @Transactional
    public MaintenanceReminder addReminder(ReminderRequest dto, Long vehicleId, Long userId) {
        checkOwnership(vehicleId, userId);
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);

        String title;
        if ("Другое".equals(dto.getReminderType())) {
            title = dto.getCustomTitle() != null && !dto.getCustomTitle().isEmpty()
                    ? dto.getCustomTitle()
                    : "Другое";
        } else {
            title = dto.getReminderType() != null ? dto.getReminderType() : "Напоминание";
        }

        MaintenanceReminder reminder = new MaintenanceReminder();
        reminder.setVehicle(vehicle);
        reminder.setTitle(title);
        reminder.setDescription(dto.getDescription());
        reminder.setDueDate(dto.getDueDate());
        reminder.setEstimatedCost(dto.getEstimatedCost());
        reminder.setIsDone(dto.getIsDone() != null ? dto.getIsDone() : false);
        reminder.setReminderDaysBefore(dto.getReminderDaysBefore() != null ? dto.getReminderDaysBefore() : 7);

        log.info("Добавлено напоминание '{}' для ТС {}", title, vehicle.getName());
        return reminderRepository.save(reminder);
    }

    @Transactional
    public MaintenanceReminder updateReminder(Long id, ReminderRequest dto, Long userId) {
        MaintenanceReminder reminder = getReminderById(id, userId);
        String title;
        if ("Другое".equals(dto.getReminderType())) {
            title = dto.getCustomTitle() != null && !dto.getCustomTitle().isEmpty()
                    ? dto.getCustomTitle()
                    : "Другое";
        } else {
            title = dto.getReminderType() != null ? dto.getReminderType() : reminder.getTitle();
        }
        reminder.setTitle(title);
        reminder.setDescription(dto.getDescription());
        reminder.setDueDate(dto.getDueDate());
        reminder.setEstimatedCost(dto.getEstimatedCost());
        reminder.setIsDone(dto.getIsDone() != null ? dto.getIsDone() : false);
        reminder.setReminderDaysBefore(dto.getReminderDaysBefore() != null ? dto.getReminderDaysBefore() : 7);
        log.info("Обновлено напоминание (ID: {})", id);
        return reminderRepository.save(reminder);
    }

    @Transactional
    public void deleteReminder(Long id, Long userId) {
        MaintenanceReminder reminder = getReminderById(id, userId);
        reminderRepository.deleteById(id);
        log.info("Удалено напоминание (ID: {})", id);
    }

    @Transactional
    public void markAsDone(Long id, Long userId) {
        MaintenanceReminder reminder = getReminderById(id, userId);
        reminder.setIsDone(true);
        reminderRepository.save(reminder);
        log.info("Напоминание отмечено как выполненное (ID: {})", id);
    }

    // Получение предстоящих напоминаний (для колокольчика)
    public List<MaintenanceReminder> getUpcomingMaintenance() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(maintenanceReminderDays);
        return reminderRepository.findByIsDoneFalseAndDueDateBetween(today, endDate);
    }

    // Получение предстоящих напоминаний для пользователя (для колокольчика)
    public List<MaintenanceReminder> getUpcomingMaintenanceForUser(Long userId) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);
        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        if (vehicleIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(maintenanceReminderDays);
        return reminderRepository.findByVehicleIdInAndIsDoneFalseAndDueDateBetween(vehicleIds, today, endDate);
    }

    // ===== НОВЫЙ МЕТОД: все напоминания пользователя (без ограничения по дате) =====
    public List<MaintenanceReminder> getAllRemindersForUser(Long userId) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);
        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        if (vehicleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reminderRepository.findByVehicleIdIn(vehicleIds);
    }
}