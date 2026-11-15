package vn.edu.middlejava.learning.commerce.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStrategyFactoryTest {
    private final PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(
            new CodPaymentStrategy(), new VnPayPaymentStrategy(), new BankTransferPaymentStrategy()));

    @Test
    void createsTheMatchingStrategyIgnoringCase() {
        assertThat(factory.create("vnpay")).isInstanceOf(VnPayPaymentStrategy.class);
        assertThat(factory.create(" COD ")).isInstanceOf(CodPaymentStrategy.class);
        assertThat(factory.supportedMethods()).containsExactly("BANK_TRANSFER", "COD", "VNPAY");
    }

    @Test
    void eachStrategyAppliesItsOwnPaymentRules() {
        PaymentRequest request = new PaymentRequest(1L, new BigDecimal("29.99"));

        assertThat(factory.create("COD").pay(request).status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(factory.create("BANK_TRANSFER").pay(request).reference()).startsWith("BT-");
        PaymentResult vnpay = factory.create("VNPAY").pay(request);
        assertThat(vnpay.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(vnpay.reference()).matches("VNP\\d{18}");
    }

    @Test
    void rejectsUnknownOrMissingMethod() {
        assertThatThrownBy(() -> factory.create("BITCOIN")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.create(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void openClosedPrincipleNewMethodNeedsNoFactoryChange() {
        PaymentStrategy momo = new PaymentStrategy() {
            @Override
            public String method() {
                return "MOMO";
            }

            @Override
            public PaymentResult pay(PaymentRequest request) {
                return PaymentResult.paid("MOMO-1");
            }
        };
        PaymentStrategyFactory extended = new PaymentStrategyFactory(List.of(new CodPaymentStrategy(), momo));

        assertThat(extended.create("momo").pay(new PaymentRequest(1L, BigDecimal.TEN)).reference())
                .isEqualTo("MOMO-1");
    }
}
