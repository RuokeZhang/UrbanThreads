package com.urbanthreads.inventoryservice.model;

public record ItemInput(String itemName, String description, float price, int stockQuantity, String category) {
}
