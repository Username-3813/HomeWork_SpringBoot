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

        // Получаем контент главной страницы
        String defaultContent = "<h1 class=\"display-4\">🚗 Электронный гараж</h1>"
                + "<p class=\"lead\">Управляйте своей техникой, ремонтами, расходами и страховками в одном месте.</p>"
                + "<hr class=\"my-4\">"
                + "<p>Войдите или зарегистрируйтесь, чтобы начать пользоваться всеми возможностями.</p>"
                + "<a href=\"/login\" class=\"btn btn-primary btn-lg me-2\">Войти</a>"
                + "<a href=\"/register\" class=\"btn btn-secondary btn-lg\">Регистрация</a>";

        String content = siteContentService.getContent("home", defaultContent);
        model.addAttribute("content", content);
        model.addAttribute("isAdmin", isAdmin); // передаём в шаблон, чтобы показать кнопку
        return "index";
    }
}