package ru.naujava.taskmanager.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Кастомный AuthenticationEntryPoint.
 * Для API запросов возвращает 401, для веб - перенаправляет на login.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } else {
            response.sendRedirect("/login");
        }
    }
}
