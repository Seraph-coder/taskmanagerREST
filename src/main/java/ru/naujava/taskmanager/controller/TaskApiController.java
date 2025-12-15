package ru.naujava.taskmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.service.TaskService;
import ru.naujava.taskmanager.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST API контроллер для управления задачами.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management API", description = "API для управления задачами пользователя")
public class TaskApiController {
    private static final Logger logger = LoggerFactory.getLogger(TaskApiController.class);

    private final TaskService taskService;
    private final UserService userService;

    public TaskApiController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Получить все активные задачи пользователя.
     */
    @GetMapping
    @Operation(summary = "Получить активные задачи",
            description = "Возвращает список всех активных (не выполненных) задач текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<List<TaskDto>> getActiveTasks(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        List<Task> tasks = taskService.findActiveTasksByUser(user);
        List<TaskDto> taskDtos = tasks.stream().map(this::convertToDto).toList();
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * Получить все выполненные задачи пользователя.
     */
    @GetMapping("/completed")
    @Operation(summary = "Получить выполненные задачи",
            description = "Возвращает список всех выполненных задач текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<List<TaskDto>> getCompletedTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        List<Task> tasks = taskService.findCompletedTasksByUser(user);
        List<TaskDto> taskDtos = tasks.stream().map(this::convertToDto).toList();
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * Создать новую задачу.
     */
    @PostMapping
    @Operation(summary = "Создать новую задачу",
            description = "Создает новую задачу для текущего пользователя. Описание должно быть уникальным.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description =
                    "Неверные данные или задача с таким описанием уже существует"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<TaskDto> createTask(@RequestBody @Valid TaskRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        try {
            Task task = taskService.createTask(request.getDescription(), user);
            TaskDto taskDto = convertToDto(task);
            return ResponseEntity.ok(taskDto);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании задачи для пользователя {}", userDetails.getUsername(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Отметить задачу как выполненную.
     */
    @PutMapping("/{id}/done")
    @Operation(summary = "Отметить задачу как выполненную",
            description = "Помечает указанную задачу как выполненную.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача отмечена как выполненная"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<TaskDto> markAsDone(@Parameter(description = "ID задачи") @PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        try {
            Task task = taskService.markTaskAsDone(id, user);
            TaskDto taskDto = convertToDto(task);
            return ResponseEntity.ok(taskDto);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при отметке задачи {} как выполненной для пользователя {}",
                    id, userDetails.getUsername(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Отметить задачу как не выполненную.
     */
    @PutMapping("/{id}/undone")
    @Operation(summary = "Вернуть задачу в активные",
            description = "Помечает указанную задачу как не выполненную (активную).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача возвращена в активные"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<TaskDto> markAsNotDone(@Parameter(description = "ID задачи") @PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        try {
            Task task = taskService.markTaskAsNotDone(id, user);
            TaskDto taskDto = convertToDto(task);
            return ResponseEntity.ok(taskDto);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при возврате задачи {} в активные для пользователя {}",
                    id, userDetails.getUsername(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Обновить описание задачи.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить задачу",
            description = "Обновляет описание указанной задачи.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "400", description = "Неверные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<TaskDto> updateTask(@Parameter(description = "ID задачи") @PathVariable Long id,
                                              @RequestBody @Valid TaskRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        try {
            Task task = taskService.updateTaskDescription(id, request.getDescription(), user);
            TaskDto taskDto = convertToDto(task);
            return ResponseEntity.ok(taskDto);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при обновлении задачи {} для пользователя {}", id, userDetails.getUsername(), e);
            if (e.getMessage().contains("не найдена")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    /**
     * Удалить задачу.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить задачу",
            description = "Удаляет указанную задачу.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<Void> deleteTask(@Parameter(description = "ID задачи") @PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getCurrentUser(userDetails);
        try {
            taskService.deleteTaskByIdAndUser(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при удалении задачи {} для пользователя {}", id, userDetails.getUsername(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить текущего пользователя.
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
    }

    /**
     * Преобразовать Task в TaskDto.
     */
    private TaskDto convertToDto(Task task) {
        return new TaskDto(task.getId(), task.getDescription(), task.isDone(),
                task.getCreatedAt(), task.getUpdatedAt());
    }

    /**
     * DTO для задачи.
     */
    public static class TaskDto {
        private final Long id;
        private final String description;
        private final boolean done;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public TaskDto(Long id, String description, boolean done,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.description = description;
            this.done = done;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        /**
         * Получить ID.
         */
        public Long getId() {
            return id;
        }

        /**
         * Получить описание.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Проверить, выполнена ли задача.
         */
        public boolean isDone() {
            return done;
        }

        /**
         * Получить дату создания.
         */
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        /**
         * Получить дату обновления.
         */
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }

    /**
     * DTO для запроса задачи.
     */
    public static class TaskRequest {
        @NotBlank(message = "Описание задачи не должно быть пустым")
        private String description;

        public TaskRequest() {
        }

        public TaskRequest(String description) {
            this.description = description;
        }

        /**
         * Получить описание.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Установить описание.
         */
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
