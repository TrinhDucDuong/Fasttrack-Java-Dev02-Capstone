package vn.edu.middlejava.learning.commerce.service;

import vn.edu.middlejava.learning.commerce.dto.OrderResponse;

import java.util.List;

public interface IAdminOrderService {
    List<OrderResponse> getAllOrders();

    OrderResponse confirmPayment(Long orderId);
}
