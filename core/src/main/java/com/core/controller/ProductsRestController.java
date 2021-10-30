package com.core.controller;

import com.core.dto.product.ProductRequestDto;
import com.core.dto.product.ProductResponseDto;
import com.core.exception.NotFoundResourceException;
import com.core.service.ProductsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
public class ProductsRestController {

    private final ProductsService productsService;

    public ProductsRestController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @PostMapping(value = "/product", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDto> create(@RequestBody @Valid ProductRequestDto requestDto) {
        var product = productsService.create(requestDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(product);
    }

    @GetMapping(value = "/product/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDto> getById(@PathVariable @Min(1) long id) {
        return productsService.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundResourceException(String.format("product with id: %s not found", id)));
    }

    @PutMapping(value = "/product/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDto> update(@PathVariable @Min(1) long id,
                                                     @RequestBody @Valid ProductRequestDto requestDto) {
        return ResponseEntity.ok(productsService.update(id, requestDto));
    }

    @DeleteMapping(value = "/product/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id) {
        productsService.deleteById(id);
        return ResponseEntity.noContent()
                .build();
    }
}
