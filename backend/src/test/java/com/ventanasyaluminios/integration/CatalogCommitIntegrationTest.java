package com.ventanasyaluminios.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.TestcontainersConfiguration;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.request.ProductSpecRequest;
import com.ventanasyaluminios.dto.response.GlassTypeResponse;
import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.ProductSpec;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.repository.GlassTypeRepository;
import com.ventanasyaluminios.repository.ProductRepository;
import com.ventanasyaluminios.service.GlassOptionService;
import com.ventanasyaluminios.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Casos que solo aparecen al confirmar la transacción: ediciones cruzadas y restricciones diferibles. Sin
 * {@code @Transactional}: cada prueba deja la base como la encontró.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CatalogCommitIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GlassOptionService glassOptionService;

    @Autowired
    private GlassTypeRepository glassTypeRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void unaEdicionConDatosViejosSeRechazaYNoMezclaLasListas() {
        long id = productService.create(request("Proyectantes", List.of("A", "B", "C"))).id();
        try {
            // Pestaña 1 abre el producto; pestaña 2 lo guarda primero dejando una sola fila.
            Product staleTab = transactionTemplate.execute(status -> {
                Product product = productRepository.findById(id).orElseThrow();
                // Carga las listas antes de cerrar la transacción, como las tendría en memoria una pestaña abierta.
                product.getSpecs().size();
                product.getFeatures().size();
                return product;
            });
            productService.update(id, request("Proyectantes", List.of("A")));

            staleTab.replaceSpecs(List.of(new ProductSpec("1", "1"), new ProductSpec("2", "2"), new ProductSpec("3", "3"),
                    new ProductSpec("4", "4"), new ProductSpec("5", "5")));
            assertThatThrownBy(() -> productRepository.save(staleTab))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);

            assertThat(productService.get(id).specs()).extracting(spec -> spec.label()).containsExactly("A");
        } finally {
            productService.delete(id);
        }
    }

    @Test
    void cambiarLaOpcionPorDefectoFuncionaEnAmbosSentidosAlConfirmar() {
        GlassTypeResponse crudo = type("CRUDO");
        GlassTypeResponse templado = type("TEMPLADO");
        try {
            glassOptionService.updateType(templado.id(), new GlassTypeRequest("TEMPLADO", templado.name(), true, true));
            assertThat(defaultCodes()).containsExactly("TEMPLADO");

            glassOptionService.updateType(crudo.id(), new GlassTypeRequest("CRUDO", crudo.name(), true, true));
            assertThat(defaultCodes()).containsExactly("CRUDO");
        } finally {
            glassOptionService.updateType(crudo.id(), new GlassTypeRequest("CRUDO", crudo.name(), true, true));
        }
    }

    @Test
    void dosOpcionesPorDefectoFallanAlConfirmar() {
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status ->
                glassTypeRepository.findAllByOrderByPositionAsc().forEach(type -> type.setDefaultOption(true))))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(defaultCodes()).containsExactly("CRUDO");
    }

    private List<String> defaultCodes() {
        return glassOptionService.getAllOptions().types().stream()
                .filter(GlassTypeResponse::defaultOption)
                .map(GlassTypeResponse::code)
                .toList();
    }

    private GlassTypeResponse type(String code) {
        return glassOptionService.getAllOptions().types().stream()
                .filter(type -> type.code().equals(code))
                .findFirst()
                .orElseThrow();
    }

    private static ProductRequest request(String title, List<String> specLabels) {
        return new ProductRequest(ProductCategory.VENTANAS, "01", title, "Línea", "Descripción", null, null, null, false,
                specLabels.stream().map(label -> new ProductSpecRequest(label, label)).toList(), List.of());
    }
}
