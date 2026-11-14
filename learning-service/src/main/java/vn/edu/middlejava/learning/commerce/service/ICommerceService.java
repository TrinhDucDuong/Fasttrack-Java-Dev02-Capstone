package vn.edu.middlejava.learning.commerce.service;

import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;

import java.util.List;

public interface ICommerceService {
    CartResponse getCart(Long userId);

    CartResponse addToCart(Long userId, AddCartItemRequest request);

    CartResponse updateCartItem(Long userId, Long courseId, int quantity);

    void removeCartItem(Long userId, Long courseId);

    OrderResponse checkout(Long userId, CheckoutRequest request);

    List<OrderResponse> getOrders(Long userId);

    OrderResponse getOrder(Long userId, Long orderId);
}
