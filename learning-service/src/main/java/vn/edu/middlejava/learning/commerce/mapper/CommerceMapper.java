package vn.edu.middlejava.learning.commerce.mapper;

import org.springframework.stereotype.Component;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;
import vn.edu.middlejava.learning.commerce.pricing.PricedCart;

/** Extract Class refactoring: DTO mapping moved out of the services. */
@Component
public class CommerceMapper {
    public CartResponse toCartResponse(Long cartId, PricedCart pricedCart) {
        var items = pricedCart.lines().stream()
                .map(line -> new CartResponse.Item(line.courseId(), line.title(), line.unitPrice(),
                        line.quantity(), line.lineTotal()))
                .toList();
        return new CartResponse(cartId, items, pricedCart.total());
    }

    public OrderResponse toOrderResponse(CustomerOrder order) {
        var items = order.getItems().stream()
                .map(item -> new OrderResponse.Item(item.getCourseId(), item.getCourseTitle(),
                        item.getPrice(), item.getQuantity()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getTotal(), order.getPaymentMethod(),
                order.getPaymentStatus(), order.getPaymentReference(), order.getStatus(),
                order.getCreatedAt(), items);
    }
}
