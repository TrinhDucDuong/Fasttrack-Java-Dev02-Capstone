package vn.edu.middlejava.learning.commerce.controller;

import vn.edu.middlejava.learning.commerce.dto.OrderResponse;

import java.util.List;

public interface IAdminOrderController {
    List<OrderResponse> getAllOrders();

    OrderResponse confirmPayment(Long orderId);
}
