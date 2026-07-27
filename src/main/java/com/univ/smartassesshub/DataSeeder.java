package com.univ.smartassesshub;

import com.univ.smartassesshub.model.User;
import com.univ.smartassesshub.repository.AssignmentRepository;
import com.univ.smartassesshub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      AssignmentRepository assignmentRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Only seed demo user accounts on very first run (empty database)
            if (userRepository.count() == 0) {

                // Demo teacher account
                User teacher = new User();
                teacher.setFullName("Dr. Shernika S.S.");
                teacher.setEmail("teacher@univ.edu");
                teacher.setPassword(passwordEncoder.encode("teacher123"));
                teacher.setRole(User.Role.ROLE_TEACHER);
                teacher.setIdentificationNumber("T101");
                userRepository.save(teacher);

                // Demo student account — 1st Year
                User student = new User();
                student.setFullName("John Doe");
                student.setEmail("student@univ.edu");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole(User.Role.ROLE_STUDENT);
                student.setIdentificationNumber("502400");
                student.setYearGroup(1);
                userRepository.save(student);

                System.out.println("[DataSeeder] Demo accounts ready:");
                System.out.println("  Teacher  → teacher@univ.edu  / teacher123");
                System.out.println("  Student  → student@univ.edu  / student123  (1st Year)");
            } else {
                System.out.println("[DataSeeder] Existing database retained — no seeding needed.");
            }

            // NO sample assignments are injected here.
            // All assignments are created exclusively by teachers through the Teacher Portal.
        };
    }
}
