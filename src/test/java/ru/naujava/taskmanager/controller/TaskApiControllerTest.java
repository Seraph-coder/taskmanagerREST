package ru.naujava.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.service.TaskService;
import ru.naujava.taskmanager.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для TaskApiController.
 */
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@Transactional
public class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private String getToken(String username) throws Exception {
        AuthApiController.LoginRequest loginRequest = new AuthApiController.LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("password");

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
     * Проверяет получение активных задач пользователя.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и список задач.
     */
    @Test
    public void getActiveTasks() throws Exception {
        User user = userService.register("testuser", "password", Role.USER);
        taskService.createTask("Test task", user);
        String token = getToken("testuser");

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test task"));
    }

    /**
     * Проверяет получение выполненных задач пользователя.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и список задач.
     */
    @Test
    public void getCompletedTasks() throws Exception {
        User user = userService.register("testuser2", "password", Role.USER);
        Task task = taskService.createTask("Completed task", user);
        taskService.markTaskAsDone(task.getId(), user);
        String token = getToken("testuser2");

        mockMvc.perform(get("/api/tasks/completed")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Completed task"));
    }

    /**
     * Проверяет создание новой задачи.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и созданную задачу.
     */
    @Test
    public void createTask() throws Exception {
        userService.register("testuser3", "password", Role.USER);
        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("New task");
        String token = getToken("testuser3");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("New task"));
    }

    /**
     * Проверяет отметку задачи как выполненной.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и обновленную задачу.
     */
    @Test
    public void markAsDone() throws Exception {
        User user = userService.register("testuser4", "password", Role.USER);
        Task task = taskService.createTask("Task to complete", user);
        String token = getToken("testuser4");

        mockMvc.perform(put("/api/tasks/" + task.getId() + "/done")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));
    }

    /**
     * Проверяет отметку задачи как не выполненной.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и обновленную задачу.
     */
    @Test
    public void markAsNotDone() throws Exception {
        User user = userService.register("testuser7", "password", Role.USER);
        Task task = taskService.createTask("Task to uncomplete", user);
        taskService.markTaskAsDone(task.getId(), user);
        String token = getToken("testuser7");

        mockMvc.perform(put("/api/tasks/" + task.getId() + "/undone")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(false));
    }

    /**
     * Проверяет удаление задачи.
     * <br>
     * Ожидаемое поведение: возвращает статус 204.
     */
    @Test
    public void deleteTask() throws Exception {
        User user = userService.register("testuser5", "password", Role.USER);
        Task task = taskService.createTask("Task to delete", user);
        String token = getToken("testuser5");

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    /**
     * Проверяет обновление описания задачи.
     * <br>
     * Ожидаемое поведение: возвращает статус 200 и обновленную задачу.
     */
    @Test
    public void updateTask() throws Exception {
        User user = userService.register("testuser6", "password", Role.USER);
        Task task = taskService.createTask("Original task", user);
        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("Updated task");
        String token = getToken("testuser6");

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated task"));
    }

    /**
     * Проверяет создание задачи с дублирующимся описанием.
     * <br>
     * Ожидаемое поведение: возвращает статус 400.
     */
    @Test
    public void createTaskDuplicateDescription() throws Exception {
        User user = userService.register("testuser8", "password", Role.USER);
        taskService.createTask("Unique task", user);
        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("Unique task");
        String token = getToken("testuser8");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Проверяет создание задачи с пустым описанием.
     * <br>
     * Ожидаемое поведение: возвращает статус 400.
     */
    @Test
    public void createTaskEmptyDescription() throws Exception {
        userService.register("testuser9", "password", Role.USER);
        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("");
        String token = getToken("testuser9");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Проверяет отметку несуществующей задачи как выполненной.
     * <br>
     * Ожидаемое поведение: возвращает статус 404.
     */
    @Test
    public void markAsDoneInvalidId() throws Exception {
        userService.register("testuser10", "password", Role.USER);
        String token = getToken("testuser10");

        mockMvc.perform(put("/api/tasks/99999/done")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /**
     * Проверяет удаление несуществующей задачи.
     * <br>
     * Ожидаемое поведение: возвращает статус 404.
     */
    @Test
    public void deleteTaskInvalidId() throws Exception {
        userService.register("testuser11", "password", Role.USER);
        String token = getToken("testuser11");

        mockMvc.perform(delete("/api/tasks/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /**
     * Проверяет обновление несуществующей задачи.
     * <br>
     * Ожидаемое поведение: возвращает статус 404.
     */
    @Test
    public void updateTaskInvalidId() throws Exception {
        userService.register("testuser12", "password", Role.USER);
        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("Updated");
        String token = getToken("testuser12");

        mockMvc.perform(put("/api/tasks/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * Проверяет доступ к активным задачам без авторизации.
     * <br>
     * Ожидаемое поведение: возвращает статус 401.
     */
    @Test
    public void getActiveTasksUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет создание задачи без авторизации.
     * <br>
     * Ожидаемое поведение: возвращает статус 401.
     */
    @Test
    public void createTaskUnauthorized() throws Exception {
        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("Task");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет доступ к активным задачам с недействительным токеном.
     * <br>
     * Ожидаемое поведение: возвращает статус 401.
     */
    @Test
    public void getActiveTasksInvalidToken() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет, что пользователь не может получить доступ к задачам другого пользователя.
     * <br>
     * Ожидаемое поведение: возвращает статус 400 или 404 в зависимости от операции.
     */
    @Test
    public void userCannotAccessOtherUserTask() throws Exception {
        User user1 = userService.register("user1", "password", Role.USER);
        User user2 = userService.register("user2", "password", Role.USER);
        Task task1 = taskService.createTask("Task of user1", user1);

        String tokenUser2 = getToken("user2");

        TaskApiController.TaskRequest request = new TaskApiController.TaskRequest();
        request.setDescription("Updated by user2");
        mockMvc.perform(put("/api/tasks/" + task1.getId())
                        .header("Authorization", "Bearer " + tokenUser2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/tasks/" + task1.getId())
                        .header("Authorization", "Bearer " + tokenUser2))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/tasks/" + task1.getId() + "/done")
                        .header("Authorization", "Bearer " + tokenUser2))
                .andExpect(status().isNotFound());
    }
}
