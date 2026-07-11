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
import org.springframework.web.bind.annotation.RequestParam;

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
    public String showCalendar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Long userId = getCurrentUserId();

        // Получаем ВСЕ события (уже отсортированы в сервисе)
        List<NotificationDto> allNotifications = notificationService.getAllNotifications(userId);

        int totalItems = allNotifications.size();

        // Вычисляем границы страницы
        int start = Math.min(page * size, totalItems);
        int end = Math.min(start + size, totalItems);

        // Берём только нужный кусок списка
        List<NotificationDto> pageNotifications = allNotifications.subList(start, end);

        // Группировка по дате (только для текущей страницы)
        Map<LocalDate, List<NotificationDto>> groupedByDate = pageNotifications.stream()
                .collect(Collectors.groupingBy(
                        NotificationDto::getDate,
                        TreeMap::new,
                        Collectors.toList()
                ));

        // Параметры пагинации
        int totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / size);

        model.addAttribute("groupedNotifications", groupedByDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", pageNotifications.size());
        model.addAttribute("size", size);

        return "calendar/index";
    }
}