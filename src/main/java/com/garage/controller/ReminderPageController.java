package com.garage.controller;

import com.garage.dto.ReminderRequest;
import com.garage.model.MaintenanceReminder;
import com.garage.service.ReminderService;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class ReminderPageController {

    private final ReminderService reminderService;
    private final VehicleService vehicleService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public String listReminders(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        List<MaintenanceReminder> reminders = reminderService.getRemindersByVehicle(vehicleId, userId);
        model.addAttribute("reminders", reminders);
        model.addAttribute("vehicleId", vehicleId);
        return "reminders/list";
    }

    @GetMapping("/add/{vehicleId}")
    public String showAddForm(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        ReminderRequest request = new ReminderRequest();
        request.setReminderDaysBefore(7);
        model.addAttribute("reminder", request);
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("isEdit", false);
        model.addAttribute("today", LocalDate.now());
        return "reminders/form";
    }

    @PostMapping("/add/{vehicleId}")
    public String addReminder(@PathVariable Long vehicleId,
                            @Valid @ModelAttribute("reminder") ReminderRequest dto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vehicleId", vehicleId);
            model.addAttribute("isEdit", false);
            return "reminders/form";
        }
        Long userId = getCurrentUserId();
        reminderService.addReminder(dto, vehicleId, userId);
        return "redirect:/reminders/vehicle/" + vehicleId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Long userId = getCurrentUserId();
        MaintenanceReminder reminder = reminderService.getReminderById(id, userId);
        ReminderRequest dto = new ReminderRequest();
        String[] predefined = {"Техническое обслуживание", "Замена масла", "Плановая запись в автосервис"};
        String reminderType = "Другое";
        for (String type : predefined) {
            if (type.equals(reminder.getTitle())) {
                reminderType = type;
                break;
            }
        }
        dto.setReminderType(reminderType);
        if ("Другое".equals(reminderType)) {
            dto.setCustomTitle(reminder.getTitle());
        }
        dto.setDescription(reminder.getDescription());
        dto.setDueDate(reminder.getDueDate());
        dto.setEstimatedCost(reminder.getEstimatedCost());
        dto.setIsDone(reminder.getIsDone());
        dto.setReminderDaysBefore(reminder.getReminderDaysBefore());
        model.addAttribute("reminder", dto);
        model.addAttribute("reminderId", id);
        model.addAttribute("vehicleId", reminder.getVehicle().getId());
        model.addAttribute("isEdit", true);
        return "reminders/form";
    }

    @PostMapping("/edit/{id}")
    public String updateReminder(@PathVariable Long id,
                                @Valid @ModelAttribute("reminder") ReminderRequest dto,
                                BindingResult bindingResult,
                                Model model) {
        Long userId = getCurrentUserId();

        if (bindingResult.hasErrors()) {
            // Получаем vehicleId для кнопки "Отмена"
            Long vehicleId = reminderService.getReminderById(id, userId).getVehicle().getId();
            model.addAttribute("vehicleId", vehicleId);
            model.addAttribute("reminderId", id);
            model.addAttribute("isEdit", true);
            return "reminders/form";
        }

        reminderService.updateReminder(id, dto, userId);

        MaintenanceReminder updated = reminderService.getReminderById(id, userId);
        return "redirect:/reminders/vehicle/" + updated.getVehicle().getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteReminder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        MaintenanceReminder reminder = reminderService.getReminderById(id, userId);
        Long vehicleId = reminder.getVehicle().getId();
        reminderService.deleteReminder(id, userId);
        return "redirect:/reminders/vehicle/" + vehicleId;
    }

    @GetMapping("/done/{id}")
    public String markDone(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        MaintenanceReminder reminder = reminderService.getReminderById(id, userId);
        Long vehicleId = reminder.getVehicle().getId();
        reminderService.markAsDone(id, userId);
        return "redirect:/reminders/vehicle/" + vehicleId;
    }
}