package vn.edu.middlejava.learning.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;

import java.util.Locale;

/**
 * Keeps the configured admin account (ADMIN_EMAIL / ADMIN_PASSWORD, from AWS Parameter Store in the cloud)
 * in sync on every startup: created if missing, and if the e-mail already exists - for example it was
 * registered as a customer first - it is promoted to ADMIN and its password reset to the configured one.
 * Rotating the admin password is therefore just updating the secret and redeploying.
 */
@Configuration
public class AdminAccountSeeder {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountSeeder.class);

    @Bean
    ApplicationRunner createLocalAdmin(IStudyUserRepository users, PasswordEncoder encoder,
                                        @Value("${app.admin.email}") String email,
                                        @Value("${app.admin.password}") String password) {
        return args -> seed(users, encoder, email, password);
    }

    void seed(IStudyUserRepository users, PasswordEncoder encoder, String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        users.findByEmail(normalizedEmail).ifPresentOrElse(
                existing -> syncExistingAccount(users, encoder, existing, password),
                () -> {
                    users.save(new StudyUser(normalizedEmail, encoder.encode(password), "ADMIN"));
                    log.info("Created configured admin account");
                });
    }

    private void syncExistingAccount(IStudyUserRepository users, PasswordEncoder encoder,
                                     StudyUser existing, String password) {
        boolean isAdmin = "ADMIN".equals(existing.getRole());
        boolean passwordMatches = encoder.matches(password, existing.getPasswordHash());
        if (isAdmin && passwordMatches) {
            return;
        }
        existing.becomeAdmin(encoder.encode(password));
        users.save(existing);
        log.info("Synced configured admin account (role {} -> ADMIN, password {})",
                existing.getRole(), passwordMatches ? "unchanged" : "reset to configured value");
    }
}
