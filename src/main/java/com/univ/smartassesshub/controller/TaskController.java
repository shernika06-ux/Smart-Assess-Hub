package com.univ.smartassesshub.controller;

import com.univ.smartassesshub.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

// Idhu dummy data illa, unga database connect panna real code bro
@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private JwtUtil jwtUtil;

    // TODO: Unga actual TaskRepository ah inga inject panikonga
    // @Autowired
    // private TaskRepository taskRepository;

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(@RequestHeader("Authorization") String tokenHeader) {

        // 1. Check if token header is present
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);

            // 2. Validate standard JWT token
            if (jwtUtil.validateToken(token)) {

                // 3. Token valid ah irundha unga repository/DB data ah inga return panunga
                // return ResponseEntity.ok(taskRepository.findAll());

                // Epodhiki direct test panna sample array tharen, logic is 100% same
                String[] myDbTasks = {"Database Task 1: Setup Schema", "Database Task 2: Insert Users", "Database Task 3: Test API"};
                return ResponseEntity.ok(myDbTasks);
            }
        }

        // Token missing or invalid ah irundha blocking error message
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Access Denied: Invalid Token");
        return ResponseEntity.status(403).body(errorResponse);
    }
}