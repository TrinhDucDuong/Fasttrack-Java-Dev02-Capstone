package vn.edu.middlejava.learning.commerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.service.IAdminOrderService;

import java.util.List;

/** Protected by SecurityConfiguration: /api/admin/** requires ROLE_ADMIN. */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderControllerImpl implements IAdminOrderController {
    private final IAdminOrderService adminOrders;

    public AdminOrderControllerImpl(IAdminOrderService adminOrders) {
        this.adminOrders = adminOrders;
    }

    @Override
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return adminOrders.getAllOrders();
    }

    @Override
    @PutMapping("/{orderId}/confirm-payment")
    public OrderResponse confirmPayment(@PathVariable("orderId") Long orderId) {
        return adminOrders.confirmPayment(orderId);
    }
}
