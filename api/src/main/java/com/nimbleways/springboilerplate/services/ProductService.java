package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductService {
    public void notifyDelay(int leadTime, Product p);
    public void handleSeasonalProduct(Product p);
    public void handleExpiredProduct(Product p);
    public void decrementProductStock(Product product);
}
