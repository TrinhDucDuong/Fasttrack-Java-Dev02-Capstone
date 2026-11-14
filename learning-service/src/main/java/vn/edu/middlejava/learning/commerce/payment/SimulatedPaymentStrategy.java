package vn.edu.middlejava.learning.commerce.payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
@Component
public class SimulatedPaymentStrategy implements PaymentStrategy {
    @Override
    public String method() {
        return "SIMULATED";
    }

    @Override
    public String charge(BigDecimal amount) {
        return "PAID";
    }
}
