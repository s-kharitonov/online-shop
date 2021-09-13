package com.core.service;

import com.core.dto.CurrencyRequestDto;
import com.core.dto.CurrencyResponseDto;

import java.util.Optional;

public interface CurrenciesService {
    CurrencyResponseDto create(CurrencyRequestDto requestDto);

    Optional<CurrencyResponseDto> getByCode(String code);

    CurrencyResponseDto update(long id, CurrencyRequestDto requestDto);

    void deleteById(long id);
}
