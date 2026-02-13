package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "insumo_producto_tienda")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsumoProductoTienda {

    @EmbeddedId
    private InsumoProductoTiendaId id;

    /** FK → Insumo */
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("insumoId")
    @JoinColumn(name = "insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ipt_insumo"))
    private Insumo insumo;

    /** FK → ProductoTienda */
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("productoTiendaId")
    @JoinColumn(name = "producto_tienda_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ipt_producto_tienda"))
    private ProductoTienda productoTienda;

    /** Factor de conversión entre la unidad listada y la unidad del insumo */
    @Column(name = "factor_conversion_unidad", nullable = false)
    private BigDecimal factorConversionUnidad;

    private Integer prioridad;

    @Lob
    private String observacion; // opcional
}
