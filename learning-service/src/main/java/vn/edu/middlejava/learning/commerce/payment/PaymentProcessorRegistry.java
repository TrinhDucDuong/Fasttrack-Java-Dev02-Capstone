package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Picks the Creator ({@link PaymentProcessor}) for the method the customer chose.
 * Spring injects every processor bean, so a new payment method registers itself
 * and this class never changes (Open/Closed Principle).
 */
@Component
public class PaymentProcessorRegistry {
    private final Map<String, PaymentProcessor> processors;

    public PaymentProcessorRegistry(List<PaymentProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toUnmodifiableMap(processor -> normalize(processor.method()), Function.identity()));
    }

    public PaymentProcessor processorFor(String method) {
        return Optional.ofNullable(method)
                .map(PaymentProcessorRegistry::normalize)
                .map(processors::get)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported payment method. Supported: " + supportedMethods()));
    }

    public List<String> supportedMethods() {
        return processors.keySet().stream().sorted().toList();
    }

    private static String normalize(String method) {
        return method.trim().toUpperCase(Locale.ROOT);
    }
}
