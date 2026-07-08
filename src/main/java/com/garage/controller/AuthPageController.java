package com.garage.controller;

import com.garage.dto.RegistrationRequest;
import com.garage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registration", new RegistrationRequest());
        return "register";
    }

@PostMapping("/register")
public String registerUser(@Valid @ModelAttribute("registration") RegistrationRequest dto,
                           BindingResult bindingResult,
                           Model model) {
    if (bindingResult.hasErrors()) {
        return "register";
    }
    try {
        userService.registerUser(dto);
        return "redirect:/login";
    } catch (RuntimeException e) {
        model.addAttribute("error", e.getMessage());
        return "register";
    }
}
}