package com.core.repository;

import com.core.model.Language;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static com.core.constants.ContainerConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@DisplayName("languages repository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LanguagesRepositoryUnitTest {

    @Container
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DB_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD);

    private static final String RU_CODE = "RU";
    private static final String EN_CODE = "EN";

    @TestConfiguration
    static class LanguagesRepositoryUnitTestConfig {

        @Bean
        public DataSource dataSource() {
            var hikariConfig = new HikariConfig();

            hikariConfig.setUsername(container.getUsername());
            hikariConfig.setPassword(container.getPassword());
            hikariConfig.setJdbcUrl(container.getJdbcUrl());

            return new HikariDataSource(hikariConfig);
        }
    }

    @Autowired
    private LanguagesRepository repository;

    @Autowired
    private TestEntityManager em;

    @ParameterizedTest
    @Transactional(readOnly = true)
    @ValueSource(strings = {RU_CODE, EN_CODE})
    @DisplayName("should return language by code")
    void shouldReturnLanguageByCode(String code) {
        var expectedLanguage = em.getEntityManager()
                .createQuery("select l from Language l where l.code = :code", Language.class)
                .setParameter("code", code)
                .getSingleResult();
        var language = repository.findByCode(code);

        assertThat(language).isNotEmpty()
                .usingFieldByFieldValueComparator()
                .get()
                .isEqualTo(expectedLanguage);
    }

    @ParameterizedTest
    @Transactional(readOnly = true)
    @ValueSource(strings = {RU_CODE, EN_CODE})
    @DisplayName("should check on exist language by code")
    void shouldCheckOnExistLanguageByCode(String code) {
        assertTrue(repository.existsByCode(code));
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("should return languages by code in")
    void shouldReturnLanguagesByCodeIn() {
        List<String> languageCodes = List.of(RU_CODE, EN_CODE);
        List<Language> expectedLanguages = em.getEntityManager()
                .createQuery("select l from Language l where l.code in :codes", Language.class)
                .setParameter("codes", languageCodes)
                .getResultList();
        List<Language> languages = repository.findAllByCodeIn(languageCodes)
                .toList();

        assertThat(languages).isNotEmpty()
                .containsExactlyElementsOf(expectedLanguages);
    }
}