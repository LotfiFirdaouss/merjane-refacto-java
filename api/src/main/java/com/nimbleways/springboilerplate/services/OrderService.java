package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Order;

public interface OrderService {
    public Order processOrder(Long orderId);
}
