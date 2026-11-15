package vn.edu.middlejava.learning.auth.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAccountSeederTest {
    private final IStudyUserRepository users = mock(IStudyUserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AdminAccountSeeder seeder = new AdminAccountSeeder();

    @Test
    void createsConfiguredAdminWithEncodedPasswordAndAdminRole() {
        when(users.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("demo-password")).thenReturn("encoded-password");

        seeder.seed(users, encoder, " Admin@Example.com ", "demo-password");

        var savedUser = ArgumentCaptor.forClass(StudyUser.class);
        verify(users).save(savedUser.capture());
        assertThat(savedUser.getValue().getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUser.getValue().getRole()).isEqualTo("ADMIN");
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void keepsAdminUnchangedWhenPasswordAlreadyMatches() {
        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(new StudyUser(
                "admin@example.com", "existing-hash", "ADMIN")));
        when(encoder.matches("demo-password", "existing-hash")).thenReturn(true);

        seeder.seed(users, encoder, "admin@example.com", "demo-password");

        verify(users, never()).save(any());
        verify(encoder, never()).encode(any());
    }

    @Test
    void promotesCustomerWhoRegisteredTheAdminEmailFirstAndResetsPassword() {
        StudyUser existing = new StudyUser("admin@example.com", "customer-hash");
        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));
        when(encoder.matches("demo-password", "customer-hash")).thenReturn(false);
        when(encoder.encode("demo-password")).thenReturn("admin-hash");

        seeder.seed(users, encoder, "admin@example.com", "demo-password");

        verify(users).save(existing);
        assertThat(existing.getRole()).isEqualTo("ADMIN");
        assertThat(existing.getPasswordHash()).isEqualTo("admin-hash");
    }

    @Test
    void resetsPasswordWhenConfiguredSecretWasRotated() {
        StudyUser existing = new StudyUser("admin@example.com", "old-hash", "ADMIN");
        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));
        when(encoder.matches("new-password", "old-hash")).thenReturn(false);
        when(encoder.encode("new-password")).thenReturn("new-hash");

        seeder.seed(users, encoder, "admin@example.com", "new-password");

        verify(users).save(existing);
        assertThat(existing.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void skipsAdminCreationWhenCredentialsAreBlank() {
        seeder.seed(users, encoder, "", "");

        verify(users, never()).findByEmail(any());
        verify(users, never()).save(any());
    }
}
