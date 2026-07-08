package com.garage.repository;

import com.garage.model.MaintenanceReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceReminderRepository extends JpaRepository<MaintenanceReminder, Long> {
    List<MaintenanceReminder> findByVehicleId(Long vehicleId);
    List<MaintenanceReminder> findByIsDoneFalseAndDueDateBetween(LocalDate start, LocalDate end);
    List<MaintenanceReminder> findByVehicleIdInAndIsDoneFalseAndDueDateBetween(List<Long> vehicleIds, LocalDate start, LocalDate end);
    List<MaintenanceReminder> findByVehicleIdIn(List<Long> vehicleIds); // добавлен
}