package com.garage.controller;

import com.garage.dto.PasswordChangeDto;
import com.garage.dto.ProfileDto;
import com.garage.model.User;
import com.garage.service.ProfileService;
import com.garage.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfilePageController {

    private final UserService userService;
    private final ProfileService profileService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email);
    }

    @GetMapping("/delete")
    public String showDeleteConfirm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("currentUser", user);
        return "profile/delete";
    }

    // Метод для обработки удаления
    @PostMapping("/delete")
    public String deleteAccount(HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        userService.deleteUserWithAllData(user.getId());

        // Инвалидируем сессию, чтобы пользователь был разлогинен
        request.getSession().invalidate();

        redirectAttributes.addFlashAttribute("success", "Ваш аккаунт успешно удалён");
        return "redirect:/login?deleted";
    }

    @GetMapping
    public String showProfile(Model model) {
        User user = getCurrentUser();
        ProfileDto dto = profileService.getProfileDto(user);
        model.addAttribute("profile", dto);
        return "profile/index";
    }

    @GetMapping("/edit")
    public String showEditForm(Model model) {
        User user = getCurrentUser();
        ProfileDto dto = profileService.getProfileDto(user);
        model.addAttribute("profile", dto);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("profile") ProfileDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }
        User user = getCurrentUser();
        try {
            profileService.updateProfile(user, dto);
            redirectAttributes.addFlashAttribute("success", "Профиль обновлён");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "profile/edit";
        }
    }

    @GetMapping("/password")
    public String showPasswordForm(Model model) {
        model.addAttribute("passwordChange", new PasswordChangeDto());
        return "profile/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordChange") PasswordChangeDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile/password";
        }
        User user = getCurrentUser();
        try {
            profileService.changePassword(user, dto);
            redirectAttributes.addFlashAttribute("success", "Пароль изменён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {
        User user = getCurrentUser();
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Файл не выбран");
            return "redirect:/profile";
        }
        profileService.uploadProfilePhoto(user, file);
        redirectAttributes.addFlashAttribute("success", "Фото профиля обновлено");
        return "redirect:/profile";
    }
}