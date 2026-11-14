package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentStrategyFactory {
    private final List<PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public PaymentStrategy get(String method) {
        return strategies.stream()
                .filter(strategy -> strategy.method().equalsIgnoreCase(method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment method"));
    }
}
