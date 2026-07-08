package com.garage.controller;

import com.garage.dto.RepairRequest;
import com.garage.model.Repair;
import com.garage.service.RepairService;
import com.garage.service.UserService;
import com.garage.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/repairs")
@RequiredArgsConstructor
public class RepairPageController {

    private final RepairService repairService;
    private final VehicleService vehicleService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // теперь это email
        return userService.findByEmail(email).getId();
    }

    // Список ремонтов для конкретного ТС
    @GetMapping("/vehicle/{vehicleId}")
    public String listRepairs(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        List<Repair> repairs = repairService.getRepairsByVehicle(vehicleId, userId);
        model.addAttribute("repairs", repairs);
        model.addAttribute("vehicleId", vehicleId);
        return "repairs/list";
    }

    // Форма добавления ремонта
    @GetMapping("/add/{vehicleId}")
    public String showAddForm(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        model.addAttribute("repair", new RepairRequest());
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("isEdit", false);
        return "repairs/form";
    }

    // Обработка добавления ремонта
    @PostMapping("/add/{vehicleId}")
    public String addRepair(@PathVariable Long vehicleId, @ModelAttribute RepairRequest dto) {
        Long userId = getCurrentUserId();
        repairService.addRepair(dto, vehicleId, userId);
        return "redirect:/repairs/vehicle/" + vehicleId;
    }

    // Форма редактирования ремонта
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Long userId = getCurrentUserId();
        Repair repair = repairService.getRepairById(id, userId);
        RepairRequest dto = new RepairRequest();
        dto.setDate(repair.getDate());
        dto.setOdometer(repair.getOdometer());
        dto.setDescription(repair.getDescription());
        dto.setCost(repair.getCost());
        dto.setWorkshop(repair.getWorkshop());
        model.addAttribute("repair", dto);
        model.addAttribute("repairId", id);
        model.addAttribute("vehicleId", repair.getVehicle().getId());
        model.addAttribute("isEdit", true);
        return "repairs/form";
    }

    // Обработка обновления ремонта
    @PostMapping("/edit/{id}")
    public String updateRepair(@PathVariable Long id, @ModelAttribute RepairRequest dto) {
        Long userId = getCurrentUserId();
        Repair repair = repairService.updateRepair(id, dto, userId);
        return "redirect:/repairs/vehicle/" + repair.getVehicle().getId();
    }

    // Удаление ремонта
    @GetMapping("/delete/{id}")
    public String deleteRepair(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Repair repair = repairService.getRepairById(id, userId);
        Long vehicleId = repair.getVehicle().getId();
        repairService.deleteRepair(id, userId);
        return "redirect:/repairs/vehicle/" + vehicleId;
    }
}