package com.novastore.api.config;

import com.novastore.api.entity.Customer;
import com.novastore.api.entity.Product;
import com.novastore.api.entity.UserAccount;
import com.novastore.api.repository.CustomerRepository;
import com.novastore.api.repository.ProductRepository;
import com.novastore.api.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(ProductRepository products,
                           CustomerRepository customers,
                           UserAccountRepository users,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.username}") String adminUsername,
                           @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                    new Product("AP-003","AirPods Pro 3","Apple","Creadores",new BigDecimal("249.00"),18,4.9,
                        "https://www.apple.com/v/airpods-pro/t/images/overview/welcome/hero__b0eal3mn03ua_large.jpg",
                        "Audífonos inalámbricos con cancelación activa de ruido y audio espacial.",true),
                    new Product("LG-502","G502 X","Logitech G","Gaming",new BigDecimal("69.99"),25,4.8,
                        "https://resource.logitechg.com/w_544%2Ch_466%2Car_7%3A6%2Cc_pad%2Cq_auto%2Cf_auto%2Cdpr_1.0/d_transparent.gif/content/dam/gaming/en/products/g502x-corded/gallery/g502x-corded-gallery-1-white.png",
                        "Mouse gaming con interruptores LIGHTFORCE y sensor HERO 25K.",true),
                    new Product("RZ-BW4","BlackWidow V4 X","Razer","Gaming",new BigDecimal("129.99"),14,4.8,
                        "https://assets2.razerzone.com/images/pnx.assets/f588fc5b6f7b0378be5e070bef614c30/razer-blackwidow-v4-x-hero-desktop.webp",
                        "Teclado mecánico gaming con teclas macro dedicadas y Chroma RGB.",true),
                    new Product("JB-F06","Flip 6","JBL","Creadores",new BigDecimal("129.95"),21,4.8,
                        "https://global.jbl.com/dw/image/v2/BFND_PRD/on/demandware.static/-/Sites-masterCatalog_Harman/default/dw4e91d6eb/1_JBL_FLIP6_HERO_BLACK_29391_x2.png?sh=1000&sw=1000",
                        "Parlante Bluetooth portátil con protección IP67.",true),
                    new Product("SS-T7S","Portable SSD T7 Shield 2TB","Samsung","Estudio & Trabajo",new BigDecimal("199.99"),11,4.8,
                        "https://images.samsung.com/is/image/samsung/p6pim/us/mu-pe2t0s-am/gallery/us-portable-ssd-t7-shield-mu-pe2t0s-am-552408706?$product-details-jpg$=",
                        "SSD portátil resistente para proyectos y respaldos.",true),
                    new Product("AN-737","737 Power Bank (PowerCore 24K)","Anker","Movilidad",new BigDecimal("109.99"),17,4.8,
                        "https://cdn.shopify.com/s/files/1/0493/9834/9974/products/A1289011-Anker_737_Power_Bank_PowerCore_24K_1.png?v=1775967275&width=1600",
                        "Power bank de 24,000 mAh con pantalla inteligente.",true),
                    new Product("XM-B09","Smart Band 9","Xiaomi","Movilidad",new BigDecimal("59.99"),34,4.7,
                        "https://i02.appmifile.com/mi-com-product/fly-birds/xiaomi-smart-band-9-straps/PC/16758ebc92f2229383309b4e29781707.jpg",
                        "Pulsera inteligente AMOLED con autonomía extendida.",true),
                    new Product("TP-P110","Tapo P110","TP-Link","Hogar",new BigDecimal("21.90"),42,4.7,
                        "https://static.tp-link.com/upload/image-line/01-2_pack_normal_20230815063728h.jpg",
                        "Enchufe inteligente Wi-Fi con monitorización de consumo.",true)
                ));
            }

            if (customers.count() == 0) {
                customers.saveAll(List.of(
                    new Customer("Ana Torres","ana.torres@demo.com","0991112233"),
                    new Customer("Diego Molina","diego.molina@demo.com","0984567812")
                ));
            }

            if (adminEmail != null && !adminEmail.endsWith(".invalid")) {
                String configuredEmail = adminEmail.trim().toLowerCase();
                if (adminPassword == null || adminPassword.isBlank()) {
                    System.err.println("[NovaStore] ADMIN SIN CONFIGURAR: falta NOVASTORE_ADMIN_PASSWORD.");
                } else {
                    var stored = users.findByEmailIgnoreCase(configuredEmail);
                    if (stored.isEmpty()) {
                        UserAccount admin = new UserAccount();
                        admin.setEmail(configuredEmail);
                        admin.setUsername(adminUsername);
                        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                        admin.setRole("ADMIN");
                        admin.setVerified(true);
                        admin.setActive(true);
                        users.save(admin);
                        System.out.println("[NovaStore] Cuenta ADMIN inicializada.");
                    } else if ("ADMIN".equals(stored.get().getRole())) {
                        // El archivo privado de configuración es la fuente de la clave de la demo.
                        UserAccount admin = stored.get();
                        if (!passwordEncoder.matches(adminPassword, admin.getPasswordHash())) {
                            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                            users.save(admin);
                            System.out.println("[NovaStore] Contraseña ADMIN sincronizada con configuración local.");
                        }
                    } else {
                        // No ascender usuarios de manera automática. Mantener el servicio disponible.
                        System.err.println("[NovaStore] ADMIN NO CREADO: ADMIN_EMAIL ya pertenece a un usuario normal. "
                            + "Utiliza un correo ADMIN_EMAIL distinto de ese usuario en .env.local.");
                    }
                }
            }
        };
    }
}
