package com.core.service;

import com.core.dto.LanguageResponseDto;
import com.core.dto.product.*;
import com.core.exception.NotFoundResourceException;
import com.core.model.Language;
import com.core.model.product.Product;
import com.core.model.product.ProductDescription;
import com.core.model.product.ProductFeature;
import com.core.repository.product.ProductFeaturesRepository;
import com.core.repository.product.ProductsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class ProductsServiceImpl implements ProductsService {

    private final ProductsRepository productsRepository;
    private final ProductFeaturesRepository featuresRepository;
    private final LanguagesService languagesService;

    public ProductsServiceImpl(ProductsRepository productsRepository,
                               ProductFeaturesRepository featuresRepository,
                               LanguagesService languagesService) {
        this.productsRepository = productsRepository;
        this.featuresRepository = featuresRepository;
        this.languagesService = languagesService;
    }

    @Override
    @Transactional
    public ProductResponseDto create(ProductRequestDto requestDto) {
        Map<String, LanguageResponseDto> languagesByCode = findLanguagesOrThrow(requestDto);
        var product = new Product();

        product.setPrice(requestDto.price());
        addDescriptionsToProduct(product, requestDto.descriptions(), languagesByCode);
        addFeaturesToProduct(product, requestDto.features(), languagesByCode);

        var savedProduct = productsRepository.save(product);

        return makeProductDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDto> getById(long id) {
        return findProductById(id).map(this::makeProductDto);
    }

    @Override
    @Transactional
    public ProductResponseDto update(long id, ProductRequestDto requestDto) {
        var product = findProductById(id).orElseThrow(
                () -> new NotFoundResourceException(String.format("product with id: %s not found", id)));
        Map<String, LanguageResponseDto> languagesByCode = findLanguagesOrThrow(requestDto);

        product.setPrice(requestDto.price());
        updateDescriptions(product, requestDto.descriptions(), languagesByCode);
        updateFeatures(product, requestDto.features(), languagesByCode);

        var savedProduct = productsRepository.save(product);

        return makeProductDto(savedProduct);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var hasProduct = productsRepository.existsById(id);

        if (!hasProduct) {
            throw new NotFoundResourceException(String.format("product with id: %s not found", id));
        }

        productsRepository.deleteById(id);
    }

    private void updateFeatures(Product product, List<ProductFeatureRequestDto> features,
                                Map<String, LanguageResponseDto> languagesByCode) {
        List<ProductFeature> savedFeatures = new ArrayList<>(product.getFeatures());
        List<ProductFeatureRequestDto> newFeatures = features.stream()
                .filter(feature -> feature.id() == null)
                .toList();
        Map<Long, ProductFeatureRequestDto> featuresForUpdateById = features.stream()
                .filter(feature -> feature.id() != null)
                .collect(toMap(ProductFeatureRequestDto::id, Function.identity()));

        addFeaturesToProduct(product, newFeatures, languagesByCode);

        for (ProductFeature feature : savedFeatures) {
            var featureId = feature.getId();
            var featureDto = featuresForUpdateById.get(featureId);

            if (featureDto == null) {
                product.removeFeature(feature);
                continue;
            }

            var languageDto = languagesByCode.get(featureDto.languageCode());
            var language = makeLanguage(languageDto);

            feature.setName(featureDto.name());
            feature.setValue(featureDto.value());
            feature.setLanguage(language);
        }
    }

    private void updateDescriptions(Product product, List<ProductDescriptionRequestDto> descriptions,
                                    Map<String, LanguageResponseDto> languagesByCode) {
        List<ProductDescription> savedDescriptions = new ArrayList<>(product.getDescriptions());
        List<ProductDescriptionRequestDto> newDescriptions = descriptions.stream()
                .filter(description -> description.id() == null)
                .toList();
        Map<Long, ProductDescriptionRequestDto> descriptionsForUpdateById = descriptions.stream()
                .filter(description -> description.id() != null)
                .collect(toMap(ProductDescriptionRequestDto::id, Function.identity()));

        addDescriptionsToProduct(product, newDescriptions, languagesByCode);

        for (ProductDescription description : savedDescriptions) {
            var descriptionId = description.getId();
            var descriptionDto = descriptionsForUpdateById.get(descriptionId);

            if (descriptionDto == null) {
                product.removeDescription(description);
                continue;
            }

            var languageDto = languagesByCode.get(descriptionDto.languageCode());
            var language = makeLanguage(languageDto);

            description.setTitle(descriptionDto.title());
            description.setDescription(descriptionDto.description());
            description.setLanguage(language);
        }
    }

    private Optional<Product> findProductById(long id) {
        var product = productsRepository.findByIdWithDescriptions(id)
                .orElse(null);

        if (product == null) {
            return Optional.empty();
        }

        List<ProductFeature> features = featuresRepository.findAllByProductId(id)
                .toList();

        product.setFeatures(features);

        return Optional.of(product);
    }

    private void addFeaturesToProduct(Product product, List<ProductFeatureRequestDto> features,
                                      Map<String, LanguageResponseDto> languagesByCode) {
        for (ProductFeatureRequestDto featureDto : features) {
            var languageCode = featureDto.languageCode();
            var languageDto = languagesByCode.get(languageCode);
            var language = makeLanguage(languageDto);
            var feature = makeFeature(featureDto, language);

            product.addFeature(feature);
        }
    }

    private ProductFeature makeFeature(ProductFeatureRequestDto featureDto, Language language) {
        var feature = new ProductFeature();

        feature.setName(featureDto.name());
        feature.setValue(featureDto.value());
        feature.setLanguage(language);

        return feature;
    }

    private void addDescriptionsToProduct(Product product, List<ProductDescriptionRequestDto> descriptions,
                                          Map<String, LanguageResponseDto> languagesByCode) {
        for (ProductDescriptionRequestDto descriptionDto : descriptions) {
            var languageCode = descriptionDto.languageCode();
            var languageDto = languagesByCode.get(languageCode);
            var language = makeLanguage(languageDto);
            var description = makeDescription(descriptionDto, language);

            product.addDescription(description);
        }
    }

    private ProductDescription makeDescription(ProductDescriptionRequestDto descriptionDto, Language language) {
        var description = new ProductDescription();

        description.setTitle(descriptionDto.title());
        description.setDescription(descriptionDto.description());
        description.setLanguage(language);

        return description;
    }

    private Map<String, LanguageResponseDto> findLanguagesOrThrow(ProductRequestDto requestDto) {
        Stream<String> descriptionLanguageCodes = requestDto.descriptions()
                .stream()
                .map(ProductDescriptionRequestDto::languageCode);
        Stream<String> featuresLanguageCodes = requestDto.features()
                .stream()
                .map(ProductFeatureRequestDto::languageCode);
        Set<String> languageCodes = Stream.concat(descriptionLanguageCodes, featuresLanguageCodes)
                .collect(toSet());
        List<LanguageResponseDto> foundedLanguages = languagesService.getAllByCodeIn(languageCodes);
        boolean hasAllLanguages = languageCodes.size() != foundedLanguages.size();

        if (hasAllLanguages) {
            Set<String> foundedLanguageCodes = foundedLanguages.stream()
                    .map(LanguageResponseDto::code)
                    .collect(toSet());

            languageCodes.retainAll(foundedLanguageCodes);

            throw new NotFoundResourceException(String.format("languages with id: %s not found", languageCodes));
        }

        return foundedLanguages.stream()
                .collect(toMap(LanguageResponseDto::code, Function.identity()));
    }

    private Language makeLanguage(LanguageResponseDto languageDto) {
        var language = new Language();

        language.setId(languageDto.id());
        language.setCode(languageDto.code());

        return language;
    }

    private ProductResponseDto makeProductDto(Product product) {
        List<ProductDescriptionResponseDto> descriptions = product.getDescriptions()
                .stream()
                .map(this::makeDescriptionDto)
                .toList();
        List<ProductFeatureResponseDto> features = product.getFeatures()
                .stream()
                .map(this::makeFeatureDto)
                .toList();
        var productId = product.getId();
        var price = product.getPrice();

        return new ProductResponseDto(productId, price, descriptions, features);
    }

    private ProductDescriptionResponseDto makeDescriptionDto(ProductDescription description) {
        var language = description.getLanguage();
        var languageDto = new LanguageResponseDto(language.getId(), language.getCode());
        var descriptionId = description.getId();
        var title = description.getTitle();
        var text = description.getDescription();

        return new ProductDescriptionResponseDto(descriptionId, languageDto, title, text);
    }

    private ProductFeatureResponseDto makeFeatureDto(ProductFeature feature) {
        var language = feature.getLanguage();
        var languageDto = new LanguageResponseDto(language.getId(), language.getCode());
        var featureId = feature.getId();
        var featureName = feature.getName();
        var featureValue = feature.getValue();

        return new ProductFeatureResponseDto(featureId, languageDto, featureName, featureValue);
    }
}
