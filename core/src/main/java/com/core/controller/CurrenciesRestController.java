package com.core.controller;

import com.core.dto.CurrencyRequestDto;
import com.core.dto.CurrencyResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.service.CurrenciesService;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
public class CurrenciesRestController {

    private final CurrenciesService currenciesService;

    public CurrenciesRestController(CurrenciesService currenciesService) {
        this.currenciesService = currenciesService;
    }

    @PostMapping(value = "/currency", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyResponseDto> create(@RequestBody @Valid CurrencyRequestDto requestDto) {
        var currency = currenciesService.create(requestDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(currency.code())
                .toUri();

        return ResponseEntity.created(location)
                .body(currency);
    }

    @GetMapping(value = "/currency/{code}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyResponseDto> getByCode(@PathVariable @Length(min = 3, max = 3) String code) {
        return currenciesService.getByCode(code)
                .map(ResponseEntity::ok)
                .orElseThrow(
                        () -> new NotFoundResourceException(String.format("currency with code: %s not found", code)));
    }

    @PutMapping(value = "/currency/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyResponseDto> update(@PathVariable @Min(1) long id,
                                                      @RequestBody @Valid CurrencyRequestDto requestDto) {
        return ResponseEntity.ok(currenciesService.update(id, requestDto));
    }

    @DeleteMapping(value = "/currency/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id) {
        currenciesService.deleteById(id);

        return ResponseEntity.noContent()
                .build();
    }
}
