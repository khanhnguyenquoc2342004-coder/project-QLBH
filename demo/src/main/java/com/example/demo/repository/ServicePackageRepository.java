package com.example.demo.repository;

import com.example.demo.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            select p from ServicePackage p
            where (:keyword is null
                    or lower(p.code) like lower(concat('%', :keyword, '%'))
                    or lower(p.name) like lower(concat('%', :keyword, '%')))
              and (:status is null or upper(p.status) = upper(:status))
            """)
    Page<ServicePackage> search(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}
