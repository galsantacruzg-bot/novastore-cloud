package com.novastore.api.service;

import com.novastore.api.dto.CreateOrderRequest;
import com.novastore.api.dto.GuestOrderRequest;
import com.novastore.api.dto.OrderItemRequest;
import com.novastore.api.entity.Customer;
import com.novastore.api.entity.CustomerOrder;
import com.novastore.api.entity.OrderItem;
import com.novastore.api.entity.Product;
import com.novastore.api.repository.CustomerRepository;
import com.novastore.api.repository.OrderRepository;
import com.novastore.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findAll() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(CustomerOrder::getCreatedAt).reversed())
                .toList();
    }

    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + request.customerId()));
        return buildAndSave(customer, customer.getName(), customer.getEmail(), customer.getPhone(),
                "Pedido creado desde administración", "ADMIN", request.items());
    }

    @Transactional
    public CustomerOrder createGuest(GuestOrderRequest request) {
        Customer customer = customerRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseGet(() -> {
                    Customer created = new Customer(request.name().trim(), request.email().trim().toLowerCase(), request.phone().trim());
                    return customerRepository.save(created);
                });
        // Una compra invitada NO acredita la titularidad del correo electrónico.
        // Conservar nombre/teléfono existentes; el pedido tiene su propia instantánea.
        // Evita que un tercero reescriba el registro de un cliente mediante checkout.
        return buildAndSave(customer, request.name().trim(), request.email().trim().toLowerCase(), request.phone().trim(),
                request.shippingAddress().trim(), "GUEST", request.items());
    }

    private CustomerOrder buildAndSave(Customer customer, String name, String email, String phone,
                                       String address, String purchaseMode, List<OrderItemRequest> requestedItems) {
        CustomerOrder order = new CustomerOrder();
        order.setCustomerId(customer.getId());
        order.setCustomerName(name);
        order.setCustomerEmail(email);
        order.setCustomerPhone(phone);
        order.setShippingAddress(address);
        order.setPurchaseMode(purchaseMode);

        BigDecimal total = BigDecimal.ZERO;
        // Rechazar duplicados evita ambigüedades en cantidades, auditoría y stock.
        long distinct = requestedItems.stream().map(OrderItemRequest::productId).distinct().count();
        if (distinct != requestedItems.size()) {
            throw new IllegalArgumentException("El pedido contiene productos repetidos. Agrupa las cantidades.");
        }
        // Orden fijo de bloqueo: reduce interbloqueos entre pedidos con múltiples productos.
        for (OrderItemRequest itemRequest : requestedItems.stream().sorted(Comparator.comparing(OrderItemRequest::productId)).toList()) {
            Product product = productRepository.findByIdForUpdate(itemRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemRequest.productId()));
            if (!product.isActive()) throw new IllegalArgumentException("El producto está inactivo: " + product.getName());
            if (itemRequest.quantity() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            if (product.getStock() < itemRequest.quantity()) throw new IllegalStateException("Stock insuficiente para " + product.getName());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);
            order.addItem(item);

            product.setStock(product.getStock() - itemRequest.quantity());
            productRepository.save(product);
            total = total.add(subtotal);
        }
        order.setTotal(total);
        return orderRepository.save(order);
    }
}
