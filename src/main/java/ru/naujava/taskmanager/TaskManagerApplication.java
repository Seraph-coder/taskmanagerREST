package ru.naujava.taskmanager;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.service.UserService;

/**
 * Главный класс приложения Task Manager.
 */
@SpringBootApplication
public class TaskManagerApplication {

    private final UserService userService;

    public TaskManagerApplication(UserService userService) {
        this.userService = userService;
    }

    /**
     * Инициализация администратора по умолчанию.
     */
    @PostConstruct
    @Profile("!test")
    public void initAdmin() {
        if (userService.findByUsername("admin").isEmpty()) {
            userService.register("admin", "admin123", Role.ADMIN);
            System.out.println("Дефолтный админ создан: username=admin, password=admin123");
        }
    }

    /**
     * Точка входа в приложение.
     */
	public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
	}

}
