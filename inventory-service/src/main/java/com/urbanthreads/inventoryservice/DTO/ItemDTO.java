package com.urbanthreads.inventoryservice.DTO;

import com.urbanthreads.inventoryservice.model.Item;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class ItemDTO {


    public ItemDTO(Item item) {
        this.id = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getPrice();
        this.category = item.getCategory();
        this.description = item.getDescription();
        this.stockQuantity = item.getStockQuantity();
        this.images = item.getImages();
    }

    private int id;

    private String itemName;

    private String description;

    private float price;

    private int stockQuantity;

    private String category;

    Set<String> images = new HashSet<>();

}
