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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "shopping_cart_items", uniqueConstraints =
        @UniqueConstraint(columnNames = {"cart_id", "course_id"}))
public class ShoppingCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private int quantity;

    protected ShoppingCartItem() {
    }

    ShoppingCartItem(ShoppingCart cart, Long courseId, int quantity) {
        this.cart = cart;
        this.courseId = courseId;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public int getQuantity() { return quantity; }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
