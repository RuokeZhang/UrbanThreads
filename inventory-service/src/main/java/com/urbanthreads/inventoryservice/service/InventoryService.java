package com.urbanthreads.inventoryservice.service;
import com.urbanthreads.inventoryservice.DTO.ItemDTO;
import com.urbanthreads.inventoryservice.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryService {

    /*

    - Return all items available in the inventory (pages)
    - Return all items in the inventory for a given match case RegEX item name.
    - Return all items in the inventory for a given list<id>
    - Return all <stock quantities, id> for a given list<id>
    - Reduce and Return all <id> for a given list <id, reduceAmount> [repeatable read]
    - Remove all items for a given list<id> [repeatable read]
    - Add item
    - Edit item [repeatable read]
     */


    Optional<Page<Item>> itemPage(Pageable pageable);
    Optional<List<ItemDTO>> itemsByName(String name);
    Optional<List<ItemDTO>> itemsByIds(List<Integer> ids);
    Optional<Map<Integer,Integer>> stockQuantity(List<Integer> ids);
    void reduceStock(Map<Integer,Integer> purchaseItems) throws Exception;
    void removeItems(List<Integer> ids);
    Optional<ItemDTO> addItem(ItemDTO item);
    Optional<Integer> editItem(ItemDTO item);


}
