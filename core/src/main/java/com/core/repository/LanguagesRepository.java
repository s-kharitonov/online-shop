package com.core.repository;

import com.core.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguagesRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCode(String code);

    boolean existsByCode(String code);
}
