package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void testNotifyDelay() {
        // GIVEN
        Product product = new Product(null, 5, 10, "NORMAL", "Delayed Product", null, null, null);

        // WHEN
        productService.notifyDelay(5, product);

        // THEN
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(5, product.getName());
    }

    @Test
    public void testHandleSeasonalProduct_BeforeSeason() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product product = new Product(null, 5, 10, "SEASONAL", "PreSeason Product", null, now.plusDays(10), now.plusDays(30));

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        verify(notificationService, times(1)).sendOutOfStockNotification(product.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void testHandleSeasonalProduct_InSeason() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product product = new Product(null, 5, 10, "SEASONAL", "InSeason Product", null, now.minusDays(5), now.plusDays(10));

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        verify(notificationService, never()).sendOutOfStockNotification(anyString());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    @Test
    public void testHandleExpiredProduct_NotExpired() {
        // GIVEN
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Product product = new Product(null, 0, 10, "EXPIRABLE", "NonExpired Product", futureDate, null, null);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        verify(productRepository, times(1)).save(product);
        verify(notificationService, never()).sendExpirationNotification(anyString(), any(LocalDate.class));
    }

    @Test
    public void testHandleExpiredProduct_Expired() {
        // GIVEN
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Product product = new Product(null, 0, 10, "EXPIRABLE", "Expired Product", pastDate, null, null);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendExpirationNotification(product.getName(), product.getExpiryDate());
    }

    @Test
    public void testDecrementProductStock() {
        // GIVEN
        Product product = new Product(null, 5, 10, "NORMAL", "Stocked Product", null, null, null);

        // WHEN
        productService.decrementProductStock(product);

        // THEN
        verify(productRepository, times(1)).save(product);
    }
}