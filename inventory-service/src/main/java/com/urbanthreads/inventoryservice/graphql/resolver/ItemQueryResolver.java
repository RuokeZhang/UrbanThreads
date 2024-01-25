package com.urbanthreads.inventoryservice.graphql.resolver;

import com.urbanthreads.inventoryservice.model.Item;
import com.urbanthreads.inventoryservice.repo.ItemRepository;
import com.urbanthreads.inventoryservice.service.InventoryService;
import com.urbanthreads.inventoryservice.service.S3Service;

import graphql.kickstart.tools.GraphQLQueryResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ItemQueryResolver implements GraphQLQueryResolver {

    private final ItemRepository itemRepository;
    @Autowired
    InventoryService inventoryService;

    @Autowired
    S3Service awservice;

    @Autowired
    public ItemQueryResolver(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;

    }

    public Item itemById(int id) {
        return itemRepository.findById(id).orElse(null);
    }

    public List<Item> allItems() {
        List<Item> items = itemRepository.findAll();
        System.out.println("Number of items found: {}"+items.size());
        return items;
    }


    public Page<Item> items(int pageNumber, int sizeOfPage) {
        Pageable pageable = PageRequest.of(pageNumber, sizeOfPage); // you should set table sort ordering here
        Optional<Page<Item>> optionalPage = inventoryService.itemPage(pageable); // returns Page<Item>
        Page<Item> page = optionalPage.get();
        List<Item> items = page.getContent();
        awservice.generatePresignedUrlsForItems(items);
        Page<Item> modifiedItemsPage = new PageImpl<>(items,
                page.getPageable(),
                page.getTotalElements());

        return modifiedItemsPage;
    }
}
