package com.garage.controller;

import com.garage.dto.ProfileDto;
import com.garage.model.Expense;
import com.garage.model.User;
import com.garage.model.Vehicle;
import com.garage.service.ExpenseService;
import com.garage.service.ProfileService;
import com.garage.service.SiteContentService;
import com.garage.service.UserService;
import com.garage.service.VehicleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final VehicleService vehicleService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;
    private final SiteContentService siteContentService;

    // Список всех пользователей
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // ТС пользователя
    @Transactional
    @GetMapping("/user/{userId}/vehicles")
    public String userVehicles(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId);
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);
        model.addAttribute("user", user);
        model.addAttribute("vehicles", vehicles);
        return "admin/user-vehicles";
    }

    // Редактирование пользователя (форма)
    @GetMapping("/user/{userId}/edit")
    public String editUserForm(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId);
        ProfileDto dto = profileService.getProfileDto(user);
        model.addAttribute("profile", dto);
        model.addAttribute("userId", userId);
        return "admin/user-edit";
    }

    // Сохранение изменений пользователя
    @PostMapping("/user/{userId}/edit")
    public String updateUser(@PathVariable Long userId, @ModelAttribute ProfileDto dto, RedirectAttributes redirectAttributes) {
        User user = userService.findById(userId);
        profileService.updateProfile(user, dto);
        redirectAttributes.addFlashAttribute("success", "Пользователь обновлён");
        return "redirect:/admin/users";
    }

    // Удаление пользователя (форма подтверждения)
    @GetMapping("/user/{userId}/delete")
    public String deleteUserConfirm(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId);
        model.addAttribute("user", user);
        return "admin/user-delete";
    }

    // Обработка удаления пользователя
    @PostMapping("/user/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.deleteUserWithAllData(userId);
        redirectAttributes.addFlashAttribute("success", "Пользователь и все его данные удалены");
        return "redirect:/admin/users";
    }

    // Расходы пользователя (с пагинацией)
    @GetMapping("/user/{userId}/expenses")
    public String userExpenses(@PathVariable Long userId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        User user = userService.findById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Expense> expensePage = expenseService.getExpensesByUser(userId, pageable);
        model.addAttribute("user", user);
        model.addAttribute("expensePage", expensePage);
        return "admin/user-expenses";
    }
    @GetMapping("/promote/{id}")
    public String promote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.promoteToAdmin(id);
        redirectAttributes.addFlashAttribute("success", "Пользователь повышен до администратора");
        return "redirect:/admin/users";
    }

    @GetMapping("/demote/{id}")
    public String demote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.demoteToUser(id);
        redirectAttributes.addFlashAttribute("success", "Пользователь лишён прав администратора");
        return "redirect:/admin/users";
    }

    @GetMapping("/page/{page}/edit")
    public String editPage(@PathVariable String page, Model model) {
        String content = siteContentService.getContent(page, "");
        model.addAttribute("page", page);
        model.addAttribute("content", content);
        return "admin/page-edit";
    }

    @PostMapping("/page/{page}/edit")
    public String savePage(@PathVariable String page,
                        @RequestParam("content") String content,
                        RedirectAttributes redirectAttributes) {
        siteContentService.saveContent(page, content);
        redirectAttributes.addFlashAttribute("success", "Страница обновлена");
        return "redirect:/admin/page/" + page + "/edit";
    }
}