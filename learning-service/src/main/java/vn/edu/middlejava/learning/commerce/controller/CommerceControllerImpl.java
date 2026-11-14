package vn.edu.middlejava.learning.commerce.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.dto.UpdateCartItemRequest;
import vn.edu.middlejava.learning.commerce.service.ICommerceService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommerceControllerImpl implements ICommerceController {
    private final ICommerceService commerceService;

    public CommerceControllerImpl(ICommerceService commerceService) {
        this.commerceService = commerceService;
    }

    @Override
    @GetMapping("/cart")
    public CartResponse getCart(@AuthenticationPrincipal Jwt jwt) {
        return commerceService.getCart(userId(jwt));
    }

    @Override
    @PostMapping("/cart/items")
    public CartResponse addCartItem(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody AddCartItemRequest request) {
        return commerceService.addToCart(userId(jwt), request);
    }

    @Override
    @PutMapping("/cart/items/{courseId}")
    public CartResponse updateCartItem(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable("courseId") Long courseId,
                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return commerceService.updateCartItem(userId(jwt), courseId, request.quantity());
    }

    @Override
    @DeleteMapping("/cart/items/{courseId}")
    public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable("courseId") Long courseId) {
        commerceService.removeCartItem(userId(jwt), courseId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(201).body(commerceService.checkout(userId(jwt), request));
    }

    @Override
    @GetMapping("/orders")
    public List<OrderResponse> getOrders(@AuthenticationPrincipal Jwt jwt) {
        return commerceService.getOrders(userId(jwt));
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable("orderId") Long orderId) {
        return commerceService.getOrder(userId(jwt), orderId);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
