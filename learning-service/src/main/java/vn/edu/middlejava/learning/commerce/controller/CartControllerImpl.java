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
import vn.edu.middlejava.learning.commerce.dto.UpdateCartItemRequest;
import vn.edu.middlejava.learning.commerce.service.ICartService;

@RestController
@RequestMapping("/api/cart")
public class CartControllerImpl implements ICartController {
    private final ICartService cartService;

    public CartControllerImpl(ICartService cartService) {
        this.cartService = cartService;
    }

    @Override
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(userId(jwt));
    }

    @Override
    @PostMapping("/items")
    public CartResponse addCartItem(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addToCart(userId(jwt), request);
    }

    @Override
    @PutMapping("/items/{courseId}")
    public CartResponse updateCartItem(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable("courseId") Long courseId,
                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateCartItem(userId(jwt), courseId, request.quantity());
    }

    @Override
    @DeleteMapping("/items/{courseId}")
    public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable("courseId") Long courseId) {
        cartService.removeCartItem(userId(jwt), courseId);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
