package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.ResourceNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Override
    public Order processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));

        Set<Product> products = order.getItems();
        for (Product product : products) {
            switch (product.getType()) {
                case "NORMAL":
                    processNormalProduct(product);
                    break;
                case "SEASONAL":
                    processSeasonalProduct(product);
                    break;
                case "EXPIRABLE":
                    processExpirableProduct(product);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown product type: " + product.getType());
            }
        }

        return order;
    }

    private void processNormalProduct(Product product) {
        if (product.getAvailable() > 0) {
            productService.decrementProductStock(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                productService.notifyDelay(leadTime, product);
            }
        }
    }

    private void processSeasonalProduct(Product product) {
        LocalDate now = LocalDate.now();
        if (isInSeason(product, now) && product.getAvailable() > 0) {
            productService.decrementProductStock(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0 && !isSeasonOver(product, leadTime, now)) {
                productService.notifyDelay(leadTime, product);
            } else {
                // Notify clients of unavailability
                productService.handleSeasonalProduct(product);
            }
        }
    }

    private void processExpirableProduct(Product product) {
        LocalDate now = LocalDate.now();
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(now)) {
            productService.decrementProductStock(product);
        } else {
            // Handle expired product
            productService.handleExpiredProduct(product);
        }
    }

    private boolean isInSeason(Product product, LocalDate date) {
        return date.isAfter(product.getSeasonStartDate()) && date.isBefore(product.getSeasonEndDate());
    }

    private boolean isSeasonOver(Product product, int leadTime, LocalDate currentDate) {
        return currentDate.plusDays(leadTime).isAfter(product.getSeasonEndDate());
    }

}
