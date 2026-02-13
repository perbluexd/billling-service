package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gg_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GGItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → SubPresupuesto (columna real en la BD: subpresupuesto_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subpresupuesto_id",                    // 👈 coincide con la BD
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ggitem_sub_presupuesto")
    )
    private SubPresupuesto subPresupuesto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGG tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoGG formato;

    @Column(nullable = false)
    private String titulo;

    private Integer orden;
}
