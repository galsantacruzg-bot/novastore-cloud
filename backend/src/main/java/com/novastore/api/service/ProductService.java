package com.novastore.api.service;

import com.novastore.api.entity.Product;
import com.novastore.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll(String search, String brand, String category, BigDecimal maxPrice) {
        String q = search == null ? "" : search.trim().toLowerCase();
        return repository.findAll().stream()
                .filter(Product::isActive)
                .filter(p -> q.isBlank()
                        || p.getName().toLowerCase().contains(q)
                        || p.getBrand().toLowerCase().contains(q)
                        || p.getCode().toLowerCase().contains(q))
                .filter(p -> brand == null || brand.isBlank() || p.getBrand().equalsIgnoreCase(brand))
                .filter(p -> category == null || category.isBlank() || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
    }

    @Transactional
    public Product create(Product product) {
        if (repository.existsByCode(product.getCode())) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + product.getCode());
        }
        return repository.save(product);
    }

    @Transactional
    public Product update(Long id, Product input) {
        Product current = findById(id);
        if (!current.getCode().equals(input.getCode()) && repository.existsByCode(input.getCode())) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + input.getCode());
        }
        current.setCode(input.getCode());
        current.setName(input.getName());
        current.setBrand(input.getBrand());
        current.setCategory(input.getCategory());
        current.setPrice(input.getPrice());
        current.setStock(input.getStock());
        current.setRating(input.getRating());
        current.setImage(input.getImage());
        current.setDescription(input.getDescription());
        current.setActive(input.isActive());
        return repository.save(current);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return repository.findAll().stream().map(Product::getCategory).distinct().sorted().toList();
    }
}
