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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("products service")
class ProductsServiceUnitTest {

    private static final String RU_MAC_DESCRIPTION =
            "С появлением чипа M1 MacBook Pro 13 дюймов становится невероятно производительным и быстрым.";
    private static final String RU_MAC_TITLE = "Ноутбук Apple MacBook Pro 13 Late 2020";
    private static final String RU_LANGUAGE_CODE = "RU";
    private static final String EN_LANGUAGE_CODE = "EN";
    private static final String EN_MAC_TITLE = "Apple MacBook Pro 13 Late 2020 laptop";
    private static final String EN_MAC_DESCRIPTION =
            "With the introduction of the M1 chip, the 13-inch MacBook Pro is incredibly powerful and fast.";
    private static final String RU_SCREEN_FEATURE_NAME = "экран";
    private static final String MAC_SCREEN_FEATURE_VALUE = "13.3 (2560x1600) IPS";
    private static final String MAC_CPU_FEATURE_VALUE = "Apple M1 (8x3200 МГц)";
    private static final String EN_SCREEN_FEATURE_NAME = "screen";
    private static final String RU_CPU_FEATURE_NAME = "процессор";
    private static final String EN_CPU_FEATURE_NAME = "CPU";
    private static final String RU_ASUS_TITLE = "Ноутбук ASUS Zenbook 13 UX325EA-KG285T";
    private static final String EN_ASUS_TITLE = "Laptop ASUS Zenbook 13 UX325EA-KG285T";
    private static final String RU_ASUS_DESCRIPTION =
            "Новый ноутбук ZenBook 13 OLED стал еще более тонким (13,9 мм) и легким (1,14 кг), а значит и еще более мобильным";
    private static final String EN_ASUS_DESCRIPTION =
            "The new ZenBook 13 OLED laptop is thinner (13.9mm), lighter (1.14kg), and therefore even more mobile";
    private static final String ASUS_SCREEN_FEATURE_VALUE = "13.3 (1920x1080) OLED";
    private static final String ASUS_CPU_FEATURE_VALUE = "Intel Core i5 1135G7 (4x2400 МГц)";
    private static final long FIRST_PRODUCT_ID = 1L;
    private static final long FIRST_LANGUAGE_ID = 1L;
    private static final long FIRST_DESCRIPTION_ID = 1L;
    private static final long FIRST_FEATURE_ID = 1L;
    private static final long SECOND_LANGUAGE_ID = 2L;
    private static final long SECOND_DESCRIPTION_ID = 2L;
    private static final long SECOND_FEATURE_ID = 2L;
    private static final long THIRD_FEATURE_ID = 3L;
    private static final long FOURTH_FEATURE_ID = 4L;

    private ProductsRepository productsRepository;
    private ProductFeaturesRepository featuresRepository;
    private LanguagesService languagesService;
    private ProductsService productsService;
    private InOrder inOrder;
    private Map<String, Language> languagesByCode;
    private List<LanguageResponseDto> languagesDto;

    @BeforeEach
    void setUp() {
        this.productsRepository = mock(ProductsRepository.class);
        this.featuresRepository = mock(ProductFeaturesRepository.class);
        this.languagesService = mock(LanguagesService.class);
        this.productsService = new ProductsServiceImpl(productsRepository, featuresRepository, languagesService);
        this.inOrder = inOrder(productsRepository, featuresRepository, languagesService);
        this.languagesByCode = makeLanguagesByCode();
        this.languagesDto = languagesByCode.values()
                .stream()
                .map(language -> new LanguageResponseDto(language.getId(), language.getCode()))
                .toList();
    }

    @ParameterizedTest
    @MethodSource("makeRequestProducts")
    @DisplayName("should create product")
    void shouldCreateProduct(ProductRequestDto requestDto) {
        var savedProduct = makeProductFromDto(requestDto);
        List<ProductDescription> savedDescriptions = savedProduct.getDescriptions();
        List<ProductFeature> savedFeatures = savedProduct.getFeatures();
        List<ProductDescriptionResponseDto> expectedDescriptions = savedDescriptions.stream()
                .map(this::convertDescriptionToDto)
                .toList();
        List<ProductFeatureResponseDto> expectedFeatures = savedFeatures.stream()
                .map(this::convertFeatureToDto)
                .toList();
        var expectedProduct = new ProductResponseDto(FIRST_PRODUCT_ID, requestDto.price(), expectedDescriptions,
                expectedFeatures);

        when(languagesService.getAllByCodeIn(anyCollection())).thenReturn(languagesDto);
        when(productsRepository.save(any())).thenReturn(savedProduct);

        assertThat(productsService.create(requestDto))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedProduct);

