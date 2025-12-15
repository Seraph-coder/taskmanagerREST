package ru.naujava.taskmanager.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.service.TaskService;
import ru.naujava.taskmanager.service.UserService;

import java.util.List;

/**
 * Контроллер для управления задачами.
 */
@Controller
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Показывает список активных задач.
     */
    @GetMapping
    public String listTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<Task> tasks = taskService.findActiveTasksByUser(user);
        model.addAttribute("tasks", tasks);
        model.addAttribute("newTask", new Task());
        return "tasks";
    }

    /**
     * Показывает выполненные задачи.
     */
    @GetMapping("/completed")
    public String listCompletedTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<Task> tasks = taskService.findCompletedTasksByUser(user);
        model.addAttribute("tasks", tasks);
        return "completed-tasks";
    }

    /**
     * Создает новую задачу.
     */
    @PostMapping
    public String createTask(@ModelAttribute("newTask") Task task,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        try {
            taskService.createTask(task.getDescription(), user);
            redirectAttributes.addFlashAttribute("success", "Задача создана успешно!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }

    /**
     * Удаляет задачу.
     */
    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id,
                             @RequestParam(defaultValue = "active") String from,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        try {
            taskService.deleteTaskByIdAndUser(id, user);
            redirectAttributes.addFlashAttribute("success", "Задача удалена!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "completed".equals(from) ? "redirect:/tasks/completed" : "redirect:/tasks";
    }

    /**
     * Отмечает задачу как выполненную.
     */
    @PostMapping("/{id}/done")
    public String markAsDone(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        try {
            taskService.markTaskAsDone(id, user);
            redirectAttributes.addFlashAttribute("success",
                    "Задача отмечена как выполненная!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }

    /**
     * Отмечает задачу как не выполненную.
     */
    @PostMapping("/{id}/undone")
    public String markTaskAsNotDone(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            taskService.markTaskAsNotDone(id, getCurrentUser(userDetails));
            redirectAttributes.addFlashAttribute("success",
                    "Задача возвращена в активные");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks/completed";
    }

    /**
     * Обновляет описание задачи.
     */
    @PostMapping("/{id}/edit")
    public String editTask(@PathVariable Long id,
                           @RequestParam String description,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        try {
            taskService.updateTaskDescription(id, description, user);
            redirectAttributes.addFlashAttribute("success", "Задача обновлена!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }

    /**
     * Получает текущего пользователя по UserDetails.
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
    }
}
