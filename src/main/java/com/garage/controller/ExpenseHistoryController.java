package com.garage.controller;

import com.garage.dto.CategoryOption;
import com.garage.dto.ExpenseSummaryDto;
import com.garage.model.Expense;
import com.garage.model.ExpenseCategory;
import com.garage.model.Vehicle;
import com.garage.service.ExpenseService;
import com.garage.service.UserService;
import com.garage.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseHistoryController {

    private final ExpenseService expenseService;
    private final VehicleService vehicleService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping("/history")
    public String history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> vehicleTypes,
            Model model) {

        Long userId = getCurrentUserId();

        // Получаем список ТС для фильтра
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);
        model.addAttribute("vehicles", vehicles);

        // Получаем уникальные категории из расходов пользователя и превращаем в CategoryOption
        List<Expense> allExpenses = expenseService.getExpensesByUser(userId);
        List<CategoryOption> availableCategories = allExpenses.stream()
                .map(Expense::getCategory)
                .distinct()
                .sorted()
                .map(code -> new CategoryOption(code, ExpenseCategory.getDisplayName(code)))
                .collect(Collectors.toList());
        model.addAttribute("availableCategories", availableCategories);

        // Получаем уникальные типы ТС пользователя (для чекбоксов)
        List<String> availableVehicleTypes = vehicles.stream()
                .map(Vehicle::getType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("availableVehicleTypes", availableVehicleTypes);

        // Пагинация
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        // Фильтруем расходы (категории передаются как коды)
        Page<Expense> expensePage = expenseService.getFilteredExpenses(
                userId, vehicleId, from, to, categories, vehicleTypes, pageable);

        // Подсчёт итогов (передаём те же фильтры)
        ExpenseSummaryDto summary = expenseService.getExpenseSummary(
                userId, vehicleId, from, to, categories, vehicleTypes);

        // Передаём в модель
        model.addAttribute("expensePage", expensePage);
        model.addAttribute("selectedVehicleId", vehicleId);
        model.addAttribute("selectedCategories", categories); // коды
        model.addAttribute("selectedVehicleTypes", vehicleTypes);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("summary", summary);

        return "expenses/history";
    }
}