package com.garage.repository;

import com.garage.dto.CategoryExpenseDto;
import com.garage.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    // Базовые методы
    List<Expense> findByVehicleId(Long vehicleId);
    List<Expense> findByVehicle_User_Id(Long userId);
    Page<Expense> findByVehicleId(Long vehicleId, Pageable pageable);
    Page<Expense> findByVehicle_User_Id(Long userId, Pageable pageable);

    // Группировка по категориям (используется в итоговом блоке истории расходов)
    @Query("SELECT new com.garage.dto.CategoryExpenseDto(e.category, SUM(e.amount)) " +
           "FROM Expense e WHERE e.vehicle.id = :vehicleId AND e.date BETWEEN :from AND :to " +
           "GROUP BY e.category")
    List<CategoryExpenseDto> sumExpensesByCategory(
            @Param("vehicleId") Long vehicleId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("SELECT new com.garage.dto.CategoryExpenseDto(e.category, SUM(e.amount)) " +
           "FROM Expense e WHERE e.vehicle.user.id = :userId AND e.date BETWEEN :from AND :to " +
           "GROUP BY e.category")
    List<CategoryExpenseDto> sumExpensesByCategoryForUser(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // Вспомогательные методы для подсчёта сумм по категориям (используются в сервисах)
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.vehicle.id = :vehicleId AND e.category = :category AND e.date BETWEEN :from AND :to")
    Double sumByCategoryAndVehicleAndPeriod(
            @Param("vehicleId") Long vehicleId,
            @Param("category") String category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.vehicle.id = :vehicleId AND e.category = :category")
    Double sumByCategoryAndVehicle(@Param("vehicleId") Long vehicleId, @Param("category") String category);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.vehicle.id = :vehicleId")
    Double totalExpensesByVehicle(@Param("vehicleId") Long vehicleId);
}