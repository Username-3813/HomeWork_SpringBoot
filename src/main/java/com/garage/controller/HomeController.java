package com.garage.controller;

import com.garage.service.SiteContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SiteContentService siteContentService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        boolean isAdmin = false;

        if (isAuthenticated) {
            isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));
        }

        // Если пользователь авторизован и НЕ админ — редирект на /vehicles
        if (isAuthenticated && !isAdmin) {
            return "redirect:/vehicles";
        }

        String content = siteContentService.getContent("home", "Контент не задан!");
        model.addAttribute("content", content);
        model.addAttribute("isAdmin", isAdmin); // передаём в шаблон, чтобы показать кнопку
        return "index";
    }
}