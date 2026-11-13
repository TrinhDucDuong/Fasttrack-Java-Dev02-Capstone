package vn.edu.middlejava.learning.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.auth.entity.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
