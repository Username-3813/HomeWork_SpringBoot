package com.garage.repository;

import com.garage.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Старый метод (можно оставить, но использовать новый)
    List<Document> findByVehicleId(Long vehicleId);

    // Новый метод - исключает документы страховок
    @Query("SELECT d FROM Document d WHERE d.vehicle.id = :vehicleId " +
           "AND NOT EXISTS (SELECT i FROM InsurancePolicy i " +
           "WHERE i.frontDocumentUrl = d.filePath OR i.backDocumentUrl = d.filePath)")
    List<Document> findNonInsuranceDocumentsByVehicleId(@Param("vehicleId") Long vehicleId);
}