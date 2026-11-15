package vn.edu.middlejava.learning.commerce.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment - Factory Method (PaymentProcessor) + Strategy (PaymentStrategy)")
class PaymentFactoryMethodTest {
    private final PaymentRequest request = new PaymentRequest(1L, new BigDecimal("29.99"));
    private final PaymentProcessorRegistry registry = new PaymentProcessorRegistry(List.of(
            new CodPaymentProcessor(), new VnPayPaymentProcessor(), new BankTransferPaymentProcessor()));

    @Test
    void eachConcreteCreatorCreatesItsOwnStrategy() {
        assertThat(new VnPayPaymentProcessor().createStrategy()).isInstanceOf(VnPayPaymentStrategy.class);
        assertThat(new CodPaymentProcessor().createStrategy()).isInstanceOf(CodPaymentStrategy.class);
        assertThat(new BankTransferPaymentProcessor().createStrategy()).isInstanceOf(BankTransferPaymentStrategy.class);
    }

    @Test
    void processUsesTheStrategyCreatedBySubclass() {
        PaymentResult vnpay = registry.processorFor("vnpay").process(request);
        assertThat(vnpay.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(vnpay.reference()).matches("VNP\\d{18}");

        assertThat(registry.processorFor(" COD ").process(request).status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(registry.processorFor("BANK_TRANSFER").process(request).reference()).startsWith("BT-");
    }

    @Test
    void registryListsMethodsAndRejectsUnknownOnes() {
        assertThat(registry.supportedMethods()).containsExactly("BANK_TRANSFER", "COD", "VNPAY");
        assertThatThrownBy(() -> registry.processorFor("BITCOIN")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.processorFor(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void processRejectsNegativeAmount() {
        assertThatThrownBy(() -> new CodPaymentProcessor().process(new PaymentRequest(1L, new BigDecimal("-1"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void openClosedPrincipleNewMethodIsOneNewCreatorAndNoChangeElsewhere() {
        PaymentProcessor momo = new PaymentProcessor() {
            @Override
            public String method() {
                return "MOMO";
            }

            @Override
            protected PaymentStrategy createStrategy() {
                return paymentRequest -> PaymentResult.paid("MOMO-1");
            }
        };
        PaymentProcessorRegistry extended = new PaymentProcessorRegistry(List.of(new CodPaymentProcessor(), momo));

        assertThat(extended.supportedMethods()).containsExactly("COD", "MOMO");
        assertThat(extended.processorFor("momo").process(request).reference()).isEqualTo("MOMO-1");
    }
}
