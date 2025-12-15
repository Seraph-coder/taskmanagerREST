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
import ru.naujava.taskmanager.service.TaskService;
import ru.naujava.taskmanager.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для TaskController.
 */
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@Transactional
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser")
    public void listTasks() throws Exception {
        userService.register("testuser", "password", Role.USER);
        taskService.createTask("Test task", userService.findByUsername("testuser").orElseThrow());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks"))
                .andExpect(model().attributeExists("tasks"));
    }

    @Test
    @WithMockUser(username = "testuser2")
    public void createTask() throws Exception {
        userService.register("testuser2", "password", Role.USER);

        mockMvc.perform(post("/tasks")
                        .param("description", "New task")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("success", "Задача создана успешно!"));
    }

    @Test
    @WithMockUser(username = "testuser3")
    public void editTask() throws Exception {
        userService.register("testuser3", "password", Role.USER);
        var user = userService.findByUsername("testuser3").orElseThrow();
        var task = taskService.createTask("Original task", user);

        mockMvc.perform(post("/tasks/" + task.getId() + "/edit")
                        .param("description", "Updated task")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("success", "Задача обновлена!"));
    }

    @Test
    @WithMockUser(username = "testuser4")
    public void editTaskInvalidId() throws Exception {
        userService.register("testuser4", "password", Role.USER);

        mockMvc.perform(post("/tasks/99999/edit")
                        .param("description", "Updated")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("error", "Задача не найдена"));
    }

    @Test
    @WithMockUser(username = "testuser5")
    public void editTaskEmptyDescription() throws Exception {
        userService.register("testuser5", "password", Role.USER);
        var user = userService.findByUsername("testuser5").orElseThrow();
        var task = taskService.createTask("Original task", user);

        mockMvc.perform(post("/tasks/" + task.getId() + "/edit")
                        .param("description", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("error", "Описание задачи не может быть пустым"));
    }

    @Test
    @WithMockUser(username = "testuser6")
    public void editTaskDuplicateDescription() throws Exception {
        userService.register("testuser6", "password", Role.USER);
        var user = userService.findByUsername("testuser6").orElseThrow();
        taskService.createTask("Existing task", user);
        var task = taskService.createTask("Task to edit", user);

        mockMvc.perform(post("/tasks/" + task.getId() + "/edit")
                        .param("description", "Existing task")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("error",
                        "Задача с описанием 'Existing task' уже существует"));
    }
}
