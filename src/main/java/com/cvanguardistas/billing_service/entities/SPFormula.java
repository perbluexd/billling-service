package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "sp_formula",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_spformula_sub_presupuesto_variable",
                columnNames = {"sub_presupuesto_id", "variable"}   // minúsculas
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SPFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → SubPresupuesto */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subpresupuesto_id",                         // minúsculas, alinea con DB y views
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_spformula_sub_presupuesto")
    )
    private SubPresupuesto subPresupuesto;                  // nombre de campo en minúscula

    /** Variables reservadas: CD, GG, UTI, ST, IGV, TOTAL, etc. */
    @Column(nullable = false)
    private String variable;

    private String descripcion;

    /** Expresión algebraica, por ejemplo: "CD + GG + UTI" */
    @Lob
    @Column(nullable = false)
    private String expresion;

    /** Valor materializado */
    private BigDecimal valor;

    /** Referencia opcional a IndiceUnificado (IU) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indice_unificado_id",
            foreignKey = @ForeignKey(name = "fk_spformula_indiceunificado")
    )
    private IndiceUnificado indiceUnificado;

    private Boolean resaltar = false;
    private Integer orden;
}
