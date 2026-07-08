package com.garage.service;

import com.garage.dto.RepairRequest;
import com.garage.model.Repair;
import com.garage.model.Vehicle;
import com.garage.repository.RepairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRepository repairRepository;
    private final VehicleService vehicleService;
    private final ExpenseService expenseService;

    private void checkOwnership(Long vehicleId, Long userId) {
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
    }

    @Transactional
    public Repair addRepair(RepairRequest dto, Long vehicleId, Long userId) {
        checkOwnership(vehicleId, userId);
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);

        Repair repair = new Repair();
        repair.setVehicle(vehicle);
        repair.setDate(dto.getDate());
        repair.setOdometer(dto.getOdometer());
        repair.setDescription(dto.getDescription());
        repair.setCost(dto.getCost());
        repair.setWorkshop(dto.getWorkshop());

        Repair savedRepair = repairRepository.save(repair);

        expenseService.createExpense(
                vehicle,
                "REPAIR",
                dto.getDate(),
                dto.getCost(),
                "Ремонт: " + dto.getDescription()
        );

        log.info("Добавлен ремонт для ТС {} на сумму {}", vehicle.getName(), dto.getCost());
        return savedRepair;
    }

    public List<Repair> getRepairsByVehicle(Long vehicleId, Long userId) {
        checkOwnership(vehicleId, userId);
        return repairRepository.findByVehicleId(vehicleId);
    }

    public Repair getRepairById(Long id, Long userId) {
        Repair repair = repairRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись о ремонте не найдена (ID: " + id + ")"));
        checkOwnership(repair.getVehicle().getId(), userId);
        return repair;
    }

    @Transactional
    public Repair updateRepair(Long id, RepairRequest dto, Long userId) {
        Repair repair = getRepairById(id, userId);
        repair.setDate(dto.getDate());
        repair.setOdometer(dto.getOdometer());
        repair.setDescription(dto.getDescription());
        repair.setCost(dto.getCost());
        repair.setWorkshop(dto.getWorkshop());
        return repairRepository.save(repair);
    }

    @Transactional
    public void deleteRepair(Long id, Long userId) {
        Repair repair = getRepairById(id, userId);
        repairRepository.deleteById(id);
        log.info("Удалён ремонт с ID {}", id);
    }
}