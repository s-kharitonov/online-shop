package com.core.controller;

import com.core.dto.LanguageRequestDto;
import com.core.dto.LanguageResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.service.LanguagesService;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
public class LanguagesRestController {

    private final LanguagesService languagesService;

    public LanguagesRestController(LanguagesService languagesService) {
        this.languagesService = languagesService;
    }

    @PostMapping(value = "/language", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<LanguageResponseDto> create(@RequestBody @Valid LanguageRequestDto requestDto) {
        var language = languagesService.create(requestDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(language.code())
                .toUri();

        return ResponseEntity.created(location)
                .body(language);
    }

    @GetMapping(value = "/language/{code}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<LanguageResponseDto> getByCode(@PathVariable @NotBlank @Length(max = 3) String code) {
        return languagesService.getByCode(code)
                .map(ResponseEntity::ok)
                .orElseThrow(
                        () -> new NotFoundResourceException(String.format("language with code: %s not found", code)));
    }

    @PutMapping(value = "/language/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<LanguageResponseDto> update(@PathVariable @Min(1) long id,
                                                      @RequestBody @Valid LanguageRequestDto requestDto) {
        return ResponseEntity.ok(languagesService.update(id, requestDto));
    }

    @DeleteMapping(value = "/language/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id) {
        languagesService.deleteById(id);

        return ResponseEntity.noContent()
                .build();
    }
}
