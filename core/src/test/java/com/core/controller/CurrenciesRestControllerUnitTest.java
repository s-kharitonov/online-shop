package com.core.controller;

import com.core.dto.CurrencyRequestDto;
import com.core.dto.CurrencyResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.service.CurrenciesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrenciesRestController.class)
@DisplayName("currencies REST controller")
class CurrenciesRestControllerUnitTest {

    private static final String CURRENCY_DOMAIN_URL = "/currency/";
    private static final String CURRENCY_BY_CODE_URL = "/currency/{code}";
    private static final String CURRENCY_BY_ID_URL = "/currency/{id}";
    private static final String RUB_CODE = "RUB";
    private static final long FIRST_CURRENCY_ID = 1L;
    private static final String GBP_CODE = "GBP";
    private static final String USD_CODE = "USD";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrenciesService currenciesService;

    private InOrder inOrder;

    @BeforeEach
    void setUp() {
        this.inOrder = inOrder(currenciesService);
    }

    @Test
    @DisplayName("should create currency")
    void shouldCreateCurrency() throws Exception {
        var currency = new CurrencyRequestDto(RUB_CODE, BigDecimal.ONE);
        var expectedCurrency = new CurrencyResponseDto(FIRST_CURRENCY_ID, RUB_CODE, BigDecimal.ONE);
        var requestBuilder = post(CURRENCY_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        when(currenciesService.create(any())).thenReturn(expectedCurrency);

        mvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, containsString("/currency/" + expectedCurrency.code())))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedCurrency)));

        inOrder.verify(currenciesService, times(1))
                .create(any());
    }

    @ParameterizedTest
    @MethodSource("makeNotValidCurrencies")
    @DisplayName("should response BAD_REQUEST when currency for save is not valid")
    void shouldResponseBadRequestWhenCurrencyForSaveIsNotValid(CurrencyRequestDto currency) throws Exception {
        var requestBuilder = post(CURRENCY_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @Test
    @DisplayName("should response CONFLICT when currency for save has not unique code")
    void shouldResponseConflictWhenCurrencyForSaveHasNotUniqueCode() throws Exception {
        var currency = new CurrencyRequestDto(RUB_CODE, BigDecimal.ONE);
        var requestBuilder = post(CURRENCY_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        when(currenciesService.create(any())).thenThrow(
                new UniqueConstraintException("currency with code: " + RUB_CODE + " exist"));

        mvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(CONFLICT.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {RUB_CODE, USD_CODE, GBP_CODE})
    @DisplayName("should return currency by code")
    void shouldReturnCurrencyByCode(String code) throws Exception {
        var expectedCurrency = new CurrencyResponseDto(FIRST_CURRENCY_ID, code, BigDecimal.ONE);
        var requestBuilder = get(CURRENCY_BY_CODE_URL, code);

        when(currenciesService.getByCode(code)).thenReturn(Optional.of(expectedCurrency));

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedCurrency)));

        inOrder.verify(currenciesService, times(1))
                .getByCode(code);
    }

    @Test
    @DisplayName("should response NOT_FOUND when currency by code not found")
    void shouldResponseNotFoundWhenCurrencyNotFoundByCode() throws Exception {
        var requestBuilder = get(CURRENCY_BY_CODE_URL, RUB_CODE);

        when(currenciesService.getByCode(RUB_CODE)).thenReturn(Optional.empty());

        mvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(NOT_FOUND.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "22", "4444"})
    @DisplayName("should response BAD_REQUEST when currency code length not equal 3")
    void shouldResponseBadRequestWhenCurrencyCodeNotEqual3(String code) throws Exception {
        var requestBuilder = get(CURRENCY_BY_CODE_URL, code);

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @Test
    @DisplayName("should update currency")
    void shouldUpdateCurrency() throws Exception {
        var currency = new CurrencyRequestDto(RUB_CODE, BigDecimal.ONE);
        var expectedCurrency = new CurrencyResponseDto(FIRST_CURRENCY_ID, RUB_CODE, BigDecimal.ONE);
        var requestBuilder = put(CURRENCY_BY_ID_URL, FIRST_CURRENCY_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        when(currenciesService.update(anyLong(), any())).thenReturn(expectedCurrency);

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedCurrency)));

        inOrder.verify(currenciesService, times(1))
                .update(anyLong(), any());
    }

    @Test
    @DisplayName("should response CONFLICT when currency for update has not unique code")
    void shouldResponseConflictWhenCurrencyForUpdateHasNotUniqueCode() throws Exception {
        var currency = new CurrencyRequestDto(RUB_CODE, BigDecimal.ONE);
        var requestBuilder = put(CURRENCY_BY_ID_URL, FIRST_CURRENCY_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        when(currenciesService.update(anyLong(), any())).thenThrow(
                new UniqueConstraintException("currency with code: " + RUB_CODE + " exist"));

        mvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(CONFLICT.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, Long.MIN_VALUE, -1})
    @DisplayName("should response BAD_REQUEST when currency id for update is negative or zero")
    void shouldResponseBadRequestWhenCurrencyIdForUpdateIsNegativeOrZero(long id) throws Exception {
        var currency = new CurrencyRequestDto(RUB_CODE, BigDecimal.ONE);
        var requestBuilder = put(CURRENCY_BY_ID_URL, id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @MethodSource("makeNotValidCurrencies")
    @DisplayName("should response BAD_REQUEST when currency for update is not valid")
    void shouldResponseBadRequestWhenCurrencyForUpdateIsNotValid(CurrencyRequestDto currency) throws Exception {
        var requestBuilder = put(CURRENCY_BY_ID_URL, FIRST_CURRENCY_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currency));

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @Test
    @DisplayName("should delete currency by id")
    void shouldDeleteCurrencyById() throws Exception {
        var requestBuilder = delete(CURRENCY_BY_ID_URL, FIRST_CURRENCY_ID);

        mvc.perform(requestBuilder)
                .andExpect(status().isNoContent());

        inOrder.verify(currenciesService, times(1))
                .deleteById(FIRST_CURRENCY_ID);
    }

    @Test
    @DisplayName("should response NOT_FOUND when currency for delete not found")
    void shouldResponseNotFoundWhenCurrencyForDeleteNotFound() throws Exception {
        var requestBuilder = delete(CURRENCY_BY_ID_URL, FIRST_CURRENCY_ID);

        doThrow(new NotFoundResourceException("currency with id: " + FIRST_CURRENCY_ID + " not found"))
                .when(currenciesService)
                .deleteById(FIRST_CURRENCY_ID);

        mvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(NOT_FOUND.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, Long.MIN_VALUE, -1})
    @DisplayName("should response BAD_REQUEST when currency id for delete is negative or zero")
    void shouldResponseBadRequestWhenCurrencyIdForDeleteIsNegativeOrZero(long id) throws Exception {
        var requestBuilder = delete(CURRENCY_BY_ID_URL, id);

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }


    private static Stream<CurrencyRequestDto> makeNotValidCurrencies() {
        var currencyWithNullCode = new CurrencyRequestDto(null, BigDecimal.ONE);
        var currencyWithEmptyCode = new CurrencyRequestDto("", BigDecimal.ONE);
        var currencyWithBlankCode = new CurrencyRequestDto(" " + RUB_CODE + " ", BigDecimal.ONE);
        var currencyWithNegativeMultiplier = new CurrencyRequestDto(RUB_CODE, BigDecimal.valueOf(-1));
        var both = new CurrencyRequestDto(null, BigDecimal.valueOf(-1));

        return Stream.of(currencyWithNullCode, currencyWithEmptyCode, currencyWithBlankCode,
                currencyWithNegativeMultiplier, both);
    }
}