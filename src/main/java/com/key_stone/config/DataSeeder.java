package com.key_stone.config;

import com.key_stone.domain.Customer;
import com.key_stone.domain.Part;
import com.key_stone.domain.Role;
import com.key_stone.domain.Site;
import com.key_stone.domain.User;
import com.key_stone.repository.CustomerRepository;
import com.key_stone.repository.PartRepository;
import com.key_stone.repository.SiteRepository;
import com.key_stone.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(
            UserRepository users,
            CustomerRepository customers,
            SiteRepository sites,
            PartRepository parts,
            PasswordEncoder encoder) {

        return args -> {

            // -----------------------------
            // CUSTOMER
            // -----------------------------

            Customer customer = customers.findAll()
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {

                        Customer c = new Customer();

                        c.setName("Meridian Demo Customer");
                        c.setContactName("Demo Contact");
                        c.setEmail("customer@demo.com");
                        c.setPhone("9000000000");

                        return customers.save(c);
                    });

            // -----------------------------
            // SITE
            // -----------------------------

            Site site = sites.findAll()
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {

                        Site s = new Site();

                        s.setName("Hyderabad Commercial Site");
                        s.setAddress("1 Demo Business Park");
                        s.setCity("Hyderabad");
                        s.setPostalCode("500001");
                        s.setCustomer(customer);

                        return sites.save(s);
                    });

            // -----------------------------
            // USERS
            // -----------------------------

            createUser(
                    users,
                    encoder,
                    "dispatcher@keystone.local",
                    "Dispatcher User",
                    Role.DISPATCHER,
                    null
            );

            createUser(
                    users,
                    encoder,
                    "technician@keystone.local",
                    "Technician User",
                    Role.TECHNICIAN,
                    null
            );

            // SECOND TECHNICIAN
            // Used for Technician RBAC testing
            createUser(
                    users,
                    encoder,
                    "technician2@keystone.local",
                    "Technician Two",
                    Role.TECHNICIAN,
                    null
            );

            createUser(
                    users,
                    encoder,
                    "admin@keystone.local",
                    "Admin User",
                    Role.ADMIN,
                    null
            );

            createUser(
                    users,
                    encoder,
                    "customer@keystone.local",
                    "Customer User",
                    Role.CUSTOMER,
                    customer
            );

            // -----------------------------
            // PART
            // -----------------------------

            if (parts.count() == 0) {

                Part part = new Part();

                part.setSku("HVAC-001");
                part.setName("Air Filter");
                part.setStockQuantity(20);
                part.setUnitPrice(new BigDecimal("25.00"));
                part.setReorderLevel(5);

                parts.save(part);
            }
        };
    }

    // -----------------------------
    // CREATE USER
    // -----------------------------

    private void createUser(
            UserRepository repository,
            PasswordEncoder encoder,
            String email,
            String name,
            Role role,
            Customer customer) {

        if (repository.findByEmailIgnoreCase(email).isEmpty()) {

            User user = new User();

            user.setEmail(email);
            user.setName(name);
            user.setPassword(encoder.encode("Password@123"));
            user.setRole(role);
            user.setCustomer(customer);
            user.setActive(true);

            repository.save(user);
        }
    }
}