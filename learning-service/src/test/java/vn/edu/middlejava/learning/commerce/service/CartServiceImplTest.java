package vn.edu.middlejava.learning.commerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.mapper.CommerceMapper;
import vn.edu.middlejava.learning.commerce.pricing.CartPricingCalculator;
import vn.edu.middlejava.learning.commerce.repository.ShoppingCartRepository;
import vn.edu.middlejava.learning.commerce.service.impl.CartServiceImpl;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.edu.middlejava.learning.commerce.CommerceTestData.course;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CartServiceImpl - add / update / remove cart items")
class CartServiceImplTest {
    private static final Long USER_ID = 7L;

    @Mock
    private ShoppingCartRepository carts;
    @Mock
    private ICourseRepository courses;

    private CartServiceImpl service;
    private Course middleJava;

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(carts, courses, new CartPricingCalculator(courses), new CommerceMapper());
        middleJava = course(31L, "Middle Java", "29.99");
        when(courses.findById(31L)).thenReturn(Optional.of(middleJava));
        when(courses.findAllById(anyList())).thenReturn(List.of(middleJava));
    }

    @Test
    void addCreatesCartWhenCustomerHasNone() {
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(carts.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse cart = service.addToCart(USER_ID, new AddCartItemRequest(31L, 1));

        verify(carts).save(any(ShoppingCart.class));
        assertThat(cart.items()).singleElement().satisfies(item -> {
            assertThat(item.courseId()).isEqualTo(31L);
            assertThat(item.quantity()).isEqualTo(1);
        });
        assertThat(cart.total()).isEqualByComparingTo("29.99");
    }

    @Test
    void addSameCourseTwiceIncreasesQuantity() {
        ShoppingCart existing = new ShoppingCart(USER_ID);
        existing.add(31L, 1);
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        CartResponse cart = service.addToCart(USER_ID, new AddCartItemRequest(31L, 2));

        assertThat(cart.items()).singleElement().extracting(CartResponse.Item::quantity).isEqualTo(3);
        assertThat(cart.total()).isEqualByComparingTo("89.97");
        verify(carts, never()).save(any());
    }

    @Test
    void addRejectsUnknownCourse() {
        when(courses.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addToCart(USER_ID, new AddCartItemRequest(404L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
        verify(carts, never()).save(any());
    }

    @Test
    void addRejectsCourseThatIsNoLongerForSale() {
        Course retired = course(50L, "Old course", "9.99", false);
        when(courses.findById(50L)).thenReturn(Optional.of(retired));

        assertThatThrownBy(() -> service.addToCart(USER_ID, new AddCartItemRequest(50L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course is no longer for sale");
    }

    @Test
    void updateChangesQuantityAndRecalculatesTotal() {
        ShoppingCart existing = new ShoppingCart(USER_ID);
        existing.add(31L, 1);
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        CartResponse cart = service.updateCartItem(USER_ID, 31L, 2);

        assertThat(cart.items()).singleElement().satisfies(item -> {
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.lineTotal()).isEqualByComparingTo("59.98");
        });
        assertThat(cart.total()).isEqualByComparingTo("59.98");
    }

    @Test
    void updateRejectsQuantityBelowOne() {
        ShoppingCart existing = new ShoppingCart(USER_ID);
        existing.add(31L, 1);
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateCartItem(USER_ID, 31L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be at least one");
        assertThat(existing.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void updateFailsWhenCourseIsNotInCart() {
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(new ShoppingCart(USER_ID)));

        assertThatThrownBy(() -> service.updateCartItem(USER_ID, 31L, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course is not in cart");
    }

    @Test
    void updateFailsWhenCustomerHasNoCart() {
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCartItem(USER_ID, 31L, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cart not found");
    }

    @Test
    void removeDeletesItemFromCart() {
        ShoppingCart existing = new ShoppingCart(USER_ID);
        existing.add(31L, 2);
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        service.removeCartItem(USER_ID, 31L);

        assertThat(existing.getItems()).isEmpty();
    }

    @Test
    void removeFailsWhenCourseIsNotInCart() {
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(new ShoppingCart(USER_ID)));

        assertThatThrownBy(() -> service.removeCartItem(USER_ID, 31L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course is not in cart");
    }

    @Test
    void getCartReturnsEmptyCartForNewCustomer() {
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.empty());

        CartResponse cart = service.getCart(USER_ID);

        assertThat(cart.items()).isEmpty();
        assertThat(cart.total()).isEqualByComparingTo("0");
    }
}
