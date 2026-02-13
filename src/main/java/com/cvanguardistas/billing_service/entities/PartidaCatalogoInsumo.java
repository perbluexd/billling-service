package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida_catalogo_insumo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Check(constraints =
        "((depende_de_rendimiento AND cuadrilla_frac IS NOT NULL AND cantidad_fija IS NULL) OR " +
                " (NOT depende_de_rendimiento AND cantidad_fija IS NOT NULL AND cuadrilla_frac IS NULL))")
public class PartidaCatalogoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A qué partida de catálogo pertenece esta línea */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_catalogo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pci_partida_catalogo"))
    private PartidaCatalogo partidaCatalogo;

    /** Insumo utilizado */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pci_insumo"))
    private Insumo insumo;

    /** Chip de costo (MO/MT/EQ/SC/SP/...) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_costo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pci_categoria_costo"))
    private CategoriaCosto categoriaCosto;

    /** Si TRUE, la cantidad depende de rendimiento usando cuadrilla */
    @Column(name = "depende_de_rendimiento", nullable = false)
    private Boolean dependeDeRendimiento = false;

    /** Fracción de cuadrilla (solo si depende_de_rendimiento = TRUE) */
    @Column(name = "cuadrilla_frac")
    private BigDecimal cuadrillaFrac;

    /** Cantidad fija (solo si depende_de_rendimiento = FALSE) */
    @Column(name = "cantidad_fija")
    private BigDecimal cantidadFija;

    /** Precio unitario manual (opcional) */
    @Column(name = "pu_override")
    private BigDecimal puOverride;

    /** Si TRUE, usa pu_override en lugar del precio vigente */
    @Column(name = "usar_pu_override", nullable = false)
    private Boolean usarPuOverride = false;

    /** Parcial materializable (opcional) */
    private BigDecimal parcial;

    /** Si esta línea referencia otra Subpartida del propio catálogo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Subpartida_catalogo_id",
            foreignKey = @ForeignKey(name = "fk_pci_Subpartida_catalogo"))
    private PartidaCatalogo SubpartidaCatalogo;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;
        if (usarPuOverride == null) usarPuOverride = false;
        if (dependeDeRendimiento == null) dependeDeRendimiento = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
