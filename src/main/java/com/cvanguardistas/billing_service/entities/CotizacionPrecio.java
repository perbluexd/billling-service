package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizacion_precio",
        indexes = {
                @Index(name = "idx_cotizacion_producto_fecha",
                        columnList = "producto_tienda_id, recogido_en DESC")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CotizacionPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → ProductoTienda */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_tienda_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cotiza_producto_tienda"))
    private ProductoTienda productoTienda;

    @Column(name = "precio_listado", nullable = false)
    private BigDecimal precioListado;

    /** Código de moneda (e.g., PEN, USD) */
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_estado", nullable = false)
    private StockEstado stockEstado;

    /** Precio convertido a la unidad del insumo */
    @Column(name = "precio_unit_equivalente", nullable = false)
    private BigDecimal precioUnitEquivalente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuenteCotizacion fuente;

    @Lob
    @Column(name = "hash_fuente")
    private String hashFuente;

    @Column(name = "recogido_en", nullable = false)
    private LocalDateTime recogidoEn;
}
