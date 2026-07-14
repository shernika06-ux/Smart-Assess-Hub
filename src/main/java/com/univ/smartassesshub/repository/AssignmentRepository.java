package com.univ.smartassesshub.repository;

import com.univ.smartassesshub.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findBySubject(String subject);
    List<Assignment> findByYearGroup(int yearGroup);
    List<Assignment> findByTeacherIdAndYearGroup(Long teacherId, int yearGroup);
    List<Assignment> findByTeacherId(Long teacherId);
}