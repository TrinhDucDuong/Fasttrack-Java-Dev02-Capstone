package vn.edu.middlejava.learning.commerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import java.util.Optional;
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart,Long> { Optional<ShoppingCart> findByUserId(Long userId); }
