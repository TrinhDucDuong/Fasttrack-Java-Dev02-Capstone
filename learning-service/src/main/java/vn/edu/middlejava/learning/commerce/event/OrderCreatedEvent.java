package vn.edu.middlejava.learning.commerce.event;

public record OrderCreatedEvent(Long orderId, Long userId) {
}
