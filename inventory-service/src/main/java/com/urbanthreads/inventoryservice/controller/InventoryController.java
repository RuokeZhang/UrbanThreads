package com.urbanthreads.inventoryservice.controller;


import com.urbanthreads.inventoryservice.DTO.ItemDTO;
import com.urbanthreads.inventoryservice.model.Item;
import com.urbanthreads.inventoryservice.repo.ItemRepository;
import com.urbanthreads.inventoryservice.service.InventoryService;
import com.urbanthreads.inventoryservice.service.S3Service;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/urban-threads")
public class InventoryController {


    @Autowired
    InventoryService inventoryService;
    @Autowired
    S3Service awservice;
    @PostConstruct
    public void loadItems() {
        //List<Item> items = new ArrayList<>();
    }


    private final ItemRepository itemRepository;



    public InventoryController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping("/items")
    Page<Item> all(@RequestParam int pageNumber, int sizeOfPage) {
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

//    @GetMapping("/images")
//    Map<Integer, Set<String>> images() {
//        List<Integer> list = new ArrayList<>();
//        list.add(25l);
//        awservice.generatePresignedUrlsForItems(items);
//    }

    @GetMapping("/items/{id}")
    Object one(@PathVariable int id) {
        return itemRepository.findById(id).orElseThrow();
    }

    @GetMapping("/items/availableitems")
    public ResponseEntity<?> stockItemsById(@RequestParam List<Integer> requestedItems) {
        try {
            Map<Integer, Integer> availablity = inventoryService.stockQuantity(requestedItems).get();
            return ResponseEntity.ok().body(availablity);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body("Failed to get stock availability for items sent." +
                    " Check request");
        }
    }
    @GetMapping("/items/byname")
    List<ItemDTO> itemsByName(@RequestParam String name) {
        System.out.println("The name input is: "+name);
        Optional<List<ItemDTO>> optional = inventoryService.itemsByName(name);
        return optional.get();
    }

    @PostMapping("/items/reducestock")
    public ResponseEntity<?> reduceStock(@RequestBody Map<Integer, Integer> itemsRequested) {
        try {
            inventoryService.reduceStock(itemsRequested);
            return ResponseEntity.ok().build(); // Return 200 OK with no content
        } catch (Exception e) {
            // Log the exception message or stack trace if needed
            // Depending on the exception type, you might want to return different status codes
            // For this example, I am returning 500 Internal Server Error
            return ResponseEntity.internalServerError().body("Stock reduction failed: " + e.getMessage() +
                    ". Check stock quantity again for this order.");
        }
    }

    @PostMapping("/items/add")
    public ResponseEntity<?> addItem(@RequestBody ItemDTO newItem) {
        try {
            Optional<ItemDTO> item = inventoryService.addItem(newItem);
            if (item.isPresent() && newItem.getImages().size() > 0) {
                awservice.generatePresignedUrls(newItem.getImages(), item.get().getId());
            }
            return ResponseEntity.ok().body(item.get());
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Item insertion failed.");
        }
    }



    @DeleteMapping("/items/delete")
    public ResponseEntity<?> deleteItem(@RequestBody List<Integer> ids) {
        try {
            inventoryService.removeItems(ids);
            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Delete failed.");
        }
    }
    @PutMapping("/items/{id}/edit")
    public ResponseEntity<?> updateItem(@PathVariable Integer id, @RequestBody ItemDTO itemDTO) {
        try {
            Optional<Integer> itemId = inventoryService.editItem(itemDTO);
            return ResponseEntity.ok().body(itemId.get());
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Item update failed.");
        }
    }



}
