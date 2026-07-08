package com.garage.controller;

import com.garage.service.SiteContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final SiteContentService siteContentService;

    @GetMapping("/about")
    public String about(Model model) {
        String defaultText = "<h2>О проекте</h2><p>Электронный гараж — это система для учёта личных транспортных средств, запланированных(прошедших) ремонтов, все виды расходов по имеющейся технике, хранения всей документации. Создан для личного использования.</p>";
        model.addAttribute("content", siteContentService.getContent("about", defaultText));
        return "page";
    }

    @GetMapping("/contacts")
    public String contacts(Model model) {
        String defaultText = "<h2>Контакты</h2><p>Email: admin@garage.com</p><p>Телефон: +7 (961) 382-381-3</p>";
        model.addAttribute("content", siteContentService.getContent("contacts", defaultText));
        return "page";
    }
}