package com.univ.smartassesshub.controller;

import com.univ.smartassesshub.model.Assignment;
import com.univ.smartassesshub.model.User;
import com.univ.smartassesshub.repository.AssignmentRepository;
import com.univ.smartassesshub.repository.UserRepository;
import com.univ.smartassesshub.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public AssignmentController(AssignmentRepository assignmentRepository,
                                UserRepository userRepository,
                                FileStorageService fileStorageService) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // ============================================================
    // POST /api/assignments/create  — Teacher creates an assignment
    // Supports both multipart/form-data (with optional reference file) and JSON body
    // ============================================================
    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> createAssignmentMultipart(
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam("dueDate") String dueDate,
            @RequestParam(value = "yearGroup", required = false, defaultValue = "1") int yearGroup,
            @RequestParam("teacherId") Long teacherId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return processCreateAssignment(title, subject, description, dueDate, yearGroup, teacherId, file);
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAssignmentJson(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("teacherId") || body.get("teacherId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "teacherId is required"));
        }
        Long teacherId = Long.parseLong(body.get("teacherId").toString());
        String title = body.getOrDefault("title", "").toString();
        String subject = body.getOrDefault("subject", "").toString();
        String description = body.getOrDefault("description", "").toString();
        String dueDate = body.getOrDefault("dueDate", "").toString();
        int yearGroup = 1;
        if (body.containsKey("yearGroup") && body.get("yearGroup") != null) {
            try { yearGroup = Integer.parseInt(body.get("yearGroup").toString()); } catch (Exception ignored) {}
        }
        return processCreateAssignment(title, subject, description, dueDate, yearGroup, teacherId, null);
    }

    private ResponseEntity<?> processCreateAssignment(String title, String subject, String description,
                                                       String dueDate, int yearGroup, Long teacherId,
                                                       MultipartFile file) {
        try {
            Optional<User> teacherOpt = userRepository.findById(teacherId);
            if (teacherOpt.isEmpty() || teacherOpt.get().getRole() != User.Role.ROLE_TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only verified teacher accounts can create assignments!"));
            }

            Assignment assignment = new Assignment();
            assignment.setTitle(title);
            assignment.setSubject(subject);
            assignment.setDescription(description != null ? description : "No description provided.");
            assignment.setDueDate(dueDate);
            assignment.setYearGroup(yearGroup > 0 ? yearGroup : 1);
            assignment.setTeacher(teacherOpt.get());

            if (file != null && !file.isEmpty()) {
                String storedFileName = fileStorageService.storeFile(file, "ref_" + teacherId);
                assignment.setReferenceFilePath(storedFileName);
            }

            Assignment saved = assignmentRepository.save(assignment);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Assignment posted successfully!",
                    "id", saved.getId(),
                    "assignmentId", saved.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create assignment: " + e.getMessage()));
        }
    }

    // ============================================================
    // GET /api/assignments/all  — All assignments
    // ============================================================
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllAssignments() {
        return ResponseEntity.ok(toMapList(assignmentRepository.findAll()));
    }

    // ============================================================
    // GET /api/assignments/year/{year}  — Assignments for a year (student view)
    // ============================================================
    @GetMapping("/year/{year}")
    public ResponseEntity<List<Map<String, Object>>> getByYear(@PathVariable int year) {
        // Only return assignments that belong to the requested year group — no cross-year fallback
        List<Assignment> assignments = assignmentRepository.findByYearGroup(year);
        return ResponseEntity.ok(toMapList(assignments));
    }

    // ============================================================
    // GET /api/assignments/teacher/{teacherId}/year/{year}
    // ============================================================
    @GetMapping("/teacher/{teacherId}/year/{year}")
    public ResponseEntity<List<Map<String, Object>>> getByTeacherAndYear(
            @PathVariable Long teacherId, @PathVariable int year) {
        return ResponseEntity.ok(toMapList(
                assignmentRepository.findByTeacherIdAndYearGroup(teacherId, year)));
    }

    // ============================================================
    // GET /api/assignments/teacher/{teacherId}  — All teacher's assignments
    // ============================================================
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Map<String, Object>>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(toMapList(assignmentRepository.findByTeacherId(teacherId)));
    }

    // ============================================================
    // DELETE /api/assignments/{id}
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        if (!assignmentRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Assignment not found!"));
        }
        assignmentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Assignment deleted!"));
    }

    // Helper: Convert Assignment entities to plain Maps
    private List<Map<String, Object>> toMapList(List<Assignment> assignments) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Assignment a : assignments) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", a.getId());
            map.put("title", a.getTitle());
            map.put("subject", a.getSubject());
            map.put("description", a.getDescription());
            map.put("yearGroup", a.getYearGroup());
            map.put("dueDate", a.getDueDate());
            map.put("referenceFilePath", a.getReferenceFilePath());
            map.put("teacherId", a.getTeacher() != null ? a.getTeacher().getId() : null);
            map.put("teacherName", a.getTeacher() != null ? a.getTeacher().getFullName() : "Faculty");
            map.put("createdAt", a.getCreatedAt());
            result.add(map);
        }
        return result;
    }
}