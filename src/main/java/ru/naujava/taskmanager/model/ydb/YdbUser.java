package ru.naujava.taskmanager.model.ydb;

import java.time.Instant;

/**
 * Модель пользователя для YDB.
 */
public class YdbUser {
    private Long id;
    private String username;
    private String password;
    private String role;

    public YdbUser() {
    }

    public YdbUser(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

// ============================================================