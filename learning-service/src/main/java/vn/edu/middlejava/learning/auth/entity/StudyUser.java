package vn.edu.middlejava.learning.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class StudyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "CUSTOMER";

    protected StudyUser() {
    }

    public StudyUser(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }

    public StudyUser(String email, String passwordHash, String role) {
        this(email, passwordHash);
        this.role = role;
    }

    /** Used only by the configured-admin seeder: makes this account the admin with the configured password. */
    public void becomeAdmin(String newPasswordHash) {
        this.role = "ADMIN";
        this.passwordHash = newPasswordHash;
    }

    public String getRole() { return role; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
}
