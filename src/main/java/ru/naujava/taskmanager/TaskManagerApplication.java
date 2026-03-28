package ru.naujava.taskmanager;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.service.UserService;

/**
 * Главный класс приложения Task Manager.
 */
@SpringBootApplication
public class TaskManagerApplication {

    private final UserService userService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    public TaskManagerApplication(UserService userService) {
        this.userService = userService;
    }

    /**
     * Инициализация администратора по умолчанию.
     * (упрощенная реализация для учебного проекта)
     */
    @PostConstruct
    @Profile("!test")
    public void initAdmin() {
        if (userService.findByUsername(adminUsername).isEmpty()) {
            userService.register(adminUsername, adminPassword, Role.ADMIN);
            System.out.println("Дефолтный админ создан: username=" + adminUsername);
        }
    }

    /**
     * Точка входа в приложение.
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }

}
