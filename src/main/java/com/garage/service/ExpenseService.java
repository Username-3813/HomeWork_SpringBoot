package com.garage.service;

import com.garage.dto.CategoryExpenseDto;
import com.garage.dto.ExpenseSummaryDto;
import com.garage.model.Expense;
import com.garage.model.Vehicle;
import com.garage.repository.ExpenseRepository;
import com.garage.specification.ExpenseSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final VehicleService vehicleService;

    @Transactional
    public Expense createExpense(Vehicle vehicle, String category, LocalDate date, Double amount, String description) {
        Expense expense = new Expense();
        expense.setVehicle(vehicle);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setAmount(amount);
        expense.setDescription(description);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense addManualExpense(Long vehicleId, String category, LocalDate date, Double amount, String description, Long userId) {
        Vehicle vehicle = vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        return createExpense(vehicle, category, date, amount, description);
    }

    public List<Expense> getExpensesByVehicle(Long vehicleId, Long userId) {
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        return expenseRepository.findByVehicleId(vehicleId);
    }

    public Page<Expense> getExpensesByVehicle(Long vehicleId, Long userId, Pageable pageable) {
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        return expenseRepository.findByVehicleId(vehicleId, pageable);
    }

    public Expense getExpenseById(Long id, Long userId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Расход не найден (ID: " + id + ")"));
        vehicleService.getVehicleByIdAndUser(expense.getVehicle().getId(), userId);
        return expense;
    }

    @Transactional
    public void deleteExpense(Long id, Long userId) {
        Expense expense = getExpenseById(id, userId);
        expenseRepository.deleteById(id);
        log.info("Расход удалён (ID: {})", id);
    }

    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByVehicle_User_Id(userId);
    }

    public Page<Expense> getExpensesByUser(Long userId, Pageable pageable) {
        return expenseRepository.findByVehicle_User_Id(userId, pageable);
    }

    // НОВЫЙ МЕТОД: фильтрация с пагинацией
    public Page<Expense> getFilteredExpenses(Long userId, Long vehicleId, LocalDate from, LocalDate to,
                                             List<String> categories, List<String> vehicleTypes, Pageable pageable) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.filterByUserId(userId));

        if (vehicleId != null && vehicleId > 0) {
            spec = spec.and(ExpenseSpecifications.filterByVehicleId(vehicleId));
        }
        if (from != null || to != null) {
            spec = spec.and(ExpenseSpecifications.filterByDateBetween(from, to));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(ExpenseSpecifications.filterByCategories(categories));
        }
        if (vehicleTypes != null && !vehicleTypes.isEmpty()) {
            spec = spec.and(ExpenseSpecifications.filterByVehicleTypes(vehicleTypes));
        }

        return expenseRepository.findAll(spec, pageable);
    }

    // НОВЫЙ МЕТОД: итоги по расходам (сумма, средний чек, разбивка по категориям)
    public ExpenseSummaryDto getExpenseSummary(Long userId, Long vehicleId, LocalDate from, LocalDate to,
                                               List<String> categories, List<String> vehicleTypes) {
        // Получаем все расходы без пагинации (только для подсчёта)
        Page<Expense> page = getFilteredExpenses(userId, vehicleId, from, to, categories, vehicleTypes, Pageable.unpaged());
        List<Expense> expenses = page.getContent();

        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double average = expenses.isEmpty() ? 0 : total / expenses.size();

        // Группировка по категориям
        List<CategoryExpenseDto> categorySummary = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)))
                .entrySet().stream()
                .map(entry -> new CategoryExpenseDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        ExpenseSummaryDto summary = new ExpenseSummaryDto();
        summary.setTotalExpenses(total);
        summary.setAverageAmount(average);
        summary.setCount(expenses.size());
        summary.setCategoryBreakdown(categorySummary);

        return summary;
    }
}