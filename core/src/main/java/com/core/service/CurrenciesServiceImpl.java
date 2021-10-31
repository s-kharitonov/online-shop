package com.core.service;

import com.core.dto.CurrencyRequestDto;
import com.core.dto.CurrencyResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import com.core.model.Currency;
import com.core.repository.CurrenciesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CurrenciesServiceImpl implements CurrenciesService {

    private final CurrenciesRepository repository;

    public CurrenciesServiceImpl(CurrenciesRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CurrencyResponseDto create(CurrencyRequestDto requestDto) {
        throwErrorIfExistByCode(requestDto.code());
        var currency = new Currency();

        currency.setCode(requestDto.code());
        currency.setMultiplier(requestDto.multiplier());

        var savedCurrency = repository.save(currency);

        return convertCurrencyToDto(savedCurrency);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CurrencyResponseDto> getByCode(String code) {
        return repository.findByCode(code)
                .map(this::convertCurrencyToDto);
    }

    @Override
    @Transactional
    public CurrencyResponseDto update(long id, CurrencyRequestDto requestDto) {
        throwErrorIfExistByCode(requestDto.code());
        var currency = repository.findById(id)
                .orElseThrow(() -> new NotFoundResourceException(String.format("currency with id: %s not found", id)));

        currency.setCode(requestDto.code());
        currency.setMultiplier(requestDto.multiplier());

        var updatedCurrency = repository.save(currency);

        return convertCurrencyToDto(updatedCurrency);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var hasCurrency = repository.existsById(id);

        if (!hasCurrency) {
            throw new NotFoundResourceException(String.format("currency with id: %s not found", id));
        }

        repository.deleteById(id);
    }

    private void throwErrorIfExistByCode(String code) {
        if (repository.existsByCode(code)) {
            throw new UniqueConstraintException(String.format("currency with code: %s is already exist", code));
        }
    }

    private CurrencyResponseDto convertCurrencyToDto(Currency currency) {
        var id = currency.getId();
        var code = currency.getCode();
        var multiplier = currency.getMultiplier();

        return new CurrencyResponseDto(id, code, multiplier);
    }
}
