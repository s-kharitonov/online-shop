package com.core.service;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LanguagesService {
    LanguageResponseDto create(LanguageRequestDto requestDto);

    Optional<LanguageResponseDto> getByCode(String code);

    List<LanguageResponseDto> getAllByCodeIn(Collection<String> codes);

    LanguageResponseDto update(long id, LanguageRequestDto requestDto);

    void deleteById(long id);
}
