package ru.naujava.taskmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для страницы отказа в доступе.
 */
@Controller
public class AccessDeniedController {

    /**
     * Отображает страницу отказа в доступе.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
