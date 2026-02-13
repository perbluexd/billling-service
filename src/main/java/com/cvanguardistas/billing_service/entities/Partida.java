package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "partida")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Check(constraints =
        "((tipo = 'HOJA' AND unidad_id IS NOT NULL AND rendimiento IS NOT NULL) OR " +
                " (tipo <> 'HOJA' AND unidad_id IS NULL AND rendimiento IS NULL AND metrado IS NULL))")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → SubPresupuesto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subpresupuesto_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_partida_sub_presupuesto"))
    private SubPresupuesto subPresupuesto;

    /** Jerarquía */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id",
            foreignKey = @ForeignKey(name = "fk_partida_padre"))
    private Partida padre;

    @Enumerated(EnumType.STRING)
    private TipoPartida tipo;

    private String codigo;
    private String nombre;

    /** Solo si tipo = HOJA */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id",
            foreignKey = @ForeignKey(name = "fk_partida_unidad"))
    private Unidad unidad;

    /** Solo si tipo = HOJA */
    private BigDecimal rendimiento;

    @Column(name = "jornada_horas_override")
    private BigDecimal jornadaHorasOverride;

    /** Solo si tipo = HOJA */
    private BigDecimal metrado;

    /** costo unitario y parcial */
    private BigDecimal cu;
    private BigDecimal parcial;

    /** opcionales legado por categoría */
    private BigDecimal mo;
    private BigDecimal mt;
    private BigDecimal eq;
    private BigDecimal sc;
    private BigDecimal sp;

    private Integer orden;
}
