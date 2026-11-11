package com.novastore.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank @Column(nullable = false)
    private String name;

    @NotBlank @Column(nullable = false)
    private String brand;

    @NotBlank @Column(nullable = false)
    private String category;

    @NotNull @DecimalMin("0.01") @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Min(0) @Column(nullable = false)
    private int stock;

    @DecimalMin("0.0") @DecimalMax("5.0")
    private double rating;

    @Column(length = 1200)
    private String image;

    @NotBlank @Column(length = 1500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public Product() {}

    public Product(String code, String name, String brand, String category, BigDecimal price, int stock,
                   double rating, String image, String description, boolean active) {
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
        this.image = image;
        this.description = description;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
