package com.univ.smartassesshub.controller;

import com.univ.smartassesshub.config.JwtUtil;
import com.univ.smartassesshub.dto.LoginDTO;
import com.univ.smartassesshub.dto.StudentRegisterDTO;
import com.univ.smartassesshub.dto.TeacherRegisterDTO;
import com.univ.smartassesshub.model.User;
import com.univ.smartassesshub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        // Support both "email" and "username" field names from different frontends
        String email = loginRequest.get("email");
        if (email == null || email.isEmpty()) {
            email = loginRequest.get("username");
        }
        String password = loginRequest.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        // UI testing fallback / admin default
        if ("admin".equals(email) && "admin123".equals(password)) {
            String token = jwtUtil.generateToken("admin");
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", "teacher");
            response.put("name", "System Administrator");
            response.put("fullName", "System Administrator");
            response.put("email", "admin");
            response.put("id", 1L);
            response.put("user", Map.of("role", "teacher", "name", "System Administrator", "email", "admin", "id", 1L));
            return ResponseEntity.ok(response);
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail());

            // Determine readable role string for frontend
            String roleStr = user.getRole().name().replace("ROLE_", "").toLowerCase();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("role", roleStr);
            userMap.put("name", user.getFullName());
            userMap.put("email", user.getEmail());
            userMap.put("id", user.getId());
            if (user.getIdentificationNumber() != null) {
                userMap.put("roll", user.getIdentificationNumber());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", roleStr);
            response.put("fullName", user.getFullName());
            response.put("name", user.getFullName());
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            if (user.getIdentificationNumber() != null) {
                response.put("roll", user.getIdentificationNumber());
                response.put("identificationNumber", user.getIdentificationNumber());
            }
            if (user.getYearGroup() != null) {
                response.put("yearGroup", user.getYearGroup());
                response.put("year", String.valueOf(user.getYearGroup()));
            }
            response.put("user", userMap);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid Username or Password"));
        }
    }

    /**
     * Unified register endpoint used by index.html frontend.
     * Supports both student and teacher registration in a single call.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUnified(@RequestBody Map<String, String> body) {
        String role = body.get("role");
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (name == null || email == null || password == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email address already registered!"));
        }

        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(password);

        if ("teacher".equals(role)) {
            user.setRole(User.Role.ROLE_TEACHER);
            String staffId = body.getOrDefault("staffId", body.getOrDefault("roll", "STAFF_" + System.currentTimeMillis()));
            if (!staffId.isEmpty() && userRepository.existsByIdentificationNumber(staffId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Staff ID already registered!"));
            }
            user.setIdentificationNumber(staffId);
        } else {
            user.setRole(User.Role.ROLE_STUDENT);
            String roll = body.getOrDefault("roll", "STU_" + System.currentTimeMillis());
            if (!roll.isEmpty() && userRepository.existsByIdentificationNumber(roll)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Roll number already registered!"));
            }
            user.setIdentificationNumber(roll);
            // Store student's year of study
            String yearStr = body.get("year");
            if (yearStr != null && !yearStr.isEmpty()) {
                try { user.setYearGroup(Integer.parseInt(yearStr)); } catch (Exception ignored) {}
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Registration successful!"));
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email address already registered!"));
        }
        if (userRepository.existsByIdentificationNumber(dto.getRollNumber())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student Roll Number already exists!"));
        }

        User student = new User();
        student.setFullName(dto.getFullName());
        student.setEmail(dto.getEmail());
        student.setPassword(dto.getPassword());
        student.setRole(User.Role.ROLE_STUDENT);
        student.setIdentificationNumber(dto.getRollNumber());

        userRepository.save(student);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Student registered successfully!"));
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<?> registerTeacher(@RequestBody TeacherRegisterDTO dto) {
        if (!"SMART_ASSESS_SECRET_2026".equals(dto.getUniversitySecretKey())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid University Secret Verification Key!"));
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email address already registered!"));
        }
        if (userRepository.existsByIdentificationNumber(dto.getStaffId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Staff ID already exists!"));
        }

        User teacher = new User();
        teacher.setFullName(dto.getFullName());
        teacher.setEmail(dto.getEmail());
        teacher.setPassword(dto.getPassword());
        teacher.setRole(User.Role.ROLE_TEACHER);
        teacher.setIdentificationNumber(dto.getStaffId());

        userRepository.save(teacher);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Teacher registered successfully!"));
    }
}