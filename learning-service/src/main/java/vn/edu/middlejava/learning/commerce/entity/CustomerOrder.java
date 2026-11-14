package vn.edu.middlejava.learning.commerce.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(Long userId, BigDecimal total, String paymentMethod, String paymentStatus) {
        this.userId = userId;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.status = "PAID".equals(paymentStatus) ? "CONFIRMED" : "AWAITING_PAYMENT";
    }
    public void addItem(Long courseId, String title, BigDecimal price, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Order quantity must be at least one");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Order item price cannot be negative");
        }
        items.add(new CustomerOrderItem(this, courseId, title, price, quantity));
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<CustomerOrderItem> getItems() { return items; }
}
