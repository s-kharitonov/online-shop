package com.core.service;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.model.Language;
import com.core.repository.LanguagesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
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

        return new LanguageResponseDto(savedLanguage.getId(), savedLanguage.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LanguageResponseDto> getByCode(String code) {
        return repository.findByCode(code)
                .map(language -> new LanguageResponseDto(language.getId(), language.getCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponseDto> getAllByCodeIn(Collection<String> codes) {
        return repository.findAllByCodeIn(codes)
                .map(language -> new LanguageResponseDto(language.getId(), language.getCode()))
                .toList();
    }

    @Override
    @Transactional
    public LanguageResponseDto update(long id, LanguageRequestDto requestDto) {
        throwErrorIfExistByCode(requestDto.code());
        var language = repository.findById(id)
                .orElseThrow(() -> new NotFoundResourceException(String.format("language with id: %s not found", id)));

        language.setCode(requestDto.code());

        var updatedLanguage = repository.save(language);

        return new LanguageResponseDto(id, updatedLanguage.getCode());
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var hasLanguage = repository.existsById(id);

        if (!hasLanguage) {
            throw new NotFoundResourceException(String.format("language with id: %s not found", id));
        }

        repository.deleteById(id);
    }

    private void throwErrorIfExistByCode(String code) {
        if (repository.existsByCode(code)) {
            throw new UniqueConstraintException(String.format("language with code: %s is already exist", code));
        }
    }
}
