package com.ventanasyaluminios.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.TestcontainersConfiguration;
import com.ventanasyaluminios.entity.FeaturedLine;
import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.entity.SiteSettings;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/** Repositorios de settings contra PostgreSQL real, con los datos iniciales de la migración V2. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class SettingsRepositoriesTest {

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @Autowired
    private FeaturedLineRepository featuredLineRepository;

    @Autowired
    private GalleryImageRepository galleryImageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void laConfiguracionInicialExisteConLosDatosDeLaEmpresa() {
        SiteSettings settings = siteSettingsRepository.findById(SiteSettings.SINGLETON_ID).orElseThrow();

        assertThat(settings.getHeroTitle()).isEqualTo("Ventanas y Aluminios SAS");
        assertThat(settings.getPhone()).isEqualTo("+57 317 846 3260");
        assertThat(settings.getWhatsappNumber()).isEqualTo("573178463260");
        assertThat(settings.getExperienceYears()).isEqualTo((short) 25);
        assertThat(settings.isQuotingEnabled()).isTrue();
    }

    @Test
    void lasLineasDestacadasVienenOrdenadasPorPosicion() {
        List<FeaturedLine> lines = featuredLineRepository.findAllByOrderByPositionAsc();

        assertThat(lines).extracting(FeaturedLine::getTitle)
                .containsExactly("Ventanas", "Puertas", "Portones", "Divisiones");
        assertThat(featuredLineRepository.findMaxPosition()).isEqualTo(4);
    }

    @Test
    void reordenarIntercambiandoPosicionesNoViolaLaUnicidad() {
        List<FeaturedLine> lines = featuredLineRepository.findAllByOrderByPositionAsc();
        lines.get(0).setPosition(2);
        lines.get(1).setPosition(1);

        entityManager.flush();
        forceDeferredConstraintCheck();
        entityManager.clear();

        assertThat(featuredLineRepository.findAllByOrderByPositionAsc()).extracting(FeaturedLine::getTitle)
                .startsWith("Puertas", "Ventanas");
    }

    @Test
    void dosLineasNoPuedenQuedarEnLaMismaPosicion() {
        List<FeaturedLine> lines = featuredLineRepository.findAllByOrderByPositionAsc();
        lines.get(0).setPosition(2);
        entityManager.flush();

        assertThatThrownBy(this::forceDeferredConstraintCheck).hasMessageContaining("uq_featured_line_position");
    }

    @Test
    void laGaleriaSeFiltraPorLinea() {
        assertThat(galleryImageRepository.findAllByOrderByPositionAsc()).hasSize(5);
        assertThat(galleryImageRepository.findByCategoryOrderByPositionAsc(ProductCategory.VENTANAS))
                .extracting(GalleryImage::getTitle)
                .containsExactly("Fachada Minimalista Cristales Altos", "Ventanal Panorámico Sala Europea");
        assertThat(galleryImageRepository.findByCategoryOrderByPositionAsc(ProductCategory.PORTONES)).isEmpty();
    }

    /** La unicidad de position es diferible; dentro de la transacción de la prueba hay que verificarla a mano. */
    private void forceDeferredConstraintCheck() {
        entityManager.createNativeQuery("SET CONSTRAINTS ALL IMMEDIATE").executeUpdate();
    }
}
