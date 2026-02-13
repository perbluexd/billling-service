package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.entities.Insumo;
import com.cvanguardistas.billing_service.entities.InsumoPrecioHist;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.InsumoPrecioHistRepository;
import com.cvanguardistas.billing_service.repository.InsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrecioServiceImplTest {

    @Mock  InsumoRepository insumoRepo;
    @Mock  InsumoPrecioHistRepository repo;

    @InjectMocks
    PrecioServiceImpl svc;

    private InsumoPrecioHist mockHist(String precio) {
        var hist = mock(InsumoPrecioHist.class);
        when(hist.getPrecio()).thenReturn(new BigDecimal(precio));
        return hist;
    }

    private Insumo mockInsumo(BigDecimal precioBase) {
        var ins = mock(Insumo.class);
        when(ins.getPrecioBase()).thenReturn(precioBase);
        return ins;
    }

    @BeforeEach
    void base() {
        // Por defecto: existe el insumo y no tiene precio_base (para forzar uso de histórico)
        when(insumoRepo.findById(anyLong())).thenReturn(Optional.of(mockInsumo(null)));
    }

    @Test
    @DisplayName("Devuelve la última vigencia ≤ fecha (intervalo intermedio)")
    void devuelveUltimaMenorOIgual() {
        Long insumoId = 10L;
        LocalDate fecha = LocalDate.of(2024, 6, 15);

        when(repo.findTopByInsumo_IdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
                eq(insumoId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockHist("123.45")));

        var pu = svc.precioVigente(insumoId, fecha);
        assertEquals(new BigDecimal("123.45"), pu);
    }

    @Test
    @DisplayName("Fecha exacta a una vigencia devuelve ese precio")
    void fechaExactaDevuelveEsePrecio() {
        Long insumoId = 10L;
        LocalDate fecha = LocalDate.of(2024, 7, 1);

        when(repo.findTopByInsumo_IdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
                eq(insumoId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockHist("150.00")));

        var pu = svc.precioVigente(insumoId, fecha);
        assertEquals(new BigDecimal("150.00"), pu);
    }

    @Test
    @DisplayName("Antes de la primera vigencia: lanza DomainException")
    void antesDePrimeraVigenciaLanza() {
        Long insumoId = 10L;
        LocalDate fecha = LocalDate.of(2023, 1, 1);

        when(repo.findTopByInsumo_IdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
                eq(insumoId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> svc.precioVigente(insumoId, fecha));
    }

    @Test
    @DisplayName("Si el insumo tiene precio_base, se usa y no se consulta el histórico")
    void usaPrecioBaseSiExiste() {
        Long insumoId = 7L;
        when(insumoRepo.findById(insumoId)).thenReturn(Optional.of(mockInsumo(new BigDecimal("99.99"))));

        var pu = svc.precioVigente(insumoId, LocalDate.of(2024, 1, 1));
        assertEquals(new BigDecimal("99.99"), pu);
        verifyNoInteractions(repo);
    }
}
