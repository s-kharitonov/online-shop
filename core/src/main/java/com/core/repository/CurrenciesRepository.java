package com.core.repository;

import com.core.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrenciesRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);

    boolean existsByCode(String code);
}
