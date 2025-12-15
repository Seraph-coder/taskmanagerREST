package ru.naujava.taskmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для ProfileController.
 */
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@Transactional
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    /**
     * Проверяет отображение страницы профиля.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и модель с именем пользователя.
     */
    @Test
    @WithMockUser(username = "testuser")
    public void profile() throws Exception {
        userService.register("testuser", "password", Role.USER);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("username", "testuser"));
    }

    /**
     * Проверяет успешное изменение пароля.
     * <br>
     * Ожидаемое поведение: перенаправляет на профиль с сообщением об успехе.
     */
    @Test
    @WithMockUser(username = "testuser2")
    public void changePasswordSuccess() throws Exception {
        userService.register("testuser2", "oldpassword", Role.USER);

        mockMvc.perform(post("/profile/change-password")
                        .param("oldPassword", "oldpassword")
                        .param("newPassword", "newpassword")
                        .param("confirmPassword", "newpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Пароль успешно изменен"));
    }

    /**
     * Проверяет изменение пароля с несовпадающими новыми паролями.
     * <br>
     * Ожидаемое поведение: перенаправляет на профиль с сообщением об ошибке.
     */
    @Test
    @WithMockUser(username = "testuser3")
    public void changePasswordPasswordsDoNotMatch() throws Exception {
        userService.register("testuser3", "password", Role.USER);

        mockMvc.perform(post("/profile/change-password")
                        .param("oldPassword", "password")
                        .param("newPassword", "newpassword")
                        .param("confirmPassword", "different")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("error", "Пароли не совпадают"));
    }

    /**
     * Проверяет изменение пароля с неверным старым паролем.
     * <br>
     * Ожидаемое поведение: перенаправляет на профиль с сообщением об ошибке.
     */
    @Test
    @WithMockUser(username = "testuser4")
    public void changePasswordInvalidOldPassword() throws Exception {
        userService.register("testuser4", "correctpassword", Role.USER);

        mockMvc.perform(post("/profile/change-password")
                        .param("oldPassword", "wrong")
                        .param("newPassword", "newpassword")
                        .param("confirmPassword", "newpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("error", "Старый пароль неверный"));
    }
}
