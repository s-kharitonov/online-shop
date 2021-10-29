package com.core.repository.product;

import com.core.model.product.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.stream.Stream;

public interface ProductFeaturesRepository extends JpaRepository<ProductFeature, Long> {

    @Query("select f from ProductFeature f join fetch f.language where f.product.id = :productId")
    Stream<ProductFeature> findAllByProductId(@Param("productId") long productId);
}
