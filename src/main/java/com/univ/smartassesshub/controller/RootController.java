package com.univ.smartassesshub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiRoot() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("app", "Smart Assess Hub API");
        status.put("status", "UP");
        status.put("version", "1.0.0");
        status.put("frontendUrl", "http://localhost:8080");
        status.put("endpoints", Map.of(
                "auth", "/api/auth/login",
                "assignments", "/api/assignments/all",
                "submissions", "/api/submissions/year/1"
        ));
        return ResponseEntity.ok(status);
    }
}
