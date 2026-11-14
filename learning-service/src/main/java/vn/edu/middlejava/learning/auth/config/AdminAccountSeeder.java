package vn.edu.middlejava.learning.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;

@Configuration
public class AdminAccountSeeder {
    @Bean
    ApplicationRunner createLocalAdmin(IStudyUserRepository users, PasswordEncoder encoder,
                                        @Value("${app.admin.email}") String email,
                                        @Value("${app.admin.password}") String password) {
        return args -> seed(users, encoder, email, password);
    }

    @Transactional
    void seed(IStudyUserRepository users, PasswordEncoder encoder, String email, String password) {
        if (email.isBlank() || password.isBlank()) return;
        if (users.findByEmail(email).isEmpty()) {
            users.save(new StudyUser(email, encoder.encode(password), "ADMIN"));
        }
    }
}
