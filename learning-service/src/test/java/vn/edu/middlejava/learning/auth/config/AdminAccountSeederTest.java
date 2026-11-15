package vn.edu.middlejava.learning.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

        seeder.seed(users, encoder, "admin@example.com", "demo-password");

        var savedUser = org.mockito.ArgumentCaptor.forClass(StudyUser.class);
        verify(users).save(savedUser.capture());
        assertThat(savedUser.getValue().getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUser.getValue().getRole()).isEqualTo("ADMIN");
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void keepsExistingAccountUnchanged() {
        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(new StudyUser(
                "admin@example.com", "existing-hash", "ADMIN")));

        seeder.seed(users, encoder, "admin@example.com", "replacement-password");

        verify(users, never()).save(org.mockito.ArgumentMatchers.any());
        verify(encoder, never()).encode(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsAdminCreationWhenCredentialsAreBlank() {
        seeder.seed(users, encoder, "", "");

        verify(users, never()).findByEmail(org.mockito.ArgumentMatchers.any());
        verify(users, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
