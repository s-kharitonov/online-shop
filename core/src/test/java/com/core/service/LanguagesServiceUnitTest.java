package com.core.service;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.model.Language;
import com.core.repository.LanguagesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("languages service")
class LanguagesServiceUnitTest {

    private static final String RU_CODE = "RU";
    private static final String EN_CODE = "EN";
    private static final String DE_CODE = "DE";
    private static final long FIRST_LANGUAGE_ID = 1L;
    private static final long SECOND_LANGUAGE_ID = 2L;

    private LanguagesRepository repository;
    private LanguagesService service;
    private InOrder inOrder;

    @BeforeEach
    void setUp() {
        this.repository = mock(LanguagesRepository.class);
        this.service = new LanguagesServiceImpl(repository);
        this.inOrder = inOrder(this.repository);
    }

    @ParameterizedTest
    @MethodSource("makeRequestLanguages")
    @DisplayName("should create language")
    void shouldCreateLanguage(LanguageRequestDto requestDto) {
        var savedLanguage = new Language();

        savedLanguage.setId(FIRST_LANGUAGE_ID);
        savedLanguage.setCode(requestDto.code());

        when(repository.save(any())).thenReturn(savedLanguage);

        var expectedLanguage = new LanguageResponseDto.Builder()
                .id(FIRST_LANGUAGE_ID)
                .code(requestDto.code())
                .build();

        assertThat(service.create(requestDto))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedLanguage);
        inOrder.verify(repository, times(1))
                .existsByCode(requestDto.code());
        inOrder.verify(repository, times(1))
                .save(any());
    }

    @Test
    @DisplayName("should throw NullPointerException when language for create is null")
    void shouldThrowNpeWhenLanguageForCreateIsNull() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @Test
    @DisplayName("should throw UniqueConstraintException when language for create has not unique code")
    void shouldThrowUniqueConstraintExceptionWhenLanguageForCreateHasNotUniqueCode() {
        var ru = new LanguageRequestDto.Builder()
                .code(RU_CODE)
                .build();

        when(repository.existsByCode(ru.code())).thenReturn(true);
        assertThrows(UniqueConstraintException.class, () -> service.create(ru));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {RU_CODE, EN_CODE, DE_CODE})
    @DisplayName("should return language by code")
    void shouldReturnLanguageByCode(String code) {
        var foundedLanguage = new Language();

        foundedLanguage.setId(FIRST_LANGUAGE_ID);
        foundedLanguage.setCode(code);

        when(repository.findByCode(code)).thenReturn(Optional.of(foundedLanguage));

        var expectedLanguage = new LanguageResponseDto.Builder()
                .id(foundedLanguage.getId())
                .code(foundedLanguage.getCode())
                .build();

        assertThat(service.getByCode(code))
                .isNotEmpty()
                .usingFieldByFieldValueComparator()
                .get()
                .isEqualTo(expectedLanguage);
        inOrder.verify(repository, times(1))
                .findByCode(code);
    }

    @Test
    @DisplayName("should return languages by code in")
    void shouldReturnLanguagesByCodeIn() {
        var firstLanguage = new Language();

        firstLanguage.setId(FIRST_LANGUAGE_ID);
        firstLanguage.setCode(RU_CODE);

        var secondLanguage = new Language();

        secondLanguage.setId(SECOND_LANGUAGE_ID);
        secondLanguage.setCode(EN_CODE);

        List<String> languageCodes = List.of(RU_CODE, EN_CODE);
        List<Language> foundedLanguages = List.of(firstLanguage, secondLanguage);
        List<LanguageResponseDto> expectedLanguages = foundedLanguages.stream()
                .map(language -> new LanguageResponseDto(language.getId(), language.getCode()))
                .toList();

        when(repository.findAllByCodeIn(languageCodes)).thenReturn(foundedLanguages.stream());

        assertThat(service.getAllByCodeIn(languageCodes)).isNotEmpty()
                .containsExactlyElementsOf(expectedLanguages);

        inOrder.verify(repository, times(1))
                .findAllByCodeIn(languageCodes);
    }

    @ParameterizedTest
    @MethodSource("makeRequestLanguages")
    @DisplayName("should update language")
    void shouldUpdateLanguage(LanguageRequestDto requestDto) {
        var foundedLanguage = new Language();

        foundedLanguage.setId(FIRST_LANGUAGE_ID);
        foundedLanguage.setCode(RU_CODE);

        when(repository.findById(FIRST_LANGUAGE_ID)).thenReturn(Optional.of(foundedLanguage));

        foundedLanguage.setCode(requestDto.code());

        when(repository.save(foundedLanguage)).thenReturn(foundedLanguage);

        var expectedLanguage = new LanguageResponseDto.Builder()
                .id(foundedLanguage.getId())
                .code(foundedLanguage.getCode())
                .build();

        assertThat(service.update(FIRST_LANGUAGE_ID, requestDto))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedLanguage);
        inOrder.verify(repository, times(1))
                .existsByCode(requestDto.code());
        inOrder.verify(repository, times(1))
                .findById(FIRST_LANGUAGE_ID);
        inOrder.verify(repository, times(1))
                .save(foundedLanguage);
    }

    @Test
    @DisplayName("should throw NotFoundResourceException when language for update not found")
    void shouldThrowNotFoundResourceExceptionWhenLanguageForUpdateNotFound() {
        var languageForUpdate = new LanguageRequestDto.Builder()
                .code(RU_CODE)
                .build();

        when(repository.findById(FIRST_LANGUAGE_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundResourceException.class, () -> service.update(FIRST_LANGUAGE_ID, languageForUpdate));
    }

    @Test
    @DisplayName("should throw UniqueConstraintException when language for update has not unique code")
    void shouldThrowUniqueConstraintExceptionWhenLanguageForUpdateHasNotUniqueCode() {
        var ru = new LanguageRequestDto.Builder()
                .code(RU_CODE)
                .build();
        when(repository.existsByCode(RU_CODE)).thenReturn(true);
        assertThrows(UniqueConstraintException.class, () -> service.update(FIRST_LANGUAGE_ID, ru));
    }

    @Test
    @DisplayName("should throw NullPointerException when language for update is null")
    void shouldThrowNpeWhenLanguageForUpdateIsNull() {
        var foundedLanguage = new Language();

        foundedLanguage.setId(FIRST_LANGUAGE_ID);
        foundedLanguage.setCode(RU_CODE);

        when(repository.findById(FIRST_LANGUAGE_ID)).thenReturn(Optional.of(foundedLanguage));
        assertThrows(NullPointerException.class, () -> service.update(FIRST_LANGUAGE_ID, null));
    }

    @Test
    @DisplayName("should delete language without errors")
    void shouldDeleteLanguageWithoutErrors() {
        when(repository.existsById(FIRST_LANGUAGE_ID)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteById(FIRST_LANGUAGE_ID));
        inOrder.verify(repository, times(1))
                .existsById(FIRST_LANGUAGE_ID);
        inOrder.verify(repository, times(1))
                .deleteById(FIRST_LANGUAGE_ID);
    }

    @Test
    @DisplayName("should throw NotFoundResourceException when language for delete not found")
    void shouldThrowNotFoundResourceExceptionWhenLanguageForDeleteNotFound() {
        when(repository.existsById(FIRST_LANGUAGE_ID)).thenReturn(false);
        assertThrows(NotFoundResourceException.class, () -> service.deleteById(FIRST_LANGUAGE_ID));
    }

    private static Stream<LanguageRequestDto> makeRequestLanguages() {
        var ru = new LanguageRequestDto.Builder()
                .code(RU_CODE)
                .build();
        var en = new LanguageRequestDto.Builder()
                .code(EN_CODE)
                .build();
        var de = new LanguageRequestDto.Builder()
                .code(DE_CODE)
                .build();

        return Stream.of(ru, en, de);
    }
}