package com.cvanguardistas.billing_service.util;

import java.math.RoundingMode;

public class CalculoConstantes {
    public static final int SCALE_LINEA = 6;   // Cantidades y PU
    public static final int SCALE_MONTO = 2;   // Montos finales
    public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
}
