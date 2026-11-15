package vn.edu.middlejava.learning.commerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.dto.UpdateCartItemRequest;

public interface ICartController {
    CartResponse getCart(Jwt jwt);

    CartResponse addCartItem(Jwt jwt, AddCartItemRequest request);

    CartResponse updateCartItem(Jwt jwt, Long courseId, UpdateCartItemRequest request);

    ResponseEntity<Void> removeCartItem(Jwt jwt, Long courseId);
}
