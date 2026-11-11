package com.novastore.api.service;

import com.novastore.api.dto.CreateOrderRequest;
import com.novastore.api.dto.GuestOrderRequest;
import com.novastore.api.dto.OrderItemRequest;
import com.novastore.api.entity.Customer;
import com.novastore.api.entity.CustomerOrder;
import com.novastore.api.entity.Product;
import com.novastore.api.repository.CustomerRepository;
import com.novastore.api.repository.OrderRepository;
import com.novastore.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private final OrderRepository orders = mock(OrderRepository.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final OrderService service = new OrderService(orders, customers, products);

    @Test void checkoutInvitadoNoModificaDatosDeClienteExistente() {
        Customer existing = new Customer("Cliente registrado", "cliente@demo.com", "0990001111");
        when(customers.findByEmailIgnoreCase("cliente@demo.com")).thenReturn(Optional.of(existing));
        Product product = new Product("ABC", "Teclado", "Marca", "Gaming", new BigDecimal("12.00"), 8,
                4.0, "/assets/products/keyboard.svg", "Ejemplo", true);
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(orders.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new GuestOrderRequest("Visitante", "cliente@demo.com", "0989991111",
                "Calle de prueba 123", List.of(new OrderItemRequest(1L, 2)));
        var order = service.createGuest(request);

        assertEquals("Cliente registrado", existing.getName());
        assertEquals("0990001111", existing.getPhone());
        verify(customers, never()).save(existing);
        assertEquals("Visitante", order.getCustomerName());
        assertEquals("0989991111", order.getCustomerPhone());
        assertEquals(new BigDecimal("24.00"), order.getTotal());
        assertEquals(6, product.getStock());
    }

    @Test void productosRepetidosSeRechazanAntesDeTocarInventario() {
        Customer customer = new Customer("Cliente", "c@demo.com", "0990001111");
        when(customers.findById(5L)).thenReturn(Optional.of(customer));
        var request = new CreateOrderRequest(5L,
                List.of(new OrderItemRequest(1L, 1), new OrderItemRequest(1L, 2)));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(request));
        assertTrue(ex.getMessage().contains("repetidos"));
        verify(products, never()).findByIdForUpdate(anyLong());
        verify(orders, never()).save(any(CustomerOrder.class));
    }

    @Test void stockInsuficienteRechazaPedido() {
        Customer customer = new Customer("Cliente", "c@demo.com", "0990001111");
        Product product = new Product("ABC", "Mouse", "Marca", "Gaming", new BigDecimal("20.00"), 1,
                3.0, "/assets/products/mouse.svg", "Ejemplo", true);
        when(customers.findById(3L)).thenReturn(Optional.of(customer));
        when(products.findByIdForUpdate(9L)).thenReturn(Optional.of(product));
        assertThrows(IllegalStateException.class,
                () -> service.create(new CreateOrderRequest(3L, List.of(new OrderItemRequest(9L, 2)))));
        assertEquals(1, product.getStock());
        verify(orders, never()).save(any(CustomerOrder.class));
    }
}
