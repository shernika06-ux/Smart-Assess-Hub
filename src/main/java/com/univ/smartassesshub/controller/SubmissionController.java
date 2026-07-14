package com.univ.smartassesshub.controller;

import com.univ.smartassesshub.model.Assignment;
import com.univ.smartassesshub.model.Submission;
import com.univ.smartassesshub.model.User;
import com.univ.smartassesshub.repository.AssignmentRepository;
import com.univ.smartassesshub.repository.SubmissionRepository;
import com.univ.smartassesshub.repository.UserRepository;
import com.univ.smartassesshub.service.FileStorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SubmissionController {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public SubmissionController(SubmissionRepository submissionRepository,
                                AssignmentRepository assignmentRepository,
                                UserRepository userRepository,
                                FileStorageService fileStorageService) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // ============================================================
    // 1. POST /api/submissions/upload  — Student uploads PDF
    //    Params: assignmentId (Long), studentId (Long), file (MultipartFile)
    // ============================================================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSubmission(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam("studentId") Long studentId,
            @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> studentOpt = userRepository.findById(studentId);
            Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);

            if (studentOpt.isEmpty() || studentOpt.get().getRole() != User.Role.ROLE_STUDENT) {
                return ResponseEntity.status(403).body(Map.of("error", "Only student accounts can upload submissions!"));
            }
            if (assignmentOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Assignment not found!"));
            }

            // Prevent duplicate submissions
            Optional<Submission> existing = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
            if (existing.isPresent()) {
                return ResponseEntity.status(400).body(Map.of("error", "You have already submitted this assignment!"));
            }

            String storedFileName = fileStorageService.storeFile(file, "sub_" + assignmentId + "_" + studentId);

            Submission submission = new Submission();
            submission.setAssignment(assignmentOpt.get());
            submission.setStudent(studentOpt.get());
            submission.setSubmissionFilePath(storedFileName);
            submission.setStatus(Submission.SubmissionStatus.SUBMITTED);

            Submission saved = submissionRepository.save(submission);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Submission uploaded successfully!");
            response.put("submissionId", saved.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ============================================================
    // 2. GET /api/submissions/student/{studentId}  — Student's own submissions
    // ============================================================
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(toMapList(submissionRepository.findByStudentId(studentId)));
    }

    // ============================================================
    // 3. GET /api/submissions/assignment/{assignmentId}  — All submissions for an assignment
    // ============================================================
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<Map<String, Object>>> getByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(toMapList(submissionRepository.findByAssignmentId(assignmentId)));
    }

    // ============================================================
    // 4. GET /api/submissions/year/{year}  — All submissions for a year group (teacher view)
    // ============================================================
    @GetMapping("/year/{year}")
    public ResponseEntity<List<Map<String, Object>>> getByYear(@PathVariable int year) {
        return ResponseEntity.ok(toMapList(submissionRepository.findByAssignment_YearGroup(year)));
    }

    // ============================================================
    // 5. GET /api/submissions/view/{submissionId}  — Stream actual PDF file
    // ============================================================
    @GetMapping(value = "/view/{submissionId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> viewSubmissionPdf(@PathVariable Long submissionId) {
        try {
            Optional<Submission> subOpt = submissionRepository.findById(submissionId);
            if (subOpt.isEmpty()) {
                return ResponseEntity.status(404).build();
            }

            String fileName = subOpt.get().getSubmissionFilePath();
            Path uploadDir = Paths.get("uploads-dir").toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(fileName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).build();
            }

            byte[] pdfBytes = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ============================================================
    // 6. PUT /api/submissions/grade/{id}  — Teacher grades a submission
    //    Body: { marks: "90", feedback: "Good work!" }
    // ============================================================
    @PutMapping("/grade/{id}")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        Optional<Submission> subOpt = submissionRepository.findById(id);
        if (subOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Submission not found"));
        }

        Submission submission = subOpt.get();

        if (payload.containsKey("marks")) {
            submission.setMarksAwarded(Integer.parseInt(payload.get("marks").toString()));
        }
        // Accept both "feedback" (from frontend) and "remarks"
        if (payload.containsKey("feedback")) {
            submission.setTeacherRemarks(payload.get("feedback").toString());
        } else if (payload.containsKey("remarks")) {
            submission.setTeacherRemarks(payload.get("remarks").toString());
        }
        submission.setStatus(Submission.SubmissionStatus.EVALUATED);
        submissionRepository.save(submission);

        return ResponseEntity.ok(Map.of("status", "success", "message", "Grade saved successfully!"));
    }

    // ============================================================
    // Helper: Submission entity → plain Map (avoids circular JSON)
    // ============================================================
    private List<Map<String, Object>> toMapList(List<Submission> submissions) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Submission s : submissions) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("assignmentId", s.getAssignment().getId());
            map.put("assignmentTitle", s.getAssignment().getTitle());
            map.put("subject", s.getAssignment().getSubject());
            map.put("yearGroup", s.getAssignment().getYearGroup());
            map.put("studentId", s.getStudent().getId());
            map.put("studentName", s.getStudent().getFullName());
            map.put("studentRoll", s.getStudent().getIdentificationNumber());
            map.put("fileName", s.getSubmissionFilePath());
            map.put("submittedAt", s.getSubmittedAt());
            map.put("marks", s.getMarksAwarded());
            map.put("feedback", s.getTeacherRemarks());
            map.put("status", s.getStatus() != null ? s.getStatus().name() : "SUBMITTED");
            result.add(map);
        }
        return result;
    }
}
