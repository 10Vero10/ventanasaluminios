package com.ventanasyaluminios.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.TestcontainersConfiguration;
import com.ventanasyaluminios.entity.GlassColor;
import com.ventanasyaluminios.entity.GlassThickness;
import com.ventanasyaluminios.entity.GlassType;
import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.ProductSpec;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/** Repositorios del catálogo contra PostgreSQL real, con los datos iniciales de la migración V4. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CatalogRepositoriesTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GlassTypeRepository glassTypeRepository;

    @Autowired
    private GlassColorRepository glassColorRepository;

    @Autowired
    private GlassThicknessRepository glassThicknessRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void losOchoProductosInicialesTienenSuFichaYCaracteristicasEnOrden() {
        List<Product> products = productRepository.findByActiveTrueOrderByPositionAsc();

        assertThat(products).hasSize(8);
        Product corredizas = products.get(0);
        assertThat(corredizas.getTitle()).isEqualTo("Corredizas");
        assertThat(corredizas.getCtaText()).isEqualTo("Cotizar ventana corrediza");
        assertThat(corredizas.getSpecs()).extracting(ProductSpec::getLabel)
                .containsExactly("Hojas", "Hermeticidad", "Vidrios compatibles");
        assertThat(corredizas.getFeatures()).first().isEqualTo("Rodamientos de alta precisión en acero inox");
        assertThat(products.get(5).getCtaText()).isEqualTo("Cotizar portón americano");
    }

    @Test
    void elCatalogoSeFiltraPorLinea() {
        assertThat(productRepository.findByActiveTrueAndCategoryOrderByPositionAsc(ProductCategory.PUERTAS))
                .extracting(Product::getTitle)
                .containsExactly("Aluminio", "Vidrio", "PVC & Termoacústicas");
    }

    @Test
    void reemplazarYReordenarLaFichaTecnicaSeGuardaEnElNuevoOrden() {
        Product product = productRepository.findAllByOrderByPositionAsc().get(0);
        product.replaceSpecs(List.of(new ProductSpec("Vidrios compatibles", "Templado"), new ProductSpec("Hojas", "2")));
        product.replaceFeatures(List.of("Única característica"));

        entityManager.flush();
        entityManager.clear();

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getSpecs()).extracting(ProductSpec::getLabel).containsExactly("Vidrios compatibles", "Hojas");
        assertThat(reloaded.getFeatures()).containsExactly("Única característica");
    }

    @Test
    void lasOpcionesDeVidrioInicialesVienenEnOrdenConSuValorPorDefecto() {
        assertThat(glassTypeRepository.findByActiveTrueOrderByPositionAsc()).extracting(GlassType::getCode)
                .containsExactly("CRUDO", "LAMINADO", "TEMPLADO", "LAMINADO_TEMPLADO", "ACUSTICO", "TERMO_ACUSTICO");
        assertThat(glassColorRepository.findByActiveTrueOrderByPositionAsc()).hasSize(7)
                .filteredOn(GlassColor::isReflective).extracting(GlassColor::getCode).containsExactly("REFLECTIVO");
        assertThat(glassThicknessRepository.findByActiveTrueOrderByPositionAsc())
                .filteredOn(GlassThickness::isDefaultOption).extracting(GlassThickness::getMm).containsExactly(6);
    }

    @Test
    void soloPuedeHaberUnaOpcionPorDefectoPorTabla() {
        glassTypeRepository.findAllByOrderByPositionAsc().get(1).setDefaultOption(true);

        assertThatThrownBy(() -> entityManager.flush()).hasMessageContaining("uq_glass_type_default");
    }
}
