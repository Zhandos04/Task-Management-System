package org.example.taskmanagementsystem.config;

import org.example.taskmanagementsystem.entity.Role;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin")) {
            User admin = new User();
            admin.setEmail("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setIsVerified(true);
            userRepository.save(admin);
            System.out.println("Админ аккаунт успешно создан!");
        } else {
            System.out.println("Админ аккаунт уже существует.");
        }
    }
}
