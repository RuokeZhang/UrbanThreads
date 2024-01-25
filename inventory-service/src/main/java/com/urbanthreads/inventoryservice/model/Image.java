package com.urbanthreads.inventoryservice.model;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
@AllArgsConstructor
@Entity
@Table(name= "Image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;



    @Column(name = "image_url", nullable = false)
    private String imageUrl;


    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    public Image() {

    }
}
