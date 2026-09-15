package com.key_stone.repository;
import com.key_stone.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CustomerRepository extends JpaRepository<Customer,Long>{ boolean existsByNameIgnoreCase(String name); }