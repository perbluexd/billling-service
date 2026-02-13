// src/main/java/com/cvanguardistas/billing_service/entities/SubPresupuesto.java
package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subpresupuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubPresupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → Presupuesto */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "presupuesto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subpresupuesto_presupuesto")
    )
    private Presupuesto presupuesto;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    /** Orden de visualización dentro del presupuesto */
    private Integer orden;

    // ==========================
    // Materializados (cache)
    // ==========================
    @Column(name = "mo_total")
    private BigDecimal moTotal;

    @Column(name = "mt_total")
    private BigDecimal mtTotal;

    @Column(name = "eq_total")
    private BigDecimal eqTotal;

    @Column(name = "sc_total")
    private BigDecimal scTotal;

    @Column(name = "sp_total")
    private BigDecimal spTotal;

    @Column(name = "cd_total")
    private BigDecimal cdTotal;

    // ==========================
    // Relaciones hijas
    // ==========================

    /** Partidas/insumos del SubPresupuesto. */
    @OneToMany(
            mappedBy = "subPresupuesto",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @Setter(AccessLevel.NONE) // evita reemplazar la colección
    private List<Partida> partidas = new ArrayList<>();

    /** Fórmulas (PIE/TOTAL) del SubPresupuesto. */
    @OneToMany(
            mappedBy = "subPresupuesto",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @Setter(AccessLevel.NONE) // evita reemplazar la colección
    private List<SPFormula> formulas = new ArrayList<>();

    // --------------------------
    // Helpers bidireccionales
    // --------------------------
    public void addPartida(Partida p) {
        if (p == null) return;
        this.partidas.add(p);
        p.setSubPresupuesto(this);
    }

    public void removePartida(Partida p) {
        if (p == null) return;
        this.partidas.remove(p);
        p.setSubPresupuesto(null);
    }

    public void addFormula(SPFormula f) {
        if (f == null) return;
        this.formulas.add(f);
        f.setSubPresupuesto(this);
    }

    public void removeFormula(SPFormula f) {
        if (f == null) return;
        this.formulas.remove(f);
        f.setSubPresupuesto(null);
    }
}
