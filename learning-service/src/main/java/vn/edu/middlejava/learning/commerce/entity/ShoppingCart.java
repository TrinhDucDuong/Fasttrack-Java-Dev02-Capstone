package vn.edu.middlejava.learning.commerce.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartItem> items = new ArrayList<>();
    protected ShoppingCart() {
    }

    public ShoppingCart(Long userId) {
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<ShoppingCartItem> getItems() { return items; }

    public void add(Long courseId, int quantity) {
        validateQuantity(quantity);
        ShoppingCartItem currentItem = findItem(courseId);
        if (currentItem == null) {
            items.add(new ShoppingCartItem(this, courseId, quantity));
        } else {
            currentItem.setQuantity(currentItem.getQuantity() + quantity);
        }
        updatedAt = Instant.now();
    }

    public void setQuantity(Long courseId, int quantity) {
        validateQuantity(quantity);
        ShoppingCartItem item = findItem(courseId);
        if (item == null) {
            throw new IllegalArgumentException("Course is not in cart");
        }
        item.setQuantity(quantity);
        updatedAt = Instant.now();
    }

    public void remove(Long courseId) {
        items.removeIf(item -> item.getCourseId().equals(courseId));
        updatedAt = Instant.now();
    }

    public void clear() {
        items.clear();
        updatedAt = Instant.now();
    }

    private ShoppingCartItem findItem(Long courseId) {
        return items.stream()
                .filter(item -> item.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least one");
        }
    }
}
