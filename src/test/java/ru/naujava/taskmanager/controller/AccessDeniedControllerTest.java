package ru.naujava.taskmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для AccessDeniedController.
 */
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@Transactional
public class AccessDeniedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Тест отображения страницы отказа в доступе.
     */
    @Test
    public void testAccessDeniedPage() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("access-denied"))
                .andExpect(content().string(containsString("У вас недостаточно прав")));
    }
}
