package ru.naujava.taskmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.security.JwtUtil;
import ru.naujava.taskmanager.service.UserService;

import java.util.List;

/**
 * REST API контроллер для аутентификации и управления пользователями.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "API для аутентификации и управления пользователями")
public class AuthApiController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthApiController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Регистрация нового пользователя.
     */
    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с указанным именем и паролем. " +
                    "Пароль должен быть не менее 8 символов.")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        try {
            userService.register(request.getUsername(), request.getPassword(), Role.USER);
            return ResponseEntity.ok("Пользователь успешно зарегистрирован");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Получение информации о текущем пользователе.
     */
    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе",
            description = "Возвращает информацию о аутентифицированном пользователе.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        UserInfo userInfo = new UserInfo(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Изменение пароля пользователя.
     */
    @PostMapping("/change-password")
    @Operation(summary = "Изменить пароль",
            description = "Изменяет пароль текущего пользователя. Требуется указать старый пароль.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            userService.changePassword(userDetails.getUsername(),
                    request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Пароль успешно изменен");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Выход из системы.
     */
    @PostMapping("/logout")
    @Operation(summary = "Выход из системы",
            description = "Выполняет выход из системы для текущего пользователя.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, null);
        return ResponseEntity.ok("Выход выполнен успешно");
    }

    /**
     * Вход в систему.
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему",
            description = "Аутентифицирует пользователя и возвращает JWT токен.")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        // Проверка учетных данных через UserService
        var user = userService.findByUsername(request.getUsername());
        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                    user.get().getUsername(), user.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.get().getRole().name()))));
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(401).body("Неверные учетные данные");
        }
    }

    /**
     * DTO для запроса регистрации.
     */
    public static class RegisterRequest {
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        private String username;

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        private String password;

        /**
         * Получить имя пользователя.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Установить имя пользователя.
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Получить пароль.
         */
        public String getPassword() {
            return password;
        }

        /**
         * Установить пароль.
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * DTO для информации о пользователе.
     */
    public static class UserInfo {
        private final String username;
        private final String role;

        public UserInfo(String username, String role) {
            this.username = username;
            this.role = role;
        }

        /**
         * Получить имя пользователя.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Получить роль.
         */
        public String getRole() {
            return role;
        }
    }

    /**
     * DTO для запроса изменения пароля.
     */
    public static class ChangePasswordRequest {
        @NotBlank(message = "Старый пароль не должен быть пустым")
        private String oldPassword;

        @NotBlank(message = "Новый пароль не должен быть пустым")
        @Size(min = 8, message = "Новый пароль должен содержать не менее 8 символов")
        private String newPassword;

        /**
         * Получить старый пароль.
         */
        public String getOldPassword() {
            return oldPassword;
        }

        /**
         * Установить старый пароль.
         */
        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        /**
         * Получить новый пароль.
         */
        public String getNewPassword() {
            return newPassword;
        }

        /**
         * Установить новый пароль.
         */
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /**
     * DTO для запроса входа в систему.
     */
    public static class LoginRequest {
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        private String username;

        @NotBlank(message = "Пароль не должен быть пустым")
        private String password;

        /**
         * Получить имя пользователя.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Установить имя пользователя.
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Получить пароль.
         */
        public String getPassword() {
            return password;
        }

        /**
         * Установить пароль.
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * DTO для ответа при входе в систему (содержит JWT токен).
     */
    public static class LoginResponse {
        private final String token;

        public LoginResponse(String token) {
            this.token = token;
        }

        /**
         * Получить токен.
         */
        public String getToken() {
            return token;
        }
    }
}
