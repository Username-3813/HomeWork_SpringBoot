package com.garage.repository;

import com.garage.model.Repair;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RepairRepository extends JpaRepository<Repair, Long> {
    List<Repair> findByVehicleId(Long vehicleId);
}