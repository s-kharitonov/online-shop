package com.core.controller;

import com.core.dto.LanguageResponseDto;
import com.core.dto.product.*;
import com.core.exception.NotFoundResourceException;
import com.core.service.ProductsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductsRestController.class)
@DisplayName("products REST controller")
class ProductsRestControllerUnitTest {

    private static final String PRODUCT_DOMAIN_URL = "/product/";
    private static final String PRODUCT_BY_ID_URL = "/product/{id}";
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

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductsService productsService;

    private InOrder inOrder;

    private Map<String, LanguageResponseDto> languagesByCode;

    @BeforeEach
    void setUp() {
        this.inOrder = inOrder(productsService);
        this.languagesByCode = Map.ofEntries(
                Map.entry(RU_LANGUAGE_CODE, new LanguageResponseDto(FIRST_LANGUAGE_ID, RU_LANGUAGE_CODE)),
                Map.entry(EN_LANGUAGE_CODE, new LanguageResponseDto(SECOND_LANGUAGE_ID, EN_LANGUAGE_CODE))
        );
        ;
    }

    @ParameterizedTest
    @MethodSource("makeRequestProducts")
    @DisplayName("should create product")
    void shouldCreateProduct(ProductRequestDto product) throws Exception {
        List<ProductDescriptionResponseDto> expectedDescriptions = product.descriptions()
                .stream()
                .map(this::convertDescriptionToDto)
                .toList();
        List<ProductFeatureResponseDto> expectedFeatures = product.features()
                .stream()
                .map(this::convertFeatureToDto)
                .toList();
        var price = product.price();
        var expectedProduct = new ProductResponseDto(FIRST_PRODUCT_ID, price, expectedDescriptions, expectedFeatures);
        var requestBuilder = post(PRODUCT_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product));

        when(productsService.create(product)).thenReturn(expectedProduct);

        mvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, containsString("/product/" + expectedProduct.id())))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedProduct)));

        inOrder.verify(productsService, times(1))
                .create(product);
    }

    @ParameterizedTest
    @MethodSource("makeNotValidProducts")
    @DisplayName("should response BAD_REQUEST when product for create is not valid")
    void shouldResponseBadRequestWhenProductForCreateIsNotValid(ProductRequestDto product) throws Exception {
        var requestBuilder = post(PRODUCT_DOMAIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product));

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, Long.MAX_VALUE})
    @DisplayName("should return product by id")
    void shouldReturnProductById(long id) throws Exception {
        var requestBuilder = get(PRODUCT_BY_ID_URL, id);
        var expectedProduct = makeProductById(id);

        when(productsService.getById(id)).thenReturn(Optional.of(expectedProduct));

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedProduct)));

        inOrder.verify(productsService, times(1))
                .getById(id);
    }

    @Test
    @DisplayName("should response NOT_FOUND when product by id not found")
    void shouldResponseNotFoundWhenProductByIdNotFound() throws Exception {
        var requestBuilder = get(PRODUCT_BY_ID_URL, FIRST_PRODUCT_ID);

        when(productsService.getById(FIRST_PRODUCT_ID)).thenReturn(Optional.empty());

        mvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(NOT_FOUND.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, 0})
    @DisplayName("should response BAD_REQUEST when product id less than one")
    void shouldResponseBadRequestWhenProductIdLessThanOne(long id) throws Exception {
        var requestBuilder = get(PRODUCT_BY_ID_URL, id);

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @MethodSource("makeRequestProducts")
    @DisplayName("should update product")
    void shouldUpdateProduct(ProductRequestDto product) throws Exception {
        List<ProductDescriptionResponseDto> expectedDescriptions = product.descriptions()
                .stream()
                .map(this::convertDescriptionToDto)
                .toList();
        List<ProductFeatureResponseDto> expectedFeatures = product.features()
                .stream()
                .map(this::convertFeatureToDto)
                .toList();
        var price = product.price();
        var expectedProduct = new ProductResponseDto(FIRST_PRODUCT_ID, price, expectedDescriptions, expectedFeatures);
        var requestBuilder = put(PRODUCT_BY_ID_URL, FIRST_PRODUCT_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product));

        when(productsService.update(FIRST_PRODUCT_ID, product)).thenReturn(expectedProduct);

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedProduct)));

        inOrder.verify(productsService, times(1))
                .update(FIRST_PRODUCT_ID, product);
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, 0})
    @DisplayName("should response BAD_REQUEST when product id for update less than one")
    void shouldResponseBadRequestWhenProductIdForUpdateLessThanOne(long id) throws Exception {
        var product = makeRequestProducts().findFirst()
                .orElseThrow();
        var requestBuilder = put(PRODUCT_BY_ID_URL, id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product));

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @MethodSource("makeNotValidProducts")
    @DisplayName("should response BAD_REQUEST when product for update is not valid")
    void shouldResponseBadRequestWhenProductForUpdateNotValid(ProductRequestDto product) throws Exception {
        var requestBuilder = put(PRODUCT_BY_ID_URL, FIRST_PRODUCT_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product));

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @Test
    @DisplayName("should delete product by id")
    void shouldDeleteProductById() throws Exception {
        var requestBuilder = delete(PRODUCT_BY_ID_URL, FIRST_PRODUCT_ID);

        mvc.perform(requestBuilder)
                .andExpect(status().isNoContent());

        inOrder.verify(productsService, times(1))
                .deleteById(FIRST_PRODUCT_ID);
    }

    @Test
    @DisplayName("should response NOT_FOUND when product for delete not found")
    void shouldResponseNotFoundWhenProductForDeleteNotFound() throws Exception {
        var requestBuilder = delete(PRODUCT_BY_ID_URL, FIRST_PRODUCT_ID);

        doThrow(new NotFoundResourceException("product with id: " + FIRST_PRODUCT_ID + " not found"))
                .when(productsService)
                .deleteById(FIRST_PRODUCT_ID);

        mvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(NOT_FOUND.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, 0})
    @DisplayName("should response BAD_REQUEST when product id for delete less than one")
    void shouldResponseBadRequestWhenProductIdForDeleteLessThanOne(long id) throws Exception {
        var requestBuilder = delete(PRODUCT_BY_ID_URL, id);

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
                .andExpect(jsonPath("$.date", notNullValue()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.messages[*]", not(blankOrNullString())));
    }

    private ProductDescriptionResponseDto convertDescriptionToDto(ProductDescriptionRequestDto description) {
        var id = description.id();
        var title = description.title();
        var text = description.description();
        var languageCode = description.languageCode();
        var language = languagesByCode.get(languageCode);

        return new ProductDescriptionResponseDto(id, language, title, text);
    }

    private ProductFeatureResponseDto convertFeatureToDto(ProductFeatureRequestDto feature) {
        var id = feature.id();
        var name = feature.name();
        var value = feature.value();
        var languageCode = feature.languageCode();
        var language = languagesByCode.get(languageCode);

        return new ProductFeatureResponseDto(id, language, name, value);
    }

    private ProductResponseDto makeProductById(long id) {
        var language = languagesByCode.get(RU_LANGUAGE_CODE);
        var description = new ProductDescriptionResponseDto(FIRST_DESCRIPTION_ID, language, RU_MAC_TITLE,
                RU_MAC_DESCRIPTION);
        var feature = new ProductFeatureResponseDto(FIRST_FEATURE_ID, language, RU_CPU_FEATURE_NAME,
                MAC_CPU_FEATURE_VALUE);

        return new ProductResponseDto(id, BigDecimal.ONE, List.of(description), List.of(feature));
    }

    private static Stream<ProductRequestDto> makeRequestProducts() {
        var ruMacDescription = new ProductDescriptionRequestDto(FIRST_DESCRIPTION_ID, RU_LANGUAGE_CODE, RU_MAC_TITLE,
                RU_MAC_DESCRIPTION);
        var enMacDescription = new ProductDescriptionRequestDto(SECOND_DESCRIPTION_ID, EN_LANGUAGE_CODE, EN_MAC_TITLE,
                EN_MAC_DESCRIPTION);

        List<ProductDescriptionRequestDto> macDescriptions = List.of(ruMacDescription, enMacDescription);

        var ruMacScreenFeature = new ProductFeatureRequestDto(FIRST_FEATURE_ID, RU_LANGUAGE_CODE,
                RU_SCREEN_FEATURE_NAME, MAC_SCREEN_FEATURE_VALUE);
        var ruMacCpuFeature = new ProductFeatureRequestDto(SECOND_FEATURE_ID, RU_LANGUAGE_CODE, RU_CPU_FEATURE_NAME,
                MAC_CPU_FEATURE_VALUE);
        var enMacScreenFeature = new ProductFeatureRequestDto(THIRD_FEATURE_ID, EN_LANGUAGE_CODE,
                EN_SCREEN_FEATURE_NAME, MAC_SCREEN_FEATURE_VALUE);
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

        var ruAsusScreenFeature = new ProductFeatureRequestDto(FIRST_FEATURE_ID, RU_LANGUAGE_CODE,
                RU_SCREEN_FEATURE_NAME, ASUS_SCREEN_FEATURE_VALUE);
        var ruAsusCpuFeature = new ProductFeatureRequestDto(SECOND_FEATURE_ID, RU_LANGUAGE_CODE, RU_CPU_FEATURE_NAME,
                ASUS_CPU_FEATURE_VALUE);
        var enAsusScreenFeature = new ProductFeatureRequestDto(THIRD_FEATURE_ID, EN_LANGUAGE_CODE,
                EN_SCREEN_FEATURE_NAME, ASUS_SCREEN_FEATURE_VALUE);
        var enAsusCpuFeature = new ProductFeatureRequestDto(FOURTH_FEATURE_ID, EN_LANGUAGE_CODE, EN_CPU_FEATURE_NAME,
                ASUS_CPU_FEATURE_VALUE);

        List<ProductFeatureRequestDto> asusFeatures = List.of(ruAsusScreenFeature, ruAsusCpuFeature,
                enAsusScreenFeature, enAsusCpuFeature);
        var asus = new ProductRequestDto(BigDecimal.ONE, asusDescriptions, asusFeatures);

        return Stream.of(mac, asus);
    }

    private static Stream<ProductRequestDto> makeNotValidProducts() {
        var ruMacDescription = new ProductDescriptionRequestDto(FIRST_DESCRIPTION_ID, RU_LANGUAGE_CODE, RU_MAC_TITLE,
                RU_MAC_DESCRIPTION);
        var enMacDescription = new ProductDescriptionRequestDto(SECOND_DESCRIPTION_ID, EN_LANGUAGE_CODE, EN_MAC_TITLE,
                EN_MAC_DESCRIPTION);

        List<ProductDescriptionRequestDto> validDescriptions = List.of(ruMacDescription, enMacDescription);

        var ruMacScreenFeature = new ProductFeatureRequestDto(FIRST_FEATURE_ID, RU_LANGUAGE_CODE,
                RU_SCREEN_FEATURE_NAME, MAC_SCREEN_FEATURE_VALUE);
        var ruMacCpuFeature = new ProductFeatureRequestDto(SECOND_FEATURE_ID, RU_LANGUAGE_CODE, RU_CPU_FEATURE_NAME,
                MAC_CPU_FEATURE_VALUE);
        var enMacScreenFeature = new ProductFeatureRequestDto(THIRD_FEATURE_ID, EN_LANGUAGE_CODE,
                EN_SCREEN_FEATURE_NAME, MAC_SCREEN_FEATURE_VALUE);
        var enMacCpuFeature = new ProductFeatureRequestDto(FOURTH_FEATURE_ID, EN_LANGUAGE_CODE, EN_CPU_FEATURE_NAME,
                MAC_CPU_FEATURE_VALUE);

        List<ProductFeatureRequestDto> validFeatures = List.of(ruMacScreenFeature, ruMacCpuFeature, enMacScreenFeature,
                enMacCpuFeature);

        var productWithPriceLessThanZero = new ProductRequestDto(BigDecimal.valueOf(-1L), validDescriptions,
                validFeatures);
        var productWithoutDescriptions = new ProductRequestDto(BigDecimal.ONE, null,
                validFeatures);
        var productWithoutFeatures = new ProductRequestDto(BigDecimal.ONE, validDescriptions,
                null);
        var productWithEmptyDescriptions = new ProductRequestDto(BigDecimal.ONE, new ArrayList<>(),
                validFeatures);
        var productWithEmptyFeatures = new ProductRequestDto(BigDecimal.ONE, validDescriptions,
                new ArrayList<>());
        var productWithoutPrice = new ProductRequestDto(null, validDescriptions, validFeatures);

        return Stream.of(productWithPriceLessThanZero, productWithoutDescriptions, productWithoutFeatures,
                productWithEmptyDescriptions, productWithEmptyFeatures, productWithoutPrice);
    }
}