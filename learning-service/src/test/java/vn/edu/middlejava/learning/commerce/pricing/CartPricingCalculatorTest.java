package vn.edu.middlejava.learning.commerce.pricing;

import org.junit.jupiter.api.Test;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static vn.edu.middlejava.learning.commerce.CommerceTestData.course;

class CartPricingCalculatorTest {
    private final ICourseRepository courses = mock(ICourseRepository.class);
    private final CartPricingCalculator calculator = new CartPricingCalculator(courses);

    @Test
    void sumsLineTotalsWithCurrentCatalogPrices() {
        ShoppingCart cart = new ShoppingCart(1L);
        cart.add(1L, 2);
        cart.add(2L, 1);
        var middleJava = course(1L, "Middle Java", "29.99");
        var springBoot = course(2L, "Spring Boot", "39.99");
        when(courses.findAllById(anyList())).thenReturn(List.of(middleJava, springBoot));

        PricedCart priced = calculator.price(cart);

        assertThat(priced.lines()).hasSize(2);
        assertThat(priced.total()).isEqualByComparingTo("99.97");
        assertThat(priced.courseIds()).containsExactly(1L, 2L);
    }

    @Test
    void failsWhenACourseDisappearedFromCatalog() {
        ShoppingCart cart = new ShoppingCart(1L);
        cart.add(1L, 1);
        when(courses.findAllById(anyList())).thenReturn(List.of());

        assertThatThrownBy(() -> calculator.price(cart)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void emptyCartCostsNothing() {
        PricedCart priced = calculator.price(new ShoppingCart(1L));

        assertThat(priced.isEmpty()).isTrue();
        assertThat(priced.total()).isEqualByComparingTo("0");
    }
}
