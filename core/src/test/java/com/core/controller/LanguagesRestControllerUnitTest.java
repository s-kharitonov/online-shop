package com.core.controller;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.service.LanguagesService;
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

@WebMvcTest(LanguagesRestController.class)
@DisplayName("languages REST controller")
class LanguagesRestControllerUnitTest {

    private static final String RU_CODE = "RU";
    private static final String EN_CODE = "EN";
    private static final String DE_CODE = "DE";
    private static final String LANGUAGE_DOMAIN_URL = "/language/";
    private static final String LANGUAGE_BY_CODE_URL = "/language/{code}";
    private static final String LANGUAGE_BY_ID_URL = "/language/{id}";
    private static final long FIRST_LANGUAGE_ID = 1L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LanguagesService languagesService;

    private InOrder inOrder;

    @BeforeEach
    void setUp() {
        this.inOrder = inOrder(languagesService);
    }

    @Test
    @DisplayName("should create language")
    void shouldCreateLanguage() throws Exception {
        var language = new LanguageRequestDto(RU_CODE);
        var expectedLanguage = new LanguageResponseDto.Builder()
                .id(FIRST_LANGUAGE_ID)
                .code(RU_CODE)
                .build();
        var requestBuilder = post(LANGUAGE_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

        when(languagesService.create(any())).thenReturn(expectedLanguage);

        mvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, containsString("/language/" + expectedLanguage.code())))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedLanguage)));
        inOrder.verify(languagesService, times(1))
                .create(any());
    }

    @ParameterizedTest
    @MethodSource("makeNotValidLanguages")
    @DisplayName("should response BAD_REQUEST when language for save is not valid")
    void shouldResponseBadRequestWhenLanguageForSaveIsNotValid(LanguageRequestDto language) throws Exception {
        var requestBuilder = post(LANGUAGE_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

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
    @DisplayName("should response CONFLICT when language for save has not unique code")
    void shouldResponseConflictWhenLanguageForSaveHasNotUniqueCode() throws Exception {
        var language = new LanguageRequestDto(RU_CODE);
        var requestBuilder = post(LANGUAGE_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

        when(languagesService.create(any())).thenThrow(
                new UniqueConstraintException("language with code: " + RU_CODE + " exist"));

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
    @ValueSource(strings = {RU_CODE, EN_CODE, DE_CODE})
    @DisplayName("should return language by code")
    void shouldReturnLanguageByCode(String code) throws Exception {
        var expectedLanguage = new LanguageResponseDto.Builder()
                .id(FIRST_LANGUAGE_ID)
                .code(code)
                .build();
        var requestBuilder = get(LANGUAGE_BY_CODE_URL, code);

        when(languagesService.getByCode(code)).thenReturn(Optional.of(expectedLanguage));

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedLanguage)));
        inOrder.verify(languagesService, times(1)).getByCode(code);
    }

    @Test
    @DisplayName("should response NOT_FOUND when language by code not found")
    void shouldResponseNotFoundWhenLanguageNotFoundByCode() throws Exception {
        var requestBuilder = get(LANGUAGE_BY_CODE_URL, RU_CODE);

        when(languagesService.getByCode(RU_CODE)).thenReturn(Optional.empty());

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
    @ValueSource(strings = {" " + RU_CODE + " ", RU_CODE + DE_CODE + EN_CODE})
    @DisplayName("should response BAD_REQUEST when language code not valid")
    void shouldResponseBadRequestWhenLanguageCodeNotValid(String code) throws Exception {
        var requestBuilder = get(LANGUAGE_BY_CODE_URL, code);

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
    @DisplayName("should update language")
    void shouldUpdateLanguage() throws Exception {
        var language = new LanguageRequestDto(RU_CODE);
        var expectedLanguage = new LanguageResponseDto.Builder()
                .id(FIRST_LANGUAGE_ID)
                .code(RU_CODE)
                .build();
        var requestBuilder = put(LANGUAGE_BY_ID_URL, FIRST_LANGUAGE_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

        when(languagesService.update(anyLong(), any())).thenReturn(expectedLanguage);

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedLanguage)));
        inOrder.verify(languagesService, times(1)).update(anyLong(), any());
    }

    @Test
    @DisplayName("should response CONFLICT when language for update has not unique code")
    void shouldResponseConflictWhenLanguageForUpdateHasNotUniqueCode() throws Exception {
        var language = new LanguageRequestDto(RU_CODE);
        var requestBuilder = put(LANGUAGE_BY_ID_URL, FIRST_LANGUAGE_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

        when(languagesService.update(anyLong(), any())).thenThrow(
                new UniqueConstraintException("language with code: " + RU_CODE + " exist"));

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
    @ValueSource(longs = {0, Long.MIN_VALUE, -1})
    @DisplayName("should response BAD_REQUEST when language id for update is negative or zero")
    void shouldResponseBadRequestWhenLanguageIdForUpdateIsNegativeOrZero(long id) throws Exception {
        var language = new LanguageRequestDto(RU_CODE);
        var requestBuilder = put(LANGUAGE_BY_ID_URL, id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

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
    @MethodSource("makeNotValidLanguages")
    @DisplayName("should response BAD_REQUEST when language for update is not valid")
    void shouldResponseBadRequestWhenLanguageForUpdateIsNotValid(LanguageRequestDto language) throws Exception {
        var requestBuilder = put(LANGUAGE_BY_ID_URL, FIRST_LANGUAGE_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language));

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
    @DisplayName("should delete language by id")
    void shouldDeleteLanguageById() throws Exception {
        var requestBuilder = delete(LANGUAGE_BY_ID_URL, FIRST_LANGUAGE_ID);

        mvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
        inOrder.verify(languagesService, times(1)).deleteById(FIRST_LANGUAGE_ID);
    }

    @Test
    @DisplayName("should response NOT_FOUND when language for delete not found")
    void shouldResponseNotFoundWhenLanguageForDeleteNotFound() throws Exception {
        var requestBuilder = delete(LANGUAGE_BY_ID_URL, FIRST_LANGUAGE_ID);

        doThrow(new NotFoundResourceException("language with id: " + FIRST_LANGUAGE_ID + " not found"))
                .when(languagesService)
                .deleteById(FIRST_LANGUAGE_ID);

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
    @DisplayName("should response BAD_REQUEST when language id for delete is negative or zero")
    void shouldResponseBadRequestWhenLanguageIdForDeleteIsNegativeOrZero(long id) throws Exception {
        var requestBuilder = delete(LANGUAGE_BY_ID_URL, id);

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

    private static Stream<LanguageRequestDto> makeNotValidLanguages() {
        var languageWithNullCode = new LanguageRequestDto(null);
        var languageWithEmptyCode = new LanguageRequestDto("");
        var languageWithBlankCode = new LanguageRequestDto(" " + RU_CODE + " ");
        var languageWithNotValidLengthCode = new LanguageRequestDto(RU_CODE + DE_CODE + EN_CODE);

        return Stream.of(languageWithNullCode, languageWithEmptyCode, languageWithBlankCode,
                languageWithNotValidLengthCode);
    }
}