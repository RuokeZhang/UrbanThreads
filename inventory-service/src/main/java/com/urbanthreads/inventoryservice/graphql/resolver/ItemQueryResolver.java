package com.urbanthreads.inventoryservice.graphql.resolver;
import com.urbanthreads.inventoryservice.model.Item;
import com.urbanthreads.inventoryservice.repo.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import graphql.kickstart.tools.GraphQLQueryResolver;

@Component
public class ItemQueryResolver implements GraphQLQueryResolver {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemQueryResolver(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item itemById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }
}
