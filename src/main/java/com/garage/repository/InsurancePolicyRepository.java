package com.garage.repository;

import com.garage.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {
    List<InsurancePolicy> findByVehicleId(Long vehicleId);
    List<InsurancePolicy> findByEndDateBetween(LocalDate start, LocalDate end);
    List<InsurancePolicy> findByVehicleIdInAndEndDateBetween(List<Long> vehicleIds, LocalDate start, LocalDate end);
    List<InsurancePolicy> findByVehicleIdIn(List<Long> vehicleIds); // добавлен
}