package com.urbanthreads.inventoryservice.service;

import com.urbanthreads.inventoryservice.DTO.ItemDTO;
import com.urbanthreads.inventoryservice.model.Item;
import com.urbanthreads.inventoryservice.repo.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InventoryServiceImpl implements InventoryService{

    @Autowired
    private S3Service awservice;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    ItemRepository repo;

    @PersistenceContext
    EntityManager entityManager;

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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Page<Item>> itemPage(Pageable pageable) {
        Page<Item> page = repo.findAll(pageable);
        return Optional.of(page); // S3 Bucket
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<List<ItemDTO>> itemsByName(String name) {
        String hql = "SELECT e FROM Item e WHERE e.itemName LIKE :namePattern";
        System.out.println("The name input is: "+name);
        TypedQuery<Item> query = entityManager.createQuery(hql, Item.class);
        query.setParameter("namePattern",name+"%");
        List<Item> items = query.getResultList();
        List<ItemDTO> itemDTOS = new ArrayList<>();
        for (Item item : items) {
            itemDTOS.add(new ItemDTO(item));
        }
        return Optional.of(itemDTOS); // S3 Bucket
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<List<ItemDTO>> itemsByIds(List<Integer> ids) {
        // S3 Bucket
        List<Item> items = repo.findAllById(ids);
        List<ItemDTO> itemDTOS = new ArrayList<>();
        for (Item item : items) {
            itemDTOS.add(new ItemDTO(item));
        }
        return Optional.of(itemDTOS);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Map<Integer, Integer>> stockQuantity(List<Integer> ids) {
        String hql = "SELECT e.id, e.stockQuantity FROM Item e WHERE e.id IN (:listOfIds)";
        TypedQuery<Object[]> query = entityManager.createQuery(hql, Object[].class);
        query.setParameter("listOfIds",ids);
        List<Object[]> results = query.getResultList();
        Map<Integer,Integer> map = new HashMap<>();
        for (Object[] result : results) {
            int id = (int) result[0];
            int stockQuantity = (int) result[1];
            map.put(id,stockQuantity);
        }

        return Optional.of(map);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void reduceStock(Map<Integer, Integer> purchaseItems) throws Exception {
        String hql = "UPDATE Item e SET e.stockQuantity = e.stockQuantity - :reduceAmount WHERE e.id = :id AND e.stockQuantity >= :reduceAmount";
        for (Map.Entry<Integer, Integer> entry : purchaseItems.entrySet()) {
            int updateCount = entityManager.createQuery(hql)
                    .setParameter("reduceAmount", entry.getValue())
                    .setParameter("id", entry.getKey())
                    .executeUpdate();
            // If any of the updates fails to update a row, throw an exception to trigger rollback
            if (updateCount == 0) {
                throw new Exception("Stock reduction failed for item ID " + entry.getKey());
            }
        }
        // No need to return anything, method execution success means all stock was reduced
    }


    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ,rollbackFor = Exception.class)
    public void removeItems(List<Integer> ids) {
            repo.deleteAllById(ids);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,rollbackFor = Exception.class)
    public Optional<ItemDTO> addItem(ItemDTO itemDTO) {
        Item item = new Item();
        item.setImages(itemDTO.getImages());
        item.setItemName(itemDTO.getItemName());
        item.setCategory(itemDTO.getCategory());
        item.setPrice(itemDTO.getPrice());
        item.setDescription(itemDTO.getDescription());
        item.setStockQuantity(itemDTO.getStockQuantity());
        Item savedItem = repo.save(item);

        ItemDTO newItem = new ItemDTO();
        newItem.setImages(awservice.generatePresignedUrls(itemDTO.getImages(), savedItem.getId()));
        newItem.setId(savedItem.getId());
        return Optional.of(newItem); // S3 Bucket
    }

    @Override
    public Optional<Integer> editItem(ItemDTO item) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'editItem'");
    }


}
