package vn.edu.middlejava.learning.commerce.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.payment.PaymentProcessorRegistry;
import vn.edu.middlejava.learning.commerce.service.IOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderControllerImpl implements IOrderController {
    private final IOrderService orderService;
    private final PaymentProcessorRegistry payments;

    public OrderControllerImpl(IOrderService orderService, PaymentProcessorRegistry payments) {
        this.orderService = orderService;
        this.payments = payments;
    }

    @Override
    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(201).body(orderService.checkout(userId(jwt), request));
    }

    @Override
    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrders(userId(jwt));
    }

    @Override
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable("orderId") Long orderId) {
        return orderService.getOrder(userId(jwt), orderId);
    }

    /** Lists every registered PaymentProcessor - grows automatically when a new one is added. */
    @Override
    @GetMapping("/payment-methods")
    public List<String> getPaymentMethods() {
        return payments.supportedMethods();
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
