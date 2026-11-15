package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Method Pattern: {@link #create(String)} is the single place that decides which
 * {@link PaymentStrategy} serves a request; callers only know the interface.
 * Every strategy bean registers itself here, so a new payment method is one new class
 * and this factory never changes (Open/Closed Principle).
 */
@Component
public class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(strategy -> normalize(strategy.method()), Function.identity()));
    }

    public PaymentStrategy create(String method) {
        return Optional.ofNullable(method)
                .map(PaymentStrategyFactory::normalize)
                .map(strategies::get)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported payment method. Supported: " + supportedMethods()));
    }

    public List<String> supportedMethods() {
        return strategies.keySet().stream().sorted().toList();
    }

    private static String normalize(String method) {
        return method.trim().toUpperCase(Locale.ROOT);
    }
}
