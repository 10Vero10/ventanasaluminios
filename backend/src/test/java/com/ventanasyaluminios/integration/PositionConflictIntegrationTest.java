package com.ventanasyaluminios.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.TestcontainersConfiguration;
import com.ventanasyaluminios.entity.FeaturedLine;
import com.ventanasyaluminios.repository.FeaturedLineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * La unicidad de position es diferible: el choque aparece al confirmar la transacción. Esta prueba confirma que
 * Spring lo entrega como {@link DataIntegrityViolationException}, que el manejador global convierte en 409.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PositionConflictIntegrationTest {

    @Autowired
    private FeaturedLineRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void dosLineasEnLaMismaPosicionFallanAlConfirmarConErrorDeIntegridad() {
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            var lines = repository.findAllByOrderByPositionAsc();
            FeaturedLine first = lines.get(0);
            first.setPosition(lines.get(1).getPosition());
        })).isInstanceOf(DataIntegrityViolationException.class);
    }
}
