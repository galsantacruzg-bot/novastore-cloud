package com.novastore.api.controller;

import com.novastore.api.service.ProductService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {
    private final ProductService productService;
    private final JdbcTemplate jdbc;

    public SystemController(ProductService productService, JdbcTemplate jdbc) {
        this.productService = productService;
        this.jdbc = jdbc;
    }

    /** Comprobación real: Spring + SELECT 1 a PostgreSQL. */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            Integer ok = jdbc.queryForObject("SELECT 1", Integer.class);
            if (!Integer.valueOf(1).equals(ok)) throw new IllegalStateException("Consulta de salud inválida");
            return ResponseEntity.ok(Map.of("status", "UP", "service", "NovaStore API", "database", "UP", "time", OffsetDateTime.now().toString()));
        } catch (DataAccessException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "DOWN", "service", "NovaStore API", "database", "DOWN", "time", OffsetDateTime.now().toString()));
        }
    }

    @GetMapping("/categories")
    public List<String> categories() { return productService.categories(); }
}
