package vn.edu.middlejava.learning.commerce.service;

import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse checkout(Long userId, CheckoutRequest request);

    List<OrderResponse> getOrders(Long userId);

    OrderResponse getOrder(Long userId, Long orderId);
}
