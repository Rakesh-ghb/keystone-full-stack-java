package com.key_stone.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String name;

    @Column(length = 120)
    private String contactName;

    @Column(length = 150)
    private String email;

    @Column(length = 40)
    private String phone;

    @OneToMany(mappedBy = "customer")
    private List<Site> sites = new ArrayList<>();

    public Customer() {
    }

    public Customer(
            Long id,
            String name,
            String contactName,
            String email,
            String phone,
            List<Site> sites) {

        this.id = id;
        this.name = name;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.sites = sites;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }
}