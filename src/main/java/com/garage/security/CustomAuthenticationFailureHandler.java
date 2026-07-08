package com.garage.security;

import com.garage.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("email");
        boolean userExists = userRepository.existsByEmail(email);

        String errorMessage;
        if (!userExists) {
            errorMessage = "Пользователь с таким email не найден. Пожалуйста, зарегистрируйтесь.";
        } else {
            errorMessage = "Неверный пароль. Попробуйте снова.";
        }

        request.getSession().setAttribute("loginError", errorMessage);
        request.getSession().setAttribute("email", email);
        response.sendRedirect("/login?error");
    }
}