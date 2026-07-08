package com.garage.service;

import com.garage.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ReminderService reminderService;
    private final InsuranceService insuranceService;

    // Для колокольчика (только предстоящие на 30 дней)
    public List<NotificationDto> getUpcomingNotifications(Long userId) {
        List<NotificationDto> notifications = new ArrayList<>();

        reminderService.getUpcomingMaintenanceForUser(userId).forEach(r -> {
            NotificationDto dto = new NotificationDto();
            dto.setId(r.getId());
            dto.setType("MAINTENANCE");
            dto.setTitle(r.getTitle());
            dto.setDescription(r.getDescription());
            dto.setDate(r.getDueDate());
            dto.setVehicleId(r.getVehicle().getId());
            dto.setVehicleName(r.getVehicle().getName());
            dto.setVehicleType(r.getVehicle().getType());
            dto.setBrand(r.getVehicle().getBrand());
            dto.setModel(r.getVehicle().getModel());
            dto.setCost(r.getEstimatedCost());
            dto.setIsDone(r.getIsDone());
            notifications.add(dto);
        });

        insuranceService.getExpiringInsurancesForUser(userId).forEach(p -> {
            NotificationDto dto = new NotificationDto();
            dto.setId(p.getId());
            dto.setType("INSURANCE");
            dto.setTitle("Страховка " + p.getType());
            dto.setDescription("Полис №" + p.getPolicyNumber());
            dto.setDate(p.getEndDate());
            dto.setVehicleId(p.getVehicle().getId());
            dto.setVehicleName(p.getVehicle().getName());
            dto.setVehicleType(p.getVehicle().getType());
            dto.setBrand(p.getVehicle().getBrand());
            dto.setModel(p.getVehicle().getModel());
            dto.setCost(p.getCost());
            dto.setIsDone(null);
            notifications.add(dto);
        });

        notifications.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        return notifications;
    }

    // Все события (для календаря)
    public List<NotificationDto> getAllNotifications(Long userId) {
        List<NotificationDto> notifications = new ArrayList<>();

        reminderService.getAllRemindersForUser(userId).forEach(r -> {
            NotificationDto dto = new NotificationDto();
            dto.setId(r.getId());
            dto.setType("MAINTENANCE");
            dto.setTitle(r.getTitle());
            dto.setDescription(r.getDescription());
            dto.setDate(r.getDueDate());
            dto.setVehicleId(r.getVehicle().getId());
            dto.setVehicleName(r.getVehicle().getName());
            dto.setVehicleType(r.getVehicle().getType());
            dto.setBrand(r.getVehicle().getBrand());
            dto.setModel(r.getVehicle().getModel());
            dto.setCost(r.getEstimatedCost());
            dto.setIsDone(r.getIsDone());
            notifications.add(dto);
        });

        insuranceService.getAllInsurancesForUser(userId).forEach(p -> {
            NotificationDto dto = new NotificationDto();
            dto.setId(p.getId());
            dto.setType("INSURANCE");
            dto.setTitle("Страховка " + p.getType());
            dto.setDescription("Полис №" + p.getPolicyNumber());
            dto.setDate(p.getEndDate());
            dto.setVehicleId(p.getVehicle().getId());
            dto.setVehicleName(p.getVehicle().getName());
            dto.setVehicleType(p.getVehicle().getType());
            dto.setBrand(p.getVehicle().getBrand());
            dto.setModel(p.getVehicle().getModel());
            dto.setCost(p.getCost());
            dto.setIsDone(null);
            notifications.add(dto);
        });

        notifications.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        return notifications;
    }
}