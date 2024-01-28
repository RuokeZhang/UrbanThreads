package com.urbanthreads.inventoryservice.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.urbanthreads.inventoryservice.DTO.ItemDTO;
import com.urbanthreads.inventoryservice.model.Item;
import com.urbanthreads.inventoryservice.model.ItemInput;
import com.urbanthreads.inventoryservice.model.ItemPage;
import com.urbanthreads.inventoryservice.repo.ItemRepository;
import com.urbanthreads.inventoryservice.service.InventoryService;
import com.urbanthreads.inventoryservice.service.S3Service;

@Controller
public class InventoryController {
    private final InventoryService inventoryService;
    private final S3Service s3Service;
    private final ItemRepository itemRepository;

    public InventoryController(InventoryService inventoryService, S3Service s3Service, ItemRepository itemRepository) {
        this.inventoryService = inventoryService;
        this.s3Service = s3Service;
        this.itemRepository = itemRepository;
    }

    @QueryMapping
    public Item itemById(@Argument int id) {
        return itemRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Item> allItems() {
        List<Item> items = itemRepository.findAll();
        return items;
    }

    @QueryMapping
    public ItemPage itemsByPage(@Argument int pageNumber, @Argument int sizeOfPage) {
        System.out.println("QueryMapping: ItemsByPage");
        Pageable pageable = PageRequest.of(pageNumber, sizeOfPage); // set table sort ordering here
        Optional<Page<Item>> optionalPage = inventoryService.itemPage(pageable); // returns Page<Item>
        Page<Item> page = optionalPage.get();
        List<Item> items = page.getContent();
        s3Service.generatePresignedUrlsForItems(items);
        Page<Item> modifiedItemsPage = new PageImpl<>(items,
                page.getPageable(),
                page.getTotalElements());

        return new ItemPage(modifiedItemsPage);
    }

    @MutationMapping
    public Item addItem(@Argument("newItem") ItemInput newItemInput) {
        // Create a new Item entity from the newItemInput
        Item newItem = new Item();
        newItem.setItemName(newItemInput.itemName());
        newItem.setDescription(newItemInput.description());
        newItem.setPrice(newItemInput.price());
        newItem.setStockQuantity(newItemInput.stockQuantity());
        newItem.setCategory(newItemInput.category());

        // Use the repository to save the new item entity
        Item savedItem = itemRepository.save(newItem);

        // TODO: Upload the images to S3

        // Return the saved item entity
        return savedItem;
    }

}
