package com.garage.controller;

import com.garage.dto.NotificationDto;
import com.garage.service.NotificationService;
import com.garage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarPageController {

    private final NotificationService notificationService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping
    public String showCalendar(Model model) {
        Long userId = getCurrentUserId();

        // Получаем ВСЕ события (без ограничения по дате)
        List<NotificationDto> notifications = notificationService.getAllNotifications(userId);

        // Группировка по дате
        Map<LocalDate, List<NotificationDto>> groupedByDate = notifications.stream()
                .collect(Collectors.groupingBy(
                        NotificationDto::getDate,
                        TreeMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("groupedNotifications", groupedByDate);
        return "calendar/index";
    }
}