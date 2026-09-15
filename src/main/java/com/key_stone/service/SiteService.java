package com.key_stone.service;

import com.key_stone.domain.Customer;
import com.key_stone.domain.Site;
import com.key_stone.dto.ApiDtos.SiteRequest;
import com.key_stone.dto.ApiDtos.SiteResponse;
import com.key_stone.repository.CustomerRepository;
import com.key_stone.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository sites;
    private final CustomerRepository customers;

    public SiteService(
            SiteRepository sites,
            CustomerRepository customers) {

        this.sites = sites;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> byCustomer(Long customerId) {

        return sites.findAll()
                .stream()
                .filter(s ->
                        s.getCustomer() != null &&
                        s.getCustomer().getId() != null &&
                        s.getCustomer().getId().equals(customerId))
                .map(this::map)
                .toList();
    }

    @Transactional
    public SiteResponse create(SiteRequest r) {

        Customer customer = customers.findById(r.customerId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found"));

        Site site = new Site();

        site.setName(r.name());
        site.setAddress(r.address());
        site.setCity(r.city());
        site.setPostalCode(r.postalCode());
        site.setCustomer(customer);

        return map(sites.save(site));
    }

    @Transactional(readOnly = true)
    public SiteResponse get(Long id) {

        Site site = sites.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Site not found"));

        return map(site);
    }

    private SiteResponse map(Site site) {

        Customer customer = site.getCustomer();

        return new SiteResponse(
                site.getId(),
                site.getName(),
                site.getAddress(),
                site.getCity(),
                site.getPostalCode(),
                customer != null ? customer.getId() : null,
                customer != null ? customer.getName() : null
        );
    }
}