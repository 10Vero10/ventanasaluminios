package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.request.ProductSpecRequest;
import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.ProductSpec;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.CatalogMapper;
import com.ventanasyaluminios.repository.ProductRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Mock
    private ProductRepository repository;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository, new CatalogMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void crearLimpiaTextosYQuedaAlFinalConFechas() {
        when(repository.findMaxPosition()).thenReturn(8);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new ProductRequest(ProductCategory.VENTANAS, " ", " Proyectantes ", "Línea nueva",
                "Descripción", "", " ", "Cotizar proyectante", true,
                List.of(new ProductSpecRequest(" Hojas ", " 1 ")), List.of(" Felpa ")));

        assertThat(response.position()).isEqualTo(9);
        assertThat(response.title()).isEqualTo("Proyectantes");
        assertThat(response.categoryNumber()).isNull();
        assertThat(response.imageUrl()).isNull();
        assertThat(response.badge()).isNull();
        assertThat(response.specs()).singleElement().satisfies(spec -> {
            assertThat(spec.label()).isEqualTo("Hojas");
            assertThat(spec.value()).isEqualTo("1");
        });
        assertThat(response.features()).containsExactly("Felpa");
    }

    @Test
    void duplicarCreaUnaCopiaOcultaAlFinalConSuPropiaFicha() {
        Product source = product(3L, "Corredizas", true);
        when(repository.findById(3L)).thenReturn(Optional.of(source));
        when(repository.findMaxPosition()).thenReturn(8);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var copy = service.duplicate(3L);

        assertThat(copy.title()).isEqualTo("Corredizas (copia)");
        assertThat(copy.active()).isFalse();
        assertThat(copy.position()).isEqualTo(9);
        assertThat(copy.specs()).extracting(s -> s.label()).containsExactly("Hojas");
        assertThat(copy.features()).containsExactly("Felpa siliconada");
    }

    @Test
    void laCopiaConservaTodosLosCamposSalvoLosQueDecideElService() {
        Product source = product(3L, "Corredizas", true);
        source.setCategoryNumber("01");
        source.setImageUrl("https://ejemplo.com/corrediza.jpg");
        source.setBadge("4 Obras instaladas");
        source.setCtaText("Cotizar ventana corrediza");

        Product copy = new CatalogMapper().copyOf(source, "Corredizas (copia)");

        // Si se agrega un campo a Product y copyOf no lo copia, esta comparación falla.
        assertThat(copy).usingRecursiveComparison()
                .ignoringFields("id", "position", "title", "active", "version", "createdAt", "updatedAt")
                .isEqualTo(source);
    }

    @Test
    void elTituloDeLaCopiaNuncaSuperaElMaximoNiParteUnEmoji() {
        assertThat(ProductServiceImpl.copyTitle("x".repeat(80))).hasSize(80).endsWith(" (copia)");
        assertThat(ProductServiceImpl.copyTitle("Batientes")).isEqualTo("Batientes (copia)");

        String withEmoji = "x".repeat(71) + "\uD83E\uDE9F" + "yyyyyyy";
        String title = ProductServiceImpl.copyTitle(withEmoji);
        assertThat(title).endsWith(" (copia)").doesNotContain("\uD83E (copia)");
        assertThat(Character.isHighSurrogate(title.charAt(title.length() - " (copia)".length() - 1))).isFalse();
    }

    @Test
    void unProductoOcultoNoExisteParaElPublico() {
        when(repository.findByIdAndActiveTrue(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublic(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("El producto no existe.");
    }

    @Test
    void editarActualizaLaFechaDeModificacion() {
        Product product = product(1L, "Corredizas", true);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        service.update(1L, new ProductRequest(ProductCategory.VENTANAS, "01", "Corredizas", "Sub", "Desc", null, null,
                null, false, List.of(), List.of()));

        assertThat(product.getUpdatedAt()).isEqualTo(NOW);
        assertThat(product.isActive()).isFalse();
        assertThat(product.getSpecs()).isEmpty();
    }

    private static Product product(Long id, String title, boolean active) {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", id);
        product.setCategory(ProductCategory.VENTANAS);
        product.setTitle(title);
        product.setSubtitle("Línea Panorámica");
        product.setDescription("Descripción");
        product.setActive(active);
        product.setPosition(1);
        product.replaceSpecs(List.of(new ProductSpec("Hojas", "2, 3 o 4")));
        product.replaceFeatures(List.of("Felpa siliconada"));
        return product;
    }
}
