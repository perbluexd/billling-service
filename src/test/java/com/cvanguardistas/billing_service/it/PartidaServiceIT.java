package com.cvanguardistas.billing_service.it;

import com.cvanguardistas.billing_service.dto.HojaUpdateCmd;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.PartidaService;
import com.cvanguardistas.billing_service.service.PrecioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@Transactional
class PartidaServiceIT {

    // --- Test beans (reemplaza @MockBean) ---
    @TestConfiguration
    static class TestBeans {
        @Bean
        PrecioService precioService() {
            return Mockito.mock(PrecioService.class);
        }
    }

    @Autowired PresupuestoRepository presupuestoRepo;
    @Autowired SubPresupuestoRepository SubPresRepo;
    @Autowired PartidaRepository partidaRepo;
    @Autowired PartidaInsumoRepository partidaInsumoRepo;
    @Autowired PartidaTotalCategoriaRepository ptcRepo;
    @Autowired UnidadRepository unidadRepo;
    @Autowired CategoriaCostoRepository categoriaRepo;
    @Autowired InsumoRepository insumoRepo;

    @Autowired PartidaService partidaService;
    @Autowired PrecioService precioService; // mockeado vía TestBeans

    private Presupuesto presupuesto;
    private SubPresupuesto sp;
    private Unidad und;
    private CategoriaCosto catMO;
    private Insumo insumo;

    @BeforeEach
    void seedMinimo() {
        und = unidadRepo.save(
                Unidad.builder()
                        .codigo("HH")
                        .descripcion("Hora-Hombre")
                        .build()
        );

        catMO = categoriaRepo.save(
                CategoriaCosto.builder()
                        .codigo("MO")
                        .nombre("Mano de Obra")
                        .incluyeEnCu(true)
                        .visible(true)
                        .orden(1)
                        .build()
        );

        insumo = insumoRepo.save(
                Insumo.builder()
                        .nombre("Oficial de albañilería")
                        .unidad(und)
                        .build()
        );

        presupuesto = presupuestoRepo.save(
                Presupuesto.builder()
                        .nombre("P1")
                        .fechaBase(LocalDate.of(2024, 1, 1))
                        .jornadaHoras(new BigDecimal("8"))
                        .build()
        );

        sp = SubPresRepo.save(
                SubPresupuesto.builder()
                        .presupuesto(presupuesto)
                        .nombre("SP1")
                        .orden(1)
                        .build()
        );
    }

    @Test
    @DisplayName("Propagación: recalcula hoja, Sube a padres y actualiza cd_total del SP (PU histórico)")
    void recalculoPropagaYActualizaCd() {
        // -------------------------
        // Crear TÍTULO usando el service (9 parámetros)
        // SubPresupuestoId, padreId, tipo, codigo, nombre, unidadId, rendimiento, metrado, orden
        Long tituloId = partidaService.crear(
                sp.getId(),
                null,
                TipoPartida.TITULO,
                "COD-001",
                "T1",
                null,
                null,
                null,
                1
        );

        // Crear HOJA bajo ese título (unidad, rendimiento y metrado requeridos)
        Long hojaId = partidaService.crear(
                sp.getId(),
                tituloId,
                TipoPartida.HOJA,
                "COD-H1",
                "H1",
                und.getId(),
                new BigDecimal("4"),
                new BigDecimal("10"),
                1
        );
        // -------------------------

        Partida titulo = partidaRepo.findById(tituloId).orElseThrow();
        Partida hoja = partidaRepo.findById(hojaId).orElseThrow();

        // 1 línea depende (cuadrilla 2, MO)
        partidaInsumoRepo.save(
                PartidaInsumo.builder()
                        .partida(hoja)
                        .insumo(insumo)
                        .categoriaCosto(catMO)
                        .dependeDeRendimiento(true)
                        .cuadrillaFrac(new BigDecimal("2"))
                        .build()
        );

        // Mock del precio vigente: PU = 50.00
        BDDMockito.given(precioService.precioVigente(eq(insumo.getId()), any(LocalDate.class)))
                .willReturn(new BigDecimal("50.00"));

        // Recalcular hoja
        HojaUpdateCmd cmd = new HojaUpdateCmd(
                hoja.getId(),   // partidaId
                null,           // metrado
                null,           // rendimiento
                null,           // unidadId
                null,           // padreId
                null,           // orden
                List.of()       // lineas vacío
        );
        var dto = partidaService.actualizarHoja(cmd);

        // Esperados:
        // cantidad = (2*8)/4 = 4; parcial línea = 4*50 = 200; CU = 200/10=20; parcial hoja=200
        assertThat(dto.cu()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(dto.parcial()).isEqualByComparingTo(new BigDecimal("200.00"));

        var chips = ptcRepo.findByPartidaId(hoja.getId());
        assertThat(chips).hasSize(1);
        assertThat(chips.get(0).getUnitarioCalc()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(chips.get(0).getTotalCalc()).isEqualByComparingTo(new BigDecimal("200.00"));

        Partida tituloRef = partidaRepo.findById(titulo.getId()).orElseThrow();
        assertThat(tituloRef.getParcial()).isEqualByComparingTo(new BigDecimal("200.00"));

        SubPresupuesto spRef = SubPresRepo.findById(sp.getId()).orElseThrow();
        assertThat(spRef.getCdTotal()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Override de PU tiene prioridad sobre histórico en el recálculo")
    void overrideTienePrioridad() {
        // Crear HOJA con el service
        Long hojaId = partidaService.crear(
                sp.getId(),
                null,
                TipoPartida.HOJA,
                "COD-HOV",
                "H-ov",
                und.getId(),
                new BigDecimal("4"),
                new BigDecimal("10"),
                1
        );
        Partida hoja = partidaRepo.findById(hojaId).orElseThrow();

        partidaInsumoRepo.save(
                PartidaInsumo.builder()
                        .partida(hoja)
                        .insumo(insumo)
                        .categoriaCosto(catMO)
                        .dependeDeRendimiento(true)
                        .cuadrillaFrac(new BigDecimal("2"))
                        .usarPuOverride(true)
                        .puOverride(new BigDecimal("99.99"))
                        .build()
        );

        // Histórico “diría” 50, pero debe ignorarse por override
        BDDMockito.given(precioService.precioVigente(eq(insumo.getId()), any(LocalDate.class)))
                .willReturn(new BigDecimal("50.00"));

        HojaUpdateCmd cmd = new HojaUpdateCmd(
                hoja.getId(),
                null,   // metrado
                null,   // rendimiento
                null,   // unidadId
                null,   // padreId
                null,   // orden
                List.of() // lineas vacío
        );
        var dto = partidaService.actualizarHoja(cmd);

        // cantidad = 4; parcial = 4*99.99 = 399.96; CU = 39.996; cd_total = 399.96
        assertThat(dto.parcial().setScale(2)).isEqualByComparingTo(new BigDecimal("399.96"));
        assertThat(dto.cu().setScale(3)).isEqualByComparingTo(new BigDecimal("39.996"));

        SubPresupuesto spRef = SubPresRepo.findById(sp.getId()).orElseThrow();
        assertThat(spRef.getCdTotal().setScale(2)).isEqualByComparingTo(new BigDecimal("399.96"));
    }
}
