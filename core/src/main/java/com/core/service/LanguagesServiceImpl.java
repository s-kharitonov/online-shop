package com.core.service;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.model.Language;
import com.core.repository.LanguagesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LanguagesServiceImpl implements LanguagesService {

    private final LanguagesRepository repository;

    public LanguagesServiceImpl(LanguagesRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LanguageResponseDto create(LanguageRequestDto requestDto) {
        throwErrorIfExistByCode(requestDto.code());
        var language = new Language();

        language.setCode(requestDto.code());

        var savedLanguage = repository.save(language);

        return convertLanguageToDto(savedLanguage);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LanguageResponseDto> getByCode(String code) {
        return repository.findByCode(code)
                .map(this::convertLanguageToDto);
    }

    @Override
    @Transactional
    public LanguageResponseDto update(long id, LanguageRequestDto requestDto) {
        throwErrorIfExistByCode(requestDto.code());
        var language = repository.findById(id)
                .orElseThrow(() -> new NotFoundResourceException(String.format("language with id: %s not found", id)));

        language.setCode(requestDto.code());

        var updatedLanguage = repository.save(language);

        return convertLanguageToDto(updatedLanguage);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var language = repository.findById(id)
                .orElseThrow(() -> new NotFoundResourceException(String.format("language with id: %s not found", id)));

        repository.delete(language);
    }

    private void throwErrorIfExistByCode(String code) {
        if (repository.existsByCode(code)) {
            throw new UniqueConstraintException(String.format("language with code: %s is already exist", code));
        }
    }

    private LanguageResponseDto convertLanguageToDto(Language language) {
        return new LanguageResponseDto.Builder()
                .id(language.getId())
                .code(language.getCode())
                .build();
    }
}
