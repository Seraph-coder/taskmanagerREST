package ru.naujava.taskmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для административных функций.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    /**
     * Страница метрик (перенаправление на JavaMelody).
     */
    @GetMapping("/metrics")
    public String metrics() {
        return "redirect:/monitoring";
    }
}
