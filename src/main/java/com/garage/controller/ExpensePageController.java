package com.garage.controller;

import com.garage.dto.ExpenseRequest;
import com.garage.service.ExpenseService;
import com.garage.service.UserService;
import com.garage.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpensePageController {

    private final ExpenseService expenseService;
    private final VehicleService vehicleService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping("/add/{vehicleId}")
    public String showAddForm(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        ExpenseRequest request = new ExpenseRequest();
        request.setDate(LocalDate.now());
        model.addAttribute("expense", request);
        model.addAttribute("vehicleId", vehicleId);
        return "expenses/form";
    }

    @PostMapping("/add/{vehicleId}")
    public String addExpense(@PathVariable Long vehicleId,
                             @Valid @ModelAttribute("expense") ExpenseRequest dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vehicleId", vehicleId);
            return "expenses/form";
        }
        Long userId = getCurrentUserId();
        expenseService.addManualExpense(vehicleId, dto.getCategory(), dto.getDate(), dto.getAmount(), dto.getDescription(), userId);
        redirectAttributes.addFlashAttribute("success", "Расход добавлен!");
        return "redirect:/vehicles/" + vehicleId;
    }
}