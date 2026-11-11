package com.novastore.api.controller;

import com.novastore.api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemControllerTest {
    @Test void healthOkWhenDatabaseResponds() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        var response = new SystemController(mock(ProductService.class), jdbc).health();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("database"));
    }
    @Test void healthReturns503WhenDatabaseIsDown() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenThrow(new DataAccessResourceFailureException("unavailable"));
        var response = new SystemController(mock(ProductService.class), jdbc).health();
        assertEquals(503, response.getStatusCode().value());
        assertEquals("DOWN", response.getBody().get("database"));
    }
}
