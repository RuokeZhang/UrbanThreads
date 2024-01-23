package com.urbanthreads.inventoryservice.graphql.resolver;

import com.urbanthreads.inventoryservice.model.Image;
import com.urbanthreads.inventoryservice.model.Item;
import com.urbanthreads.inventoryservice.repo.ItemRepository;
import com.urbanthreads.inventoryservice.service.InventoryService;
import com.urbanthreads.inventoryservice.service.S3Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.NonNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    public Item itemById(@NonNull Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Page<Item> allItems(int pageNumber, int sizeOfPage) {
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
