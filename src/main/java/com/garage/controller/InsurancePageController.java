package com.garage.controller;

import com.garage.dto.InsuranceRequest;
import com.garage.model.InsurancePolicy;
import com.garage.service.InsuranceService;
import com.garage.service.UserService;
import com.garage.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/insurances")
@RequiredArgsConstructor
public class InsurancePageController {

    private final InsuranceService insuranceService;
    private final VehicleService vehicleService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public String listInsurances(@PathVariable Long vehicleId, Model model, @RequestParam(required = false) String from) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        List<InsurancePolicy> policies = insuranceService.getInsurancesByVehicle(vehicleId, userId);
        model.addAttribute("insurances", policies);
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("from", from);
        return "insurances/list";
    }

    @GetMapping("/add/{vehicleId}")
    public String showAddForm(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        InsuranceRequest request = new InsuranceRequest();
        request.setPeriodMonths(12);
        model.addAttribute("insurance", request);
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("isEdit", false);
        return "insurances/form";
    }

    @PostMapping("/add/{vehicleId}")
    public String addInsurance(@PathVariable Long vehicleId,
                               @Valid @ModelAttribute("insurance") InsuranceRequest dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vehicleId", vehicleId);
            return "insurances/form";
        }
        Long userId = getCurrentUserId();
        insuranceService.addInsuranceWithFile(dto, vehicleId, userId, null);
        redirectAttributes.addFlashAttribute("success", "Страховка добавлена!");
        return "redirect:/vehicles/" + vehicleId;
    }

    @Transactional
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Long userId = getCurrentUserId();
        InsurancePolicy policy = insuranceService.getInsuranceById(id, userId);
        InsuranceRequest dto = new InsuranceRequest();

        dto.setPolicyNumber(policy.getPolicyNumber());
        dto.setCompany(policy.getCompany());
        dto.setType(policy.getType());
        dto.setStartDate(policy.getStartDate());
        dto.setEndDate(policy.getEndDate());
        dto.setCost(policy.getCost());

        if (policy.getStartDate() != null && policy.getEndDate() != null) {
            long months = ChronoUnit.MONTHS.between(policy.getStartDate(), policy.getEndDate());
            if (months == 1 || months == 3 || months == 6 || months == 12) {
                dto.setPeriodMonths((int) months);
            } else {
                dto.setPeriodMonths(0);
            }
        } else {
            dto.setPeriodMonths(12);
        }

        model.addAttribute("insurance", dto);
        model.addAttribute("insuranceId", id);
        model.addAttribute("vehicleId", policy.getVehicle().getId());
        model.addAttribute("isEdit", true);
        return "insurances/form";
    }

    @PostMapping("/edit/{id}")
    public String updateInsurance(@PathVariable Long id,
                                  @Valid @ModelAttribute("insurance") InsuranceRequest dto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("insuranceId", id);
            try {
                InsurancePolicy policy = insuranceService.getInsuranceById(id, getCurrentUserId());
                model.addAttribute("vehicleId", policy.getVehicle().getId());
            } catch (Exception e) {
                model.addAttribute("vehicleId", 0L);
            }
            return "insurances/form";
        }
        Long userId = getCurrentUserId();
        InsurancePolicy policy = insuranceService.updateInsurance(id, dto, userId);
        redirectAttributes.addFlashAttribute("success", "Страховка обновлена!");
        return "redirect:/vehicles/" + policy.getVehicle().getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteInsurance(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        InsurancePolicy policy = insuranceService.getInsuranceById(id, userId);
        Long vehicleId = policy.getVehicle().getId();
        insuranceService.deleteInsurance(id, userId);
        return "redirect:/insurances/vehicle/" + vehicleId;
    }
}