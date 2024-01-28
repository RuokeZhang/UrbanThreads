package com.urbanthreads.inventoryservice.model;

import java.util.List;

import org.springframework.data.domain.Page;

public class ItemPage {
    private List<Item> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;

    // 构造函数，可以接收PageImpl对象
    public ItemPage(Page<Item> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.number = page.getNumber();
        this.size = page.getSize();
    }

    // Getter 和 Setter
}
