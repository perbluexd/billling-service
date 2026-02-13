package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculoACUServiceImplTest {

    private CalculoACUServiceImpl svc;

    @BeforeEach
    void setUp() {
        svc = new CalculoACUServiceImpl();
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    @DisplayName("dependeDeRendimiento: calcula cantidad, chip, CU y parcial de hoja")
    void dependeDeRendimiento_calculaCantidadYParcial() {
        var linea = new LineaACURequest(
                100L,          // insumoId
                1L,            // categoriaCostoId
                true,          // dependeDeRendimiento
                bd("2"),       // cuadrillaFrac
                null,          // cantidadFija
                bd("50.00"),   // puOverride (lo usamos en el motor para PU)
                true           // usarPuOverride
        );

        var req = new HojaCalcRequest(
                bd("8"),  // jornadaEfectiva
                bd("4"),  // rendimiento
                bd("10"), // metrado
                List.of(linea)
        );

        HojaDto out = svc.calcular(req);

        // cantidad = (2 * 8) / 4 = 4.000000
        assertEquals(0, bd("4.000000").compareTo(out.lineas().get(0).cantidad()));

        // parcial(línea) = 4 * 50 = 200.00
        assertEquals(0, bd("200.00").compareTo(out.lineas().get(0).parcial()));

        // chip unitario = 200/10 = 20.00
        var chip = out.chips().get(0);
        assertEquals(0, bd("20.00").compareTo(chip.unitarioCalc()));

        // CU = 20.00
        assertEquals(0, bd("20.00").compareTo(out.cu()));

        // parcial hoja = 20 * 10 = 200.00
        assertEquals(0, bd("200.00").compareTo(out.parcial()));
    }

    @Test
    @DisplayName("no depende: cantidad fija OK, CU y parcial correctos")
    void noDepende_cantidadFijaOk() {
        var linea = new LineaACURequest(
                101L,
                1L,
                false,         // NO depende
                null,
                bd("3.5"),     // cantidadFija
                bd("40.00"),
                true
        );

        var req = new HojaCalcRequest(
                bd("8"),
                bd("4"),
                bd("5"),
                List.of(linea)
        );

        HojaDto out = svc.calcular(req);

        // cantidad = 3.500000
        assertEquals(0, bd("3.500000").compareTo(out.lineas().get(0).cantidad()));

        // parcial(línea) = 3.5 * 40 = 140.00
        assertEquals(0, bd("140.00").compareTo(out.lineas().get(0).parcial()));

        // chip unitario = 140/5 = 28.00
        assertEquals(0, bd("28.00").compareTo(out.chips().get(0).unitarioCalc()));

        // CU = 28.00
        assertEquals(0, bd("28.00").compareTo(out.cu()));

        // parcial hoja = 28 * 5 = 140.00
        assertEquals(0, bd("140.00").compareTo(out.parcial()));
    }

    @Test
    @DisplayName("puOverride: tiene prioridad sobre cualquier otro PU")
    void puOverride_tienePrioridad() {
        var linea = new LineaACURequest(
                102L,
                1L,
                false,
                null,
                bd("2"),
                bd("99.99"),
                true // usar override
        );

        var req = new HojaCalcRequest(
                bd("8"),
                bd("4"),
                bd("3"),
                List.of(linea)
        );

        HojaDto out = svc.calcular(req);

        // PU usado = 99.99
        assertEquals(0, bd("99.99").compareTo(out.lineas().get(0).pu()));

        // parcial(línea) = 2 * 99.99 = 199.98
        assertEquals(0, bd("199.98").compareTo(out.lineas().get(0).parcial()));
    }
}
