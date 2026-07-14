package com.univ.smartassesshub.controller;

import com.univ.smartassesshub.model.Assignment;
import com.univ.smartassesshub.model.User;
import com.univ.smartassesshub.repository.AssignmentRepository;
import com.univ.smartassesshub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public AssignmentController(AssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    // ============================================================
    // POST /api/assignments/create  — Teacher creates an assignment
    // Body: { title, subject, yearGroup, dueDate, description, teacherId }
    // ============================================================
    @PostMapping("/create")
    public ResponseEntity<?> createAssignment(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("teacherId") || body.get("teacherId") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "teacherId is required"));
            }
            Long teacherId = Long.parseLong(body.get("teacherId").toString());
            Optional<User> teacherOpt = userRepository.findById(teacherId);
            if (teacherOpt.isEmpty() || teacherOpt.get().getRole() != User.Role.ROLE_TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only verified teacher accounts can create assignments!"));
            }

            Assignment assignment = new Assignment();
            assignment.setTitle(body.get("title").toString());
            assignment.setSubject(body.get("subject").toString());
            assignment.setDescription(body.getOrDefault("description", "").toString());
            assignment.setDueDate(body.get("dueDate").toString());
            
            int year = 1;
            if (body.containsKey("yearGroup") && body.get("yearGroup") != null) {
                year = Integer.parseInt(body.get("yearGroup").toString());
            }
            assignment.setYearGroup(year);
            assignment.setTeacher(teacherOpt.get());

            assignmentRepository.save(assignment);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Assignment posted successfully!"));
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
        return ResponseEntity.ok(toMapList(assignmentRepository.findByYearGroup(year)));
    }

    // ============================================================
    // GET /api/assignments/teacher/{teacherId}/year/{year}
    // — Teacher's posted assignments filtered by year
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

    // ============================================================
    // Helper: Convert Assignment entities to plain Maps (avoids circular JSON)
    // ============================================================
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
            map.put("teacherId", a.getTeacher().getId());
            map.put("teacherName", a.getTeacher().getFullName());
            map.put("createdAt", a.getCreatedAt());
            result.add(map);
        }
        return result;
    }
}