        inOrder.verify(languagesService, times(1))
                .getAllByCodeIn(anyCollection());
        inOrder.verify(productsRepository, times(1))
                .save(any());
    }

    @Test
    @DisplayName("should throw NullPointerException when product for create is null")
    void shouldThrowNpeWhenProductForCreateIsNull() {
        assertThrows(NullPointerException.class, () -> productsService.create(null));
    }

    @ParameterizedTest
    @MethodSource("makeRequestProducts")
    @DisplayName("should throw NouFoundResourceException when language by code not found")
    void shouldThrowNotFoundResourceExceptionForCreateWhenLanguageNotFound(ProductRequestDto requestDto) {
        when(languagesService.getAllByCodeIn(anyCollection())).thenReturn(emptyList());
        assertThrows(NotFoundResourceException.class, () -> productsService.create(requestDto));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, Long.MAX_VALUE, 0})
    @DisplayName("should return product by id")
    void shouldReturnProductById(long id) {
        var foundedProduct = makeProductById(id);
        List<ProductDescription> descriptions = foundedProduct.getDescriptions();
        List<ProductFeature> features = foundedProduct.getFeatures();
        List<ProductDescriptionResponseDto> expectedDescriptions = descriptions.stream()
                .map(this::convertDescriptionToDto)
                .toList();
        List<ProductFeatureResponseDto> expectedFeatures = features.stream()
                .map(this::convertFeatureToDto)
                .toList();
        var expectedProduct = new ProductResponseDto(foundedProduct.getId(), foundedProduct.getPrice(),
                expectedDescriptions, expectedFeatures);

        when(productsRepository.findByIdWithDescriptions(id)).thenReturn(Optional.of(foundedProduct));
        when(featuresRepository.findAllByProductId(id)).thenReturn(foundedProduct.getFeatures()
                .stream());

        assertThat(productsService.getById(id)).isNotEmpty()
                .usingFieldByFieldValueComparator()
                .get()
                .isEqualTo(expectedProduct);

        inOrder.verify(productsRepository, times(1))
                .findByIdWithDescriptions(id);
        inOrder.verify(featuresRepository, times(1))
                .findAllByProductId(id);
    }

    @ParameterizedTest
    @MethodSource("makeRequestProducts")
    void shouldUpdateProduct(ProductRequestDto requestDto) {
        var foundedProduct = makeProductById(FIRST_PRODUCT_ID);
        List<ProductFeature> features = foundedProduct.getFeatures();
        var expectedProduct = makeProductFromDto(requestDto);

        when(productsRepository.findByIdWithDescriptions(FIRST_PRODUCT_ID)).thenReturn(Optional.of(foundedProduct));
        when(featuresRepository.findAllByProductId(FIRST_PRODUCT_ID)).thenReturn(features.stream());
        when(languagesService.getAllByCodeIn(anyCollection())).thenReturn(languagesDto);
        when(productsRepository.save(any())).thenReturn(expectedProduct);

        assertThat(productsService.update(FIRST_PRODUCT_ID, requestDto)).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedProduct);

        inOrder.verify(productsRepository, times(1))
                .findByIdWithDescriptions(FIRST_PRODUCT_ID);
        inOrder.verify(featuresRepository, times(1))
                .findAllByProductId(FIRST_PRODUCT_ID);
        inOrder.verify(languagesService, times(1))
                .getAllByCodeIn(anyCollection());
        inOrder.verify(productsRepository, times(1))
                .save(any());
    }

    @ParameterizedTest
    @MethodSource("makeRequestProducts")
    @DisplayName("should throw NotFoundResourceException when language by code not found")
    void shouldThrowNotFoundResourceExceptionForUpdateWhenLanguageNotFound(ProductRequestDto requestDto) {
        when(productsRepository.findByIdWithDescriptions(FIRST_PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        when(languagesService.getAllByCodeIn(anyCollection())).thenReturn(emptyList());
        assertThrows(NotFoundResourceException.class, () -> productsService.update(FIRST_PRODUCT_ID, requestDto));
    }

    @Test
    @DisplayName("should throw NullPointerException when product for update is null")
    void shouldThrowNpeWhenProductForUpdateIsNull() {
        when(productsRepository.findByIdWithDescriptions(FIRST_PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        assertThrows(NullPointerException.class, () -> productsService.update(FIRST_PRODUCT_ID, null));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MAX_VALUE, Long.MIN_VALUE, 0})
    void shouldDeleteProductWithoutErrors(long id) {
        when(productsRepository.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> productsService.deleteById(id));

        inOrder.verify(productsRepository, times(1))
                .existsById(id);
        inOrder.verify(productsRepository, times(1))
                .deleteById(id);
    }

    @Test
    @DisplayName("should throw NotFoundResourceException when product for delete not found")
    void shouldThrowNotFoundResourceExceptionWhenProductForDeleteNotFound() {
        when(productsRepository.existsById(FIRST_PRODUCT_ID)).thenReturn(false);
        assertThrows(NotFoundResourceException.class, () -> productsService.deleteById(FIRST_PRODUCT_ID));
    }

    private ProductFeatureResponseDto convertFeatureToDto(ProductFeature feature) {
        var language = feature.getLanguage();
        var languageDto = new LanguageResponseDto(language.getId(), language.getCode());

        return new ProductFeatureResponseDto(feature.getId(), languageDto, feature.getName(),
                feature.getValue());
    }

    private ProductDescriptionResponseDto convertDescriptionToDto(ProductDescription description) {
        var language = description.getLanguage();
        var languageDto = new LanguageResponseDto(language.getId(), language.getCode());

        return new ProductDescriptionResponseDto(description.getId(), languageDto, description.getTitle(),
                description.getDescription());
    }

    private Product makeProductById(long id) {
        var language = languagesByCode.get(RU_LANGUAGE_CODE);
        var mac = new Product();
        var description = new ProductDescription();

        description.setId(FIRST_DESCRIPTION_ID);
        description.setTitle(RU_MAC_TITLE);
        description.setDescription(RU_MAC_DESCRIPTION);
        description.setProduct(mac);
        description.setLanguage(language);

        var feature = new ProductFeature();

        feature.setId(FIRST_FEATURE_ID);
        feature.setName(RU_CPU_FEATURE_NAME);
        feature.setValue(MAC_CPU_FEATURE_VALUE);
        feature.setProduct(mac);
        feature.setLanguage(language);

        mac.setId(id);
        mac.setPrice(BigDecimal.ONE);
        mac.setDescriptions(List.of(description));
        mac.setFeatures(List.of(feature));

        return mac;
    }

    private Product makeProductFromDto(ProductRequestDto requestDto) {
        var product = new Product();
        List<ProductDescription> savedDescriptions = requestDto.descriptions()
                .stream()
                .map(descriptionDto -> {
                    var language = languagesByCode.get(descriptionDto.languageCode());
                    var description = new ProductDescription();

                    description.setId(descriptionDto.id());
                    description.setTitle(descriptionDto.title());
                    description.setDescription(descriptionDto.description());
                    description.setLanguage(language);
                    description.setProduct(product);

                    return description;
                })
                .toList();
        List<ProductFeature> savedFeatures = requestDto.features()
                .stream()
                .map(featureDto -> {
                    var language = languagesByCode.get(featureDto.languageCode());
                    var feature = new ProductFeature();

                    feature.setId(featureDto.id());
                    feature.setName(featureDto.name());
                    feature.setValue(featureDto.value());
                    feature.setLanguage(language);
                    feature.setProduct(product);

                    return feature;
                })
                .toList();

        product.setId(FIRST_PRODUCT_ID);
        product.setPrice(requestDto.price());
        product.setDescriptions(savedDescriptions);
        product.setFeatures(savedFeatures);

        return product;
    }

    private Map<String, Language> makeLanguagesByCode() {
        var ruLanguage = new Language();

        ruLanguage.setCode(RU_LANGUAGE_CODE);
        ruLanguage.setId(FIRST_LANGUAGE_ID);

        var enLanguage = new Language();

        enLanguage.setId(SECOND_LANGUAGE_ID);
        enLanguage.setCode(EN_LANGUAGE_CODE);

        return Map.ofEntries(
                Map.entry(RU_LANGUAGE_CODE, ruLanguage),
                Map.entry(EN_LANGUAGE_CODE, enLanguage)
        );
    }

    private static Stream<ProductRequestDto> makeRequestProducts() {
        var ruMacDescription = new ProductDescriptionRequestDto(FIRST_DESCRIPTION_ID, RU_LANGUAGE_CODE, RU_MAC_TITLE,
                RU_MAC_DESCRIPTION);
        var enMacDescription = new ProductDescriptionRequestDto(SECOND_DESCRIPTION_ID, EN_LANGUAGE_CODE, EN_MAC_TITLE,
                EN_MAC_DESCRIPTION);

        List<ProductDescriptionRequestDto> macDescriptions = List.of(ruMacDescription, enMacDescription);

        var ruMacScreenFeature =
                new ProductFeatureRequestDto(FIRST_FEATURE_ID, RU_LANGUAGE_CODE, RU_SCREEN_FEATURE_NAME,
                        MAC_SCREEN_FEATURE_VALUE);
        var ruMacCpuFeature = new ProductFeatureRequestDto(SECOND_FEATURE_ID, RU_LANGUAGE_CODE, RU_CPU_FEATURE_NAME,
                MAC_CPU_FEATURE_VALUE);
        var enMacScreenFeature =
                new ProductFeatureRequestDto(THIRD_FEATURE_ID, EN_LANGUAGE_CODE, EN_SCREEN_FEATURE_NAME,
                        MAC_SCREEN_FEATURE_VALUE);
        var enMacCpuFeature = new ProductFeatureRequestDto(FOURTH_FEATURE_ID, EN_LANGUAGE_CODE, EN_CPU_FEATURE_NAME,
                MAC_CPU_FEATURE_VALUE);

        List<ProductFeatureRequestDto> macFeatures = List.of(ruMacScreenFeature, ruMacCpuFeature, enMacScreenFeature,
                enMacCpuFeature);
        var mac = new ProductRequestDto(BigDecimal.ONE, macDescriptions, macFeatures);

        var ruAsusDescription = new ProductDescriptionRequestDto(FIRST_DESCRIPTION_ID, RU_LANGUAGE_CODE, RU_ASUS_TITLE,
                RU_ASUS_DESCRIPTION);
        var enAsusDescription = new ProductDescriptionRequestDto(SECOND_DESCRIPTION_ID, EN_LANGUAGE_CODE, EN_ASUS_TITLE,
                EN_ASUS_DESCRIPTION);

        List<ProductDescriptionRequestDto> asusDescriptions = List.of(ruAsusDescription, enAsusDescription);

        var ruAsusScreenFeature =
                new ProductFeatureRequestDto(FIRST_FEATURE_ID, RU_LANGUAGE_CODE, RU_SCREEN_FEATURE_NAME,
                        ASUS_SCREEN_FEATURE_VALUE);
        var ruAsusCpuFeature = new ProductFeatureRequestDto(SECOND_FEATURE_ID, RU_LANGUAGE_CODE, RU_CPU_FEATURE_NAME,
                ASUS_CPU_FEATURE_VALUE);
        var enAsusScreenFeature =
                new ProductFeatureRequestDto(THIRD_FEATURE_ID, EN_LANGUAGE_CODE, EN_SCREEN_FEATURE_NAME,
                        ASUS_SCREEN_FEATURE_VALUE);
        var enAsusCpuFeature = new ProductFeatureRequestDto(FOURTH_FEATURE_ID, EN_LANGUAGE_CODE, EN_CPU_FEATURE_NAME,
                ASUS_CPU_FEATURE_VALUE);

        List<ProductFeatureRequestDto> asusFeatures = List.of(ruAsusScreenFeature, ruAsusCpuFeature,
                enAsusScreenFeature, enAsusCpuFeature);
        var asus = new ProductRequestDto(BigDecimal.ONE, asusDescriptions, asusFeatures);

        return Stream.of(mac, asus);
    }
}