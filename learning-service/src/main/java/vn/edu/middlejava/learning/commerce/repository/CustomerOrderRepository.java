package vn.edu.middlejava.learning.commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByIdAndUserId(Long id, Long userId);
    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
