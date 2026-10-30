package in.anurag.CreatorStore.services;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {
    // Simple flat rate logic: Free shipping for orders over $100, otherwise $10
    public BigDecimal calculateShippingCost(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(10.00);
    }
}
