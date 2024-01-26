package com.urbanthreads.inventoryservice.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.urbanthreads.inventoryservice.model.Item;
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




}
