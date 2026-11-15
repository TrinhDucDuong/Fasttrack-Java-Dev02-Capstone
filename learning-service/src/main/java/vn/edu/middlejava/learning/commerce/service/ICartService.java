package vn.edu.middlejava.learning.commerce.service;

import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;

public interface ICartService {
    CartResponse getCart(Long userId);

    CartResponse addToCart(Long userId, AddCartItemRequest request);

    CartResponse updateCartItem(Long userId, Long courseId, int quantity);

    void removeCartItem(Long userId, Long courseId);
}
