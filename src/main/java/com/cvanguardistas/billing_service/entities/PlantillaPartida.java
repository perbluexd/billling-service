package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plantilla_partida")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlantillaPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → Plantilla */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pp_plantilla"))
    private Plantilla plantilla;

    /** Jerarquía de partidas dentro de la plantilla */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id",
            foreignKey = @ForeignKey(name = "fk_pp_padre"))
    private PlantillaPartida padre;

    private String codigo;

    private String nombre;

    /** Unidad de medida */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id",
            foreignKey = @ForeignKey(name = "fk_pp_unidad"))
    private Unidad unidad;

    @Column(name = "cantidad_base")
    private BigDecimal cantidadBase;

    @Column(name = "rendimiento_base")
    private BigDecimal rendimientoBase;

    @Column(name = "precio_unit_ref")
    private BigDecimal precioUnitRef;

    private Integer orden;
}
