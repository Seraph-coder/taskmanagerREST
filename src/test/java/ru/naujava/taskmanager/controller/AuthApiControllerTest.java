package ru.naujava.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для AuthApiController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private String getToken(String username, String password) throws Exception {
        AuthApiController.LoginRequest loginRequest = new AuthApiController.LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthApiController.LoginResponse loginResponse =
                objectMapper.readValue(response, AuthApiController.LoginResponse.class);
        return loginResponse.getToken();
    }

    /**
     * Проверяет успешную регистрацию пользователя.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и сообщение об успехе.
     */
    @Test
    public void registerSuccess() throws Exception {
        AuthApiController.RegisterRequest request = new AuthApiController.RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно зарегистрирован"));
    }

    /**
     * Проверяет регистрацию пользователя с существующим именем.
     * <br>
     * Ожидаемое поведение: возвращает статус 400 и сообщение об ошибке.
     */
    @Test
    public void registerExistingUser() throws Exception {
        userService.register("existinguser", "password", Role.USER);

        AuthApiController.RegisterRequest request = new AuthApiController.RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь с таким именем уже существует"));
    }

    /**
     * Проверяет получение информации о текущем пользователе.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и данные пользователя.
     */
    @Test
    public void getCurrentUser() throws Exception {
        userService.register("testuser", "password", Role.USER);
        String token = getToken("testuser", "password");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    /**
     * Проверяет изменение пароля пользователя.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и сообщение об успехе.
     */
    @Test
    public void changePasswordSuccess() throws Exception {
        userService.register("testuser2", "oldpassword", Role.USER);
        String token = getToken("testuser2", "oldpassword");

        AuthApiController.ChangePasswordRequest request = new AuthApiController.ChangePasswordRequest();
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Пароль успешно изменен"));
    }

    /**
     * Проверяет изменение пароля с неверным старым паролем.
     * <br>
     * Ожидаемое поведение: возвращает статус 400 и сообщение об ошибке.
     */
    @Test
    public void changePasswordWrongOldPassword() throws Exception {
        userService.register("testuser3", "correctpassword", Role.USER);
        String token = getToken("testuser3", "correctpassword");

        AuthApiController.ChangePasswordRequest request = new AuthApiController.ChangePasswordRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Старый пароль неверный"));
    }

    /**
     * Проверяет выход из системы.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и сообщение об успехе.
     */
    @Test
    public void logout() throws Exception {
        userService.register("testuser4", "password", Role.USER);
        String token = getToken("testuser4", "password");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Выход выполнен успешно"));
    }

    /**
     * Проверяет успешный вход в систему.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и JWT токен.
     */
    @Test
    public void loginSuccess() throws Exception {
        userService.register("loginuser", "password123", Role.USER);

        AuthApiController.LoginRequest request = new AuthApiController.LoginRequest();
        request.setUsername("loginuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    /**
     * Проверяет вход в систему с неверным паролем.
     * <br>
     * Ожидаемое поведение: возвращает статус 401 и сообщение об ошибке.
     */
    @Test
    public void loginWrongPassword() throws Exception {
        userService.register("loginuser2", "password123", Role.USER);

        AuthApiController.LoginRequest request = new AuthApiController.LoginRequest();
        request.setUsername("loginuser2");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Неверные учетные данные"));
    }

    /**
     * Проверяет регистрацию с паролем короче 8 символов.
     * <br>
     * Ожидаемое поведение: возвращает статус 400 и сообщение об ошибке.
     */
    @Test
    public void registerPasswordTooShort() throws Exception {
        AuthApiController.RegisterRequest request = new AuthApiController.RegisterRequest();
        request.setUsername("shortpassuser");
        request.setPassword("1234567"); // 7 символов

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password")
                        .value("Пароль должен содержать не менее 8 символов"));
    }

    /**
     * Проверяет доступ к защищенному эндпоинту без авторизации.
     * <br>
     * Ожидаемое поведение: возвращает статус 401.
     */
    @Test
    public void getCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет изменение пароля без авторизации.
     * <br>
     * Ожидаемое поведение: возвращает статус 401.
     */
    @Test
    public void changePasswordUnauthorized() throws Exception {
        AuthApiController.ChangePasswordRequest request = new AuthApiController.ChangePasswordRequest();
        request.setOldPassword("old");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет выход из системы без авторизации.
     * <br>
     * Ожидаемое поведение: возвращает статус 401.
     */
    @Test
    public void logoutUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
