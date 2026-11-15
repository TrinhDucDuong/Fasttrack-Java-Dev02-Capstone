package vn.edu.middlejava.learning.commerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;

import java.util.List;

public interface IOrderController {
    ResponseEntity<OrderResponse> checkout(Jwt jwt, CheckoutRequest request);

    List<OrderResponse> getOrders(Jwt jwt);

    OrderResponse getOrder(Jwt jwt, Long orderId);

    List<String> getPaymentMethods();
}
