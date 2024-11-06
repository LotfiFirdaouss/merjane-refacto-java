package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.ResourceNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Test
    public void testProcessOrder_NormalProduct() {
        // GIVEN
        Product normalProduct = new Product(null, 5, 10, "NORMAL", "Normal Product", null, null, null);
        Order order = new Order();
        order.setItems(Set.of(normalProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        Order processedOrder = orderServiceImpl.processOrder(1L);

        // THEN
        verify(productService, times(1)).decrementProductStock(normalProduct);
        assertEquals(order, processedOrder);
    }

    @Test
    public void testProcessOrder_SeasonalProductInSeason() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product seasonalProduct = new Product(null, 5, 10, "SEASONAL", "Seasonal Product", null, now.minusDays(10), now.plusDays(10));
        Order order = new Order();
        order.setItems(Set.of(seasonalProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        Order processedOrder = orderServiceImpl.processOrder(1L);

        // THEN
        verify(productService, times(1)).decrementProductStock(seasonalProduct);
        assertEquals(order, processedOrder);
    }

    @Test
    public void testProcessOrder_SeasonalProductOutOfSeason() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product seasonalProduct = new Product(null, 5, 0, "SEASONAL", "Seasonal Product", null, now.minusDays(30), now.minusDays(10));
        Order order = new Order();
        order.setItems(Set.of(seasonalProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        Order processedOrder = orderServiceImpl.processOrder(1L);

        // THEN
        verify(productService, times(1)).handleSeasonalProduct(seasonalProduct);
        assertEquals(order, processedOrder);
    }

    @Test
    public void testProcessOrder_ExpirableProductNotExpired() {
        // GIVEN
        Product expirableProduct = new Product(null, 5, 10, "EXPIRABLE", "Expirable Product", LocalDate.now().plusDays(5), null, null);
        Order order = new Order();
        order.setItems(Set.of(expirableProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        Order processedOrder = orderServiceImpl.processOrder(1L);

        // THEN
        verify(productService, times(1)).decrementProductStock(expirableProduct);
        assertEquals(order, processedOrder);
    }

    @Test
    public void testProcessOrder_ExpirableProductExpired() {
        // GIVEN
        Product expirableProduct = new Product(null, 5, 10, "EXPIRABLE", "Expired Product", LocalDate.now().minusDays(1), null, null);
        Order order = new Order();
        order.setItems(Set.of(expirableProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        Order processedOrder = orderServiceImpl.processOrder(1L);

        // THEN
        verify(productService, times(1)).handleExpiredProduct(expirableProduct);
        assertEquals(order, processedOrder);
    }

    @Test
    public void testProcessOrder_OrderNotFound() {
        // GIVEN
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () -> orderServiceImpl.processOrder(1L));
        verify(orderRepository, times(1)).findById(1L);
    }
}
