package com.novastore.api.service;

import com.novastore.api.entity.Customer;
import com.novastore.api.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Transactional
    public Customer create(Customer customer) {
        if (repository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese correo.");
        }
        return repository.save(customer);
    }

    @Transactional
    public Customer update(Long id, Customer input) {
        Customer current = findById(id);
        current.setName(input.getName());
        current.setEmail(input.getEmail());
        current.setPhone(input.getPhone());
        return repository.save(current);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
