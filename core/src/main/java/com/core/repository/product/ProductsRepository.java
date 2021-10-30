package com.core.repository.product;

import com.core.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Product, Long> {
    @NonNull
    @Query("select p from Product p join fetch p.descriptions d join fetch d.language l where p.id = :id")
    Optional<Product> findByIdWithDescriptions(@NonNull @Param("id") Long id);
}
