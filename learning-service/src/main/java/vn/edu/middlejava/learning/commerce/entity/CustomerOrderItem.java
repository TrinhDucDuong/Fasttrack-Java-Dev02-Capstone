package vn.edu.middlejava.learning.commerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "customer_order_items")
public class CustomerOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_title", nullable = false, length = 200)
    private String courseTitle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    protected CustomerOrderItem() {
    }

    CustomerOrderItem(CustomerOrder order, Long courseId, String title, BigDecimal price, int quantity) {
        this.order = order;
        this.courseId = courseId;
        this.courseTitle = title;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
