package com.core.repository;

import com.core.model.Currency;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
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

import static com.core.constants.ContainerConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@DisplayName("currencies repository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CurrenciesRepositoryUnitTest {

    @Container
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DB_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD);
    private static final String RUB_CODE = "RUB";
    private static final String USD_CODE = "USD";
    private static final String GBP_CODE = "GBP";

    @TestConfiguration
    static class CurrenciesRepositoryUnitTestConfig {

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
    private CurrenciesRepository repository;

    @Autowired
    private TestEntityManager em;

    @ParameterizedTest
    @Transactional(readOnly = true)
    @ValueSource(strings = {RUB_CODE, USD_CODE, GBP_CODE})
    @DisplayName("should return currency by code from database")
    void shouldReturnCurrencyByCode(String code) {
        var expectedCurrency = em.getEntityManager()
                .createQuery("select c from Currency c where c.code = :code", Currency.class)
                .setParameter("code", code)
                .getSingleResult();
        var currency = repository.findByCode(code);

        assertThat(currency).isNotEmpty()
                .usingFieldByFieldValueComparator()
                .get()
                .isEqualTo(expectedCurrency);
    }

    @ParameterizedTest
    @Transactional(readOnly = true)
    @ValueSource(strings = {RUB_CODE, USD_CODE, GBP_CODE})
    @DisplayName("should check on exist currency by code")
    void shouldCheckOnExistCurrencyByCode(String code) {
        assertTrue(repository.existsByCode(code));
    }
}