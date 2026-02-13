package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gg_item_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GGItemDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → GGItem */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gg_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ggdetalle_item"))
    private GGItem ggItem;

    private String descripcion;

    /** Unidad de este detalle (e.g., MES, %CD, etc.) */
    private String unidad;

    /** Texto libre para explicar la cantidad (fórmula, base, etc.) */
    @Column(name = "cantidad_desc")
    private String cantidadDesc;

    private BigDecimal cantidad;

    /** Precio unitario o base */
    private BigDecimal precio;

    /** Si depende de CD (porcentaje opcional) */
    private BigDecimal porcentaje;

    /** Parcial materializado */
    private BigDecimal parcial;

    private Integer orden;
}
