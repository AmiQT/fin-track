package com.amiqt.fintrackpro.config;

import com.amiqt.fintrackpro.enums.Role;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Value("${app.admin.email}")
    private String adminEmail;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Fix legacy database rows if any
        try {
            jdbcTemplate.execute(
                "UPDATE employees SET " +
                "full_name = COALESCE(full_name, first_name || ' ' || last_name), " +
                "employee_code = COALESCE(employee_code, employee_id), " +
                "basic_salary = COALESCE(basic_salary, salary), " +
                "join_date = COALESCE(join_date, joined_date::date) " +
                "WHERE full_name IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE employees SET housing_allowance = 0 WHERE housing_allowance IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE employees SET transport_allowance = 0 WHERE transport_allowance IS NULL"
            );
            System.out.println("Legacy employee rows synced successfully.");
        } catch (Exception e) {
            System.err.println("Note: No legacy employee rows to sync: " + e.getMessage());
        }

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin user created: " + adminEmail);
        }
    }
}
