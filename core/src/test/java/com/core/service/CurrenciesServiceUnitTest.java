package com.core.service;

import com.core.dto.CurrencyRequestDto;
import com.core.dto.CurrencyResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.model.Currency;
import com.core.repository.CurrenciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("currencies service")
class CurrenciesServiceUnitTest {

    private static final String RUB_CODE = "RUB";
    private static final String USD_CODE = "USD";
    private static final String GBP_CODE = "GBP";
    private static final long FIRST_CURRENCY_ID = 1L;

    private CurrenciesRepository repository;
    private CurrenciesService service;
    private InOrder inOrder;

    @BeforeEach
    void setUp() {
        this.repository = mock(CurrenciesRepository.class);
        this.service = new CurrenciesServiceImpl(repository);
        this.inOrder = inOrder(this.repository);
    }

    @ParameterizedTest
    @MethodSource("makeRequestCurrencies")
    @DisplayName("should create currency")
    void shouldCreateCurrency(CurrencyRequestDto requestDto) {
        var savedCurrency = new Currency();

        savedCurrency.setId(FIRST_CURRENCY_ID);
        savedCurrency.setCode(requestDto.code());
        savedCurrency.setMultiplier(requestDto.multiplier());

        when(repository.save(any()))
                .thenReturn(savedCurrency);

        var expectedCurrency = new CurrencyResponseDto.Builder()
                .id(savedCurrency.getId())
                .code(savedCurrency.getCode())
                .multiplier(savedCurrency.getMultiplier())
                .build();

        assertThat(service.create(requestDto))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedCurrency);
        inOrder.verify(repository, times(1))
                .save(any());
    }

    @Test
    @DisplayName("should throw NullPointerException when currency for create is null")
    void shouldThrowNpeWhenCurrencyForCreateIsNull() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    @DisplayName("should throw UniqueConstraintException when currency for save has not unique code")
    void shouldThrowUniqueConstraintExceptionWhenCurrencyForSaveHasNotUniqueCode() {
        var rub = new CurrencyRequestDto.Builder()
                .code(RUB_CODE)
                .multiplier(BigDecimal.ONE)
                .build();

        when(repository.existsByCode(RUB_CODE)).thenReturn(true);
        assertThrows(UniqueConstraintException.class, () -> service.create(rub));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {RUB_CODE, GBP_CODE, USD_CODE})
    @DisplayName("should return currency by code")
    void shouldReturnCurrencyByCode(String currencyCode) {
        var foundedCurrency = new Currency();

        foundedCurrency.setId(FIRST_CURRENCY_ID);
        foundedCurrency.setCode(currencyCode);
        foundedCurrency.setMultiplier(BigDecimal.ONE);

        when(repository.findByCode(currencyCode))
                .thenReturn(Optional.of(foundedCurrency));

        var expectedCurrency = new CurrencyResponseDto.Builder()
                .id(foundedCurrency.getId())
                .code(foundedCurrency.getCode())
                .multiplier(foundedCurrency.getMultiplier())
                .build();

        assertThat(service.getByCode(currencyCode))
                .isNotEmpty()
                .usingFieldByFieldValueComparator()
                .get()
                .isEqualTo(expectedCurrency);
        inOrder.verify(repository, times(1))
                .findByCode(currencyCode);
    }

    @ParameterizedTest
    @MethodSource("makeRequestCurrencies")
    @DisplayName("should update currency")
    void shouldUpdateCurrency(CurrencyRequestDto requestDto) {
        var foundedCurrency = new Currency();

        foundedCurrency.setId(FIRST_CURRENCY_ID);
        foundedCurrency.setCode(RUB_CODE);
        foundedCurrency.setMultiplier(BigDecimal.ONE);

        when(repository.findById(FIRST_CURRENCY_ID))
                .thenReturn(Optional.of(foundedCurrency));

        foundedCurrency.setCode(requestDto.code());
        foundedCurrency.setMultiplier(requestDto.multiplier());

        when(repository.save(foundedCurrency))
                .thenReturn(foundedCurrency);

        var expectedCurrency = new CurrencyResponseDto.Builder()
                .id(FIRST_CURRENCY_ID)
                .code(foundedCurrency.getCode())
                .multiplier(foundedCurrency.getMultiplier())
                .build();

        assertThat(service.update(FIRST_CURRENCY_ID, requestDto))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedCurrency);
        inOrder.verify(repository, times(1))
                .findById(FIRST_CURRENCY_ID);
        inOrder.verify(repository, times(1))
                .save(foundedCurrency);
    }

    @Test
    @DisplayName("should throw NotFoundResourceException when currency for update not found")
    void shouldThrowNotFoundResourceExceptionWhenCurrencyForUpdateNotFound() {
        var currencyForUpdate = new CurrencyRequestDto.Builder()
                .code(RUB_CODE)
                .multiplier(BigDecimal.ONE)
                .build();

        when(repository.findById(FIRST_CURRENCY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundResourceException.class, () -> service.update(FIRST_CURRENCY_ID, currencyForUpdate));
    }


    @Test
    @DisplayName("should throw UniqueConstraintException when currency for update has not unique code")
    void shouldThrowUniqueConstraintExceptionWhenCurrencyForUpdateHasNotUniqueCode() {
        var rub = new CurrencyRequestDto.Builder()
                .code(RUB_CODE)
                .multiplier(BigDecimal.ONE)
                .build();

        when(repository.existsByCode(RUB_CODE)).thenReturn(true);
        assertThrows(UniqueConstraintException.class, () -> service.update(FIRST_CURRENCY_ID, rub));
    }

    @Test
    @DisplayName("should throw NullPointerException when currency for update is null")
    void shouldThrowNpeWhenCurrencyForUpdateIsNull() {
        var foundedCurrency = new Currency();

        foundedCurrency.setId(FIRST_CURRENCY_ID);
        foundedCurrency.setCode(RUB_CODE);
        foundedCurrency.setMultiplier(BigDecimal.ONE);

        when(repository.findById(FIRST_CURRENCY_ID))
                .thenReturn(Optional.of(foundedCurrency));

        assertThrows(NullPointerException.class, () -> service.update(FIRST_CURRENCY_ID, null));
    }

    @Test
    @DisplayName("should delete currency without errors")
    void shouldDeleteCurrencyWithoutErrors() {
        when(repository.existsById(FIRST_CURRENCY_ID)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteById(FIRST_CURRENCY_ID));

        inOrder.verify(repository, times(1))
                .existsById(FIRST_CURRENCY_ID);
        inOrder.verify(repository, times(1))
                .deleteById(FIRST_CURRENCY_ID);
    }

    @Test
    @DisplayName("should throw NotFoundResourceException when currency for delete not found")
    void shouldThrowNotFoundResourceExceptionWhenCurrencyForDeleteNotFound() {
        when(repository.existsById(FIRST_CURRENCY_ID)).thenReturn(false);
        assertThrows(NotFoundResourceException.class, () -> service.deleteById(FIRST_CURRENCY_ID));
    }

    private static Stream<CurrencyRequestDto> makeRequestCurrencies() {
        var rub = new CurrencyRequestDto.Builder()
                .code(RUB_CODE)
                .multiplier(BigDecimal.ONE)
                .build();
        var usd = new CurrencyRequestDto.Builder()
                .code(USD_CODE)
                .multiplier(BigDecimal.valueOf(75))
                .build();
        var gbp = new CurrencyRequestDto.Builder()
                .code(GBP_CODE)
                .multiplier(BigDecimal.valueOf(100))
                .build();

        return Stream.of(rub, usd, gbp);
    }
}