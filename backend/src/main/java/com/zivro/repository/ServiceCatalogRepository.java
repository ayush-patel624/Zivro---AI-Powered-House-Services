package com.zivro.repository;

import com.zivro.domain.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {

    List<ServiceCatalog> findAllByOrderBySortOrderAscIdAsc();
}
