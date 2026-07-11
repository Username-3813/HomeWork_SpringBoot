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
        String defaultText = "<h2>О проекте</h2><p>Информация отсутствует.</p>";
        model.addAttribute("content", siteContentService.getContent("about", defaultText));
        model.addAttribute("page", "about");
        return "page";
    }

    @GetMapping("/contacts")
    public String contacts(Model model) {
        String defaultText = "<h2>О проекте</h2><p>Информация отсутствует.</p>";
        model.addAttribute("content", siteContentService.getContent("contacts", defaultText));
        model.addAttribute("page", "contacts");
        return "page";
    }
}