package com.core.service;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;

import java.util.Optional;

public interface LanguagesService {
    LanguageResponseDto create(LanguageRequestDto requestDto);

    Optional<LanguageResponseDto> getByCode(String code);

    LanguageResponseDto update(long id, LanguageRequestDto requestDto);

    void deleteById(long id);
}
