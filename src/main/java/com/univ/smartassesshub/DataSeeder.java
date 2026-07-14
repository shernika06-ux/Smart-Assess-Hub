package com.univ.smartassesshub;

import com.univ.smartassesshub.model.User;
import com.univ.smartassesshub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                // Seed teacher
                User teacher = new User();
                teacher.setFullName("Dr. Shernika S.S.");
                teacher.setEmail("teacher@univ.edu");
                teacher.setPassword("teacher123");
                teacher.setRole(User.Role.ROLE_TEACHER);
                teacher.setIdentificationNumber("T101");
                userRepository.save(teacher);

                // Seed student
                User student = new User();
                student.setFullName("John Doe");
                student.setEmail("student@univ.edu");
                student.setPassword("student123");
                student.setRole(User.Role.ROLE_STUDENT);
                student.setIdentificationNumber("S101");
                userRepository.save(student);

                System.out.println("Database seeded with sample users: teacher@univ.edu and student@univ.edu");
            }
        };
    }
}
