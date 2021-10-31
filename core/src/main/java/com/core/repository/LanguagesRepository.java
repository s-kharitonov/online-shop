package com.core.repository;

import com.core.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface LanguagesRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCode(String code);

    boolean existsByCode(String code);

    Stream<Language> findAllByCodeIn(Collection<String> codes);
}
