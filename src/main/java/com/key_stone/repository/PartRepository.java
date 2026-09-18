package com.key_stone.repository;

import com.key_stone.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {

    boolean existsBySkuIgnoreCase(String sku);

}
