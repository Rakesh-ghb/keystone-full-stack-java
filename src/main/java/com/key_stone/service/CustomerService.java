package com.key_stone.service;

import com.key_stone.domain.Customer;
import com.key_stone.dto.ApiDtos.CustomerRequest;
import com.key_stone.dto.ApiDtos.CustomerResponse;
import com.key_stone.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public List<CustomerResponse> all() {
        return repo.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public CustomerResponse get(Long id) {
        Customer customer = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found: " + id));

        return map(customer);
    }

    public CustomerResponse create(CustomerRequest r) {

        if (repo.existsByNameIgnoreCase(r.name())) {
            throw new IllegalArgumentException("Customer already exists");
        }

        Customer customer = new Customer();

        customer.setName(r.name());
        customer.setContactName(r.contactName());
        customer.setEmail(r.email());
        customer.setPhone(r.phone());

        return map(repo.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest r) {

        Customer customer = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found: " + id));

        if (!customer.getName().equalsIgnoreCase(r.name())
                && repo.existsByNameIgnoreCase(r.name())) {
            throw new IllegalArgumentException("Customer already exists");
        }

        customer.setName(r.name());
        customer.setContactName(r.contactName());
        customer.setEmail(r.email());
        customer.setPhone(r.phone());

        return map(repo.save(customer));
    }

    private CustomerResponse map(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getName(),
                c.getContactName(),
                c.getEmail(),
                c.getPhone()
        );
    }
}