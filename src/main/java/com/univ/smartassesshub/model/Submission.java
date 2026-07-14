package com.univ.smartassesshub.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "student_id"}) // Oru student oru assignment-ku oru submission thaan panna mudiyum
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String submissionFilePath; // Student upload panna PDF path pointer

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Evaluation Data fields managed by Staff/Teacher Workspace
    private Integer marksAwarded; // Out of 100 or Max grade weights

    @Column(columnDefinition = "TEXT")
    private String teacherRemarks;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status; // SUBMITTED, EVALUATED, LATE_SUBMISSION

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    public enum SubmissionStatus {
        SUBMITTED,
        EVALUATED,
        LATE_SUBMISSION
    }
}