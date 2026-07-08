package com.garage.controller;

import com.garage.dto.NotificationDto;
import com.garage.model.User;
import com.garage.service.NotificationService;
import com.garage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationAdvice {

    private final NotificationService notificationService;
    private final UserService userService;

    @ModelAttribute("notifications")
    public List<NotificationDto> addNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return List.of();
        }
        try {
            String email = auth.getName();
            Long userId = userService.findByEmail(email).getId();
            return notificationService.getUpcomingNotifications(userId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @ModelAttribute("currentUser")
    public User addCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        try {
            String email = auth.getName();
            return userService.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }
}